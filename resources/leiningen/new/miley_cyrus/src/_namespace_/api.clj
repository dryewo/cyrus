(ns {{namespace}}.api
  (:require [io.sarnowski.swagger1st.core :as s1st]
            [io.sarnowski.swagger1st.util.api :as s1stapi]
            [taoensso.timbre :as timbre]
            [{{namespace}}.lib.logging :as log]
            [{{namespace}}.lib.swagger1st :as s1stlib]))

(defn get-hello [{:keys [who]} req]
  (log/info "Saying hello to %s" who)
  {:status 200 :body {:message (str "Hello " who)}})

(def handler
  (-> (s1st/context :yaml-cp "api.yaml")
      (s1st/ring s1stlib/enrich-log-lines)
      (s1st/ring s1stapi/add-hsts-header)
      (s1st/ring s1stapi/add-cors-headers)
      (s1st/discoverer :definition-path "/api/swagger.json" :ui-path "/api/ui/")
      ;; Given a path, figures out the spec part describing it
      (s1st/mapper)
      ;; Extracts parameter values from path, query and body of the request
      (s1st/parser)
      ;; Calls the handler function for the request. Customizable through :resolver
      (s1st/executor :resolver s1stlib/resolve-operation-basic)))
