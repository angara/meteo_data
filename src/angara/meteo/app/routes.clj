(ns angara.meteo.app.routes
  (:require
    [reitit.ring :refer [ring-handler routes router create-default-handler create-resource-handler]]
    [angara.meteo.app.inbound :refer [inbound-handler]]
    [angara.meteo.app.api :as api]
    [angara.meteo.app.throttle :refer [wrap-throttle]]
    [angara.meteo.app.auth :refer [wrap-auth]]
  ,))


(defn make-routes []
  [
    ["/meteo/_in" {:get inbound-handler :post inbound-handler}] ;; local rs.angara.net handler
   ;
    ["/meteo/api" {:middleware [[wrap-auth] [wrap-throttle]]}
      ["/active-stations"  {:get api/active-stations}]    ;; lat?/lon?
      ["/station-info"     {:get api/station-info}]       ;; st
      ["/station-hourly"   {:get api/station-hourly}]]    ;; st, ts_beg, ts_end
  ,])


(defn make-handler []
  (ring-handler
    (router (make-routes))
    (routes
     (create-resource-handler {:path "/meteo" :root "public"})
     (create-default-handler)
     )))
