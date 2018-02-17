(ns {{namespace}}.authenticator
  (:require [mount.lite :as m]
            [dovetail.core :as log]
            [cyrus-config.core :as cfg]{{#swagger1st-oauth2}}
            [fahrscheine-bitte.core :as oauth2]{{/swagger1st-oauth2}}{{#ui-oauth2}}
            [cyrus-ui-oauth2.core :as ui-oauth2]{{/ui-oauth2}}))


(cfg/def TOKENINFO_URL "URL to check access tokens against. If not set, tokens won't be checked.")
{{#swagger1st-oauth2}}


;; Checks if TOKENINFO_URL is set and returns a pass-through handler in case it's not
;; Works as a security handler for io.sarnowski.swagger1st.core/protector
(m/defstate oauth2-s1st-security-handler
  :start (if TOKENINFO_URL
           (let [access-token-resolver-fn (oauth2/make-cached-access-token-resolver TOKENINFO_URL {})]
             (log/info "Checking OAuth2 access tokens against %s." TOKENINFO_URL)
             (oauth2/make-oauth2-s1st-security-handler access-token-resolver-fn oauth2/check-corresponding-attributes))
           (do
             (log/warn "TOKENINFO_URL is not set; NOT CHECKING ACCESS TOKENS!")
             (fn [request definition requirements]
               request))))


(defn log-access-denied-reason [reason]
  (log/info "Access denied: %s" reason))


(defn wrap-reason-logger [handler]
  (oauth2/wrap-log-auth-error handler log-access-denied-reason))
{{/swagger1st-oauth2}}{{#ui-oauth2}}


(cfg/def EXTERNAL_URL "Public URL of the deployed application, used to generate redirect back from IAM provider.")
(cfg/def UI_ALLOW_ANON "Whether to allow unauthenticated users in web UI."
                       {:spec boolean?})


(m/defstate ui-oauth2-profile
  :start (if UI_ALLOW_ANON
           (do
             (log/warn "UI_ALLOW_ANON is set; NOT PROTECTING UI!")
             nil)
           ;; Go to https://github.com/settings/developers and register a new app (use http://localhost:8080/ui/callback),
           ;; copy its client credentials here.
           {:authorize-url            "https://github.com/login/oauth/authorize"
            :access-token-url         "https://github.com/login/oauth/access_token"
            :client-id                "CLIENT_ID"
            :client-secret            "CLIENT_SECRET"
            :scopes                   ["user:email"]
            :allow-anon?              UI_ALLOW_ANON
            :external-url             EXTERNAL_URL
            :default-landing-endpoint "/ui"
            :redirect-endpoint        "/ui/callback"
            :login-endpoint           "/ui/login"
            :logout-endpoint          "/ui/logout"}))


(defn wrap-ui-oauth2 [handler]
  (if @ui-oauth2-profile
    (ui-oauth2/wrap-ui-oauth2 handler @ui-oauth2-profile)
    handler))
{{/ui-oauth2}}
