(ns angara.meteo.app.routes
  (:require
    [reitit.ring :refer [ring-handler routes router create-default-handler create-resource-handler]]
  ))


(defn make-routes [system]
  (prn "make-routes:" system)
  [
    ["/meteo/in" {:get nil}]  ;; post
    ["/meteo/data" {:get nil}]
  ])


(defn make-handler [system]
  (ring-handler
    (router (make-routes system))
    (routes
     (create-resource-handler {:path "/meteo" :root "public"})
     (create-default-handler)
     )))
