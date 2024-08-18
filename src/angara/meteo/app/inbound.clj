(ns angara.meteo.app.inbound
  (:require 
   [clojure.string :as str]
   [angara.meteo.app.auth :refer [load-auth]]
   [mlib.base64 :refer [safe-decode64]]
   ,))


;; (def inbound-schema
;;   (m/schema
;;    [:map
;;     [:t [:and number? [:>= -70] [:<= 70]]]   ;; C degrees
;;     [:d [:and number? [:>= -70] [:<= 70]]]   ;; C degrees
;;     [:p [:and number? [:>= 500] [:<= 1500]]] ;; hPa
;;     [:h [:and number? [:>= 0] [:<= 100]]]    ;; %%
;;     [:w [:and number? [:>= 0] [:<= 50]]]     ;; m/s
;;     [:g [:and number? [:>= 0] [:<= 100]]]    ;; m/s
;;     [:b [:and number? [:>= 0] [:<= 360]]]    ;; wind direction
;;     [:r [:and number? [:>= 0] [:<= 50]]]     ;; mm/hour
;;     ,]))


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

  (split-auth-header {}))
  ;; => ["_" "_"]


(def parsers 
  {:t nil
   :p nil
   :h nil
   :d nil
   :b nil
   :w nil
   :g nil
   :v nil
   :r nil
   })
  
(defn parse-t [data]
  
  )

(defn parse-p [data]
  (try
    (let [p (if-let [p (:p data)]
             (parse-double p)
             (* 1.3332239 (parse-double (:mmhg data))))]
      (when (and (>= p 500) (<= p 1500))
        p))
    (catch Exception _)
    ,))


(defn data-in [{:keys [params headers]}]  ;; req
  (let [[auth-id secret] (split-auth-header headers)
        auth (load-auth auth-id secret)]
    
    (prn params auth)
    ;; when-not
    ;;   report
    
    ;; precondition 
    ;; 
    {:status 500
     :body "not implemented"}))
  

