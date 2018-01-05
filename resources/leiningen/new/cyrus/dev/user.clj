(ns user
  (:require [clojure.java.javadoc :refer [javadoc]]
            [clojure.pprint :refer [pprint]]
            [clojure.reflect :refer [reflect]]
            [clojure.repl :refer [apropos dir doc find-doc pst source]]
            [clojure.tools.namespace.repl :refer [refresh-all] :as repl]
            [clojure.test :refer [run-all-tests]]
            [clojure.edn :as edn]
            [mount.lite :as m]
            [mount.extensions.refresh :refer [refresh]]
            [cyrus-config.core :as cfg]))

(defn slurp-if-exists [file]
  (when (.exists (clojure.java.io/as-file file))
    (slurp file)))

(defn load-dev-env
  ([]
   (load-dev-env "./dev-env.edn"))
  ([file]
   (edn/read-string (slurp-if-exists file))))

(defn reload-dev-env []
  (cfg/reload-with-override! (load-dev-env))
  (cfg/validate!))

(defn start []
  (reload-dev-env)
  (println (str "Config loaded:\n" (cfg/show)))
  (m/start))

(defn stop []
  (reload-dev-env)
  (m/stop))

(defn reset []
  (stop)
  (repl/refresh :after 'user/start))

(defn run-tests []
  (run-all-tests #"{{namespace}}.*-test"))

(defn tests []
  (stop)
  (repl/refresh :after 'user/run-tests))
