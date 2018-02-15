(ns {{namespace}}.http
  (:require [mount.lite :as m]
            [aleph.http]
            [aleph.netty]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [compojure.middleware :refer [wrap-canonical-redirect]]
            [ring.middleware.defaults :refer [wrap-defaults]]
            [ring.middleware.json :refer [wrap-json-response wrap-json-params wrap-json-body]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.gzip :refer [wrap-gzip]]
            [ring.util.response :as resp]
            [manifold.deferred :as md]
            [dovetail.core :as log]
            [cyrus-config.core :as cfg]
            [{{namespace}}.lib.http :as httplib]{{#swagger1st}}
            [{{namespace}}.api :as api]{{/swagger1st}}{{#ui}}
            [{{namespace}}.ui :as ui]{{/ui}}))


(cfg/def HTTP_PORT "Port for HTTP server to listen on."
                   {:spec    int?
                    :default 8090})


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


(def api-defaults
  (-> ring.middleware.defaults/api-defaults
      (assoc-in [:security :hsts] true)))


;; Middleware rule of thumb: Request goes bottom to top, response goes top to bottom
(defn make-handler []
  (-> (routes
        (GET "/.health" _ {:status 200})
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
              (ANY "/api/*" req (@api/handler req)))
            (wrap-json-response)
            (wrap-defaults api-defaults)){{/swagger1st}}{{#ui}}
        ;; UI routes, can view in the browser
        (-> (routes
              (GET "/" _ (resp/redirect "/ui" :moved-permanently))
              (ANY "/ui" req (@ui/handler req))
              (ANY "/ui/*" req (@ui/handler req)))
            (wrap-canonical-redirect remove-trailing-slash)){{/ui}}
        (route/not-found nil))
      ;; It never hurts to gzip
      (wrap-gzip)
      (httplib/wrap-request-log-context)))


(m/defstate server
  :start (do
           (log/info "Starting HTTP server")
           (let [started-server (aleph.http/start-server (make-handler) {:port HTTP_PORT})]
             (log/info "HTTP server is listening on port %s" (aleph.netty/port started-server))
             started-server))
  :stop (.close @server))
