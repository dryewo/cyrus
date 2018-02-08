(ns {{namespace}}.ui
  (:require [mount.lite :as m]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [dovetail.core :as log]
            [hiccup.core :as h]
            [ring.util.response :as r]{{#ui-oauth2}}
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.session.cookie :as cookie]
            [cyrus-ui-oauth2.core :refer [wrap-ui-oauth2]]{{/ui-oauth2}}
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
        [:h1 "Hello, World!"]{{#ui-oauth2}}
        [:p [:a {:href "/ui/login"} "login"]]
        [:p [:a {:href "/ui/logout"} "logout"]]
        [:pre (with-out-str (clojure.pprint/pprint req))]{{/ui-oauth2}}))


(def ui-routes
  (routes
    (GET "/ui" req (get-root req))
    (route/resources "/ui" {:root "ui"})
    (route/not-found (page "Not found" [:h1 "Not found"]))))
{{#ui-oauth2}}


;; Go to https://github.com/settings/developers and register a new app (use http://localhost:8080/ui/callback),
;; copy its client credentials here.
(def github {:authorize-url            "https://github.com/login/oauth/authorize"
             :access-token-url         "https://github.com/login/oauth/access_token"
             :client-id                "CLIENT_ID"
             :client-secret            "CLIENT_SECRET"
             :scopes                   ["user:email"]
             :allow-anon?              true
             :default-landing-endpoint "/ui"
             :redirect-endpoint        "/ui/callback"
             :login-endpoint           "/ui/login"
             :logout-endpoint          "/ui/logout"})
{{/ui-oauth2}}


(m/defstate handler
  :start {{^ui-oauth2}}ui-routes{{/ui-oauth2}}{{#ui-oauth2}}(-> ui-routes
             (wrap-ui-oauth2 github)
             (wrap-defaults (-> site-defaults
                                (assoc-in [:session :cookie-attrs :same-site] :lax)
                                (assoc-in [:session :store] (cookie/cookie-store {:key "0123456789abcdef"}))))){{/ui-oauth2}})
