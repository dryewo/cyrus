(defproject {{raw-name}} "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [cyrus/dovetail "0.3.0"]
                 [cheshire "5.8.1"]
                 [functionalbytes/mount-lite "2.1.1"]
                 [cyrus/config "0.3.1"]{{#nrepl}}
                 [org.clojure/tools.nrepl "0.2.13"]{{/nrepl}}{{#http}}
                 [aleph "0.4.6"]
                 [compojure "1.6.1"]
                 [ring/ring-defaults "0.3.2"]
                 [ring/ring-json "0.4.0"]
                 [amalloy/ring-gzip-middleware "0.1.3"]
                 [clj-http "3.9.1"]{{/http}}{{#swagger1st}}
                 [org.zalando/swagger1st "0.25.0"]{{/swagger1st}}{{#swagger1st-oauth2}}
                 [fahrscheine-bitte "0.4.0"]{{/swagger1st-oauth2}}{{#db}}
                 [conman "0.8.3"]
                 [org.postgresql/postgresql "42.2.5"]
                 [camel-snake-kebab "0.4.0"]
                 [org.flywaydb/flyway-core "5.2.4"]{{/db}}{{#nakadi}}
                 [me.dryewo/clj-nakadi-java "0.0.7"]{{/nakadi}}{{#ui}}
                 [hiccup "1.0.5"]{{/ui}}{{#ui-oauth2}}
                 [cyrus/ui-oauth2 "0.1.4"]{{/ui-oauth2}}{{#credentials}}
                 [me.dryewo/mem-files "0.1.1"]{{/credentials}}]
  :main ^:skip-aot {{namespace}}.core
  :target-path "target/%s"
  :uberjar-name "{{name}}.jar"
  :manifest {"Implementation-Version" ~#(:version %)}
  :plugins [[lein-cloverage "1.0.13"]
            [lein-set-version "0.4.1"]
            [lein-ancient "0.6.15"]]
  :profiles {:uberjar {:aot :all}
             :dev     {:repl-options   {:init-ns user}
                       :source-paths   ["dev"]
                       :resource-paths ["test/resources"]
                       :dependencies   [[org.clojure/tools.namespace "0.2.11"]
                                        [org.clojure/java.classpath "0.3.0"]]}})
