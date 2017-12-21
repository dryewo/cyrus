(ns {{namespace}}.lib.logging
  (:require [taoensso.timbre :as timbre]
            [taoensso.encore :as enc]
            [io.aviso.exception :as aviso-ex]
            [clojure.string :as str]
            [cheshire.core :as json]
            [clojure.tools.logging :as clojure-logging]))

(defn format-log-message
  "Similar to `format`, but uses JSON representation of values.
   Expects only %s in the format string."
  [fmt & args]
  (apply format fmt (map json/encode args)))

(defmacro logf
  "Logs a message, formatted with clear distinction for dynamic values."
  [level throwable message & more]
  `(when (clojure-logging/enabled? ~level)
     (let [msg# (format-log-message ~message ~@more)]
       (clojure-logging/log ~level ~throwable msg#))))

(defmacro trace [message & args]
  `(logf :trace nil ~message ~@args))

(defmacro debug [message & args]
  `(logf :debug nil ~message ~@args))

(defmacro info [message & args]
  `(logf :info nil ~message ~@args))

(defmacro warn [message & args]
  `(logf :warn nil ~message ~@args))

(defmacro error [throwable message & args]
  `(logf :error ~throwable (str ~message "\n") ~@args))

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

;; From https://github.com/alexander-yakushev/ns-graph/blob/master/src/ns_graph/core.clj
(defn abbrev-name
  "Abbreviate a dot- and dash- separated string by first letter. Leave the last
  part intact unless `abbr-last` is true."
  [string & [abbr-last]]
  (let [parts (partition-by #{\. \-} string)]
    (str/join
      (if abbr-last
        (map first parts)
        (concat (map first (butlast parts)) (last parts))))))

(defn default-log-output-fn
  "Formatting function for all log output."
  ([data]
   (default-log-output-fn nil data))
  ([_ data]
   (let [{:keys [level ?ns-str ?msg-fmt vargs ?err context]} data]
     (format "%5s [%s]%s %s - %s%s"
             (str/upper-case (name level))
             (.getName (Thread/currentThread))
             (str (:request context))
             (abbrev-name ?ns-str)
             (if-let [fmt ?msg-fmt]
               (apply format fmt vargs)
               (apply str vargs))
             (str (when ?err (timbre/stacktrace ?err)))))))

(defn set-default-output-fn! []
  (timbre/merge-config! {:output-fn default-log-output-fn}))
