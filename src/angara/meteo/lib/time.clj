(ns angara.meteo.lib.time
  (:require
   [cljc.java-time.format.date-time-formatter :as dtf]
  ))


(def df_YMD (dtf/of-pattern "yyyy-MM-dd"))


(defn date->ymd [dt]
  (dtf/format df_YMD dt))
