(ns {{namespace}}.events
  (:require [cyrus-config.core :as cfg]
            [mount.lite :as m]
            [dovetail.core :as log]
            [clojure.string :as str]
            [clojure.java.shell :as shell]
            [clj-nakadi-java.core :as nakadi]))


(cfg/def nakadi-url)
(cfg/def subscription-id {:required (some? nakadi-url)})


(m/defstate access-token
  :start "" #_(-> (shell/sh "ztoken") :out str/trim))


(m/defstate client
  :start (if-not nakadi-url
           (log/warn "%s is not set, will not publish events or subscribe to Nakadi." (-> (meta #'nakadi-url) ::cfg/effective-definition :var-name))
           (do
             (log/info "Initializing Nakadi client with URL %s" nakadi-url)
             (nakadi/make-client nakadi-url (fn [] @access-token)))))


(defn callback [event]
  (println "Processed" (pr-str event)))


(m/defstate consumer
  :start (when @client
           (log/info "Starting Nakadi consumer for subscription %s." subscription-id)
           (nakadi/consume-subscription @client subscription-id callback)
           ;(nakadi/consume-raw-events @client "foobar.event" callback)
           )
  :stop (when @consumer
          (@consumer)))

(comment
  (nakadi/publish-events @client "foobar.event" [{:foo "bar"}]))
