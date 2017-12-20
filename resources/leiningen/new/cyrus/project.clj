(defproject {{raw-name}} "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [com.taoensso/timbre "4.10.0"]
                 [com.fzakaria/slf4j-timbre "0.3.8"]
                 [mount "0.1.11"]
                 [environ "1.1.0"]
                 [squeeze "0.1.1"]{{#nrepl}}
                 [org.clojure/tools.nrepl "0.2.13"]{{/nrepl}}{{#http}}
                 [aleph "0.4.4"]
                 [compojure "1.6.0"]
                 [ring/ring-defaults "0.3.1"]
                 [ring/ring-json "0.4.0"]
                 [amalloy/ring-gzip-middleware "0.1.3"]
                 [clj-http "3.7.0"]{{/http}}{{#swagger1st}}
                 [org.zalando/swagger1st "0.25.0"]{{/swagger1st}}{{#swagger1st-oauth2}}
                 [fahrscheine-bitte "0.2.0"]{{/swagger1st-oauth2}}{{#db}}
                 [conman "0.7.4"]
                 [org.postgresql/postgresql "42.1.4"]
                 [cheshire "5.8.0"]
                 [camel-snake-kebab "0.4.0"]
                 [migratus "1.0.2"]{{/db}}]
  :main ^:skip-aot {{namespace}}.core
  :target-path "target/%s"
  :uberjar-name "{{name}}.jar"
  :manifest {"Implementation-Version" ~#(:version %)}
  :plugins [[lein-cloverage "1.0.9"]
            [lein-set-version "0.4.1"]
            [lein-ancient "0.6.14"]]
  :profiles {:uberjar {:aot :all}
             :dev     {:repl-options   {:init-ns user}
                       :source-paths   ["dev"]
                       :resource-paths ["test/resources"]
                       :dependencies   [[org.clojure/tools.namespace "0.2.11"]
                                        [org.clojure/java.classpath "0.2.3"]]}})
