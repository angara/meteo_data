(ns angara.meteo.http.middleware
  (:import  [com.fasterxml.jackson.core JsonParseException])
  (:require [jsonista.core :as json]))


(def CORS_HEADERS
  { "Access-Control-Allow-Origin"   "*"
    "Access-Control-Allow-Methods"  "GET, POST, OPTIONS"
    "Access-Control-Allow-Headers"  "Content-Type, Authorization"
    "Access-Control-Expose-Headers" "X-ServerTime, X-ServerName, *"})


(defn wrap-cors [handler]
  (fn [req]
    (if (= :options (:request-method req))
      {:status 200 :headers CORS_HEADERS :body ""}
      (->
        (handler req)
        (update :headers merge CORS_HEADERS)))))


(defn wrap-server-name [handler server-name]
  (fn [req]
    (-> 
      (handler req)
      (update :headers merge
        { "X-ServerName"  server-name
          "X-ServerTime"  (str (System/currentTimeMillis))}))))

;; ;; ;; ;; ;; ;; ;; ;; ;; ;;

(defn ^:dynamic *malformed-json-response-fn* [_req]
  {:status  400
   :headers {"Content-Type" "text/plain"}
   :body    "Malformed JSON request."})


(def RE_APPLICATION_JSON #"^application/(.+?\+)?json")

(defn- json-request? [request]
  (when-let [ctype (get-in request [:headers "content-type"])]
    (boolean (re-find RE_APPLICATION_JSON ctype))))


(defn- parse-json-data [body]
  (try
    [true (json/read-value body json/keyword-keys-object-mapper)]
    (catch JsonParseException _ex
      [false nil])))


(defn- merge-json-params [req data]
  (let [request (assoc req :json-params data)]
    (if (map? data)
      (update-in request [:params] merge data)
      request)))


(defn wrap-json-params [handler]
  (fn [req]
    (if (json-request? req)
      (let [[valid? data] (parse-json-data (:body req))]
        (if valid?
          (handler (merge-json-params req data))
          (*malformed-json-response-fn* req)))
      (handler req))))


(comment
  (json-request? {:headers {"content-type" "application/vnd+json"} :body "[true]"})

  ( 
    (wrap-json-params :params)
    { :headers {"content-type" "application/json; charset=utf-8"}
      :body "{\"a\":[true],\"b\":1}"})

  ,)
