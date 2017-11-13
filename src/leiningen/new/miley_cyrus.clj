(ns leiningen.new.miley-cyrus
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
        :now-ts      (timestamp)}
       (when (contains? feature-set "+nrepl")
         {:nrepl true})
       (when (contains? feature-set "+http")
         {:http true})
       (when (contains? feature-set "+swagger1st")
         {:swagger1st true})
       (when (contains? feature-set "+db")
         {:db true})))))

(defn prepare-files
  "Generates arguments for ->files. Extracted for testing."
  [name feature-set]
  (let [data   (prepare-data name feature-set)
        render (renderer "miley_cyrus")
        now-ts (timestamp)]
    (main/debug "Template data:" data)
    (main/info "Generating a project called" name "based on the 'miley-cyrus' template.")
    (when-not (System/getenv "MILEY_CYRUS_TEST")
      (ga/hit (ga/make-payload {:t  "event"
                                :an "miley-cyrus"
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
       ["src/{{nested-dirs}}/env.clj" (render "src/_namespace_/env.clj" data)]
       ["src/{{nested-dirs}}/lib/logging.clj" (render "src/_namespace_/lib/logging.clj" data)]
       ["src/{{nested-dirs}}/lib/config.clj" (render "src/_namespace_/lib/config.clj" data)]
       ["test/{{nested-dirs}}/core_test.clj" (render "test/_namespace_/core_test.clj" data)]
       "resources"]
      (when (contains? feature-set "+nrepl")
        [["src/{{nested-dirs}}/nrepl.clj" (render "src/_namespace_/nrepl.clj" data)]])
      (when (contains? feature-set "+swagger1st")
        [["resources/api.yaml" (render "resources/api.yaml" data)]
         ["src/{{nested-dirs}}/lib/swagger1st.clj" (render "src/_namespace_/lib/swagger1st.clj" data)]
         ["src/{{nested-dirs}}/api.clj" (render "src/_namespace_/api.clj" data)]
         ["test/{{nested-dirs}}/api_test.clj" (render "test/_namespace_/api_test.clj" data)]])
      (when (contains? feature-set "+http")
        [["src/{{nested-dirs}}/http.clj" (render "src/_namespace_/http.clj" data)]
         ["test/{{nested-dirs}}/http_test.clj" (render "test/_namespace_/http_test.clj" data)]])
      (when (contains? feature-set "+db")
        [["src/{{nested-dirs}}/lib/db.clj" (render "src/_namespace_/lib/db.clj" data)]
         ["src/{{nested-dirs}}/db.clj" (render "src/_namespace_/db.clj" data)]
         ["test/{{nested-dirs}}/db_test.clj" (render "test/_namespace_/db_test.clj" data)]
         ["test/{{nested-dirs}}/lib/config_test.clj" (render "test/_namespace_/lib/config_test.clj" data)]
         ["make.sh" (render "make.sh" data) :executable true]
         ["resources/db/queries.sql" (render "resources/db/queries.sql" data)]
         ["resources/db/migrations/19891109193400-add-memories-table.up.sql" (render "resources/db/migrations/19891109193400-add-memories-table.up.sql" data)]
         ["resources/db/migrations/19891109193400-add-memories-table.down.sql" (render "resources/db/migrations/19891109193400-add-memories-table.down.sql" data)]]))))

(def supported-features #{"+http" "+db" "+nrepl" "+swagger1st"})

(def feature-dependencies
  {"+swagger1st" ["+http"]})

(defn add-dependent-features "recursively resolves features"
  [dependencies features]
  (let [features  (set features)
        features+ (into features (for [f  features
                                       df (get dependencies f)]
                                   df))]
    (if (= features+ features)
      features+
      (recur dependencies features+))))

(defn miley-cyrus [project-name & feature-params]
  (let [features     (set feature-params)
        unsupported  (not-empty (clojure.set/difference features supported-features))
        all-features (add-dependent-features feature-dependencies features)]
    (cond
      unsupported
      (main/info "Unrecognized options:" unsupported "\nSupported options are:" supported-features)
      :else
      (apply ->files (prepare-files project-name all-features)))))
