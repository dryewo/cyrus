(ns {{namespace}}.ui
  (:require [mount.lite :as m]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [dovetail.core :as log]
            [hiccup.core :as h]
            [ring.util.response :as r]
            [{{namespace}}.utils :as u]))


(defn page [title & body-contents]
  (-> (h/html [:html
               [:head
                [:title title]
                [:link {:rel "stylesheet" :href "/ui/style.css"}]]
               (into [:body] body-contents)
               [:footer
                [:p [:br]]
                [:p {:align "right"} [:small "Version: " (u/implementation-version)]]]])
      (r/response)
      (r/content-type "text/html")))


(defn get-root [req]
  (page "Hello"
        [:h1 "Hello, World!"]))


(def ui-routes
  (routes
    (GET "/ui" req (get-root req))
    (route/resources "/ui" {:root "ui"})
    (route/not-found (page "Not found" [:h1 "Not found"]))))


(m/defstate handler
  :start ui-routes)
