(ns leiningen.new.cyrus
  (:require [leiningen.new.templates :refer [renderer year date project-name
                                             ->files sanitize-ns name-to-path
                                             multi-segment sanitize]]
            [leiningen.core.main :as main]
            [clojure.string :as str]
            [leiningen.new.ga :as ga])
  (:import java.util.Date
           java.text.SimpleDateFormat))


(defn prefix [name]
  (->> (str/split name #"(-|_)")
       (map first)
       (apply str)))


(defn timestamp []
  (.format (SimpleDateFormat. "yyyyMMddHHmmss") (Date.)))


(defn feature-flags [feature-set]
  (into {} (for [f feature-set]
             [(-> f (str/replace "+" "") keyword) true])))


(defn prepare-data
  ([name]
   (prepare-data name #{}))
  ([name feature-set]
   (let [namespace (project-name name)]
     (merge
       {:raw-name    name
        :name        (project-name name)
        :namespace   namespace
        :package     (sanitize namespace)
        :nested-dirs (name-to-path namespace)
        :prefix      (prefix (project-name name))
        :year        (year)
        :date        (date)
        :now-ts      (timestamp)
        :debug       (System/getenv "DEBUG")}
       (feature-flags feature-set)))))


(defn prepare-files
  "Generates arguments for ->files. Extracted for testing."
  [name feature-set]
  (let [data   (prepare-data name feature-set)
        render (renderer "cyrus")
        now-ts (timestamp)]
    (main/debug "Template data:" data)
    (main/info "Generating a project called" name "based on the 'cyrus' template.")
    (when-not (System/getenv "CYRUS_TEST")
      (ga/hit (ga/make-payload {:t  "event"
                                :an "cyrus"
                                :el "leinnew"})))
    (concat
      [data
       ["project.clj" (render "project.clj" data)]
       ["README.md" (render "README.md" data)]
       ["LICENSE" (render "LICENSE" data)]
       [".gitignore" (render "_gitignore" data)]
       ["dev-env.edn" (render "dev-env.edn" data)]
       ["dev/user.clj" (render "dev/user.clj" data)]
       ["src/{{nested-dirs}}/core.clj" (render "src/_namespace_/core.clj" data)]
       ["src/{{nested-dirs}}/utils.clj" (render "src/_namespace_/utils.clj" data)]
       ["test/{{nested-dirs}}/core_test.clj" (render "test/_namespace_/core_test.clj" data)]
       ["test/{{nested-dirs}}/test_utils.clj" (render "test/_namespace_/test_utils.clj" data)]
       "resources"]
      (when (:ui data)
        [["src/{{nested-dirs}}/ui.clj" (render "src/_namespace_/ui.clj" data)]
         ["test/{{nested-dirs}}/ui_test.clj" (render "test/_namespace_/ui_test.clj" data)]
         ["resources/ui/style.css" (render "resources/ui/style.css" data)]])
      (when (:nakadi data)
        [["src/{{nested-dirs}}/events.clj" (render "src/_namespace_/events.clj" data)]])
      (when (:credentials data)
        [["src/{{nested-dirs}}/credentials.clj" (render "src/_namespace_/credentials.clj" data)]
         ["test/{{nested-dirs}}/credentials_test.clj" (render "test/_namespace_/credentials_test.clj" data)]
         ["credentials/nakadi-token-secret" "foo-token-replace-me-with-real-one"]])
      (when (:nrepl data)
        [["src/{{nested-dirs}}/nrepl.clj" (render "src/_namespace_/nrepl.clj" data)]])
      (when (:swagger1st data)
        [["resources/api.yaml" (render "resources/api.yaml" data)]
         ["src/{{nested-dirs}}/api.clj" (render "src/_namespace_/api.clj" data)]
         ["test/{{nested-dirs}}/api_test.clj" (render "test/_namespace_/api_test.clj" data)]])
      (when (:swagger1st-oauth2 data)
        [["src/{{nested-dirs}}/authenticator.clj" (render "src/_namespace_/authenticator.clj" data)]])
      (when (:http data)
        [["src/{{nested-dirs}}/http.clj" (render "src/_namespace_/http.clj" data)]
         ["test/{{nested-dirs}}/http_test.clj" (render "test/_namespace_/http_test.clj" data)]
         ["src/{{nested-dirs}}/lib/http.clj" (render "src/_namespace_/lib/http.clj" data)]])
      (when (:db data)
        [["src/{{nested-dirs}}/lib/db.clj" (render "src/_namespace_/lib/db.clj" data)]
         ["src/{{nested-dirs}}/db.clj" (render "src/_namespace_/db.clj" data)]
         ["test/{{nested-dirs}}/db_test.clj" (render "test/_namespace_/db_test.clj" data)]
         ["make.sh" (render "make.sh" data) :executable true]
         ["resources/db/queries.sql" (render "resources/db/queries.sql" data)]
         ["resources/db/migrations/init.sql" (render "resources/db/migrations/init.sql" data)]
         ["resources/db/migrations/19891109193400-add-memories-table.up.sql" (render "resources/db/migrations/19891109193400-add-memories-table.up.sql" data)]
         ["resources/db/migrations/19891109193400-add-memories-table.down.sql" (render "resources/db/migrations/19891109193400-add-memories-table.down.sql" data)]]))))


(def all-features #{"+all" "+http" "+db" "+nrepl" "+swagger1st" "+swagger1st-oauth2" "+ui" "+ui-oauth2"})
(def hidden-features #{"+nakadi" "+credentials" "+everything"})
(def supported-features (into all-features hidden-features))


(def feature-dependencies
  {"+swagger1st"        ["+http"]
   "+swagger1st-oauth2" ["+swagger1st"]
   "+ui"                ["+http"]
   "+ui-oauth2"         ["+ui"]
   "+all"               all-features
   "+everything"        supported-features})


(defn add-dependent-features "recursively resolves features"
  [dependencies features]
  (let [features  (set features)
        features+ (into features (for [f  features
                                       df (get dependencies f)]
                                   df))]
    (if (= features+ features)
      features+
      (recur dependencies features+))))


(defn cyrus [project-name & feature-params]
  (let [features     (set feature-params)
        unsupported  (not-empty (clojure.set/difference features supported-features))
        all-features (add-dependent-features feature-dependencies features)]
    (cond
      unsupported
      (main/info "Unrecognized options:" unsupported "\nSupported options are:" supported-features)
      :else
      (apply ->files (prepare-files project-name all-features)))))
