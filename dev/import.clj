(ns import
  (:import [org.pg.error PGErrorResponse])
  (:require
   [clojure.string :as str]
   [clojure.java.io :as io]
   [mount.core :as mount]
   [tick.core :as t]
   [mlib.json :refer [parse-json]]
   [pg.pool :refer [with-connection]]
   [pg.honey :as pgh]
   [pg.core :as pg]
   [angara.meteo.config :refer [load-config]]
   [angara.meteo.db.pg :refer [dbc]]
   ,))


(defn station-data [id_ obj]
  (let [st_id (or (:old_id obj)
                  (swap! id_ inc))
        st     (:_id obj)
        title  (:title obj)
        descr  (str/join " " (remove str/blank? [(:descr obj) (:addr obj)]))
        [lon lat] (:ll obj)
        elev      (:elev obj)]
    {:st_id st_id
     :st st
     :title title
     :descr descr
     :lat lat
     :lon lon
     :elev elev
     :publ true}
    ,))


(def st-map {}) ;; (alter-var-root st-map ...)


(defn convert-data [obj]
  {:st_id (:st_id (get st-map (:st obj)))
   :ts    (t/instant (-> obj :ts :$date))
   :vmap  (select-keys obj [:t :p :h :d :b :w :g])}
  ,)


(defn process-jsonl [fname handler]
  (with-open [rdr (clojure.java.io/reader fname)]
    (doseq [line (line-seq rdr)] 
      (handler (parse-json line)) 
      ,)))


(defn insert-data [conn data]  
  (let [st_id (:st_id data)
        ts    (:ts data)]
    (for [[vt fval] (:vmap data)]
      (try
        (let [fval (if (string? fval) (parse-double fval) fval)] 
          (pg/execute conn 
                      "insert into meteo_data (st_id, ts, vt, fval) values ($1, $2, $3, $4)" 
                      {:params [st_id ts (name vt) fval]}))
        (catch PGErrorResponse _ex)))))

;; ;; ;; ;; ;; ;;


(defn read-jsonl [fname handler]
  (with-open [rdr (clojure.java.io/reader fname)]
    (let [id_ (atom 50)]
      (doall (map #(handler id_ (parse-json %)) (line-seq rdr)))
      ,)))


(defn insert-station [conn data]
  (pgh/insert-one conn :meteo_stations data)) 
  


(defn load-stations-map [conn]
  (->>
   (pg/query conn "select * from meteo_stations")
   (reduce (fn [a s] (assoc a (:st s) s)) {})))


(comment
  
  ; (read-jsonl "tmp/meteo_st.json" station-data)
 
  (mount/start-with-args (load-config))
 
  (mount/stop)
  
  (let [st-map-data (with-connection [conn dbc]
                      (load-stations-map conn))]
    (alter-var-root #'st-map (constantly st-map-data)))
  
  (def home (System/getenv "HOME"))

  (let [cnt_ (atom 0)]
    (with-connection [conn dbc]
      (process-jsonl (str home "/tmp/meteo_dat.jsonl") 
                     (fn [obj]
                       (let [i (count (insert-data conn (convert-data obj)))
                             n (swap! cnt_ #(+ % (or i 0)))]
                        (when (= 0 (mod n 10000))
                          (prn n)))))
      ,))


  ;; (with-connection [conn dbc]
  ;;   (doseq [st-data (read-jsonl "tmp/meteo_st.json" station-data)]
  ;;     (prn "st-data:" st-data)
  ;;     (insert-station conn st-data)
  ;;     )
  ;;   )

  ;; (with-connection [conn dbc]
  ;; (doseq [data (read-jsonl "tmp/meteo_data.json" station-data)]
  ;;   (prn "st-data:" st-data)
  ;;   (insert-station conn st-data)))


  ,)


;; P
;; H
;; R
;; G
;; T
;; W

;; with vals as (
;;   select stamp::timestamptz as ts, loc_id as st_id, lower(typ) as vt, val as fval from dat where typ in ('T','W','G','H')
;;   union
;;   select stamp::timestamptz as ts, loc_id as st_id, 'p' as vt, (val-ext) as fval from dat where typ = 'P'
;;   union
;;   select stamp::timestamptz as ts, loc_id as st_id, 'b' as vt, ext as fval from dat where typ = 'W'
;; )
;; insert into meteo_data select * from vals;

;; --
;; INSERT 0 8854049

;; select count (*) from dat where typ = 'W';
;; count
;; --------
;; 649221

;; meteo=> select count (*) from dat where typ = 'R';
;; count
;; --------
;; 114514

;; meteo=> select count (*) from dat;
;; count
;; ---------
;; 8319342

;; select 8319342 - 114514 + 649221;
;; ?column?
;; ----------
;; 8854049


;; select distinct on (st_id) * from meteo_data order by st_id, ts;
;; select distinct on (st_id) * from meteo_data order by st_id, ts desc;


;; mongoexport --collection dat --uri mongodb://localhost/meteo --out meteo-20240813.jsonl
