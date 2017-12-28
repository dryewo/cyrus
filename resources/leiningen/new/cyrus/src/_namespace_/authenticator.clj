(ns {{namespace}}.authenticator
  (:require [mount.core :as m]
            [schema.core :as s]
            [fahrscheine-bitte.core :as oauth2]
            [squeeze.core :as squeeze]
            [dovetail.core :as log]
            [{{namespace}}.env :as env]))

(def config-defaults {})

(s/defschema Config
  {(s/optional-key :tokeninfo-url) s/Str})

;; Checks if TOKENINFO_URL is set and returns a pass-through handler in case it's not
;; Works as a security handler for io.sarnowski.swagger1st.core/protector
(m/defstate oauth2-s1st-security-handler
  :start (let [{:keys [tokeninfo-url]} (squeeze/coerce-config Config (merge config-defaults @env/env))]
           (if tokeninfo-url
             (let [access-token-resolver-fn (oauth2/make-cached-access-token-resolver tokeninfo-url {})]
               (log/info "Checking OAuth2 access tokens against %s." tokeninfo-url)
               (oauth2/make-oauth2-s1st-security-handler access-token-resolver-fn oauth2/check-corresponding-attributes))
             (do
               (log/warn "No TOKENINFO_URL set; NOT ENFORCING SECURITY!")
               (fn [request definition requirements]
                 request)))))

(defn log-access-denied-reason [reason]
  (log/info "Access denied: %s" reason))

(defn wrap-reason-logger [handler]
  (oauth2/wrap-log-auth-error handler log-access-denied-reason))
