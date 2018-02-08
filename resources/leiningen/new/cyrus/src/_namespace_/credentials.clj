(ns {{namespace}}.credentials
  (:refer-clojure :exclude [get])
  (:require [mount.lite :as m]
            [dovetail.core :as log]
            [cyrus-config.core :as cfg]
            [mem-files.core :as mem-files]))


(cfg/def credentials-dir {:default "/meta/credentials"})


(m/defstate refresher
            :start (let [interval-ms 1000
                         keys-files  {:nakadi-token-secret    (str credentials-dir "/" "nakadi-token-secret")
                                      :employee-client-id     (str credentials-dir "/" "employee-client-id")
                                      :employee-client-secret (str credentials-dir "/" "employee-client-secret")}]
                     (log/info "Starting Credential Refresher with interval %s ms." interval-ms)
                     (mem-files/start interval-ms keys-files))
            :stop (.close @refresher))


(defn get [k]
  (let [credentials @@refresher]
    (assert (contains? credentials k) (str "Credential " k " not registered."))
    (clojure.core/get credentials k)))


(comment
  (get :nakadi-token-secret))
