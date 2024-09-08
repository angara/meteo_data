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


(defn get-station [auth hwid]
  (when-not (str/blank? hwid)
    (with-connection [conn dbc]
      (ms/get-station conn auth hwid)
      ,)))



(def ^:const LAST_VALS_INTERVAL 4000) ;; seconds

(def ^:const SUBMIT_FVAL_INTERVAL 200) ;; 200 seconds

(def ^:const FVALS_PER_HOUR 30)


(defn submit-fval [st-id ts vt fval]
  (try
    (with-connection [conn dbc]
      (with-tx [conn]
        (let [{last-ts :last_ts cnt :cnt} (ms/last-hour-ts-count conn st-id vt)]
          (cond
            (> cnt FVALS_PER_HOUR) [nil "too frequent"]
            (and last-ts (tick/<= ts last-ts)) [nil "old timestamp"]
            :else 
            (let [avg (when (not= "b" vt)
                        (ms/hour-avg conn st-id ts vt))
                  delta (if avg (- fval avg) 0)]
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


(defn last-vals [st-list]
  (try 
    (with-connection [conn dbc]
      (let [after-ts (tick/<< (tick/now) (tick/of-seconds LAST_VALS_INTERVAL))
            data (api-sql/select-last conn {:st-list st-list :after-ts after-ts})]
        (->> data
             (group-by :st)
             (map (fn [[st val-maps]]
                    {(keyword st)
                     (reduce
                      (fn [a {:keys [vt ts fval delta]}]
                        (cond-> (assoc a
                                  (keyword (str vt "_ts")) ts
                                  (keyword vt) fval)
                          (not= vt "b")
                          (assoc (keyword (str vt "_delta")) delta))) ;; hourly delta
                      {}
                      val-maps)})))
        ,))
    (catch Exception ex
      (log! :warn ["last-vals error" {:st-list st-list} (ex-message ex)])
      :dberr
      ,)))


(comment
  
  (last-vals ["uiii" "irgp"])
  ;; => ({:uiii {:b 310.0,
  ;;             :b_ts #time/offset-date-time "2024-09-08T16:00+08:00",
  ;;             :d 10.0,
  ;;             :d_delta 0.0,
  ;;             :d_ts #time/offset-date-time "2024-09-08T16:00+08:00",
  ;;             :p 954.0,
  ;;             :p_delta 0.0,
  ;;             :p_ts #time/offset-date-time "2024-09-08T16:00+08:00",
  ;;             :t 10.0,
  ;;             :t_delta 0.0,
  ;;             :t_ts #time/offset-date-time "2024-09-08T16:00+08:00",
  ;;             :w 6.0,
  ;;             :w_delta -1.0,
  ;;             :w_ts #time/offset-date-time "2024-09-08T16:00+08:00"}})

  ;; => ({:uiii {:b 320.0,
  ;;             :b_ts #time/offset-date-time "2024-09-08T15:30+08:00",
  ;;             :d 10.0,
  ;;             :d_delta 0.0,
  ;;             :d_ts #time/offset-date-time "2024-09-08T15:30+08:00",
  ;;             :p 954.0,
  ;;             :p_delta 0.0,
  ;;             :p_ts #time/offset-date-time "2024-09-08T15:30+08:00",
  ;;             :t 10.0,
  ;;             :t_delta 0.0,
  ;;             :t_ts #time/offset-date-time "2024-09-08T15:30+08:00",
  ;;             :w 7.0,
  ;;             :w_delta 2.0,
  ;;             :w_ts #time/offset-date-time "2024-09-08T15:30+08:00"}}
  ;;     {:uuee {:b 140.0,
  ;;             :b_ts #time/offset-date-time "2024-09-08T15:30+08:00",
  ;;             :d 12.0,
  ;;             :d_delta 0.0,
  ;;             :d_ts #time/offset-date-time "2024-09-08T15:30+08:00",
  ;;             :p 1029.0,
  ;;             :p_delta 0.0,
  ;;             :p_ts #time/offset-date-time "2024-09-08T15:30+08:00",
  ;;             :t 22.0,
  ;;             :t_delta 2.0,
  ;;             :t_ts #time/offset-date-time "2024-09-08T15:30+08:00",
  ;;             :w 2.0,
  ;;             :w_delta 0.0,
  ;;             :w_ts #time/offset-date-time "2024-09-08T15:30+08:00"}})


  ,)
    
    
  
  