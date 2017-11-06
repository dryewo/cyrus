(ns {{namespace}}.http
  (:require [mount.core :refer [defstate]]
            [aleph.http]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [compojure.middleware :refer [wrap-canonical-redirect]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.json :refer [wrap-json-response]]
            [{{namespace}}.lib.logging :as log]
            [manifold.deferred :as md]))

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

(defn handler []
  (-> (routes
        (GET "/hello" req (hello-handler req))
        (GET "/hello2" req (hello2-handler req))
        (GET "/json" req (json-handler req))
        (route/not-found nil))
      (wrap-json-response)
      (wrap-defaults api-defaults)
      (wrap-canonical-redirect remove-trailing-slash)))

(defstate server
          :start (aleph.http/start-server (handler) {:port 8090})
          :stop (.close server))
