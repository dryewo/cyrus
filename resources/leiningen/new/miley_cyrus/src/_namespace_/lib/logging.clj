(ns {{namespace}}.lib.logging
  (:require [taoensso.timbre :as timbre]
            [taoensso.encore :as enc]
            [io.aviso.exception :as aviso-ex]
            [clojure.string :as str]))

(defmacro trace [& args]
  `(timbre/tracef ~@args))

(defmacro debug [& args]
  `(timbre/debugf ~@args))

(defmacro info [& args]
  `(timbre/infof ~@args))

(defmacro warn [& args]
  `(timbre/warnf ~@args))

(defmacro error [& args]
  `(timbre/errorf ~@args))

(defn make-colorless-appender [appender]
  (update appender :fn
          (fn [f]
            (fn [data]
              (binding [aviso-ex/*fonts* {}]
                (f data))))))

(defn disable-console-logging-colors []
  (timbre/merge-config! {:appenders {:println (make-colorless-appender
                                                (get-in timbre/example-config [:appenders :println]))}}))

(defn ns-filter [fltr]
  (-> fltr enc/compile-ns-filter enc/memoize_))

(defn find-best-ns-pattern [ns-str ns-patterns]
  (some->> ns-patterns
           (filter #(and (string? %)
                         ((ns-filter %) ns-str)))
           not-empty
           (apply max-key count)))

(defn log-by-ns-pattern
  [ns-patterns & [{:keys [?ns-str level] :as opts}]]
  (let [best-ns-pattern       (or (find-best-ns-pattern ?ns-str (keys ns-patterns))
                                  :all)
        best-ns-pattern-level (get ns-patterns best-ns-pattern :trace)]
    (when (timbre/level>= level best-ns-pattern-level)
      opts)))

(defn set-ns-log-levels! [log-ns-map]
  (timbre/merge-config! {:middleware [(partial log-by-ns-pattern log-ns-map)]}))

(defn set-level! [level]
  (timbre/set-level! level))

(defn canonical-level-name [strname]
  (-> strname
      (name)
      (str/lower-case)
      (keyword)))

(defn set-log-level-from-env! [level-name]
  (some-> level-name
          (canonical-level-name)
          (timbre/valid-level)
          (timbre/set-level!)))
