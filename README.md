# Angara.Net: Meteorological Database Service

## API

- **/meteo/api/active-statrions**
  - query params:
    - `lat` optional float
    - `lon` optional float
    - `last-vals` optional "1|yes|true" - include last vals
  
- **/meteo/api/last-vals**
  - query params: one or more `st=<station_id>`
  - response: `{vals:[{st:"..", t:<val>, t_ts:<timestamp>, t_delta:<val>, ...}]}`


## Snippets

- <https://github.com/fmnoise/flow>

```txt
(:import [org.postgis LinearRing LineString MultiLineString MultiPoint MultiPolygon Point Polygon]))

https://github.com/bugramovic/korma.postgis/blob/master/src/korma/postgis.clj


((org.postgresql.PGConnection)conn).addDataType("geometry",Class.forName("org.postgis.PGgeometry"));
((org.postgresql.PGConnection)conn).addDataType("box3d",Class.forName("org.postgis.PGbox3d"));

PGgeometry geom = (PGgeometry)r.getObject(1);
```

```sql
update the_table
  set attr = jsonb_set(attr, array['is_default'], to_jsonb(false));
  
-- If you're on version 14 (released September 2021) or greater, you can simplify this to:

update the_table
   set attr['is_default'] = to_jsonb(false);
```   