(ns user
  (:require
    [portal.api :as portal]
    [mount.core :as mount]
    [angara.meteo.config :refer [load-config]]
    [angara.meteo.main  :refer [-main]]
   ;
   [angara.meteo.db.pg :refer [dbc]]
   ;
   [pg.pool :refer [with-connection]]
   [pg.core :as pg]
   ,))


;; (defn restart []
;;   (mount/stop)
;;   (mount/start))

;; (defn reset []
;;   (tnr/refresh :after 'user/restart))


(comment

  (def p (portal/open {:launcher :vs-code})) ;; NOTE: portal extension required
  (add-tap #'portal/submit)

  (tap> :tap)
  (portal/clear) ; Clear all values

  (prn @p) ; bring selected value back into repl

  (remove-tap #'portal/submit) ; Remove portal from tap> targetset

  (portal/close) ; Close the inspector when done

  (portal/docs) ; View docs locally via Portal - jvm / node only

  
  ;; (require '[clojure.datafy :as d])
  ;; (require '[portal.api :as p])
  
  ;; (def submit (comp p/submit d/datafy))
  ;; (add-tap #'submit)
  
  ;; (tap> *ns*)


  (def cfg (load-config))

  (mount/start-with-args cfg)
  (mount/stop)

  (with-connection [conn dbc]
    (pg/query conn "select * from meteo_stations"))

  (-main)

  ())
