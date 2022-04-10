(ns mlib.psql.adapters
  (:import
    [java.sql             Array PreparedStatement]
    [org.postgresql.util  PGobject])
  (:require 
    [jsonista.core        :as    json]
    ; [next.jdbc.date-time refer [read-as-instant read-as-local]]
    [next.jdbc.prepare    :refer [SettableParameter]]
    [next.jdbc.result-set :refer [ReadableColumn]]
  ))


(set! *warn-on-reflection* true)

;; https://cljdoc.org/d/seancorfield/next.jdbc/1.0.424/doc/getting-started/tips-tricks

(def to-json json/write-value-as-string)
(def from-json #(json/read-value % json/keyword-keys-object-mapper))


(defn to-pgobject
  "Transforms Clojure data to a PGobject that contains the data as JSON. 
  PGObject type defaults to `jsonb` but can be changed via metadata key `:pgtype`"
  [x]
  (let [pgtype (or (:pgtype (meta x)) "jsonb")]
    (doto (PGobject.)
      (.setType pgtype)
      (.setValue (to-json x)))))


(defn from-pgobject
  "Transform PGobject containing `json` or `jsonb` value to Clojure data."
  [^PGobject v]
  (let [type  (.getType v)
        value (.getValue v)]
    (if (#{"jsonb" "json"} type)
      #_(with-meta (from-json value) {:pgtype type})
      (from-json value)
      value)))
;;


;; if a SQL parameter is a Clojure hash map or vector, it'll be transformed
;; to a PGobject for JSON/JSONB:
(extend-protocol SettableParameter
  clojure.lang.IPersistentMap
  (set-parameter [m, ^PreparedStatement s, i]
    (.setObject s i (to-pgobject m)))

  clojure.lang.IPersistentVector
  (set-parameter [v, ^PreparedStatement s, i]
    (.setObject s i (to-pgobject v))))
;;

;; if a row contains a PGobject then we'll convert them to Clojure data
;; while reading (if column is either "json" or "jsonb" type):
(extend-protocol ReadableColumn
  PGobject
  (read-column-by-label [^PGobject v _]     (from-pgobject v))
  (read-column-by-index [^PGobject v _2 _3] (from-pgobject v)))
;;

;; sql Array -> Vector
;; 
(extend-protocol ReadableColumn
  Array
  (read-column-by-label [^Array v _]    (vec (.getArray v)))
  (read-column-by-index [^Array v _ _]  (vec (.getArray v))))
;;
