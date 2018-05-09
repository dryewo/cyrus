(ns {{namespace}}.ui-test
  (:require [clojure.test :refer :all]
            [{{namespace}}.test-utils :as tu]
            [mount.lite :as m]
            [clj-http.client :as http]
            [{{namespace}}.http]))


(use-fixtures
  :each (fn [f]
          (tu/start-with-env-override '{HTTP_PORT     8080
                                        UI_ALLOW_ANON true}
                                      #'{{namespace}}.http/server)
          (f)
          (m/stop)))


(deftest test-ui
  (let [{:keys [status body]} (http/get "http://localhost:8080/ui")]
    (is (= 200 status))
    (is (re-seq #"Hello, World!" body))))
