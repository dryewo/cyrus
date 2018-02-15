(ns {{namespace}}.events
  (:require [cyrus-config.core :as cfg]
            [mount.lite :as m]
            [dovetail.core :as log]
            [clojure.string :as str]
            [clojure.java.shell :as shell]
            [clj-nakadi-java.core :as nakadi]{{#credentials}}
            [{{namespace}}.credentials :as creds]{{/credentials}}))


(cfg/def NAKADI_URL)
(cfg/def SUBSCRIPTION_ID {:required (some? NAKADI_URL)})
{{^credentials}}


(m/defstate access-token
  :start "" #_(-> (shell/sh "ztoken") :out str/trim))
{{/credentials}}


(m/defstate client
  :start (if-not NAKADI_URL
           (log/warn "NAKADI_URL is not set, will not publish events or subscribe to Nakadi.")
           (do
             (log/info "Initializing Nakadi client with URL %s" NAKADI_URL)
             (nakadi/make-client NAKADI_URL {{^credentials}}(fn [] @access-token){{/credentials}}{{#credentials}}(fn [] (creds/get :nakadi-token-secret)){{/credentials}}))))


(defn callback [event]
  (println "Processed" (pr-str event)))


(m/defstate consumer
  :start (when @client
           (log/info "Starting Nakadi consumer for subscription %s." SUBSCRIPTION_ID)
           (nakadi/consume-subscription @client SUBSCRIPTION_ID callback)
           ;(nakadi/consume-raw-events @client "foobar.event" callback)
           )
  :stop (when @consumer
          (@consumer)))


(comment
  (nakadi/publish-events @client "foobar.event" [{:foo "bar"}]))
