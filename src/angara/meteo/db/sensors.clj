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
