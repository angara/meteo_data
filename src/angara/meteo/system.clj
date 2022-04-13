(ns angara.meteo.system
  (:require
    [clojure.spec.alpha :as    s]
    [clojure.string     :refer [trim blank?]]
    [clojure.java.io    :as    io]
    [integrant.core     :as    ig]
    ;
    [angara.meteo.lib.env :as env]
    [angara.meteo.db.psql :refer [start-ds stop-ds]]
    [angara.meteo.http.server :as server]
    [angara.meteo.app.routes :as routes]
  ))


; ; ; ; ; ; ; ; ; ;

(s/def ::not-blank (complement blank?))

(s/def ::meteo-database-url ::not-blank)
(s/def ::meteo-http-host    ::not-blank)
(s/def ::meteo-http-port    pos-int?)

; ; ; ; ; ; ; ; ; ;

(def ENV_VARS
  [[:meteo-database-url "METEO_DATABASE_URL"]              ;; postgres://pg-host:5432/dbname?user=...&password=...
   [:meteo-http-host    "METEO_HTTP_HOST" "localhost"]
   [:meteo-http-port    "METEO_HTTP_PORT" 8002 env/to-int]])

; ; ; ; ; ; ; ; ; ;

(def build-info
  (delay (-> "build-info" (io/resource) (slurp) (trim))))


(def system 
  {
    :config/env {}  ;; must be set by read-config "env.edn"
    :datasource/meteo {:config (ig/ref :config/env)}
  })


;; https://github.com/weavejester/integrant

(defn system-env []
  (assoc system :config/env (env/load-env-vars ENV_VARS)))


(defmethod ig/init-key :config/env [_ env] env)

;; NOTE: move spec to particular keys
(defmethod ig/pre-init-spec :config/env [_]
  (s/keys :req-un 
          [::meteo-database-url ::meteo-http-host ::meteo-http-port]))


(defmethod ig/init-key :datasource/meteo [_ {:keys [config] :as sys}]
  (prn "env:" config sys)
  (start-ds (:meteo-database-url config)))

(defmethod ig/halt-key! :datasource/meteo [_ ds]
  (stop-ds ds))


(defmethod ig/init-key :http/handler [_ system]
  (routes/make-handler system))


(defmethod ig/init-key :http/server [_ {:keys [http-server http-handler]}]
  (let [{:keys [host port]} http-server]
    (prn "http-server:" :http-server)
    (server/start http-handler host port @build-info)))

(defmethod ig/halt-key! :http/server [_ http-server]
  (prn "stop http-server.")
  (server/stop http-server))
