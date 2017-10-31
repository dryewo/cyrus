(ns user
  (:require [clojure.java.javadoc :refer [javadoc]]
            [clojure.pprint :refer [pprint]]
            [clojure.reflect :refer [reflect]]
            [clojure.repl :refer [apropos dir doc find-doc pst source]]
            [clojure.tools.namespace.repl :refer [refresh refresh-all]]
            [clojure.test :refer [run-all-tests]]
            [mount.core :as mount]
            [{{namespace}}.core :as core]))

(defn stop []
  (mount/stop))

(defn reset []
  (mount/stop)
  (refresh :after 'mount/start))

(defn run-tests []
  (run-all-tests #"{{namespace}}.*-test"))

(defn tests []
  (stop)
  (refresh :after 'user/run-tests))
