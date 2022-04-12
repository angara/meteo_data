(ns angara.meteo.main
  (:gen-class)
  (:require
    [taoensso.timbre :refer [debug info warn] :as timbre]
    [integrant.core :as ig]
    ;;
    [angara.meteo.system :refer [build-info system]]
  ))


(defn setup-logger! []
  (timbre/merge-config!
   {:output-fn (partial timbre/default-output-fn {:stacktrace-fonts {}})
    :min-level [[#{"angara.*"} :debug]
                [#{"*"} :info]]}))


(defn -main []
  (info "start:" @build-info)
  (setup-logger!)
  (try
    (let [started (ig/init system)]
      (info "system started:" started))
    (catch Throwable ex
      (warn ex "main interrupted")
      (Thread/sleep 1000)
      (System/exit 1)
    )))
