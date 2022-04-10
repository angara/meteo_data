(ns angara.meteo.main
  (:gen-class)
  (:require
   [taoensso.timbre    :refer  [debug info warn] :as timbre]
   [mount.core         :refer  [start stop defstate]]
    ;;
    ; [mlib.thread        :refer  [join]]
    ;;
   [angara.meteo.cfg          :refer [build-info config]]
   [angara.meteo.db.datasource]    ;; start/stop ds
   ;
   [angara.meteo.http.server :as srv]
   [angara.meteo.app.routes :refer [make-handler]]
  ))



(defstate http-server
  :start
  (let [host (:http-host config)
        port (:http-port config)
        server-name @build-info
        ]
    (debug "http-server.start:" host port server-name)
    (srv/start (make-handler) host port server-name))
  :stop
  (do
    (debug "stop server")
    (srv/stop http-server)))


(defn -main []
  (info "start:" @build-info)

  (timbre/merge-config!
    {:output-fn (partial timbre/default-output-fn {:stacktrace-fonts {}})
     :min-level [
                  [#{"angara.*"} :debug]
                  [#{"*"} :info]
                  ,]})

  (try
    (let [mounted (start)]
      (info "mounted:" (:started mounted)))
      ;; (doseq [[site-name data] (-> cfg/app :sites)]
      ;;   (process-site site-name data))
      ; (info "exiting:" (join worker)))
    (catch Throwable ex
      (warn ex "main interrupted")
      (stop)))
  ;
  ,)

;;
