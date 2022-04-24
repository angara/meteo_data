(ns angara.meteo.system
  (:require
    [clojure.spec.alpha :as    s]
    [integrant.core     :as    ig]
    ;[taoensso.timbre :refer [debug warn]]
    ;
    [angara.meteo.config :refer [load-config build-info]]
    [angara.meteo.db.psql :refer [start-ds stop-ds]]
    [angara.meteo.http.server :as server]
    [angara.meteo.app.routes :as routes]
  ))



(def system 
  {:config/env {}  ;; must be set by read-config "env.edn"
   :datasource/meteo {:config (ig/ref :config/env)}
   :http/handler {}
   :http/server {:config (ig/ref :config/env) :handler (ig/ref :http/handler)}
  })


;; https://github.com/weavejester/integrant

(defn system-env []
  (assoc system :config/env (load-config)))


(defmethod ig/init-key :config/env [_ env] env)

;; NOTE: move spec to particular keys
(defmethod ig/pre-init-spec :config/env [_]
  (s/keys :req-un 
          [::meteo-database-url ::meteo-http-host ::meteo-http-port]))

; ; ; ; ; ; ; ; ; ; ; ; ; ; ;

(defmethod ig/init-key :datasource/meteo [_ {:keys [config]}]
  (start-ds (:meteo-database-url config)))

(defmethod ig/halt-key! :datasource/meteo [_ ds]
  (stop-ds ds))

; ; ; ; ; ; ; ; ; ; ; ; ; ; ;

(defmethod ig/init-key :http/handler [_ system]
  (routes/make-handler system))

; ; ; ; ; ; ; ; ; ; ; ; ; ; ;

(defmethod ig/init-key :http/server [_ {:keys [config handler]}]
  (let [{:keys [meteo-http-host meteo-http-port]} config]
    (server/start handler meteo-http-host meteo-http-port @build-info)))


(defmethod ig/halt-key! :http/server [_ http-server]
  (server/stop http-server))

; ; ; ; ; ; ; ; ; ; ; ; ; ; ;
