(ns {{namespace}}.core
  (:require [mount.core :as mount]
            [{{namespace}}.lib.logging :as log])
  (:gen-class))

(defn implementation-version []
  (or
    ;; When running in a REPL
    (System/getProperty "{{name}}.version")
    ;; When running as `java -jar ...`
    (-> (eval '{{namespace}}.core) .getPackage .getImplementationVersion)))

(defn -main [& args]
  (log/disable-console-logging-colors)
  (log/set-level! :info)
  (log/set-log-level-from-env! (System/getenv "LOG_LEVEL"))
  (log/info "Starting {{name}} version %s" (implementation-version))
  (try
    (mount/start)
    (log/debug "Started")
    (catch Exception e
      (log/error e "Could not start the application because of %s." (str e))
      (System/exit 1))))

(log/set-ns-log-levels!
  {"{{namespace}}.*" :debug
   :all              :info})

(log/set-default-output-fn!)
