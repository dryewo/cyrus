(ns {{namespace}}.lib.swagger1st
  (:require [io.sarnowski.swagger1st.executor :as s1stexec]
            [taoensso.timbre :as timbre]))

(defn merged-parameters
  "According to the swagger spec, parameter names are only unique with their type. This one assumes that parameter names
   are unique in general and flattens them for easier access."
  [request]
  (apply merge (vals (:parameters request))))

(defn resolve-operation-basic
  "Calls operationId function with flattened request params and raw request map."
  [request-definition]
  (when-let [operation-fn (s1stexec/operationId-to-function request-definition)]
    (fn [request]
      (operation-fn (merged-parameters request) request))))

(defn compute-request-info
  "Creates a nice, readable request info text for logline prefixing."
  [request]
  (str
    (.toUpperCase (-> request :request-method name))
    " "
    (:uri request)
    " <- "
    (if-let [x-forwarded-for (-> request :headers (get "x-forwarded-for"))]
      x-forwarded-for
      (:remote-addr request))
    (if-let [tokeninfo (:tokeninfo request)]
      (str " / " (get tokeninfo "uid") " @ " (get tokeninfo "realm"))
      "")))

(defn enrich-log-lines
  "Adds HTTP request context information to the logging facility's MDC in the 'request' key."
  [next-handler]
  (fn [request]
    (let [request-info (compute-request-info request)]
      (timbre/with-context
        {:request (str " [" request-info "]")}
        (next-handler request)))))
