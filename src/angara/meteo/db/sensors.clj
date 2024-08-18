(ns angara.meteo.db.sensors
  (:require
   [pg.core :as pg]
   ,))


(defn get-auth [conn auth]
  (-> conn
      (pg/execute "select auth, params from meteo_auth where auth = $1 limit 1" 
                  {:params [auth]})
      (first)
      ,))


(defn get-station [conn auth hwid]
  (-> conn
      (pg/execute 
       (str "select s.st_id, n.st, n.params from meteo_sensors n"
            " join meteo_stations s on n.st = s.st where auth = $1 and hwid = $2")
       {:params [auth hwid]})
      (first)
      ,))
