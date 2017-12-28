(ns {{namespace}}.env
  (:require [mount.lite :as m]
            [environ.core]))

(defonce ^:dynamic *env-override* nil)

(m/defstate env
  :start (merge environ.core/env *env-override*))
