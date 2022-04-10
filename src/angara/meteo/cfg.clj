(ns angara.meteo.cfg
  (:require
   [clojure.string         :refer [trim]]
   [clojure.java.io        :as    io]
   [clojure.spec.alpha     :as    s]
   [aero.core              :refer [read-config]]
   ;[mount.core             :refer [defstate args]]
   ))


(def build-info
  (delay (-> "build-info" (io/resource) (slurp) (trim))))

; - - - - - - - - - - - - - - - - - - - 

(s/def ::database-url       ::b/url)

(s/def ::http-host        ::b/not-blank)

(s/def ::http-port        pos-int?)


(s/def ::config
  (s/keys 
    :req-un [::database-url 
             ::http-host 
             ::http-port
             ]))

; - - - - - - - - - - - - - - - - - - - 

(defstate config
  :start
  (->> 
    (read-config (io/resource "config.edn"))
    (#(merge % (args)))
    (b/conform! ::config))
  )
;=

