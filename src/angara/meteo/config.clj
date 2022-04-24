(ns angara.meteo.config
  (:require
    [clojure.string :as str]
    [clojure.java.io :as io]
    [angara.meteo.lib.envvar :refer [env-str env-int]]
  ))


;; (s/def ::not-blank (complement str/blank?))

;; (s/def ::meteo-database-url ::not-blank)
;; (s/def ::meteo-http-host    ::not-blank)
;; (s/def ::meteo-http-port    pos-int?)

; ; ; ; ; ; ; ; ; ;

(def build-info
  (delay (-> "build-info" (io/resource) (slurp) (str/trim))))


(defn load-config []
  ;; TODO: conform spec
  {:meteo-database-url (env-str "METEO_DATABASE_URL")       ;; postgres://pg-host:5432/dbname?user=...&password=...
   :meteo-http-host    (env-str "METEO_HTTP_HOST" "localhost")
   :meteo-http-port    (env-int "METEO_HTTP_PORT" 8002)
  })

