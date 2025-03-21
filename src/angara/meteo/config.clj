(ns angara.meteo.config
  (:require
    [clojure.java.io :as io]
    [clojure.edn :as edn]
    [mlib.envvar :refer [env-str env-int]]
  ,))


(defn build-info []
  (-> "build-info.edn" (io/resource) (slurp) (edn/read-string)))


(defn load-config []
  {:meteo-database-url (env-str "METEO_DATABASE_URL")       ;; postgres://pg-host:5432/dbname?user=...&password=...
   ;
   :meteo-http-host    (env-str "METEO_HTTP_HOST" "localhost")
   :meteo-http-port    (env-int "METEO_HTTP_PORT" 8004)
   ;
   ; :redis-url           (env-str "REDIS_URL")                 ;; "redis://user:password@localhost:6379/"
   ;
   :build-info (build-info)
   ,})
