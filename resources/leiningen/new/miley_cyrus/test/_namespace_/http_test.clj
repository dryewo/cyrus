(ns {{namespace}}.http-test
  (:require [clojure.test :refer :all]
            [mount.core :as mount]
            [{{namespace}}.http :refer :all]
            [aleph.http :as http]))

(deftest test-http
  (mount/start #'{{namespace}}.http/server)
  (is (= 200 (:status @(http/get "http://localhost:8090/hello"))))
  (is (= 200 (:status @(http/get "http://localhost:8090/hello2"))))
  (is (= [200 {:message "Hello"}]
         ((juxt :status :body) @(http/get "http://localhost:8090/json" {:as :json}))))
  (mount/stop))
