(ns mlib.http
  (:import [java.net URLEncoder URLDecoder])
  (:require
   [clojure.string :as str]
  ,))


(defn encode-query-string [params]
  (->> params
       (map (fn [[f s]] (str (URLEncoder/encode (name f)) "=" (URLEncoder/encode (str s) "UTF-8"))))
       (str/join '&)))


(defn decode-query-string [^String qs]
  (when qs
    (->> (str/split qs #"&")
         (map #(let [[k v] (str/split % #"=")]
                 [(when k
                    (keyword (URLDecoder/decode ^String k "UTF-8")))
                  (when v
                    (URLDecoder/decode ^String v "UTF-8"))]))
         (into {}))))
