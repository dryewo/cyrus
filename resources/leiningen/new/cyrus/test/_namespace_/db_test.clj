(ns {{namespace}}.db-test
  (:require [clojure.test :refer :all]
            [mount.lite :as m]
            [clojure.java.jdbc :as jdbc]
            [{{namespace}}.db :refer :all]
            [{{namespace}}.test-utils :as tu]))


(use-fixtures
  :each (fn [f]
          (tu/start-with-env-override {} #'*db*)
          (tu/wipe-db @*db*)
          (f)
          (m/stop)))


(deftest test-memories
  (is (= nil (get-memory {:id "1"})))
  (is (= 1 (create-memory! {:id "1" :memory-text "foo"})))
  ;; Column names are converted from camel_case to kebab-case
  (is (= {:id "1" :memory-text "foo"} (get-memory {:id "1"})))
  (is (= 1 (update-memory! {:id "1" :memory-text "bar"})))
  (is (= {:id "1" :memory-text "bar"} (get-memory {:id "1"})))
  (is (= 1 (delete-memory! {:id "1"})))
  (is (= nil (get-memory {:id "1"}))))
