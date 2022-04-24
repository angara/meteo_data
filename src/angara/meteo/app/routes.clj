(ns angara.meteo.app.routes
  (:require
    [reitit.ring :refer [ring-handler routes router create-default-handler create-resource-handler]]
    [angara.meteo.app.inbound :as in]
    [angara.meteo.app.data-out :as out]
  ))


(defn make-routes [system]
  (prn "make-routes:" system)
  [
    ["/meteo/in" {:get in/data-in
                  :post in/data-in}]
    ["/meteo/data" {:get out/data}]
  ])


(defn make-handler [system]
  (ring-handler
    (router (make-routes system))
    (routes
     (create-resource-handler {:path "/meteo" :root "public"})
     (create-default-handler)
     )))
