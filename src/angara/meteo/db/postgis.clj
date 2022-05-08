(ns angara.meteo.db.postgis
  (:import 
    [net.postgis.jdbc PGgeography PGgeometry]
    [net.postgis.jdbc.geometry Point
      ;; Geometry PGgeometryLW PGgeometry LineString LinearRing MultiLineString MultiPoint MultiPolygon Point Polygon
     ]
  )
  (:require
    [next.jdbc.result-set :as rs]
  ))


;; https://github.com/remodoy/clj-postgresql/blob/master/src/clj_postgresql/spatial.clj


(defn point [x y] ;; lon, lat
  (Point. x y))


(extend-protocol rs/ReadableColumn
  PGgeography
  (read-column-by-label [^PGgeography v _]
    (prn "geog:" v (type v) (.getGeometry v))
    (.getGeometry v))
  (read-column-by-index [^PGgeography v _ _]
    (prn "geog:" v (type v) (.getGeometry v))
    (.getGeometry v))
  PGgeometry
  (read-column-by-label [v _]
                        (prn "geom:" v)
                        v)
  (read-column-by-index [v _ _]
                        (prn "geom:" v)
                        v)
  )
