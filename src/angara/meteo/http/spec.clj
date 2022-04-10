(ns angara.meteo.http.spec
  (:require
    [clojure.string :refer [blank?]]
    [clojure.spec.alpha :as s]
  ))


(defn not-blank? [s]
  (and (string? s) (not (blank? s))))


(s/def ::host   not-blank?)
(s/def ::port   pos-int?)
(s/def ::prefix not-blank?)


(s/def ::config
  (s/keys
    :req-un [::host ::port ::prefix]
  ))
