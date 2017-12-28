(ns {{namespace}}.lib.http
  (:require [dovetail.core :as log]))

(defn compute-request-info
  "Creates a nice, readable request info text for logline prefixing."
  [request]
  (str
    (-> request :request-method name .toUpperCase)
    " "
    (:uri request)
    " <- "
    (if-let [x-forwarded-for (-> request :headers (get "x-forwarded-for"))]
      x-forwarded-for
      (:remote-addr request))
    (when-let [tokeninfo (:tokeninfo request)]
      (str " / " (get tokeninfo "uid") " @ " (get tokeninfo "realm")))))

(defn wrap-request-log-context
  "Adds HTTP request context information to the logging facility's MDC in the 'request' key."
  [next-handler]
  (fn [request]
    (let [request-info (compute-request-info request)]
      (log/with-context (str " [" request-info "]")
        (next-handler request)))))
