(ns {{namespace}}.http-test
  (:require [clojure.test :refer :all]
            [mount.lite :as m]
            [clj-http.client :as http]
            [{{namespace}}.http :refer :all]
            [{{namespace}}.test-utils :as tu]))

(use-fixtures
  :each (fn [f]
          (tu/start-with-env-override {:http-port 8080} #'server)
          (f)
          (m/stop)))

(deftest test-http
  (is (= 200 (:status (http/get "http://localhost:8080/hello"))))
  (is (= 200 (:status (http/get "http://localhost:8080/hello2"))))
  (is (= [200 {:message "Hello"} "application/json; charset=utf-8"]
         ((juxt :status :body #(get-in % [:headers "Content-Type"]))
           (http/get "http://localhost:8080/json" {:as :json})))))
