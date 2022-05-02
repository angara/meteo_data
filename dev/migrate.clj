(ns migrate
  (:require
    [clojure.java.io :as io]
    [clojure.string :as str]
    [angara.meteo.lib.util :refer [parse-json]]
  ))


(defn read-json-file [fname]
  (with-open [rdr (io/reader fname)]
    (->> (line-seq rdr)
         (mapv parse-json))
  ))
 

(defn st-convert [st]
  (let [[lon lat] (:ll st)
        pub (:pub st)
        elev (:elev st)
        ]
    {:stid (:_id st)
     :title (:title st)
     :descr (str (:descr st) "\n" (:addr st))
     :elev  (when elev (int elev))
     :location {:lat lat :lon lon}
     :active (if pub (= 1 (int pub)) false)
     }
  ))



(comment

  (str/join "-" [nil 1 "qqq"])

  (int 1.0)

  (def STATIONS
    (read-json-file "tmp/meteo_st.json"))


  (set (mapcat keys STATIONS))
  ;; => #{:_id :addr :descr :dp :elev :last :ll :note :old_id :pub :series :title :trends :ts :url}

  (filter :dp STATIONS)

  (map #(println (:note %)) STATIONS)

  (filter :elev (map st-convert STATIONS))

  ;(def sts (map #(dissoc %)))
  (count STATIONS)

  (nth STATIONS 10)

  )