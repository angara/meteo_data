(ns angara.meteo.lib.util
  (:require
    [jsonista.core :as json]
  ))


(defn to-int [s]
  (Long/parseLong s 10))


(defn parse-json [s]
  (json/read-value s json/keyword-keys-object-mapper))

