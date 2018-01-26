(ns {{namespace}}.utils)


(defn implementation-version []
  (or
    ;; When running in a REPL
    (System/getProperty "{{name}}.version")
    ;; When running as `java -jar ...`
    (-> (eval '{{package}}.core) .getPackage .getImplementationVersion)))
