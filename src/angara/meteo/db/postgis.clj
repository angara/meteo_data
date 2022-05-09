(ns angara.meteo.db.postgis
  (:import 
    [net.postgis.jdbc PGgeography PGgeometry]
    ;; [net.postgis.jdbc.geometry Point
    ;;   ;; Geometry PGgeometryLW PGgeometry LineString LinearRing MultiLineString MultiPoint MultiPolygon Point Polygon
    ;;  ]
  )
  (:require
    [next.jdbc.result-set :as rs]
  ))


(set! *warn-on-reflection* true)

;; https://github.com/remodoy/clj-postgresql/blob/master/src/clj_postgresql/spatial.clj


;; (defn point [x y] ;; lon, lat
;;   (Point. x y))


(extend-protocol rs/ReadableColumn
  
  PGgeography
  (read-column-by-label [^PGgeography v _]
    (.getGeometry v))
  (read-column-by-index [^PGgeography v _ _]
    (.getGeometry v))
  
  PGgeometry
  (read-column-by-label [^PGgeometry v _]
    (.getGeometry v))
  (read-column-by-index [^PGgeometry v _ _]
    (.getGeometry v))
  )
