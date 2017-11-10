(ns {{namespace}}.http
  (:require [mount.core :refer [defstate]]
            [aleph.http]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [compojure.middleware :refer [wrap-canonical-redirect]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.json :refer [wrap-json-response wrap-json-params wrap-json-body]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [manifold.deferred :as md]
            [{{namespace}}.lib.logging :as log]))

(defn hello-handler [req]
  (log/info "Hello")
  {:status 200 :body "Hello" :headers {"Content-type" "text/plain"}})

(defn hello2-handler [req]
  ;; Can also return a future for asynchronous processing
  (md/future
    (log/info "Hello2")
    {:status 200 :body "Hello2" :headers {"Content-type" "text/plain"}}))

(defn json-handler [req]
  {:status 200 :body {:message "Hello"}})

(defn remove-trailing-slash
  "Remove the trailing '/' from a URI string, if it exists, unless the URI is just '/'"
  [^String uri]
  (if (= "/" uri)
    uri
    (compojure.middleware/remove-trailing-slash uri)))

;; Middleware rule of thumb: Request goes bottom to top, response goes top to bottom
(defn handler []
  (-> (routes
        (GET "/hello" req (hello-handler req))
        (GET "/hello2" req (hello2-handler req))
        (GET "/json" req (json-handler req))
        (route/not-found nil))
      (wrap-defaults api-defaults)
      ;; About JSON support read: https://github.com/ring-clojure/ring-json
      (wrap-json-response)
      ;; Uncomment only one of the following
      ;(wrap-json-params)
      ;(wrap-json-body)
      (wrap-canonical-redirect remove-trailing-slash)))

(defstate server
          :start (aleph.http/start-server (handler) {:port 8090})
          :stop (.close server))
