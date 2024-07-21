(ns angara.meteo.main
  (:gen-class)
  (:require
    [taoensso.telemere :refer [log!]]
    [mount.core :as mount]
    [angara.meteo.config :refer [build-info]])
  ,)


(defn -main []
  (log! ["start:" build-info])
  (try
    (let [mnt (mount/start-with-args {})]
      (log! ["system started:" (:started mnt)]))
    (catch Throwable ex
      (log! :warn ["main interrupted" ex])
      (Thread/sleep 1000)
      (System/exit 1)
      ,)))
