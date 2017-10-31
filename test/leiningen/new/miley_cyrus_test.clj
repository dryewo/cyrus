(ns leiningen.new.miley-cyrus-test
  (:require [clojure.test :refer :all]
            [midje.sweet :refer :all]
            [leiningen.new.miley-cyrus :refer :all]))

(deftest test-prepare-data
  (facts ""
    (prepare-data "foo")
    => (contains {:raw-name    "foo"
                  :name        "foo"
                  :namespace   "foo"
                  :nested-dirs "foo"
                  :package     "foo"
                  :prefix      "f"})
    (prepare-data "foo-bar")
    => (contains {:raw-name    "foo-bar"
                  :name        "foo-bar"
                  :namespace   "foo-bar"
                  :package     "foo_bar"
                  :nested-dirs "foo_bar"
                  :prefix      "fb"})
    (prepare-data "foo/bar")
    => (contains {:raw-name    "foo/bar"
                  :name        "bar"
                  :namespace   "bar"
                  :package     "bar"
                  :nested-dirs "bar"
                  :prefix      "b"})
    (prepare-data "foo.baz/aaa-bbb")
    => (contains {:raw-name    "foo.baz/aaa-bbb"
                  :name        "aaa-bbb"
                  :namespace   "aaa-bbb"
                  :package     "aaa_bbb"
                  :nested-dirs "aaa_bbb"
                  :prefix      "ab"}))
  )
