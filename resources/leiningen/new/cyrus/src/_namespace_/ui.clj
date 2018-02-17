(ns {{namespace}}.ui
  (:require [mount.lite :as m]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [dovetail.core :as log]
            [hiccup.core :as h]
            [ring.util.response :as r]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.session.cookie :as cookie]{{#ui-oauth2}}
            [{{namespace}}.authenticator :as authenticator]{{/ui-oauth2}}
            [{{namespace}}.utils :as u]))


(defn page [title & main-contents]
  (-> (h/html [:html
               [:head
                [:title title]
                [:link {:rel "stylesheet" :href "/ui/style.css"}]]
               [:body
                [:header [:h1 "{{name}}"]]
                ;[:nav (render-menu)]
                (into [:main] main-contents)
                [:footer
                 [:p [:br]]
                 [:p {:align "right"} [:small "Version: " (u/implementation-version)]]]]])
      (r/response)
      (r/content-type "text/html")))


(defn get-root [req]
  (page "Hello"
        [:h2 "Hello, World!"]{{#ui-oauth2}}
        [:p [:a {:href "/ui/login"} "login"]]
        [:p [:a {:href "/ui/logout"} "logout"]]
        [:pre (with-out-str (clojure.pprint/pprint req))]{{/ui-oauth2}}))


(def ui-routes
  (context "/ui" []
    (GET "/" req (get-root req))
    (route/resources "/" {:root "ui"})
    (route/not-found (page "Not found" [:h2 "Not found"]))))


(m/defstate handler
  :start (-> ui-routes{{#ui-oauth2}}
             (authenticator/wrap-ui-oauth2){{/ui-oauth2}}
             (wrap-defaults (-> site-defaults
                                (assoc-in [:session :cookie-attrs :same-site] :lax)
                                (assoc-in [:session :store] (cookie/cookie-store {:key "{{{session-store-key}}}"}))))))
