(ns {{namespace}}.lib.db
  (:require [cheshire.core :as json]
            [clojure.java.jdbc :as jdbc]
            [camel-snake-kebab.core :as csk]
            [hugsql.adapter]
            [hugsql.core]
            [conman.core :as conman])
  (:import org.postgresql.util.PGobject
           clojure.lang.IPersistentMap
           clojure.lang.IPersistentVector
           clojure.lang.LazySeq
           clojure.lang.Symbol
           java.sql.Array
           java.sql.PreparedStatement))


;; Conversion when getting from JDBC
(extend-protocol jdbc/IResultSetReadColumn
  Array
  (result-set-read-column [v _ _] (vec (.getArray v)))
  PGobject
  (result-set-read-column [pgobj _metadata _index]
    (let [type  (.getType pgobj)
          value (.getValue pgobj)]
      (case type
        "json" (json/parse-string value true)
        "jsonb" (json/parse-string value true)
        "citext" (str value)
        value)))
  ;; Uncomment for clj-time
  ;Timestamp
  ;(result-set-read-column [val _ _]
  ;  (tc/from-sql-time val))
  )


(defn to-pg-json [value]
  (doto (PGobject.)
    (.setType "jsonb")
    (.setValue (json/generate-string value))))


;; Conversion when passing to JDBC
(extend-protocol jdbc/ISQLParameter
  IPersistentVector
  (set-parameter [v ^PreparedStatement stmt ^long idx]
    (let [conn      (.getConnection stmt)
          meta      (.getParameterMetaData stmt)
          type-name (.getParameterTypeName meta idx)]
      (if-let [elem-type (when (= (first type-name) \_) (apply str (rest type-name)))]
        (.setObject stmt idx (.createArrayOf conn elem-type (to-array v)))
        (.setObject stmt idx (to-pg-json v))))))


;; Conversion when passing to JDBC
(extend-protocol jdbc/ISQLValue
  IPersistentMap
  (sql-value [value] (to-pg-json value))
  IPersistentVector
  (sql-value [value] (to-pg-json value))
  LazySeq
  (sql-value [value] (to-pg-json value))
  ; Uncomment for clj-time
  ;DateTime
  ;(sql-value [v]
  ;  (tc/to-sql-time v))
  )


;; ## This enables automatic case conversion to kebab-case so that no snake_case needs to be used in application code


(defn ->kebab-case-keyword-top-level [coll]
  (if (or (seq? coll) (map? coll))
    (into {} (for [[k v] coll]
               [(csk/->kebab-case-keyword k) v]))
    coll))


(defn result-one-snake->kebab [this result options]
  (->> (hugsql.adapter/result-one this result options)
       ->kebab-case-keyword-top-level))


(defn result-raw-snake->kebab [this result options]
  (->> (hugsql.adapter/result-raw this result options)
       ->kebab-case-keyword-top-level))


(defn result-many-snake->kebab [this result options]
  (->> (hugsql.adapter/result-many this result options)
       (map ->kebab-case-keyword-top-level)))


(defmethod hugsql.core/hugsql-result-fn :1 [sym] '{{namespace}}.lib.db/result-one-snake->kebab)
(defmethod hugsql.core/hugsql-result-fn :one [sym] '{{namespace}}.lib.db/result-one-snake->kebab)
(defmethod hugsql.core/hugsql-result-fn :* [sym] '{{namespace}}.lib.db/result-many-snake->kebab)
(defmethod hugsql.core/hugsql-result-fn :many [sym] '{{namespace}}.lib.db/result-many-snake->kebab)
(defmethod hugsql.core/hugsql-result-fn :raw [sym] '{{namespace}}.lib.db/result-raw-snake->kebab)
(defmethod hugsql.core/hugsql-result-fn :default [sym] '{{namespace}}.lib.db/result-raw-snake->kebab)


(defn pg-advisory-xact-lock
  "Acquires transaction level lock in the DB. If the lock is taken, waits until it's available."
  [db lock-id]
  (jdbc/query db ["SELECT pg_advisory_xact_lock(?);" lock-id]))


(defn pg-try-advisory-xact-lock
  "Acquires transaction level lock in the DB and returns true. If the lock is taken, returns false immediately."
  [db lock-id]
  (-> (jdbc/query db ["SELECT pg_try_advisory_xact_lock(?);" lock-id])
      first
      :pg_try_advisory_xact_lock))


(def *dbstate-sym)


(defn set-db-state-sym! [dbstate-sym]
  {:pre [(instance? Symbol dbstate-sym)]}
  (alter-var-root #'*dbstate-sym (constantly dbstate-sym)))


;; Before using any of the with-* functions below, set your database state symbol:
;;   (set-db-state-sym! `*db*)


(defmacro with-transaction [& body]
  `(conman/with-transaction [~*dbstate-sym]
     ~@body))


(defmacro with-transaction* [opts & body]
  `(conman/with-transaction [~*dbstate-sym ~opts]
     ~@body))


;; Keeps track of transactions per each lock. It is needed to ensure that locks are independent and reentrant.
(def ^:dynamic *lock-txs* {})


(defmacro with-lock-transaction [[txsym lock-id] & body]
  `(if-let [lock-tx# (get *lock-txs* ~lock-id)]
     (let [~txsym lock-tx#]
       ~@body)
     (jdbc/with-db-transaction [lock-tx# @~*dbstate-sym]
       (binding [*lock-txs* (assoc *lock-txs* ~lock-id lock-tx#)]
         (let [~txsym lock-tx#]
           ~@body)))))


(defmacro with-advisory-xact-lock
  "Acquires transaction level lock in the DB. If the lock is taken, waits until it's available."
  ;; TODO implement :timeout option, see clojure.java.jdbc/prepare-statement
  ;; Could be something like (with-advisory-xact-lock {:lock-id 123 :timeout 5000} ...)
  ;; Then check if first parameter is a map or not, or create with-advisory-xact-lock* that takes map only
  [lock-id & body]
  `(with-lock-transaction [tx# ~lock-id]
     (pg-advisory-xact-lock tx# ~lock-id)
     ~@body))


(defmacro with-try-advisory-xact-lock
  "Acquires transaction level lock in the DB and returns true. If the lock is taken, returns false immediately."
  [lock-id & body]
  `(with-lock-transaction [tx# ~lock-id]
     (when (pg-try-advisory-xact-lock tx# ~lock-id)
       ~@body)))
