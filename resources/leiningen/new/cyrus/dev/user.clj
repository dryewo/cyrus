(ns user
  (:require [clojure.java.javadoc :refer [javadoc]]
            [clojure.pprint :refer [pprint]]
            [clojure.reflect :refer [reflect]]
            [clojure.repl :refer [apropos dir doc find-doc pst source]]
            [clojure.tools.namespace.repl :refer [refresh refresh-all]]
            [clojure.test :refer [run-all-tests]]
            [mount.core :as m]
            [clojure.edn :as edn]))

(defn stop []
  (m/stop))

(defn slurp-if-exists [file]
  (when (.exists (clojure.java.io/as-file file))
    (slurp file)))

(defn load-dev-env
  ([]
   (load-dev-env "./dev-env.edn"))
  ([file]
   (edn/read-string (slurp-if-exists file))))

(defn start []
  (m/start-with-args (load-dev-env)))

(defn reset []
  (m/stop)
  (refresh :after 'user/start))

(defn run-tests []
  (run-all-tests #"{{namespace}}.*-test"))

(defn tests []
  (stop)
  (refresh :after 'user/run-tests))
