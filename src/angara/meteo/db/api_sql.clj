(ns angara.meteo.db.api-sql
  (:require
   [clojure.java.io :as io]
   [pg.hugsql :as hug]
   ,))


(declare select-active-stations)
(declare select-last)
(declare station-hourly-avg)

(hug/def-db-fns (io/resource "angara/meteo/db/api.sql"))


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


