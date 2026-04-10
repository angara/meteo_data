(ns angara.meteo.db.pg
  (:import [java.net URI URLDecoder])
  (:require
   [clojure.string :as str]
   [mount.core :refer [defstate args]]
   [pg.core :as pg]
   [pg.pool :as pool]))


(set! *warn-on-reflection* true)


(defn exec [conn sql params]
  (-> conn (pg/execute sql {:params params})))


(defn exec-one [conn sql params]
  (-> conn (pg/execute sql {:params params}) (first)))


(defn- split-qs [^String qs]
  (when qs
    (->> (str/split qs #"&")
         (map #(let [[k v] (str/split % #"=")]
                 [(when k
                    (keyword (URLDecoder/decode ^String k "UTF-8")))
                  (when v
                    (URLDecoder/decode ^String v "UTF-8"))]))
         (into {}))))


(defn url->db-spec [^String url]
  (let [u (URI/create url)
        params (-> (.getQuery u) (split-qs))]
    {:host     (.getHost u)
     :port     (.getPort u)
     :database (subs (.getPath u) 1)
     :user     (:user params)
     :password (:password params)
     :params params}))


(defn make-pool [cfg]
  (let [db-spec (-> (url->db-spec (:meteo-database-url cfg))
                    (assoc :pool-min-size (:meteo-db-pool-min-size cfg))
                    (assoc :pool-max-size (:meteo-db-pool-max-size cfg)))]
    (pg/pool db-spec)))


(defn pool-snapshot [pool]
  (when (pg/pool? pool)
    (pool/stats pool)))

(comment
  (url->db-spec "postgresql://localhost:5432/meteo?user=meteo&password=XXX")
  ;; => {:database "meteo",
  ;;     :host "localhost",
  ;;     :params {:password "XXX", :user "meteo"},
  ;;     :password "XXX",
  ;;     :port 5432,
  ;;     :user "meteo"}
  )


(defstate dbc
  :start (make-pool (args))
  :stop  (.close ^java.lang.AutoCloseable dbc))
