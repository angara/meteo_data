(ns angara.meteo.db.meteo
  (:require
    [honey.sql :as sql]
    [honey.sql.helpers :as h]
  ))


(def STATIONS :meteo_stations)


;; select stid,title, ST_Distance (location,ST_Point (104.8,51.4)) as dist from meteo_stations order by dist limit 10;

;; elect stid,title, ST_Distance (location,ST_Point (104.8,51.4)) as dist from meteo_stations where ST_DWithin (location, ST_Point (104.8,51.4), 100000)  order by dist limit 10;


(defn q-stations-near [lon lat radius limit only-public?]
  (->
    (h/select :st.* [[:ST_Distance :st.location [:ST_Point lon lat]] :dist])
    (h/from [STATIONS :st]) 
    (h/where 
      [:ST_DWithin
        :st.location
        [:ST_Point lon lat]
        radius
       ]
      (when only-public?
        [:= :st.public true])
     )
    (h/order-by [:dist :desc])
    (h/limit limit)
    (sql/format)
   )
  )


(comment

  (require '[integrant.core :as ig])
  (require '[angara.meteo.system :refer [system-env]])
  (require '[angara.meteo.db.psql :refer [exec-one! exec!]])

  (def system (ig/init (system-env)))
  (ig/halt! system)

  (def ds (:datasource/meteo system))


  (def sss
    (exec! (q-stations-near 104.8 51.4 10000 100 false) ds))

  
  (-> sss first :location)

  ;;(q-stations-near 11 22 10 100 false)

  ,)


(defn active-stations-near [])