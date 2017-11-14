(ns {{namespace}}.api-test
  (:require [clojure.test :refer :all]
            [mount.core :as m]
            [clj-http.client :as http]
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
