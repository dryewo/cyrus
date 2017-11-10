(ns {{namespace}}.db-test
  (:require [clojure.test :refer :all]
            [mount.core :as mount]
            [{{namespace}}.db :refer :all]
            [clojure.java.jdbc :as jdbc]))

(defn wipe-db []
  (println "Wiping the DB")
  (jdbc/delete! *db* :memories ["true"]))

(deftest test-memories
  (mount/start #'{{namespace}}.db/*db*)
  (wipe-db)
  (is (= nil (get-memory {:id "1"})))
  (is (= 1 (create-memory! {:id "1" :memory-text "foo"})))
  ;; Fields are converted from camel_case to kebab-case
  (is (= {:id "1" :memory-text "foo"} (get-memory {:id "1"})))
  (is (= 1 (update-memory! {:id "1" :memory-text "bar"})))
  (is (= {:id "1" :memory-text "bar"} (get-memory {:id "1"})))
  (is (= 1 (delete-memory! {:id "1"})))
  (is (= nil (get-memory {:id "1"})))
  (mount/stop))
