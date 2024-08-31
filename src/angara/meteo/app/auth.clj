(ns angara.meteo.app.auth
  (:require 
     [clojure.string :as str]
     [taoensso.encore :refer [defn-cached]]
     [mlib.base64 :refer [safe-decode64]]
     [angara.meteo.db.meteo :refer [check-auth]]
   ,))


(defn split-auth-header [headers]
  (if-let [a (get headers "authorization")]
    (when (str/starts-with? a "Basic ")
      (when-let [pair (safe-decode64 (subs a (.length "Basic ")))]
        (str/split pair #":" 2)))
    ;; default auth id
    ["_" "_"]))


(comment

  (split-auth-header {"authorization" "Basic cXdlOjEyMw=="})
  ;; => ["qwe" "123"]

  (split-auth-header {"authorization" "Basic ???"})
  ;; => nil

  (split-auth-header {"authorization" "apikey 123123"})
  ;; => nil 

  (split-auth-header {})
  ;; => ["_" "_"]
  
  ,)


(defn-cached check-auth-cached {:ttl-ms 100000 :size 1000000}
  [auth-id secret]
  (check-auth auth-id secret)
  )


(defn header-auth [{headers :headers}]
  (let [[auth-id secret] (split-auth-header headers)
        {auth :auth params :params} (check-auth-cached auth-id secret)]
    [auth (dissoc params :secret) auth-id]
    ,))


(defn wrap-auth [handler]
  (fn [req]
    (let [[auth auth-params _auth-id] (header-auth req)]
      (if auth
        (-> req
            (assoc :auth-id auth :auth-params auth-params)
            (handler))
        {:status 401 :body "Unauthorized"})
      )))
