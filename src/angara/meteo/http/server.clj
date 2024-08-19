(ns angara.meteo.http.server
  (:require
    [taoensso.telemere :refer [log! error!]]
    [org.httpkit.server :refer [run-server server-stop!]]
    [ring.middleware.params :refer [wrap-params]]
    [ring.middleware.keyword-params :refer [wrap-keyword-params]]
    [ring.middleware.multipart-params :refer [wrap-multipart-params]]
    [angara.meteo.http.middleware :refer [wrap-cors wrap-json-params wrap-server-name]]
  ,))


(defn wrap-exception [handler]
  (fn [req]
    (try
      (handler req)
      (catch Exception ex
        (if-let [resp (-> ex (ex-data) (get :http/response))]
          (do
            (log! ["response" resp])
            resp)
          (do
            (error! ::wrap-exception ex)
            {:status 500 
             :headers {"Content-Type" "text/plain"} 
             :body (str "exception: " (ex-message ex))})
      ))
    ,)))


(defn start [handler host port server-name]
  (log! ["http.server start -" (str host ":" port) server-name])
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
                   :legacy-return-value? false})
      ,))


(defn stop
  ([server] 
    (stop server 3000))
  ([server timeout]
    (when server
      (log! "http.server stop.")
      (server-stop! server {:timeout timeout}))
   ,))
