(ns user
  (:require
    [clojure.tools.namespace.repl :as tnr]
    [integrant.repl :refer [clear go halt prep init reset reset-all]]
   
    ;[criterium.core :refer [quick-bench]]
    ;    
    [angara.meteo-data.main  :refer [-main]]
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

  ;; (restart)

  (-main)

  ,)

;;.
