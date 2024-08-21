(ns angara.meteo.app.routes
  (:require
    [reitit.ring :refer [ring-handler routes router create-default-handler create-resource-handler]]
    [angara.meteo.app.inbound :refer [inbound-handler]]
    [angara.meteo.app.data-out :as out]
  ,))


(defn make-routes []
  [
    ["/meteo/_in" {:get inbound-handler :post inbound-handler}] ;; local rs.angara.net handler
   ;
    ["/meteo/api"
     ;; { throttle middleware }
     ["/active"  {:get out/data}]  ;; lat/lon
     ["/station" {:get out/data}]  ;; st
     ["/last"    {:get out/data}]  ;; sts, vts
     ["/series"  {:get out/data}]] ;; st, vts
  ])


(defn make-handler []
  (ring-handler
    (router (make-routes))
    (routes
     (create-resource-handler {:path "/meteo" :root "public"})
     (create-default-handler)
     )))
