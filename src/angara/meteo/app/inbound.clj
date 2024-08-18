(ns angara.meteo.app.inbound
  (:require 
   [clojure.string :as str]
   [taoensso.telemere :refer [log!]]
   [angara.meteo.app.repo :refer [check-auth get-station]]
   [angara.meteo.http.resp :refer [throw-resp! jserr jsok]]
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


(def RANGES {
             :t [-70 70]   ;; C degrees
             :d [-70 70]   ;; C degrees
             :p [500 1500] ;; hPa
             :h [0 100]    ;; %%
             :w [0 50]     ;; m/s
             :g [0 100]    ;; m/s
             :b [0 360]    ;; wind direction
             :r [0  50]})     ;; mm/hour
             


(defn val-in-range [vt fval]
  (when-let [[v0 v1] (get RANGES vt)] 
    (and (<= v0 fval) (<= fval v1))
    ,))


(defn p-or-mmhg [data]
  (try
    (if-let [p (:p data)]
      (parse-double p)
      (* 1.3332239 (parse-double (:mmhg data))))
    (catch Exception _)
    ,))


(def TS_BEFORE_NOW (* 1000 3600))  ;; aged ts
(def TS_AFTER_NOW  (* 1000 100))   ;; in the future


(defn validate-ts [ts]
  (let [now (System/currentTimeMillis)]
    (if-not ts
      now
      (when-let [t (parse-long ts)]
        (when (and (> t (- now TS_BEFORE_NOW))
                   (< t (+ now TS_AFTER_NOW)))
          t))
      ,)))


(defn parse-val [s]
  (if (string? s)
    (parse-double s)
    s))


(defn submit-val [st-id ts params vt]
  (when-let [v-str (get params vt)]
    (if-let [fv (parse-val v-str)]
      (if (val-in-range vt fv)
        (do) ;;;submit
            
        {:out-of-range (name vt)})
        
      {:invalid (name vt)})
      
    ,))


(defn data-in [{:keys [params headers]}]  ;; req
  (let [[auth-id secret] (split-auth-header headers)
        {auth :auth} (check-auth auth-id secret)
        _ (when-not auth
            (throw-resp! (jserr {:msg "invalid auth" :auth auth-id})))
        hwid (:hwid params)
        {st-id :st_id st :st sn-params :params} (get-station auth hwid)
        _ (when-not st-id
            (throw-resp! (jserr {:msg "hwid not found" :auth auth-id :hwid hwid})))
        ts (validate-ts (:ts params))
        _ (when-not ts
            (throw-resp! (jserr {:msg "invalid timestamp" :ts (:ts params)})))
        ;
        ;; fix mmhg
        ;
        rc (mapv (partial submit-val st-id ts params) [:t :p :d :h :b :w :g :r])
        ]            
        
    
    (log! [ params auth st sn-params st-id ts])
    ;; when-not
    ;;   report
    
    ;; precondition 
    ;; 
    {:status 500
     :body "not implemented"}
    ,))
  

