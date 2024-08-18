(ns mlib.base64
  (:import [java.util Base64])
  ,)


;; NOTE: Base base64 version, use getUrlEncoder for url-safe https://datatracker.ietf.org/doc/html/rfc4648

(defn encode64 [^String data-str]
  (-> (Base64/getEncoder)
      (.encode (.getBytes data-str "UTF-8"))
      (String.)))


(defn decode64 [^String base64str]
  (-> (Base64/getDecoder)
      (.decode base64str)
      (String. "UTF-8")))


(defn safe-decode64 [s]
  (try
    (decode64 s)
    (catch Exception _)
    ,))


(comment
   
  (encode64 "qwe:123")
  ;; => "cXdlOjEyMw=="

  (decode64 "cXdlOjEyMw==")
  ;; => "qwe:123" 


  (safe-decode64 (encode64 "some long string example: qwe 123"))
  ;; => "some long string example: qwe 123"

  (safe-decode64 nil)
  ;; => nil

  (safe-decode64 " ??? ")
  ;; => nil

  ,)
