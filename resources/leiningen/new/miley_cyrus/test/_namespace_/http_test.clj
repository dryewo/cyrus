(ns {{namespace}}.http-test
  (:require [clojure.test :refer :all]
            [mount.core :as m]
            [clj-http.client :as http]
            [{{namespace}}.http :refer :all]
            [{{namespace}}.env :as env]))

(deftest test-http
  (env/start-with-override {:http-port 8090})
  (m/start #'server)
  (is (= 200 (:status (http/get "http://localhost:8090/hello"))))
  (is (= 200 (:status (http/get "http://localhost:8090/hello2"))))
  (is (= [200 {:message "Hello"} "application/json; charset=utf-8"]
         ((juxt :status :body #(get-in % [:headers "Content-Type"]))
           (http/get "http://localhost:8090/json" {:as :json}))))
  (m/stop))
