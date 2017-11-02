(ns leiningen.new.ga
  (:require [clj-http.client :as http]))

(def TRACKING_ID "UA-109047929-1")

(def GA_ENDPOINT "https://www.google-analytics.com/collect")
(def GA_VALIDATE_ENDPOINT "https://www.google-analytics.com/debug/collect")

(defn make-payload [params]
  (let [client-id (str (hash (str (System/getProperty "user.name")
                                  (System/getProperty "java.io.tmpdir")
                                  (System/getProperty "java.version"))))]
    (merge {:v   1
            :tid TRACKING_ID
            :cid client-id}
           params)))

(defn hit [payload]
  (try
    (http/post GA_ENDPOINT {:form-params    payload
                            :socket-timeout 500
                            :conn-timeout   1000})
    (catch Exception _)))

(defn validate [payload]
  (:body (http/post GA_VALIDATE_ENDPOINT {:form-params payload
                                          :as          :json})))

(comment
  (validate (make-payload {:t  "event"
                           :el "miley-cyrus"
                           :ec "leinnew"}))
  )
