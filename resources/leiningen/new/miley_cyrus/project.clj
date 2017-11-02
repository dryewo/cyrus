(defproject {{raw-name}} "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [com.taoensso/timbre "4.10.0"]
                 [com.fzakaria/slf4j-timbre "0.3.7"]
                 [mount "0.1.11"]{{#http}}
                 [aleph "0.4.3"]
                 [compojure "1.6.0"]
                 [ring/ring-defaults "0.3.1"]{{/http}}]
  :main ^:skip-aot {{namespace}}.core
  :target-path "target/%s"
  :uberjar-name "{{name}}.jar"
  :manifest {"Implementation-Version" ~#(:version %)}
  :plugins [[lein-cloverage "1.0.9"]
            [lein-set-version "0.4.1"]]
  :profiles {:uberjar {:aot :all}
             :dev     {:repl-options   {:init-ns user}
                       :source-paths   ["dev"]
                       :resource-paths ["test/resources"]
                       :dependencies   [[org.clojure/tools.namespace "0.2.11"]
                                        [org.clojure/java.classpath "0.2.3"]
                                        [midje "1.8.3" :exclusions [clj-time]]]}})
