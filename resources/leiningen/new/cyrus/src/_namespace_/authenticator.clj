(ns {{namespace}}.authenticator
  (:require [mount.lite :as m]
            [fahrscheine-bitte.core :as oauth2]
            [dovetail.core :as log]
            [cyrus-config.core :as cfg]))

(cfg/def tokeninfo-url {:info "URL to check access tokens against. If not set, tokens won't be checked."})

;; Checks if TOKENINFO_URL is set and returns a pass-through handler in case it's not
;; Works as a security handler for io.sarnowski.swagger1st.core/protector
(m/defstate oauth2-s1st-security-handler
  :start (if tokeninfo-url
           (let [access-token-resolver-fn (oauth2/make-cached-access-token-resolver tokeninfo-url {})]
             (log/info "Checking OAuth2 access tokens against %s." tokeninfo-url)
             (oauth2/make-oauth2-s1st-security-handler access-token-resolver-fn oauth2/check-corresponding-attributes))
           (do
             (log/warn "No TOKENINFO_URL set; NOT ENFORCING SECURITY!")
             (fn [request definition requirements]
               request))))

(defn log-access-denied-reason [reason]
  (log/info "Access denied: %s" reason))

(defn wrap-reason-logger [handler]
  (oauth2/wrap-log-auth-error handler log-access-denied-reason))
