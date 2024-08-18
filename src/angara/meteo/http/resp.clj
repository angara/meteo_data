(ns angara.meteo.http.resp
  (:require 
    [jsonista.core :refer [write-value-as-string keyword-keys-object-mapper]]
  ))


(defn ok [body]
  {:status 200
   :body body})
;;

(defn bad [body]
  {:status 400
   :body body})
;;

(defn err [body]
  {:status 500
   :body body})
;;

(defn json [status data]  
  { :status   status
    :headers  {"Content-Type" "application/json;charset=utf-8"}
    :body     (write-value-as-string data keyword-keys-object-mapper)})
;;

(defn jsok [data]
  (json 200 data))
;;

(defn jserr [error-data]
  (json 400 error-data))
;;


(defn throw-resp! [response]
  (throw
    (ex-info "http/response" {:http/response response})))


