(ns angara.meteo.lib.env)


(defn to-int [s] 
  (Long/parseLong s 10))


(defn env->key [key env-name & [default type-cast]]
  (if-let [value (System/getenv env-name)]
    (if type-cast
      (try 
        (type-cast value)
        (catch Exception ex
          (throw (ex-info (str "env->key type cast error: " env-name) 
                          {:key key :env-name env-name :value value :message (ex-message ex)} 
                          ex))
        ))
      [key value])
    [key default]))


(defn load-env-vars [vars-list]
  (->> vars-list
       (mapv #(apply env->key %))
       (into {})
  ))


(comment
  
  (def env-vars
    [
      [:meteo-database-url "METEO_DATABASE_URL"]              ;; postgres://pg-host:5432/dbname?user=...&password=...
      [:meteo-http-host    "METEO_HTTP_HOST" "localhost"]
      [:meteo-http-port    "METEO_HTTP_PORT" 8002 to-int]
    ])

  (load-env-vars env-vars)

  ,)
