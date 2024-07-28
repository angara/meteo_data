(ns user
  (:require
    [mount.core :as mount]
    [angara.meteo.config :refer [load-config]]
    [angara.meteo.main  :refer [-main]]
   ;
   [angara.meteo.db.pg]
   ,))


;; (defn restart []
;;   (mount/stop)
;;   (mount/start))

;; (defn reset []
;;   (tnr/refresh :after 'user/restart))


(comment

  (def cfg (load-config))

  (mount/start-with-args cfg)

  (mount/stop)


  (-main)

  ,)

;;.
