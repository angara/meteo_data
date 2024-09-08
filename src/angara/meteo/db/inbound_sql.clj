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


(defn get-station [conn auth hwid]
  (-> conn
      (pg/execute 
       (str "select s.st_id, n.st, n.params from meteo_sensors n"
            " join meteo_stations s on n.st = s.st where auth = $1 and hwid = $2")
       {:params [auth hwid]})
      (first)
      ,))


(defn last-hour-ts-count [conn st-id vt]
  (-> conn
      (exec-one (str "select max(ts) as last_ts, count(*) as cnt from meteo_data"
                     " where st_id = $1 and vt = $2 and ts >= now() - interval '1 hour' limit 1")
       [st-id vt])
      ,))


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
  (exec-one conn 
    (str "insert into meteo_data (st_id, ts, vt, fval) values ($1, $2, $3, $4)"
         " on conflict (ts, st_id, vt) do update set fval = $4")
    [st-id ts vt fval]
    ,))

