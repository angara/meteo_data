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
   :meteo-db-pool-min-size (env-int "METEO_DB_POOL_MIN_SIZE" 2)
   :meteo-db-pool-max-size (env-int "METEO_DB_POOL_MAX_SIZE" 16)
   ;
   :meteo-http-host    (env-str "METEO_HTTP_HOST" "localhost")
   :meteo-http-port    (env-int "METEO_HTTP_PORT" 8004)
   ;
   :build-info (build-info)
   ,})
