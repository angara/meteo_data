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


  (def sys (ig/init (sys/system-env)))

  (ig/halt! sys)
  
  (def ds (:datasource/meteo sys))

  (require '[angara.meteo.db.psql :as psql])
  
  (psql/exec-one! ds ["select 1"])

  
  (def ds0 (psql/start-ds "jdbc:postgresql://localhost:5432/meteo?user=meteo&password=123qwe"))
  
  (psql/exec-one! ds0 ["select 1"])


  (-main)

  ,)

;;.
