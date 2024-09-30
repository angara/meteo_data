(ns angara.meteo.app.api
 (:import [angara.meteo SpatialFuncs])
 (:require 
  [malli.core :as m]
  [malli.error :as me]
  [malli.transform :as mt]
  [taoensso.encore :refer [map-vals]]
  [tick.core :as tick]
  [angara.meteo.http.resp :refer [throw-resp! jserr jsok]]
  [angara.meteo.db.meteo :as db]
  ,))


(defn parse-ts [ts catch-fn]
  (try
    (tick/instant ts)
    (catch Exception _ignore
      (when catch-fn (catch-fn ts)))
    ,))


(defn throw-jserr! [err]
  (throw-resp! (jserr err)))


(defn validate-params! [schema params]
  (let [par (m/decode schema params mt/string-transformer)]
    (when-not (m/validate schema par)
      (-> (m/explain schema par)
          (me/humanize)
          (jserr)
          (throw-resp!)
          ,))
    par))


(def active-params-schema
  (m/schema
   [:and
     [:map 
      [:search     {:optional true} [:string {:min 3 :max 100}]]
      [:lat        {:optional true} [:double {:min -90 :max 90}]]
      [:lon        {:optional true} [:double {:min -180 :max 180}]]
      [:last-hours {:optional true} [:int {:min 1 :max 50}]]]
     [:fn {:error/message "both lat and lon required"}
      ; {:error/fn (fn [_ _] (str "both lat and lon required"))}
      (fn [{:keys [lat lon]}] #_xor (if lat (boolean lon) (not (boolean lon))))]]
   ,))


(comment
 (m/validate active-params-schema {:lat 1. :lon 2.})
 ;; => true
 (m/validate active-params-schema {:lat 1000. :lon 2.})
 ;; => false
 (m/validate active-params-schema {})
 ;; => true
 (m/validate active-params-schema {:lat 1.})
 ;; => false
 (m/validate active-params-schema {:lon 1.})
 ;; => false

 (m/decode active-params-schema {:lat "1" :lon "20" :last-hours "1"} mt/string-transformer)
 ;;=> {:last-hours 1, :lat 1.0, :lon 20.0}

 (m/decode active-params-schema {:lat ".0"} mt/string-transformer)
  ;; => {:lat 0.0}

 (m/decode active-params-schema nil mt/string-transformer)
  ;; => nil

 (validate-params! active-params-schema {})
  ;; => {}

 (validate-params! active-params-schema {:lat "1" :lon "2"})
  ;; => {:lat 1.0, :lon 2.0}
 
 (try
   (validate-params! active-params-schema {:lat "4"})
   (catch Exception ex (ex-data ex)))
  ;; => #:http{:response {:body "[\"both lat and lon required\"]",
  ;;                      :headers {"Content-Type" "application/json;charset=utf-8"},
  ;;                      :status 400}}

 (try
  (validate-params! active-params-schema {:lat "xxx" :lon "999"})
  (catch Exception ex (ex-data ex)))
   ;; => #:http{:response {:body "{\"lat\":[\"should be a double\"],\"lon\":[\"should be at most 180\"]}",
   ;;                      :headers {"Content-Type" "application/json;charset=utf-8"},
   ;;                      :status 400}}

 ,)


(defn get-last-vals [st-list last-hours]
  (if (not-empty st-list)
    (let [after-ts (tick/<< (tick/now) (tick/of-hours last-hours))]
      (db/last-vals st-list after-ts))
    [[] nil]
    ,))


(defn- remove-nil-vals [obj]
  (apply dissoc obj (for [[k v] obj :when (nil? v)] k)))


;; distance(double lat1, double lat2, double lon1, double lon2, double el1, double el2) 
;;
(defn calc-distance [lat lon st]
  (assoc st :distance
         (let [st-lat (:lat st)
               st-lon (:lon st)]
           (if (and (double? st-lat) (double? st-lon))
             (SpatialFuncs/distance lat st-lat lon st-lon 0. 0.)
             99999.))
         ,))


(defn active-stations [{params :params}]
  (let [{:keys [lat lon last-hours search]} (validate-params! active-params-schema params)
        after-ts (tick/<< (tick/now) (tick/of-hours (max 6 (or last-hours 1))))
        [data err-msg] (db/active-stations {:after-ts after-ts :search search :offset 0 :limit 1000})
        _ (when err-msg (throw-jserr! {:error err-msg}))
        ;
        data (if (and lat lon)
               (->> data (map #(calc-distance lat lon %)) (sort-by :distance))
               data)
        ;
        data (if last-hours
               (let [st-list (map :st data)
                     [lvals err-msg] (get-last-vals st-list last-hours)
                     _ (when err-msg (throw-jserr! {:error err-msg}))
                     lv-map (->> lvals (map #(vector (:st %) (dissoc % :st))) (into {}))]
                 (map #(assoc % :last (get lv-map (:st %))) data))
               data)
        ;
        data (map #(-> % (dissoc :st_id) (remove-nil-vals)) data)]
      (cond-> {:stations data}
        (and lat lon) (assoc :from-point {:lat lat :lon lon})
        true (jsok))
    ,))


(def last-vals-params-schema
  (m/schema
   [:map 
    [:last-hours {:optional true} [:int {:min 1 :max 50}]]
    [:st 
     [:or 
      [:string {:min 1 :max 40}] 
      [:vector {:min 1 :max 1000} [:string {:min 1 :max 40}]]]]]
   ,))


(defn last-vals [{params :params}]
  (let [{st :st last-hours :last-hours} (validate-params! last-vals-params-schema params)
        st-list (if (vector? st) st [st])
        [data err-msg] (get-last-vals st-list (or last-hours 1))]
    (if data
      (jsok {:last-vals data})
      (jserr {:error err-msg}))
    ,))


(comment
  
  (m/validate last-vals-params-schema {:st "123"})
  ;; => true

  (m/validate last-vals-params-schema {:st ["123" "uuii"]})
  ;; => true

  (m/validate last-vals-params-schema {})
  ;; => false

  (m/validate last-vals-params-schema {:st ""})
  ;; => false

  (require '[criterium.core :refer [quick-bench]])

  
  (-> {:params {:lat "53.37" :lon "104.28"}}
      (active-stations)
      (quick-bench))
  ; Evaluation count : 498 in 6 samples of 83 calls.
;              Execution time mean : 820.821500 µs
;     Execution time std-deviation : 122.815873 µs
;    Execution time lower quantile : 691.005145 µs ( 2.5%)
;    Execution time upper quantile : 980.539732 µs (97.5%)
;                    Overhead used : 1.890782 ns
  
  (-> {:params {:lat "53.37" :lon "104.28" :last-vals "1"}}
      (active-stations)
      (quick-bench))
      
  ; Evaluation count : 144 in 6 samples of 24 calls.
;              Execution time mean : 2.287720 ms
;     Execution time std-deviation : 309.699978 µs
;    Execution time lower quantile : 1.957267 ms ( 2.5%)
;    Execution time upper quantile : 2.677440 ms (97.5%)
;                    Overhead used : 1.890782 ns
  ,)

;; ;; ;; ;; ;; ;; ;; ;; ;; ;;


(def ^:const DEFAULT_INTERVAL (tick/new-period 1  :days))
(def ^:const MAX_INTERVAL     (tick/new-period 40 :days))


(def series-params-schema
  (m/schema
   [:map
    [:st [:string {:min 1 :max 40}]]
    [:ts-beg {:optional true} [:string {:min 20 :max 32}]]
    [:ts-end {:optional true} [:string {:min 20 :max 32}]] 
    ,]))


; (count "2024-09-20T02:23:07.859723+08:00")
;; => 32
; (count "2024-09-20T02:23:07Z")
;; => 20

(def ^:const DURATION_HOUR (tick/new-duration 1 :hours))

(defn fill-nulls [t0 t1 data]
  (loop [result (transient []), t t0, rec data]
    (if (tick/> t t1)
      (persistent! result)
      (let [[{:keys [ts avg]} & tail] rec]
        (if (and ts (tick/<= ts t))
          (recur (conj! result avg), (tick/>> t DURATION_HOUR), tail)
          (recur (conj! result nil), (tick/>> t DURATION_HOUR), rec))
        ,))))


(defn station-hourly [{params :params}]
  (let [{st :st ts-beg :ts-beg ts-end :ts-end} (validate-params! series-params-schema params)
        {st-id :st_id} (db/station-by-st st)
        _ (when-not st-id (throw-jserr! {:error (str "missing station: " st)}))
        ;
        ts-beg (if ts-beg (parse-ts ts-beg #(throw-jserr! {:error (str "incorrect value ts-beg: " %)}))
                   (tick/<< (tick/now) DEFAULT_INTERVAL))
        ;
        ts-end (if ts-end (parse-ts ts-end #(throw-jserr! {:error (str "incorrect value ts-end: " %)}))
                   (tick/now))
        ;
        _  (when (tick/>= ts-beg ts-end)
             (throw-jserr! {:error "incorrect interval" :ts-beg ts-beg :ts-end ts-end}))
        _  (when (tick/< ts-beg (tick/<< ts-end MAX_INTERVAL))
             (throw-jserr! {:error "interval too big" :ts-beg ts-beg :ts-end ts-end}))
        ;
        hourly (db/station-hourly-avg st-id ts-beg ts-end)
        hour0  (tick/truncate ts-beg :hours)
        hour1  (tick/truncate ts-end :hours)
        ;
        series (->> hourly (group-by :vt) (map-vals #(fill-nulls hour0 hour1 %)))
        ,]
    (jsok {:st st :ts-beg hour0 :ts-end hour1 :series series})
    ,))

(comment
  
  (try (station-hourly {:params {:ts-beg "" :ts-end "" :st ""}})
    (catch Exception ex (ex-data ex)))
  ;; => #:http{:response
  ;;             {:body
  ;;                "{\"st\":[\"should be at least 1 character\"],\"ts-beg\":[\"should be at least 20 characters\"],\"ts-end\":[\"should be at least 20 characters\"]}",
  ;;              :headers {"Content-Type" "application/json;charset=utf-8"},
  ;;              :status 400}}
  
  (try (station-hourly {:params {:ts-beg "2024-09-20T12:00:00+08:00" :ts-end "2024-09-20T12:30:00+08:00" :st "uiii"}})
       (catch Exception ex (ex-data ex)))
  ;; => {:body "{\"st\":\"uiii\",\"ts-beg\":\"2024-09-20T04:00:00Z\",\"ts-end\":\"2024-09-20T04:30:00Z\"}",
  ;;     :headers {"Content-Type" "application/json;charset=utf-8"},
  ;;     :status 200}


  (def ts-beg (tick/instant "2024-09-20T12:40:00+08:00"))
  (def ts-end (tick/instant "2024-09-20T14:50:00+08:00"))

  (try (station-hourly {:params {:ts-beg "2024-09-20T12:00:00+08:00" :ts-end "2024-09-20T17:40:00+08:00" :st "uiii"}})
       (catch Exception ex (or (ex-data ex) ex)))
  ;; => {:body
  ;;       "{\"st\":\"uiii\",\"ts-beg\":\"2024-09-20T04:00:00Z\",\"ts-end\":\"2024-09-20T09:00:00Z\",\"series\":{\"d\":[4.5,3.0,null,2.0,null,-1.0],\"p\":[969.0,969.0,null,965.0,null,964.0],\"t\":[13.0,14.0,null,16.0,null,16.0],\"w\":[2.5,3.0,null,2.0,null,2.0]}}",
  ;;     :headers {"Content-Type" "application/json;charset=utf-8"},
  ;;     :status 200}

  ;; => {:body
  ;;       "{\"st\":\"uiii\",\"ts-beg\":\"2024-09-20T04:00:00Z\",\"ts-end\":\"2024-09-20T08:00:00Z\",\"series\":{\"d\":[4.5,3.0,2.0],\"p\":[969.0,969.0,965.0],\"t\":[13.0,14.0,16.0],\"w\":[2.5,3.0,2.0]}}",
  ;;     :headers {"Content-Type" "application/json;charset=utf-8"},
  ;;     :status 200}


  ,)
