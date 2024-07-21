(ns user
  (:require
    [mount.core :as mount]
    [angara.meteo.config :as cfg]
    [angara.meteo.main  :refer [-main]]
   ,))


;; (defn restart []
;;   (mount/stop)
;;   (mount/start))

;; (defn reset []
;;   (tnr/refresh :after 'user/restart))


(comment

  (mount/start-with-args {:config-field "123"})

  cfg/config


  (-main)

  ,)

;;.
