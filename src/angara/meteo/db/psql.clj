(ns angara.meteo.db.psql
  (:require
    [clojure.string       :refer  [starts-with?]]
    [taoensso.timbre      :refer  [debug warn]]
    ;
    [next.jdbc            :refer  [with-transaction execute!]]
    [next.jdbc.result-set :refer  [as-unqualified-maps]]  ;; as-unqualified-arrays
    [next.jdbc.date-time  :refer  [read-as-local]]
    ;
    [mlib.psql.conn       :refer  [pooled-datasource]]
    [mlib.psql.adapters]
  ))
;= 

; - - - - - - - - - - - - - - - - - - -

;; https://www.postgresql.org/docs/current/errcodes-appendix.html
;;
(def ^:const SQL_STATE_UNIQUE_VIOLATION 23505)

; (def ^:const LIMIT_DEFAULT 10000)

;; ;; ;; ;; ;; ;; ;; ;; ;; ;;

(def ^:dynamic *tx*)
(def ^:dynamic *ds*)

(defn- prepend-jdbc [s]
  (if-not (starts-with? s "jdbc:")
    (str "jdbc:" s)
    s))
;-

(defn start-ds [url]
  (let [ds (pooled-datasource 
              { :jdbcUrl      (prepend-jdbc url)
                :auto-commit  false})]        ;; for :fetch-size statement option)]))
    (read-as-local)
    (alter-var-root #'*ds* (constantly ds))
    (alter-var-root #'*tx* (constantly ds))
    ds))
;;

(defn stop-ds [ds]
  (alter-var-root #'*tx* (constantly nil))
  (alter-var-root #'*ds* (constantly nil))
  (.close ds))
;;

(defmacro transact [& body]
  `(with-transaction [tx# *ds*]
     (binding [*tx* tx#]
       ~@body)))
;-

(def ^:const EXEC_OPTS
  {:builder-fn as-unqualified-maps :return-keys true})
;;

(defn exec! [stmt]
  (execute! *tx* stmt EXEC_OPTS))
;;

(defn exec-one! [stmt]
  (first
    (execute! *tx* stmt EXEC_OPTS)))
;;

;; ;; ;; ;; ;; ;; ;; ;; ;; ;; ;;
