(ns angara.meteo.db.meteo
  (:require
   [clojure.string :as str]
   [tick.core :as tick]
   [taoensso.telemere :refer [log!]]
   [angara.meteo.db.pg :refer [dbc]]
   [angara.meteo.db.meteo-sql :as ms]
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


(def SUBMIT_FVAL_INTERVAL 200) ;; 200 seconds


(defn submit-fval [st-id ts vt fval]
  (try
    (with-connection [conn dbc]
      (with-tx [conn]
        (let [after-ts (tick/<< (tick/now) (tick/of-seconds SUBMIT_FVAL_INTERVAL))
              last-ts (ms/last-ts conn st-id vt after-ts)]
          (if last-ts 
            (do
              (pg/rollback conn)
              :too-frequent)
            (let [avg (when (not= :b vt) 
                        (ms/hour-avg conn st-id ts vt))
                  delta (if avg (- fval avg) 0)]
              (ms/submit-fval conn st-id ts vt fval)
              (ms/submit-last conn st-id ts vt fval delta)
              :ok)
            ,))))
    (catch Exception ex
      (log! :warn ["database error" {:st-id st-id :ts ts :vt vt :fval fval} (ex-message ex)])
      :dberr)
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

  (let [after-ts (tick/<< (tick/now) (tick/of-seconds SUBMIT_FVAL_INTERVAL))]
    (with-connection [conn dbc]
      (ms/last-ts conn 999 "t" after-ts)))

  ,)
