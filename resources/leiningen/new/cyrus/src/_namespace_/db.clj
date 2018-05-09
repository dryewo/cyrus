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
                                      :connection-init-sql "SET search_path TO {{prefix}}_data;"})]
             (migratus/init (merge migratus-config {:db db}))
             (migratus/migrate (merge migratus-config {:db db}))
             db))
  :stop (conman/disconnect! @*db*))


(dblib/set-db-state-sym! `*db*)


(conman/bind-connection-deref *db* "db/queries.sql")


(comment
  ;; CRUD operations
  (create-memory! {:id "1" :memory-text "foo"})
  (get-memory {:id "1"})
  (update-memory! {:id "1" :memory-text "bar"})
  (delete-memory! {:id "1"})

  ;; Transaction example
  (dblib/with-transaction
    (jdbc/db-set-rollback-only! @*db*)
    (create-memory! {:id "2" :memory-text "foo"})
    (get-memory {:id "2"}))
  (get-memory {:id "2"})

  ;; Helper to create new migrations
  (migratus/create migratus-config "create-user")
  )
