(ns {{namespace}}.api-test
  (:require [clojure.test :refer :all]
            [mount.core :as m]
            [clj-http.client :as http]{{#swagger1st-oauth2}}
            [aleph.http.server]
            [ring.middleware.json :refer [wrap-json-response]]{{/swagger1st-oauth2}}
            [{{namespace}}.api :refer :all]
            [{{namespace}}.env :as env]
            [{{namespace}}.http]))

(deftest api
  (env/start-with-override {:http-port 8090})
  (m/start #'{{namespace}}.http/server)
  (is (= {:message "Hello Dude"} (:body (http/get "http://localhost:8090/api/hello/Dude" {:as :json}))))
  (is (= 200 (:status (http/get "http://localhost:8090/api/ui"))))
  (is (= 200 (:status (http/get "http://localhost:8090/api/swagger.json" {:as :json}))))
  (m/stop))
{{#swagger1st-oauth2}}

(defn mock-tokeninfo-handler [request]
  (let [authorization (get-in request [:headers "authorization"])]
    (if (= "Bearer foo" authorization)
      {:status 200 :body {:access_token "foo" :uid "mjackson" :scope ["uid"]}}
      {:status 401})))

(defn start-mock-tokeninfo-server []
  (aleph.http/start-server (wrap-json-response mock-tokeninfo-handler) {:port 7777}))

(deftest api-protection
  (with-open [mock-tokeninfo-server (start-mock-tokeninfo-server)]
    (env/start-with-override {:http-port 8090 :tokeninfo-url "http://localhost:7777/"})
    (m/start #'{{namespace}}.http/server)
    (testing "When a correct token is given, everything works"
      (is (= {:message "Hello Dude"} (:body (http/get "http://localhost:8090/api/hello/Dude"
                                                      {:as :json :oauth-token "foo"})))))
    (testing "When an invalid token is given, returns 401"
      (is (= 401 (:status (http/get "http://localhost:8090/api/hello/Dude"
                                                      {:as :json :oauth-token "foo1" :throw-exceptions? false})))))
    (m/stop)))
{{/swagger1st-oauth2}}
