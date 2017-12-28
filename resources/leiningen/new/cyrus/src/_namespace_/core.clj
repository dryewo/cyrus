(ns {{namespace}}.core
  (:require [mount.lite :as m]
            [environ.core :as environ]
            [dovetail.core :as log]{{#nrepl}}
            [{{namespace}}.nrepl :as nrepl]{{/nrepl}}{{#http}}
            [{{namespace}}.http]{{/http}}{{#db}}
            [{{namespace}}.db]{{/db}})
  (:gen-class))

;; HINT: After adding a new defstate restart the REPL

(defn implementation-version []
  (or
    ;; When running in a REPL
    (System/getProperty "{{name}}.version")
    ;; When running as `java -jar ...`
    (-> (eval '{{package}}.core) .getPackage .getImplementationVersion)))

(defn -main [& args]
  (log/disable-console-logging-colors)
  (log/set-level! :info)
  (log/set-log-level-from-env! (System/getenv "LOG_LEVEL"))
  (log/info "Starting {{name}} version %s" (implementation-version))
  (log/info "States found: %s" @m/*states*)
  (try{{#nrepl}}
    (when (= "true" (System/getenv "NREPL_ENABLED"))
      (nrepl/start-nrepl environ/env)){{/nrepl}}
    (m/start)
    (log/info "Application started")
    ;; Prevent -main from exiting to keep the application running, unless it's a special test run
    (if-let [test-timeout (System/getenv "TEST_TIMEOUT")]
      (do
        (log/warn "Test mode: terminating after %s ms" test-timeout)
        (Thread/sleep (bigint test-timeout))
        (System/exit 0))
      @(promise))
    (catch Exception e
      (log/error e "Could not start the application.")
      (System/exit 1))))

(log/set-ns-log-levels!
  {"{{namespace}}.*" :debug
   :all :info})

(log/set-output-fn! log/default-log-output-fn)

(comment
  (log/set-level! :info)
  (log/set-level! :debug)
  ;; Starting and stopping the application during NREPL access
  (m/start)
  (m/stop)
  ;; Override some environment variables
  (binding [env/*env-override* {:http-port 8888}]
    (m/start)))
