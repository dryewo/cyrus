(ns {{namespace}}.nrepl
  (:require [clojure.tools.nrepl.server :as n]
            [dovetail.core :as log]
            [cyrus-config.core :as cfg]))


(cfg/def NREPL_PORT "Port for NREPL server to listen on."
                    {:spec     int?
                     :default  55000})
(cfg/def NREPL_BIND "Network interface for NREPL server to bind to."
                    {:default  "0.0.0.0"})


(defn flatten1
  "Flattens the collection one level, for example,
   converts {:a 1 :b 2} (which is viewed as [[:a 1] [:b 2]])
   to [:a 1 :b 2]."
  [coll]
  (apply concat coll))


;; NREPL server - has to be outside components managed by mount to allow restarting
(defonce nrepl-server nil)


(defn start-nrepl []
  (log/info "Starting NREPL server")
  (let [started-server (apply n/start-server (flatten1 {:port NREPL_PORT :bind NREPL_BIND}))]
    (log/info "NREPL server is listening on %s" (str (:server-socket started-server)))
    (alter-var-root #'nrepl-server (constantly started-server))))


(defn stop-nrepl []
  (when nrepl-server
    (log/info "Stopping NREPL server")
    (n/stop-server nrepl-server))
  (alter-var-root #'nrepl-server (constantly nil)))


(comment
  (start-nrepl {})
  ;; ACHTUNG: Don't stop NREPL via NREPL session
  (stop-nrepl))
