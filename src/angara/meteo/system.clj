(ns angara.meteo.system
  (:require
    [clojure.string     :refer [trim]]
    [clojure.java.io    :as     io]
    [integrant.core     :as     ig]
    [aero.core          :refer [read-config]]
    ;
    [angara.meteo.db.psql :refer [start-ds stop-ds]]
    [angara.meteo.http.server :as server]
    [angara.meteo.app.routes :as routes]
  ))


(def build-info
  (delay (-> "build-info" (io/resource) (slurp) (trim))))


(def system 
  {
    ; ::build-info ""
    ::env-config {}
    ::meteo-db {:config (ig/ref ::env-config)}
  })


;; https://github.com/weavejester/integrant
;; https://github.com/juxt/aero

(defn load-env-config []
  (-> "env.edn" (io/resource) (read-config)))



(defmethod ig/init-key ::env-config [_ _]
  (prn "env-config")
  (load-env-config))

;; (defmethod ig/init-key :build-info [_ _]
;;   @build-info)


(defmethod ig/init-key ::meteo-db [_ {:keys [config]}]
  (start-ds (:meteo-database-url config)))

(defmethod ig/halt-key! ::datasource [_ ds]
  (stop-ds ds))


(defmethod ig/init-key ::http-handler [_ system]
  (routes/make-handler system)
  )


(defmethod ig/init-key ::http-server [_ {:keys [http-server http-handler]}]
  (let [{:keys [host port]} http-server]
    (prn "http-server:" :http-server)
    (server/start http-handler host port @build-info)))

(defmethod ig/halt-key! ::http-server [_ http-server]
  (server/stop http-server))
