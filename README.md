# Angara.Net: Meteorological Database Service

## API

Parameter suffix `?` means "optional parameter".

- **/meteo/api/active-stations**
  - query params:
    - `lat?` float - both lan and lon required to sort stations by location
    - `lon?` float
    - `last-vals?` "1|yes|true" - include last vals, default `false`
  
- **/meteo/api/last-vals**
  - query params: one or more `st=<station_name>`
  - response:  
    `{"last-vals": [{st:"..", t:<val>, t_ts:<timestamp>, t_delta:<val>, p:<val>, p_ts:<timestamp>, ...}]}`

- **/meteo/api/station-hourly**
  - query params:
    - `st` - station_name
    - `ts-beg?` - begin timestamp, ISO string (default 24 hours before now)
    - `ts-end?` - end timestamp, ISO string (defautl now)
  - response:  
    `{st, ts_beg, ts_end, series: [{t:[99.9, null, null, -99], p:[800, 900, null, ...], ... }]}`
