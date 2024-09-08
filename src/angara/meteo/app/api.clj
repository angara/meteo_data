(ns angara.meteo.app.api
  (:require 
   [malli.core :as m]
   [malli.error :as me]
   [malli.transform :as mt]
   [angara.meteo.http.resp :refer [throw-resp! jserr]]
   ,))


(defn validate-params! [schema params]
  (let [par (m/decode schema params mt/string-transformer)]
    (when-not (m/validate schema par)
      (-> (m/explain schema par)
          (me/humanize)
          (jserr)
          (throw-resp!)
          ,))
    par))


;; (try
;;   (m/validate Address {:not "an address"})
;;   (catch Exception e
;;     (-> e ex-data :data :explain me/humanize)))


;; (-> [:map
;;      [:x :int]
;;      [:y [:set :keyword]]
;;      [:z [:map
;;           [:a [:tuple :int :int]]]]]
;;     (m/explain {:x "1"
;;                 :y #{:a "b" :c}
;;                 :z {:a [1 "2"]}})
;;     (me/humanize {:wrap #(select-keys % [:value :message])}))

;; mt/string-transformer

;; (m/decode [:and {:default 42} int?] nil mt/default-value-transformer)
;; ; => 42

;; (m/decode
;;  Address
;;  {:id "Lillan",
;;   :tags ["coffee" "artesan" "garden"],
;;   :address {:street "Ahlmanintie 29"
;;             :city "Tampere"
;;             :zip 33100
;;             :lonlat [61.4858322 23.7854658]}}
;;  mt/json-transformer)


(def active-params-schema
  (m/schema
   [:and
     [:map 
      [:lat {:optional true} [:double {:min -90 :max 90}]]
      [:lon {:optional true} [:double {:min -180 :max 180}]]]
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


(defn active-stations [{params :params}]
  (let [{:keys [lat lon]} (validate-params! active-params-schema params)]
    
    
    {:status 500
     :body "not implemented"}))


(def last-vals-params-schema
  (m/schema
   [:map 
    [:st 
     [:or 
      [:string {:min 1 :max 40}] 
      [:vector {:min 1 :max 1000} [:string {:min 1 :max 40}]] ]]]
   ))


(defn last-vals [{params :params}]
  (let [{st :st} (validate-params! last-vals-params-schema params)
        st (if (vector? st) st [st])
        ]
    
    )
  (prn "params:" params)
    
    {:status 500
     :body "not implemented"})


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

