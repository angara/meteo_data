(ns angara.meteo.db.inbound-sql
  (:require
   [pg.core :as pg]
   [angara.meteo.db.pg :refer [exec exec-one]]
   ,))


(defn get-auth [conn auth]
  (-> conn
      (pg/execute "select auth, params from meteo_auth where auth = $1 limit 1" 
                  {:params [auth]})
      (first)
      ,))


(defn sensor-by-auth-hwid [conn auth hwid]
  (-> conn
      (pg/execute 
       (str "select s.st_id, n.st, n.params from meteo_sensors n"
            " join meteo_stations s on n.st = s.st where auth = $1 and hwid = $2")
       {:params [auth hwid]})
      (first)
      ,))


(defn station-by-st [conn st]
  (-> conn
      (pg/execute "select m.* from meteo_stations m where st = $1 limit 1" {:params [st]})
      (first)
      ,))


(defn last-hour-count [conn st-id vt]
  (-> conn
      (exec "select count(*) as cnt from meteo_data where st_id = $1 and vt = $2 and ts >= now() - interval '1 hour' limit 1"
            [st-id vt])
      (first)
      (:cnt)
      ,))


(defn last-ts [conn st-id vt]
  (-> conn
      (exec "select ts as last_ts from meteo_last where st_id = $1 and vt = $2 limit 1" [st-id vt])
      (first)
      (:last_ts)
      ))


(defn hour-avg [conn st-id ts vt]
  (-> conn
      (exec
            (str "select avg(fval) as fval from meteo_data" 
                 " where (st_id = $1) and (ts < $2) and (ts >= ($2::timestamptz - interval '1 hour')) and (vt = $3)")
            [st-id ts vt])
      (first)
      (:fval)
      ,))


(defn submit-last [conn st-id ts vt fval delta]
  (exec-one conn
        (str "insert into meteo_last (st_id, ts, vt, fval, delta) values ($1, $2, $3, $4, $5)"
             " on conflict (st_id, vt) do update set ts = $2, fval = $4, delta = $5")
       [st-id ts vt fval delta]
   ,))


(defn submit-fval [conn st-id ts vt fval]
  (exec conn
        "insert into meteo_data (st_id, ts, vt, fval) values ($1, $2, $3, $4) on conflict (ts, st_id, vt) do nothing"
        [st-id ts vt fval]))

