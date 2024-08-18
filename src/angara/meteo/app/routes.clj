(ns angara.meteo.app.routes
  (:require
    [reitit.ring :refer [ring-handler routes router create-default-handler create-resource-handler]]
    [angara.meteo.app.inbound :as in]
    [angara.meteo.app.data-out :as out]
  ))


(defn make-routes []
  [
    ["/dat"           {:get in/data-in :post in/data-in}] ;; old route
    ["/meteo_data/in" {:get in/data-in :post in/data-in}]
    ["/meteo_data/active"  {:get out/data}] ;; lat/lon
    ["/meteo_data/station" {:get out/data}] ;; st
    ["/meteo_data/last"    {:get out/data}] ;; st
    ["/meteo_data/series"  {:get out/data}] ;; st, vt
  ])


(defn make-handler []
  (ring-handler
    (router (make-routes))
    (routes
     (create-resource-handler {:path "/meteo" :root "public"})
     (create-default-handler)
     )))
