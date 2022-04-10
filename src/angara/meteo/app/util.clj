(ns angara.meteo.app.util
  (:require
   [cljc.java-time.format.date-time-formatter :as dtf]
   [jsonista.core :as json]))


(defn to-int [s]
  (if (string? s)
    (Long/parseLong s 10)
    s
  ))


(defn parse-json [s]
  (json/read-value s json/keyword-keys-object-mapper))


(def df_YMD (dtf/of-pattern "yyyy-MM-dd"))

(defn date->ymd [dt]
  (dtf/format df_YMD dt))
