(ns angara.meteo.app.repo
  (:require
   [clojure.string :as str]
   [angara.meteo.db.pg :refer [dbc]]
   [angara.meteo.db.sensors :as sn]
   [pg.pool :refer [with-connection]]
   ,))


(defn check-auth [auth-id req-secret]
  (when-not (str/blank? auth-id)
    (with-connection [conn dbc]
      (when-let [{{secret :secret} :params :as auth} (sn/get-auth conn auth-id)]
        (when (and secret 
                   (= secret (str/trim req-secret)))
          auth
          ,))
      ,)))


(defn get-station [auth hwid]
  (when-not (str/blank? hwid)
    (with-connection [conn dbc]
      (sn/get-station conn auth hwid)
      ,)))


(comment 
  
  (check-auth "_" "_")
  ;; => {:auth "_", :params {:secret "_"}}

  ,)
