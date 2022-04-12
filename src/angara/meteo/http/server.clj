(ns angara.meteo.http.server
  (:require
    [taoensso.timbre        :refer  [debug warn]]
    [org.httpkit.server     :refer  [run-server server-stop!]]
    [ring.middleware.params           :refer  [wrap-params]]
    [ring.middleware.keyword-params   :refer  [wrap-keyword-params]]
    [ring.middleware.multipart-params :refer  [wrap-multipart-params]]
    ;
    [angara.meteo.http.resp       :refer  [EXCEPTION_RESPONSE_KEY]]
    [angara.meteo.http.middleware :refer  [wrap-cors wrap-json-params wrap-server-name]]
  ))


(defn wrap-exception [handler]
  (fn [req]
    (try
      (handler req)
      (catch Exception ex
        (if-let [resp (-> ex (ex-data) (get EXCEPTION_RESPONSE_KEY))]
          resp
          (let [msg (str "exception: " (ex-message ex))]
            (warn ex msg (ex-data ex))
            {:status 500 :headers {"Content-Type" "text/plain"} :body msg})
      ))
    )))


(defn start [handler host port server-name]
  (debug "http.server start:" host port server-name)
  (-> handler
      (wrap-exception)
      (wrap-server-name server-name)
      (wrap-json-params)
      (wrap-keyword-params)
      (wrap-multipart-params)
      (wrap-params)
      (wrap-cors)
      (run-server {:ip                   host
                   :port                 port
                   :worker-name-prefix   "httpkit-"
                   :server-header        nil
                   :legacy-return-value? false})))
;;

(defn stop
  ([server] 
    (stop server 1000))
  ([server timeout]
    (when server
      (server-stop! server {:timeout timeout}))))
;;
