(ns {{namespace}}.db
  (:require [conman.core :as conman]
            [mount.lite :as m]
            [clojure.java.jdbc :as jdbc]
            [cyrus-config.core :as cfg]
            [dovetail.core :as log]
            [{{namespace}}.lib.db :as dblib])
  (:import org.flywaydb.core.Flyway))


(cfg/def DB_JDBC_URL "Coordinates of the database. Should start with `jdbc:postgresql://`."
                     {:default "jdbc:postgresql://localhost:5432/postgres"})
(cfg/def DB_USERNAME "Database username."
                     {:default "postgres"})
(cfg/def DB_PASSWORD "Database password."
                     {:secret true})


(def flyway-config
  {:flyway-schemas "{{prefix}}_data"})


(def conman-config
  {:connection-init-sql "SET search_path TO {{prefix}}_data;"})


(m/defstate ^:dynamic *db*
  :start (do
           (log/info "Starting DB connection pool.")
           (doto (Flyway.)
             (.configure (dblib/flyway-properties (merge flyway-config
                                                         {:flyway-url      DB_JDBC_URL
                                                          :flyway-user     DB_USERNAME
                                                          :flyway-password DB_PASSWORD})))
             (.migrate))
           (let [db (conman/connect! (merge conman-config
                                            {:jdbc-url DB_JDBC_URL
                                             :username DB_USERNAME
                                             :password DB_PASSWORD}))]
             db))
  :stop (.close (:datasource @*db*)))


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
  )
