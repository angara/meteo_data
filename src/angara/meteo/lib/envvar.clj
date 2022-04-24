(ns angara.meteo.lib.envvar)


(defn env-value [env default type-cast-fn]
  (if-let [value (System/getenv env)]
    (try
      (type-cast-fn value)
      (catch Exception ex
        (throw 
          (ex-info 
            (str "env-value type cast error - " env ": " (ex-message ex)) 
            {:env env :value value} 
            ex))))
    default))


(defn env-str
  ([env] (env-str env nil))
  ([env default]
   (env-value env default identity)))


(defn env-int
  ([env] (env-int env nil))
  ([env default]
   (env-value env default #(Long/parseLong % 10))))

