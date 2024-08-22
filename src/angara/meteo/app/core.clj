(ns angara.meteo.app.core
  (:require
    [taoensso.telemere :refer [log!]]
    [mount.core :refer [defstate args]]
    [angara.meteo.http.server :as srv]
    [angara.meteo.app.routes :refer [make-handler]]
   ,))


(defstate http-server
  :start
  (try
    (let [cfg (args)
          handler (make-handler)
          host (:meteo-http-host cfg)
          port (:meteo-http-port cfg)
          server-name (str (-> cfg :build-info :appname) "/" (-> cfg :build-info :version))
          ]
      (srv/start handler host port server-name))
    (catch Exception ex
      (log! :warn ["server start failed:" (ex-message ex)])
      (throw ex)
      )
    )
  :stop
   (srv/stop http-server)
  ,)
