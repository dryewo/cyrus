(ns {{namespace}}.nrepl
  (:require [clojure.tools.nrepl.server :as n]
            [schema.core :as s]
            [squeeze.core :as squeeze]
            [{{namespace}}.lib.logging :as log]))

(def config-defaults
  {:nrepl-port 55000
   :nrepl-bind "0.0.0.0"})

(s/defschema Config
  {(s/optional-key :nrepl-port) s/Int
   (s/optional-key :nrepl-bind) s/Str})

(defn flatten1
  "Flattens the collection one level, for example,
   converts {:a 1 :b 2} (which is viewed as [[:a 1] [:b 2]])
   to [:a 1 :b 2]."
  [coll]
  (apply concat coll))

;; NREPL server - has to be outside components managed by mount to allow restarting
(defonce nrepl-server nil)

(defn start-nrepl [env]
  (log/info "Starting NREPL server")
  (let [config         (squeeze/coerce-config Config (merge config-defaults env))
        started-server (apply n/start-server (flatten1 (squeeze/remove-key-prefix :nrepl- config)))]
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
