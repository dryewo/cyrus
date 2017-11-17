(ns {{namespace}}.http
  (:require [mount.core :as m]
            [schema.core :as s]
            [aleph.http]
            [aleph.netty]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [compojure.middleware :refer [wrap-canonical-redirect]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.json :refer [wrap-json-response wrap-json-params wrap-json-body]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.gzip :refer [wrap-gzip]]
            [ring.util.response :as resp]
            [manifold.deferred :as md]
            [{{namespace}}.lib.logging :as log]
            [squeeze.core :as squeeze]
            [{{namespace}}.env :as env]{{#swagger1st}}
            [{{namespace}}.api :as api]{{/swagger1st}}))

(def config-defaults
  {:http-port 8090})

(s/defschema Config
  {(s/optional-key :http-port) s/Int})

(defn get-hello [req]
  (log/info "Hello")
  {:status 200 :body "Hello" :headers {"Content-type" "text/plain"}})

(defn get-hello-async [req]
  ;; Can also return a future for asynchronous processing
  (md/future
    (log/info "Hello2")
    {:status 200 :body "Hello2" :headers {"Content-type" "text/plain"}}))

(defn get-json [req]
  {:status 200 :body {:message "Hello"}})

(defn remove-trailing-slash
  "Remove the trailing '/' from a URI string, if it exists, unless the URI is just '/'"
  [^String uri]
  (if (= "/" uri)
    uri
    (compojure.middleware/remove-trailing-slash uri)))

;; Middleware rule of thumb: Request goes bottom to top, response goes top to bottom
(def handler
  (-> (routes
        ;; Normal routes, can view in the browser
        (-> (routes
              (GET "/hello" req (get-hello req))
              (GET "/hello2" req (get-hello-async req)))
            (wrap-canonical-redirect remove-trailing-slash))
        ;; Simple JSON API implementation
        (-> (routes
              (GET "/json" req (get-json req)))
            ;; About JSON support read: https://github.com/ring-clojure/ring-json
            (wrap-json-response)
            (wrap-defaults api-defaults)
            ;; To parse JSON body, uncomment only one of the following
            ;(wrap-json-body)   ; as a document
            ;(wrap-json-params) ; as params, need a flat map
            ){{#swagger1st}}
        ;; Swagger1st API implementation: https://github.com/zalando-stups/swagger1st
        (-> (routes
              (ANY "/api" req (resp/redirect "/api/ui/" 301))
              (ANY "/api/" req (resp/redirect "/api/ui/" 301))
              (ANY "/api/ui" req (resp/redirect "/api/ui/" 301))
              (ANY "/api/*" req (api/handler req)))
            (wrap-json-response)){{/swagger1st}}
        (route/not-found nil))
      ;; It never hurts to gzip
      (wrap-gzip)))


(m/defstate server
  :start (do
           (log/info "Starting HTTP server")
           (let [config         (squeeze/coerce-config Config (merge config-defaults @env/env))
                 started-server (aleph.http/start-server handler (squeeze/remove-key-prefix :http- config))]
             (log/info "HTTP server is listening on port %s" (aleph.netty/port started-server))
             started-server))
  :stop (.close @server))
