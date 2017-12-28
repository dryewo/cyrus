(ns {{namespace}}.test-utils
  (:require [clojure.test :refer :all]{{#db}}
            [clojure.java.jdbc :as jdbc]{{/db}}
            [mount.extensions.namespace-deps :as mnd]
            [{{namespace}}.env :as env]))

(defn start-with-env-override [env-override & args]
  (binding [env/*env-override* env-override]
    (apply mnd/start args)))
{{#db}}

(defn wipe-db [db]
  (println "Wiping the DB")
  (jdbc/delete! db :memories ["true"]))
{{/db}}
