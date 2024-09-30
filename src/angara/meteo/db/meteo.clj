(ns angara.meteo.db.meteo
  (:require
   [clojure.string :as str]
   [tick.core :as tick]
   [taoensso.telemere :refer [log!]]
   [angara.meteo.db.pg :refer [dbc]]
   [angara.meteo.db.inbound-sql :as ms]
   [angara.meteo.db.api-sql :as api-sql]
   [pg.core :refer [with-tx] :as pg]
   [pg.pool :refer [with-connection]]
   ,))


(defn check-auth [auth-id req-secret]
  (when-not (str/blank? auth-id)
    (with-connection [conn dbc]
      (when-let [{{secret :secret} :params :as auth} (ms/get-auth conn auth-id)]
        (when (and secret 
                   (= secret (str/trim req-secret)))
          auth
          ,))
      ,)))


(defn sensor-by-hwid [auth hwid]
  (when-not (str/blank? hwid)
    (with-connection [conn dbc]
      (ms/sensor-by-auth-hwid conn auth hwid)
      ,)))


(defn station-by-st [st] 
  (when-not (str/blank? st)
    (with-connection [conn dbc]
      (ms/station-by-st conn st)
      ,)))


(def ^:const FVALS_PER_HOUR 30)


(defn submit-fval [st-id ts vt fval]
  (try
    (with-connection [conn dbc]
      (with-tx [conn]
        (let [last-ts (ms/last-ts conn st-id vt)]
          (cond
            (and last-ts (tick/<= ts last-ts))
            [nil "old timestamp"]
            ;
            (< FVALS_PER_HOUR (ms/last-hour-count conn st-id vt)) 
            [nil "too frequent"]
            ;
            :else 
            (let [avg (when (not= "b" vt)
                        (ms/hour-avg conn st-id ts vt))
                  delta (if avg (- fval avg) nil)]
              (ms/submit-fval conn st-id ts vt fval)
              (ms/submit-last conn st-id ts vt fval delta)
              [:ok nil]))
          ,)))
    (catch Exception ex
      (log! :warn ["database error" {:st-id st-id :ts ts :vt vt :fval fval} (ex-message ex)])
      [nil "database error"])
    ,))


(comment 
  
  (check-auth "_" "_")
  ;; => {:auth "_", :params {:secret "_"}}

  (def TS0 (tick/now))

  (let [; now (tick/now)
        now TS0
        st-id 999
        ts now
        vt "t"
        fval -15
        delta 1.5] 
    
    (with-connection [conn dbc]
      (ms/submit-fval conn st-id ts vt fval)
      (ms/submit-last conn st-id ts vt fval delta)
      ,))

  (with-connection [conn dbc]
    (ms/hour-avg conn 999 TS0 "t"))

  ,)


(defn active-stations [{:keys [after-ts search _offset _linit] :as par}]
  (try
    (with-connection [conn dbc]
      (let [search (if search (str "%" search "%") "%")
            data (api-sql/select-active-stations conn 
                                                 (assoc par :search search))]
        [data nil]))
    (catch Exception ex
      (log! :warn ["active-stations error" {:after-ts after-ts :search search} (ex-message ex)])
      [nil (ex-message ex)])
    ,))


(defn last-vals [st-list after-ts]
  (try 
    (with-connection [conn dbc]
      (let [data (api-sql/select-last conn {:st-list st-list :after-ts after-ts})]
        (->> data
             (group-by :st)
             (map (fn [[st val-maps]]
                    (reduce
                     (fn [a {:keys [vt ts fval delta]}]
                       (cond-> (assoc a
                                      (keyword (str vt "_ts")) ts
                                      (keyword vt) fval)
                         (not= vt "b")
                         (assoc (keyword (str vt "_delta")) delta))) ;; hourly delta
                     {:st st}
                     val-maps)
                    ,))
             (#(vector % nil)))
             
        ,))
    (catch Exception ex
      (log! :warn ["last-vals error" {:st-list st-list} (ex-message ex)])
      [nil (ex-message ex)]
      ,)))


(comment

  (def after-ts (tick/now))

  (last-vals ["uiii" "irgp" "ratseka"] after-ts)
  ;; => ({:h 71.5830078125,
  ;;      :h_delta 0.0,
  ;;      :h_ts #time/offset-date-time "2024-09-08T19:48:11.542632+08:00",
  ;;      :p 681.786015625,
  ;;      :p_delta 0.0,
  ;;      :p_ts #time/offset-date-time "2024-09-08T19:48:11.542632+08:00",
  ;;      :st "ratseka",
  ;;      :t 5.119999885559082,
  ;;      :t_delta 0.0,
  ;;      :t_ts #time/offset-date-time "2024-09-08T19:48:11.542632+08:00"}
  ;;     {:b 300.0,
  ;;      :b_ts #time/offset-date-time "2024-09-08T19:30+08:00",
  ;;      :d 9.0,
  ;;      :d_delta 0.0,
  ;;      :d_ts #time/offset-date-time "2024-09-08T19:30+08:00",
  ;;      :p 954.0,
  ;;      :p_delta 0.0,
  ;;      :p_ts #time/offset-date-time "2024-09-08T19:30+08:00",
  ;;      :st "uiii",
  ;;      :t 9.0,
  ;;      :t_delta 0.0,
  ;;      :t_ts #time/offset-date-time "2024-09-08T19:30+08:00",
  ;;      :w 3.0,
  ;;      :w_delta 0.0,
  ;;      :w_ts #time/offset-date-time "2024-09-08T19:30+08:00"})

  ,)


(defn station-hourly-avg [st-id ts-beg ts-end]
  (with-connection [conn dbc]
    (api-sql/station-hourly-avg conn {:st-id st-id :ts-beg ts-beg :ts-end ts-end})
    ,))

(comment
  
  (def st-id 3)
  (def ts-beg (tick/<< (tick/now) (tick/new-duration 10 :hours)))
  (def ts-end (tick/<< (tick/now) (tick/new-duration 1 :seconds)))
  
  (station-hourly-avg st-id ts-beg ts-end)
  ;; => [{:avg 4.5, :ts #time/offset-date-time "2024-09-20T12:00+08:00", :vt "d"}
  ;;     {:avg 969.0, :ts #time/offset-date-time "2024-09-20T12:00+08:00", :vt "p"}
  ;;     {:avg 13.0, :ts #time/offset-date-time "2024-09-20T12:00+08:00", :vt "t"}
  ;;     {:avg 2.5, :ts #time/offset-date-time "2024-09-20T12:00+08:00", :vt "w"}]

  (group-by :vt
            (station-hourly-avg st-id ts-beg ts-end))

  
  ,)