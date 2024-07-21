(ns mlib.json
  (:require
   [jsonista.core :as j]
   ,))


(defn parse-json [data]
  (j/read-value data j/keyword-keys-object-mapper))


(defn json-str [obj]
  (j/write-value-as-string obj))
