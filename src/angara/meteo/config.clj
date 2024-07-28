(ns angara.meteo.config
  (:require
    [mlib.envvar :refer [env-str env-int]]
    [mlib.build-info :as bi]
  ,))


(def build-info bi/build-info)


(defn load-config []
  {:meteo-database-url (env-str "METEO_DATABASE_URL")       ;; postgres://pg-host:5432/dbname?user=...&password=...
   ;
   :meteo-http-host    (env-str "METEO_HTTP_HOST" "localhost")
   :meteo-http-port    (env-int "METEO_HTTP_PORT" 8002)
   ;
   ; :redis-url           (env-str "REDIS_URL")                 ;; "redis://user:password@localhost:6379/"
   ;
   :build-info bi/build-info
   ,})

