(ns angara.meteo.app.throttle
  (:require
    [taoensso.encore :refer [defn-cached]]
   ,))


(def ^:const THROTTLE_INTERVAL_MS 1000)
(def ^:const THROTTLE_COUNT 10)


(defn- throttle-interval []
  (+ THROTTLE_INTERVAL_MS (System/currentTimeMillis)))


(defn-cached get-pair {:ttl-ms 100000 :size 10000}
  [_auth-id]
  ;; request-count time-latch
  (atom [0 (throttle-interval)])
  ,)


(defn- update-cnt [[cnt time] now]
  (if (< time now)
    [0 (throttle-interval)]
    [(inc cnt) time]
    ))


(defn wrap-throttle [handler]
  (fn [req]
    (let [now (System/currentTimeMillis)
          auth-id (get req :auth-id "_")
          latch_ (get-pair auth-id)
          [cnt _time] (swap! latch_ update-cnt now)
          ]
      (if (> cnt THROTTLE_COUNT)
        {:status 429 :body "Too Many Requests"}
        (handler req)
        ,))))
