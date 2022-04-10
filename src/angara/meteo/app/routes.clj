(ns angara.meteo.app.routes
  (:require
    [reitit.ring :refer [ring-handler routes router create-default-handler create-resource-handler]]
  ))


(def ANGARA_METEO_ROUTES
  [
    ["/meteo/in" {:get nil}]  ;; post
    ["/meteo/data" {:get nil}]
  ])


(defn make-handler []
  (ring-handler
    (router ANGARA_METEO_ROUTES)
    (routes
     (create-resource-handler {:path "/meteo" :root "public"})
     (create-default-handler)
     )))
