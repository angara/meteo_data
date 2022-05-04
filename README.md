# Angara.Net: Meteorological Database Service

## TODO

- meteo_stations: set correct `created_at`



## Snippets

(:import [org.postgis LinearRing LineString MultiLineString MultiPoint MultiPolygon Point Polygon]))

https://github.com/bugramovic/korma.postgis/blob/master/src/korma/postgis.clj


((org.postgresql.PGConnection)conn).addDataType("geometry",Class.forName("org.postgis.PGgeometry"));
((org.postgresql.PGConnection)conn).addDataType("box3d",Class.forName("org.postgis.PGbox3d"));

PGgeometry geom = (PGgeometry)r.getObject(1);
