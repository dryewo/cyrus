(ns {{namespace}}.db
  (:require [conman.core :as conman]
            [mount.core :refer [defstate]]
            [migratus.core :as migratus]
            [{{namespace}}.lib.db]))

(def migratus-config
  {:store         :database
   :migration-dir "db/migrations"})

(defstate ^:dynamic *db*
  :start (let [db (conman/connect! {:jdbc-url "jdbc:postgresql://localhost:5432/postgres"
                                    :username "postgres"})]
           (migratus/migrate (merge migratus-config {:db db}))
           db)
  :stop (conman/disconnect! *db*))

(conman/bind-connection *db* "db/queries.sql")

(comment
  ;; CRUD operations
  (create-memory! {:id "1" :memory-text "foo"})
  (get-memory {:id "1"})
  (update-memory! {:id "1" :memory-text "bar"})
  (delete-memory! {:id "1"})

  ;; Helper to create new migrations
  (migratus/create migratus-config "create-user")
  )
