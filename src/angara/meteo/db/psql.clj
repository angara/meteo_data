(ns angara.meteo.db.psql
  (:require
    [clojure.string       :refer  [starts-with?]]
    ;
    [next.jdbc            :refer  [execute!]]
    [next.jdbc.result-set :refer  [as-unqualified-maps]]  ;; as-unqualified-arrays
    [next.jdbc.date-time  :refer  [read-as-local]]
    ;
    [mlib.psql.conn       :refer  [pooled-datasource]]
    [mlib.psql.adapters]
  ))



;; https://www.postgresql.org/docs/current/errcodes-appendix.html
;;
(def ^:const SQL_STATE_UNIQUE_VIOLATION 23505)


(defn- prepend-jdbc [url]
  (if (starts-with? url "jdbc:")
    url
    (str "jdbc:" url)
  ))


(defn start-ds [url]
  (let [ds (pooled-datasource 
              { :jdbcUrl (prepend-jdbc url)
                ; :auto-commit  false ;; for :fetch-size statement option
               })]        
    (read-as-local)
    ds))


(defn stop-ds [ds]
  (when ds
    (.close ds)))


(def ^:const EXEC_OPTS
  {:builder-fn as-unqualified-maps :return-keys true})


(defn exec! [ds stmt]
  (execute! ds stmt EXEC_OPTS))


(defn exec-one! [ds stmt]
  (first
    (execute! ds stmt EXEC_OPTS)))
