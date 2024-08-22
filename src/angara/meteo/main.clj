(ns angara.meteo.main
  (:gen-class)
  (:require
    [taoensso.telemere :refer [log! set-middleware!]]
    [mount.core :as mount]
    [angara.meteo.config :refer [build-info load-config]]
    [angara.meteo.app.core]
   ,))


(defn -main []
  (log! ["start:" build-info])
  (try
    (set-middleware! #(dissoc % :host))
    (.addShutdownHook (Runtime/getRuntime) (Thread. #(mount/stop)))
    (let [cfg (load-config)
          mnt (mount/start-with-args cfg)]
      (log! ["system started:" (:started mnt)]))
    (catch Throwable ex
      (log! :warn ["main interrupted" ex])
      (Thread/sleep 3000)
      (System/exit 1)
      ,)))
