(ns {{namespace}}.db
  (:require [conman.core :as conman]
            [mount.core :as m]
            [schema.core :as s]
            [migratus.core :as migratus]
            [clojure.java.jdbc :as jdbc]
            [squeeze.core :as squeeze]
            [{{namespace}}.lib.db]
            [{{namespace}}.env :as env]))

(def config-defaults
  {:db-jdbc-url "jdbc:postgresql://localhost:5432/postgres"
   :db-username "postgres"})

(s/defschema Config
  {(s/optional-key :db-jdbc-url) s/Str
   (s/optional-key :db-username) s/Str
   (s/optional-key :db-password) s/Str})

(def migratus-config
  {:store                :database
   :init-script          "init.sql"
   :init-in-transaction? true
   :migration-dir        "db/migrations"})

(m/defstate ^:dynamic *db*
  :start (let [config (squeeze/coerce-config Config (merge config-defaults @env/env))
               db     (conman/connect! (merge (squeeze/remove-key-prefix :db- config)
                                              {:connection-init-sql "SET search_path TO {{prefix}}_data;"}))]
           (migratus/init (merge migratus-config {:db db}))
           (migratus/migrate (merge migratus-config {:db db}))
           db)
  :stop (conman/disconnect! @*db*))

(defmacro with-transaction [& body]
  `(conman/with-transaction [*db*]
     ~@body))

(defmacro with-transaction* [opts & body]
  `(conman/with-transaction [*db* ~opts]
     ~@body))

(defn pg-advisory-xact-lock [db lock-id]
  (jdbc/query db ["SELECT pg_advisory_xact_lock(?);" lock-id]))

(defn pg-try-advisory-xact-lock [db lock-id]
  (-> (jdbc/query db ["SELECT pg_try_advisory_xact_lock(?);" lock-id])
      first :pg_try_advisory_xact_lock))

(defmacro with-pg-advisory-xact-lock
  "Acquires transaction level lock in the DB. If the lock is taken, waits until it's available."
  [lock-id & body]
  `(jdbc/with-db-transaction [tx# @*db*]
     (pg-advisory-xact-lock tx# ~lock-id)
     ~@body))

(defmacro with-try-advisory-xact-lock
  "Acquires transaction level lock in the DB and returns true. If the lock is taken, returns false immediately."
  [lock-id & body]
  `(jdbc/with-db-transaction [tx# @*db*]
     (when (pg-try-advisory-xact-lock tx# ~lock-id)
       ~@body)))

(conman/bind-connection-deref *db* "db/queries.sql")

(comment
  ;; CRUD operations
  (create-memory! {:id "1" :memory-text "foo"})
  (get-memory {:id "1"})
  (update-memory! {:id "1" :memory-text "bar"})
  (delete-memory! {:id "1"})

  ;; Transaction example
  (with-transaction
    (jdbc/db-set-rollback-only! @*db*)
    (create-memory! {:id "2" :memory-text "foo"})
    (get-memory {:id "2"}))
  (get-memory {:id "2"})

  ;; Helper to create new migrations
  (migratus/create migratus-config "create-user")
  )
