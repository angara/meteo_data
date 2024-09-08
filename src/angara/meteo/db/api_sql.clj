(ns angara.meteo.db.api-sql
  (:require
   [clojure.java.io :as io]
   [pg.hugsql :as hug]
   [pg.core :as pg]

   #_[angara.meteo.db.pg :refer [exec exec-one]]
   ,))


(defn active-stations [conn]
  (-> conn
      (pg/execute
                  ""
                  {:params []})))
                  

(declare select-active-stations)

(declare select-last)

(hug/def-db-fns (io/resource "sql/api.sql"))

;; hugsql.core/hugsql-command-fn


(comment
   (require '[angara.meteo.db.pg :refer [dbc]])
   (require '[tick.core :as t])

   (-> select-active-stations var meta)

   (let [after-ts (-> (t/now) (t/<< (t/of-hours 1)))
         st-ids [3 5]
         ]
     (select-last dbc {:after-ts after-ts :st-ids st-ids})
     )

    (let [after-ts (-> (t/now) (t/<< (t/of-hours 1)))]
      (select-active-stations dbc {:after-ts after-ts :offset nil :limit 2}))

 ,)


