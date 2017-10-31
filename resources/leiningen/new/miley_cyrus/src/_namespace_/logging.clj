(ns {{namespace}}.logging
  (:require [taoensso.timbre :as timbre]
            [clojure.string :as str]))

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
   (let [{:keys [level ?ns-str ?msg-fmt vargs ?err]} data]
     (format "%5s [%s] %s - %s%s"
             (str/upper-case (name level))
             (.getName (Thread/currentThread))
             (abbrev-name ?ns-str)
             (if-let [fmt ?msg-fmt]
               (apply format fmt vargs)
               (apply str vargs))
             (str (when ?err (timbre/stacktrace ?err)))))))

(defn set-default-output-fn! []
  (timbre/merge-config! {:output-fn default-log-output-fn}))
