(ns {{namespace}}.api
  (:require [io.sarnowski.swagger1st.core :as s1st]
            [io.sarnowski.swagger1st.executor :as s1stexec]
            [mount.lite :as m]
            [dovetail.core :as log]
            [{{namespace}}.lib.http :as httplib]{{#swagger1st-oauth2}}
            [{{namespace}}.authenticator :as authenticator]{{/swagger1st-oauth2}}))


(defn get-hello [{:keys [who]} req]
  (log/info "Saying hello to %s" who)
  {:status 200 :body {:message (str "Hello " who)}})


(defn resolve-operation
  "Calls operationId function with flattened request params and raw request map."
  [request-definition]
  (when-let [operation-fn (s1stexec/operationId-to-function request-definition)]
    (fn [request]
      (operation-fn (apply merge (vals (:parameters request))) request))))


(m/defstate handler
  :start (-> (s1st/context :yaml-cp "api.yaml")
             (s1st/discoverer :definition-path "/api/swagger.json" :ui-path "/api/ui/")
             ;; Given a path, figures out the spec part describing it
             (s1st/mapper){{#swagger1st-oauth2}}
             (s1st/ring authenticator/wrap-reason-logger)
             ;; Enforces security according to the requirements per endpoint, depends on the mapper
             (s1st/protector {"oauth2" @authenticator/oauth2-s1st-security-handler}){{/swagger1st-oauth2}}
             ;; Extracts parameter values from path, query and body of the request
             (s1st/parser)
             ;; Now we also know the user, replace request info
             (s1st/ring httplib/wrap-request-log-context)
             ;; Calls the handler function for the request. Customizable through :resolver
             (s1st/executor :resolver resolve-operation)))
