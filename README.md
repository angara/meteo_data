# Angara.Net: Meteorological Database Service

## TODO

- <https://github.com/fmnoise/flow>

add station 
uiun Нижнеангарск, Аэроопрт Нижнеангарск, 

## Snippets

(:import [org.postgis LinearRing LineString MultiLineString MultiPoint MultiPolygon Point Polygon]))

https://github.com/bugramovic/korma.postgis/blob/master/src/korma/postgis.clj


((org.postgresql.PGConnection)conn).addDataType("geometry",Class.forName("org.postgis.PGgeometry"));
((org.postgresql.PGConnection)conn).addDataType("box3d",Class.forName("org.postgis.PGbox3d"));

PGgeometry geom = (PGgeometry)r.getObject(1);


update the_table
  set attr = jsonb_set(attr, array['is_default'], to_jsonb(false));
  
If you're on version 14 (released September 2021) or greater, you can simplify this to:

update the_table
   set attr['is_default'] = to_jsonb(false);