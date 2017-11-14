(ns {{namespace}}.env
  (:require [mount.core :as m]
            [environ.core]))

(m/defstate env
  :start (merge environ.core/env (m/args)))

(defn start-with-override [env-override]
  (-> (m/only [#'env])
      (m/with-args env-override)
      (m/start)))
