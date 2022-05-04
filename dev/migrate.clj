(ns migrate
  (:require
    [clojure.java.io :as io]
    [clojure.string :as str]
    [taoensso.timbre :refer [debug warn]]
    [honey.sql :as sql]
    [honey.sql.helpers :as h]
    [integrant.core :as ig]
    ;;
    [angara.meteo.db.psql :refer [exec-one! exec!]]
    [angara.meteo.lib.util :refer [parse-json]]
    [angara.meteo.system :refer [system-env]]
    [angara.meteo.db.postgis :as psg]
  ))


(def METEO_STATIONS_TABLE :meteo_stations)


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
     :elevation (when elev (int elev))
     :location {:lat lat :lon lon}
     :public (if pub (= 1 (int pub)) false)
     }
  ))


(defn insert-station [ds {loc :location :as st-data}]
  (->
    (h/insert-into METEO_STATIONS_TABLE)
    (h/values
      [(cond-> (select-keys st-data [:stid :title :descr :elevation :public])
          loc
          (assoc :location 
                 ;(psg/point (:lon loc) (:lat loc))
                 [:ST_MakePoint (:lon loc) (:lat loc)]
          )
       )
      ])
    (sql/format)
    (exec! ds)
  ))


(defn start-system []
  (let [system (ig/init (system-env))]
    (debug "system started:" system)
    system  
  ))


(comment

  (def system (start-system))
  (ig/halt! system)

  (def ds (:datasource/meteo system))

  (def STATIONS
    (read-json-file "tmp/meteo_st.json"))

  (def st1 (st-convert (nth STATIONS 1)))
  (def st2 (st-convert (nth STATIONS 2)))

  (insert-station ds st2)

  (set (mapcat keys STATIONS))
  ;; => #{:_id :addr :descr :dp :elev :last :ll :note :old_id :pub :series :title :trends :ts :url}

  ;(def sts (map #(dissoc %)))
  (count STATIONS)

  (nth STATIONS 10)

  (def st11 (nth STATIONS 11))

  (insert-station ds st11)

  )