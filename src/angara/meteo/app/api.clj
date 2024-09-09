(ns angara.meteo.app.api
 (:import
   [angara.meteo SpatialFuncs])
 (:require 
  [malli.core :as m]
  [malli.error :as me]
  [malli.transform :as mt]
  [tick.core :as tick]
  [angara.meteo.http.resp :refer [throw-resp! jserr jsok]]
  [angara.meteo.db.meteo :as db]
  ,))


(def ^:const LAST_VALS_INTERVAL 4000) ;; seconds
(def ^:const ACTIVE_STATION_INTERVAL 4000)


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
      [:lat {:optional true} [:double {:min -90 :max 90}]]
      [:lon {:optional true} [:double {:min -180 :max 180}]]
      [:last-vals {:optional true} [:enum "1" "yes" "true"]]]
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

 (m/decode active-params-schema {:lat "1" :lon "20"} mt/string-transformer)
  ;; => {:lat 1.0, :lon 20.0}

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


(defn get-last-vals [st-list]
  (if (not-empty st-list)
    (let [after-ts (tick/<< (tick/now) (tick/of-seconds LAST_VALS_INTERVAL))]
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
  (let [{:keys [lat lon last-vals]} (validate-params! active-params-schema params)
        after-ts (tick/<< (tick/now) (tick/of-seconds ACTIVE_STATION_INTERVAL))
        [data err-msg] (db/active-stations {:after-ts after-ts :offset 0 :limit 1000})
        _ (when err-msg
            (throw-resp! (jserr {:error err-msg})))
        ;
        data (if (and lat lon)
               (->> data (map #(calc-distance lat lon %)) (sort-by :distance))
               data)
        ;
        data (if last-vals
               (let [st-list (map :st data)
                     [lvals err-msg] (get-last-vals st-list)
                     _ (when err-msg
                         (throw-resp! (jserr {:error err-msg})))
                     lv-map (->> lvals (map #(vector (:st %) (dissoc % :st))) (into {}))
                     ]
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
    [:st 
     [:or 
      [:string {:min 1 :max 40}] 
      [:vector {:min 1 :max 1000} [:string {:min 1 :max 40}]]]]]
   ,))


(defn last-vals [{params :params}]
  (let [{st :st} (validate-params! last-vals-params-schema params)
        st-list (if (vector? st) st [st])
        [data err-msg] (get-last-vals st-list)]
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
  
  (-> {:params {:lat "53.37" :lon "104.28" :last-vals "1" }}
      (active-stations)
      (quick-bench)
      )
  ; Evaluation count : 144 in 6 samples of 24 calls.
;              Execution time mean : 2.287720 ms
;     Execution time std-deviation : 309.699978 µs
;    Execution time lower quantile : 1.957267 ms ( 2.5%)
;    Execution time upper quantile : 2.677440 ms (97.5%)
;                    Overhead used : 1.890782 ns
  

  
  ,)


;; (defn station [req]
;;   {:status 500
;;    :body "not implemented"})

(defn series [req]
  {:status 500
   :body "not implemented"})

