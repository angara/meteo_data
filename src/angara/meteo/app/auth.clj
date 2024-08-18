(ns angara.meteo.app.auth
  (:require
   [clojure.string :as str]
   [angara.meteo.db.pg :refer [dbc]]
   [angara.meteo.db.sensors :as sn]
   [pg.pool :refer [with-connection]]
   ,))


(defn load-auth [auth-id req-secret]
  (when-not (str/blank? auth-id)
    (with-connection [conn dbc]
      (when-let [{{secret :secret} :params :as auth} (sn/get-auth conn auth-id)]
        (when (and secret 
                   (= secret (str/trim req-secret)))
          auth
          ,))
      ,)))


(comment
  
  (load-auth "_" "_")
  ;; => {:auth "_", :params {:secret "_"}}

  ,)
