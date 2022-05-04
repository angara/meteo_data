(ns angara.meteo.db.postgis
  (:import 
    [net.postgis.jdbc.geometry Point
      ;; Geometry PGgeometryLW PGgeometry LineString LinearRing MultiLineString MultiPoint MultiPolygon Point Polygon
     ]
  ))


;; https://github.com/remodoy/clj-postgresql/blob/master/src/clj_postgresql/spatial.clj


(defn point [x y] ;; lon, lat
  (Point. x y))
