(ns {{namespace}}.test-utils
  (:require [clojure.test :refer :all]{{#db}}
            [clojure.java.jdbc :as jdbc]{{/db}}
            [mount.extensions.namespace-deps :as mnd]
            [cyrus-config.core :as cfg]))


(defn start-with-env-override [env-override & args]
  (cfg/reload-with-override! env-override)
  (apply mnd/start args))
{{#db}}


(defn wipe-db [db]
  (println "Wiping the DB")
  (jdbc/delete! db :memories ["true"])
  nil)
{{/db}}
