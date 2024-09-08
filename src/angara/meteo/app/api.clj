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

(defn- remove-nil-vals [obj]
  (apply dissoc obj (for [[k v] obj :when (nil? v)] k)))


;; distance(double lat1, double lat2, double lon1, double lon2, double el1, double el2) 

(defn calc-distance [lat lon st]
  (assoc st :distance
         (let [st-lat (:lat st)
               st-lon (:lon st)]
           (if (and (double? st-lat) (double? st-lon))
             (SpatialFuncs/distance lat st-lat lon st-lon 0. 0.)
             99999.
             ))))


(defn active-stations [{params :params}]
  (let [{:keys [lat lon last-val]} (validate-params! active-params-schema params)
        after-ts (tick/<< (tick/now) (tick/of-seconds ACTIVE_STATION_INTERVAL))
        [data err-msg] (db/active-stations {:after-ts after-ts :offset 0 :limit 1000})
        _ (when err-msg
            (throw-resp! (jserr {:error err-msg})))
        ;
        data (if lat
               (->> data (map #(calc-distance lat lon %)) (sort-by :distance))
               data
               )
        ;
        ;data (if last-val)
        ;; if lat
        ;; 
        ]
      (jsok {:stations (map #(-> % (dissoc :st_id) (remove-nil-vals)) data)})
    ,))


(def last-vals-params-schema
  (m/schema
   [:map 
    [:st 
     [:or 
      [:string {:min 1 :max 40}] 
      [:vector {:min 1 :max 1000} [:string {:min 1 :max 40}]] ]]]
   ))

(defn last-vals [{params :params}]
  (prn "--- params:" params)
  (let [{st :st} (validate-params! last-vals-params-schema params)
        _ (prn "===")
        st-list (if (vector? st) st [st])
        after-ts (tick/<< (tick/now) (tick/of-seconds LAST_VALS_INTERVAL))
        [data err-msg] (db/last-vals st-list after-ts)]
    (if data
      (jsok {:vals data})
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

  ,)


;; (defn station [req]
;;   {:status 500
;;    :body "not implemented"})

(defn series [req]
  {:status 500
   :body "not implemented"})

