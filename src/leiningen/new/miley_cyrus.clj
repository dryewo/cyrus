(ns leiningen.new.miley-cyrus
  (:require [leiningen.new.templates :refer [renderer year date project-name
                                             ->files sanitize-ns name-to-path
                                             multi-segment sanitize]]
            [leiningen.core.main :as main]
            [clojure.string :as str]
            [leiningen.new.ga :as ga]))

(defn prefix [name]
  (->> (str/split name #"(-|_)")
       (map first)
       (apply str)))

(defn prepare-data [name]
  (let [namespace (project-name name)]
    {:raw-name    name
     :name        (project-name name)
     :namespace   namespace
     :package     (sanitize namespace)
     :nested-dirs (name-to-path namespace)
     :prefix      (prefix (project-name name))
     :year        (year)
     :date        (date)}))

(defn prepare-files
  "Generates arguments for ->files. Extracted for testing."
  [project-name]
  (let [data   (prepare-data project-name)
        render (renderer "miley_cyrus")]
    (main/debug "Template data:" data)
    (main/info "Generating a project called" project-name "based on the 'miley-cyrus' template.")
    (when-not (System/getenv "MILEY_CYRUS_TEST")
      (ga/hit (ga/make-payload {:t  "event"
                                :an "miley-cyrus"
                                :el "leinnew"})))
    [data
     ["project.clj" (render "project.clj" data)]
     ["README.md" (render "README.md" data)]
     ["LICENSE" (render "LICENSE" data)]
     [".gitignore" (render "_gitignore" data)]
     ["dev/user.clj" (render "dev/user.clj" data)]
     ["src/{{nested-dirs}}/core.clj" (render "src/_namespace_/core.clj" data)]
     ["src/{{nested-dirs}}/lib/logging.clj" (render "src/_namespace_/lib/logging.clj" data)]
     ["test/{{nested-dirs}}/core_test.clj" (render "test/_namespace_/core_test.clj" data)]
     "resources"]))

(defn miley-cyrus [project-name]
  (apply ->files (prepare-files project-name)))

(comment
  (prepare-files "foo")
  )
