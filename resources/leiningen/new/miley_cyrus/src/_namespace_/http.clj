(ns {{namespace}}.http
  (:require [mount.core :refer [defstate]]
            [aleph.http]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [compojure.middleware]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [{{namespace}}.lib.logging :as log]
            [manifold.deferred :as md]))

(defn remove-trailing-slash
  "Remove the trailing '/' from a URI string, if it exists, unless the URI is just '/'"
  [^String uri]
  (if (= "/" uri)
    uri
    (compojure.middleware/remove-trailing-slash uri)))

(defn hello-handler [req]
  (log/info "Hello")
  (md/future
    (log/info "Hello2")
    {:status 200}))

(defn handler []
  (-> (routes
        (GET "/hello" req (hello-handler req))
        (route/not-found nil))
      (compojure.middleware/wrap-canonical-redirect remove-trailing-slash)
      (wrap-defaults api-defaults)))

(defstate server
  :start (aleph.http/start-server (handler) {:port 8090})
  :stop (.close server))
