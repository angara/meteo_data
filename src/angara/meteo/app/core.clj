(ns angara.meteo.app.core
  (:require
    [taoensso.timbre    :refer  [debug warn]]
    [honey.sql          :as     sql]
    [honey.sql.helpers  :as     h]
    [angara.meteo.db.psql     :refer  [exec!]]
  ))



(comment

  (let [id 123]
    (->
      (h/select :*)
      (h/from [:hist :h])
      (h/where [:= :id id])
      (sql/format)
      (exec!)
    ,))
  

  (def HIST3
    (->
     (h/select :*)
     (h/from :hist)
     (h/where [:= :examid 576])
     (h/order-by [:id :desc])
     (h/limit 3)
     (sql/format)
     (exec!)
    ))

  (doseq [h HIST3]
    (spit (str "wrk/hist-" (:id h) ".edn") h))

  (get HIST3 0)
  
  ,)
