(ns {{namespace}}.db
  (:require [conman.core :as conman]
            [mount.lite :as m]
            [migratus.core :as migratus]
            [clojure.java.jdbc :as jdbc]
            [cyrus-config.core :as cfg]
            [dovetail.core :as log]
            [{{namespace}}.lib.db :as dblib]))


(cfg/def DB_JDBC_URL "Coordinates of the database. Should start with `jdbc:postgresql://`."
                     {:default "jdbc:postgresql://localhost:5432/postgres"})
(cfg/def DB_USERNAME "Database username."
                     {:default "postgres"})
(cfg/def DB_PASSWORD "Database password."
                     {:secret true})


(def migratus-config
  {:store                :database
   :init-script          "init.sql"
   :init-in-transaction? true
   :migration-dir        "db/migrations"})


(m/defstate ^:dynamic *db*
  :start (do
           (log/info "Starting DB connection pool.")
           (let [db (conman/connect! {:jdbc-url            DB_JDBC_URL
                                      :username            DB_USERNAME
                                      :password            DB_PASSWORD
                                      :connection-init-sql "SET search_path TO c_data;"})]
             (migratus/init (merge migratus-config {:db db}))
             (migratus/migrate (merge migratus-config {:db db}))
             db))
  :stop (conman/disconnect! @*db*))


(defmacro with-transaction [& body]
  `(conman/with-transaction [*db*]
     ~@body))


(defmacro with-transaction* [opts & body]
  `(conman/with-transaction [*db* ~opts]
     ~@body))


(defmacro with-pg-advisory-xact-lock
  "Acquires transaction level lock in the DB. If the lock is taken, waits until it's available."
  [lock-id & body]
  `(jdbc/with-db-transaction [tx# @*db*]
     (dblib/pg-advisory-xact-lock tx# ~lock-id)
     ~@body))


(defmacro with-try-advisory-xact-lock
  "Acquires transaction level lock in the DB and returns true. If the lock is taken, returns false immediately."
  [lock-id & body]
  `(jdbc/with-db-transaction [tx# @*db*]
     (when (dblib/pg-try-advisory-xact-lock tx# ~lock-id)
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
