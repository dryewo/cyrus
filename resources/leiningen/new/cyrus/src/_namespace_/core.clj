(ns {{namespace}}.core
  (:require [mount.core :as mount]
            [environ.core :as environ]
            [{{namespace}}.lib.logging :as log]{{#nrepl}}
            [{{namespace}}.nrepl :as nrepl]{{/nrepl}}{{#http}}
            [{{namespace}}.http]{{/http}}{{#db}}
            [{{namespace}}.db]{{/db}})
  (:gen-class))

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
  (try{{#nrepl}}
    (when (= "true" (System/getenv "NREPL_ENABLED"))
      (nrepl/start-nrepl environ/env)){{/nrepl}}
    (mount/start)
    (log/info "Application started")
    ;; Prevent -main from exiting to keep the application running, unless it's a special test run
    (if-let [test-timeout (System/getenv "TEST_TIMEOUT")]
      (do
        (log/warn "Test mode: terminating after %s ms" test-timeout)
        (Thread/sleep (bigint test-timeout))
        (System/exit 0))
      @(promise))
    (catch Exception e
      (log/error e "Could not start the application because of %s." (str e))
      (System/exit 1))))

(log/set-ns-log-levels!
  {"{{namespace}}.*" :debug
   :all :info})

(log/set-default-output-fn!)

(comment
  ;; Starting and stopping the application during development{{#nrepl}} and NREPL access{{/nrepl}}
  (mount/start)
  (mount/stop)
  ;; Override some environment variables
  (mount/start-with-args {:http-port 8888}))
