(ns angara.meteo.db.meteo
  (:import
    [net.postgis.jdbc.geometry Geometry])
  (:require
    [honey.sql :as sql]
    [honey.sql.helpers :as h]
    [angara.meteo.db.psql :refer [exec-one! exec!]]
  ))


(def METEO_STATIONS :meteo_stations)
(def METEO_DATA :meteo_data)
(def METEO_LAST :meteo_last)


(defn geom-point->lat-lon [loc]
  (when (instance? Geometry loc)
    (let [pnt (.getFirstPoint loc)]
      {:lon (.x pnt) :lat (.y pnt)}
  )))


;; select stid,title, ST_Distance (location,ST_Point (104.8,51.4)) as dist from meteo_stations order by dist limit 10;

;; elect stid,title, ST_Distance (location,ST_Point (104.8,51.4)) as dist from meteo_stations where ST_DWithin (location, ST_Point (104.8,51.4), 100000)  order by dist limit 10;


(defn sql-stations-near [lon lat radius only-public? limit]
  (->
    (h/select :st.* [[:ST_Distance :st.location [:ST_Point lon lat]] :dist])
    (h/from [METEO_STATIONS :st]) 
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
  ))


(defn sql-active-stations-near [lon lat radius since-datetime only-public? limit]
  (->
   (h/select :st.* [[:ST_Distance :st.location [:ST_Point lon lat]] :dist])
   (h/from [METEO_STATIONS :st])
   (h/where
    [:ST_DWithin
     :st.location
     [:ST_Point lon lat]
     radius]
    (when only-public?
      [:= :st.public true])
    [:in :st.stid 
      (-> (h/select :ls.stid) (h/from [METEO_LAST :ls]) (h/where [:>= :ls.ts since-datetime]))] 
    )
   (h/order-by [:dist :desc])
   (h/limit limit)
   (sql/format)
  ))


(defn station-info [ds stid]
  (->
   (h/select :*) (h/from METEO_STATIONS) (h/where [:= :stid stid]) (sql/format) (exec-one! ds)
  ))



(defn station-last-values [ds stid since-datetime]
  (-> 
    (h/select :*) 
    (h/from METEO_LAST) 
    (h/where [:= :stid stid] [:>= :ts since-datetime]) 
    (sql/format) 
    (exec! ds)
  ))


(defn map-last-values [values]
  (reduce
    #(assoc %1 (-> %2 :vtype keyword) (:value %2))
    {}
    values
  ))
  


(defn append-data [ds ts stid correct vtype value extra]
  (-> (h/insert-into METEO_DATA)
      (h/values [{:ts ts :stid stid :correct correct :vtype vtype :value value :extra extra}])
      (sql/format)
  ))


(defn station-hourly-data [ds stid t0 t1 vtype]
  ;; TODO: !!!
  )


(comment

  (require '[integrant.core :as ig])
  (require '[angara.meteo.system :refer [system-env]])
  (require )

  (def system (ig/init (system-env)))
  (ig/halt! system)

  (def ds (:datasource/meteo system))

  (append-data ds (java.util.Date.) "test" true "temp" +1 nil)

  (def sss
    (exec! (sql-stations-near 104.8 51.4 10000 false 100) ds))

  (-> sss first :location geom-point->lat-lon)

  (.x (.getFirstPoint (-> sss first :location)))
  (.y (.getFirstPoint (-> sss first :location)))

  ;;(q-stations-near 11 22 10 100 false)

  (->
   (sql-active-stations-near 104.8 51.4 10000 (java.util.Date. 123123123) false 100)
   (exec-one! ds))

  )
