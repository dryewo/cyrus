(defproject cyrus/lein-template "0.22.2"
  :description "A very opinionated Clojure project template."
  :url "https://github.com/dryewo/cyrus"
  :license {:name "Apache License"
            :url  "http://www.apache.org/licenses/"}
  :eval-in-leiningen true
  :deploy-repositories [["releases" :clojars]]
  :plugins [[lein-shell "0.5.0"]
            [lein-ancient "0.6.15"]
            [lein-changelog "0.3.2"]]
  :release-tasks [["shell" "git" "diff" "--exit-code"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["changelog" "release"]
                  ["vcs" "commit"]
                  ["vcs" "tag"]
                  ["deploy"]
                  ["vcs" "push"]]
  :vcs :git
  :dependencies [[clj-http "3.9.1"]]
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.9.0"]
                                  [org.clojure/tools.namespace "0.2.11"]
                                  [org.clojure/java.classpath "0.3.0"]
                                  [midje "1.9.2"]]}})
