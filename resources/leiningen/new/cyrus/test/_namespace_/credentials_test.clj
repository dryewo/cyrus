(ns {{namespace}}.credentials-test
  (:require [clojure.test :refer :all]
            [{{namespace}}.credentials :as creds]
            [mount.extensions.basic :as mb]
            [mount.lite :as m]
            [{{namespace}}.test-utils :as tu]
            [clojure.java.io :as io]))


(def test-token-file "target/nakadi-token-secret")


(use-fixtures
  :each (fn [f]
          (spit test-token-file "haha")
          (tu/start-with-env-override '{CREDENTIALS_DIR "target"} #'{{namespace}}.credentials/refresher)
          (f)
          (m/stop)
          (io/delete-file test-token-file :silently)))


(deftest works
  (testing "Throws when unknown key."
    (is (thrown? AssertionError (creds/get :foo))))

  (testing "Returns contents of the file"
    (is (= "haha" (creds/get :nakadi-token-secret))))

  (spit test-token-file "bebe")
  (Thread/sleep 1100)

  (testing "Returns updated contents of the file"
    (is (= "bebe" (creds/get :nakadi-token-secret)))))
