(ns angara.meteo.db.datasource
  (:require
    ;[mount.core          :refer  [defstate]]
  ))
;=

(defstate psql
  :start
    (ps/start-ds (:database-url config))
  :stop
    (ps/stop-ds psql))
;=
