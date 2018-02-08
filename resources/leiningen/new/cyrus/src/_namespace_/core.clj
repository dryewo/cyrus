(ns {{namespace}}.core
  (:require [mount.lite :as m]
            [dovetail.core :as log]
            [cyrus-config.core :as cfg]
            [{{namespace}}.utils :as u]{{#nrepl}}
            [{{namespace}}.nrepl :as nrepl]{{/nrepl}}{{#http}}
            [{{namespace}}.http]{{/http}}{{#db}}
            [{{namespace}}.db]{{/db}}{{#nakadi}}
            [{{namespace}}.events]{{/nakadi}}{{#ui}}
            [{{namespace}}.ui]{{/ui}}{{#credentials}}
            [{{namespace}}.credentials]{{/credentials}})
  (:gen-class))


;; HINT: After adding or removing a defstate restart the REPL


(cfg/def log-level){{#nrepl}}
(cfg/def nrepl-enabled {:spec boolean?}){{/nrepl}}{{#debug}}
(cfg/def test-timeout {:spec int?}){{/debug}}


(defn -main [& args]
  (log/disable-console-logging-colors)
  (log/set-level! :info)
  (log/set-log-level-from-env! log-level)
  (log/info "Starting {{name}} version %s" (u/implementation-version))
  (log/info "States found: %s" @m/*states*)
  (try
    (cfg/validate!)
    (log/info (str "Config loaded:\n" (cfg/show))){{#nrepl}}
    (when nrepl-enabled
      (nrepl/start-nrepl)){{/nrepl}}
    (m/start)
    (log/info "Application started"){{#debug}}
    ;; Prevent -main from exiting to keep the application running, unless it's a special test run
    (if test-timeout
      (do
        (log/warn "Test mode: terminating after %s ms" test-timeout)
        (Thread/sleep test-timeout)
        (System/exit 0))
      @(promise)){{/debug}}{{^debug}}
    ;; Prevent -main from exiting to keep the application running
    @(promise){{/debug}}
    (catch Exception e
      (log/error e "Could not start the application.")
      (System/exit 1))))


(log/set-ns-log-levels!
  {"{{namespace}}.*" :debug
   "com.zaxxer.hikari.*" :warn
   :all :info})


(log/set-output-fn! log/default-log-output-fn)


(comment
  (log/set-level! :info)
  (log/set-level! :debug)
  ;; Starting and stopping the application during NREPL access
  (m/start)
  (m/stop)
  ;; Override some environment variables
  (cfg/reload-with-override! {"HTTP_PORT" 8888}))
