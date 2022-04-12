(ns user
  (:require
    ; [clojure.tools.namespace.repl :as tnr]
    [integrant.core :as ig]
    [integrant.repl :refer [clear go halt prep init reset reset-all]]
   
    ;[criterium.core :refer [quick-bench]]
    ;    
    [angara.meteo.system :as sys]
    [angara.meteo.main  :refer [-main]]
))


;; https://github.com/weavejester/integrant

;; https://github.com/weavejester/integrant-repl

;; (tnr/set-refresh-dirs "src/")

;; (defn restart []
;;   (mount/stop)
;;   (mount/start))

;; (defn reset []
;;   (tnr/refresh :after 'user/restart))


(comment

  (ig/init sys/system)

  (-main)

  ,)

;;.
