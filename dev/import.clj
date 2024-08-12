(ns import
  (:require
   [clojure.string :as str]
   [clojure.java.io :as io]
   [mount.core :as mount]
   [mlib.json :refer [parse-json]]
   [pg.pool :refer [with-connection]]
   [pg.honey :as pgh]
   [angara.meteo.config :refer [load-config]]
   [angara.meteo.db.pg :refer [dbc]]
   ,))


(defn station-data [id_ obj]
  (let [st_id (or (:old_id obj)
                  (swap! id_ inc))
        st     (:_id obj)
        title  (:title obj)
        descr  (str/join " " (remove str/blank? [(:descr obj) (:addr obj)]))
        [lon lat] (:ll obj)
        elev      (:elev obj)]
    {:st_id st_id
     :st st
     :title title
     :descr descr
     :lat lat
     :lon lon
     :elev elev
     :publ true
     }
    ,))


(defn read-jsonl [fname handler]
  (with-open [rdr (clojure.java.io/reader fname)]
    (let [id_ (atom 20)]
      (doall (map #(handler id_ (parse-json %)) (line-seq rdr)))
      ,)))


(defn insert-station [conn data]
  (pgh/insert-one conn :meteo_stations data) 
  )

(comment
  
  (read-jsonl "tmp/meteo_st.json" station-data)

  
  (def cfg (load-config))
 
  (mount/start-with-args cfg)
 
  (mount/stop)
  
  (with-connection [conn dbc]
    (doseq [st-data (read-jsonl "tmp/meteo_st.json" station-data)]
      (prn "st-data:" st-data)
      (insert-station conn st-data)
      )
    )

  ,)
