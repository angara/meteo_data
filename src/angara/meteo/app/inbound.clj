(ns angara.meteo.app.inbound
  (:require 
   [clojure.string :as str]
   [tick.core :as tick]
   [taoensso.telemere :refer [log!]]
   [angara.meteo.db.meteo :refer [check-auth get-station submit-fval]]
   [angara.meteo.http.resp :refer [throw-resp! jserr jsok]]
   [mlib.base64 :refer [safe-decode64]]
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

  (split-auth-header {}))
  ;; => ["_" "_"]


(def TS_BEFORE_NOW (* 1000 3600))  ;; aged ts
(def TS_AFTER_NOW  (* 1000 100))   ;; in the future


(defn validate-ts [ts]
  (let [now (System/currentTimeMillis)]
    (if-not ts
      (tick/now)
      (when-let [t (parse-long ts)]
        (when (and (> t (- now TS_BEFORE_NOW))
                   (< t (+ now TS_AFTER_NOW)))
          (tick/instant t)))
      ,)))


(def RANGES {
             :t [-70 70]   ;; C degrees
             :d [-70 70]   ;; C degrees
             :p [500 1500] ;; hPa
             :h [0 100]    ;; %%
             :w [0 50]     ;; m/s
             :g [0 100]    ;; m/s
             :b [0 360]    ;; wind direction
             :r [0  50]    ;; mm/hour
             ;
             :mmhg [380 1100] ;; mmhg->p
             ,})


(defn validate-params [errors_ params ranges]
  (reduce
   (fn [acc [k [v0 v1]]]
     (if-let [v (get params k)]
       (if-let [fval (parse-double v)]
         (if (and (<= v0 fval) (<= fval v1))
           (assoc acc k fval)
           (do
             (vswap! errors_ conj (str "out of range: " (name k) "=" v))
             acc))
         (do
           (vswap! errors_ conj (str "invalid value: " (name k) "=" v))
           acc))
       acc
       ,))
   {}
   ranges
   ,))


(defn submit-vals [errors_ st-id ts params]
  (reduce
   (fn [acc [k v]]
     (let [k (name k)
           rc (submit-fval st-id ts k v)]
       (if (= :ok rc)
         (conj acc (str k ": " v))
         (let [msg (str (if (= :too-frequent rc) "too_frequent" "db_err") " - " k)]
           (vswap! errors_ conj msg)
           acc)
         ,)))
   []
   params
   ,))


(defn inbound-handler [{:keys [headers params]}]  ;; req
  (let [[auth-id secret] (split-auth-header headers)
        {auth :auth} (check-auth auth-id secret)
        _ (when-not auth
            (throw-resp! (jserr {:msg "invalid auth" :auth auth-id})))
        hwid (:hwid params)
        ;
        {st-id :st_id st :st sn-params :params} (get-station auth hwid)
        _ (when-not st-id
            (throw-resp! (jserr {:msg "hwid not found" :auth auth-id :hwid hwid})))
        _ (when-let [psw (:psw sn-params)]
            (when (not= psw (:psw params))
              (throw-resp! (jserr {:msg "wrong psw" :auth auth-id :hwid hwid :st st}))))
        ;
        ts (validate-ts (:ts params))
        _ (when-not ts
            (throw-resp! (jserr {:msg "invalid timestamp" :ts (:ts params)})))
        ;
        errors_ (volatile! [])
        ;
        vp (validate-params errors_ params RANGES)
        vp (if (:p vp)
             vp
             (if-let [mm (:mmhg vp)]
               (-> vp (dissoc :mmhg) (assoc :p (* mm 1.3332239)))
               vp))
        ;
        ok  (submit-vals errors_ st-id ts vp)
        err  @errors_
        ;
        resp (cond-> {:st st :ts ts}
               (seq ok)  (assoc :ok ok)
               (seq err) (assoc :err err))]
    ;
    (log! :info ["response" resp])
    ;
    (jsok resp)
    ,))


(comment

  ;; Xzpf
  
  (let [errors_ (volatile! [])
        rc (validate-params errors_ {:p "1.2" :t "-100" :d "45" :w "10" :g "101" :h "unknuwn"} RANGES)]
    [rc @errors_])
  ;; => [{:d 45.0, :w 10.0}
  ;;     ["out of range: g=101" "invalid value: h=unknuwn" "out of range: t=-100" "out of range: p=1.2"]]


  (try
    (inbound-handler {:headers {"authorization" "Basic Xzpf"} :params {:hwid "test" :t "11"  :p "996"}})
    (catch Exception ex
      (ex-data ex)))
  ;; => {:body
  ;;       "{\"st\":\"test\",\"ts\":\"2024-08-21T12:57:01.904156Z\",\"err\":[\"too_frequent - t\",\"too_frequent - p\"]}",
  ;;     :headers {"Content-Type" "application/json;charset=utf-8"},
  ;;     :status 200}

  ;; => {:body "{\"st\":\"test\",\"ts\":1724243916209,\"ok\":[\"t: 11.0\",\"p: 996.0\"]}",
  ;;     :headers {"Content-Type" "application/json;charset=utf-8"},
  ;;     :status 200}


  (try
    (inbound-handler {:headers {"authorization" "Basic Xzp4eHg="} :params {:t "11" :hwid "xxx" :p "996" :b "-111"}})
    (catch Exception ex
      (ex-data ex)))
      
    
  ;; => #:http{:response {:body "{\"msg\":\"invalid auth\",\"auth\":\"_\"}",
  ;;                      :headers {"Content-Type" "application/json;charset=utf-8"},
  ;;                      :status 400}}

  ;; => #:http{:response {:body "{\"msg\":\"hwid not found\",\"auth\":\"_\",\"hwid\":\"xxx\"}",
  ;;                      :headers {"Content-Type" "application/json;charset=utf-8"},
  ;;                      :status 400}}

  ;; => {:body
  ;;       "{\"st\":\"olha2\",\"ts\":1724046713151,\"ok\":[\"t: 11.0\",\"p: 996.0\"],\"err\":[\"out of range: b=-111\"]}",
  ;;     :headers {"Content-Type" "application/json;charset=utf-8"},
  ;;     :status 200}

  ,)
