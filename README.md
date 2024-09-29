# Angara.Net: Meteorological Database Service

## API

Parameter suffix `?` means "optional parameter".

- **/meteo/api/active-stations**
  - query params:
    - `search?` - substring to search in station name or description
    - `lat?` float - both lan and lon required to sort stations by location
    - `lon?` float
    - `last-hours?` - last hours to include in last-vals (1..50)
  - response:
      `{stations:[{...station data...}, ]}`

- **/meteo/api/last-vals**
  - query params:  
    - `st=<station_name>` - one or more
    - `last-hours?` - (1..50) default 1
  - response:  
    `{"last-vals": [{st:"..", t:<val>, t_ts:<timestamp>, t_delta:<val>, p:<val>, p_ts:<timestamp>, ...}]}`

- **/meteo/api/station-hourly**
  - query params:
    - `st` - station_name
    - `ts-beg?` - begin timestamp, ISO string (default 24 hours before now)
    - `ts-end?` - end timestamp, ISO string (defautl now)
  - response:  
    `{st, ts_beg, ts_end, series: [{t:[99.9, null, null, -99], p:[800, 900, null, ...], ... }]}`

## Inbound

Username from basic authorization header used in `hwid` to station name mapping.

- **/meteo/_in**
  - query params:
    - `hwid` - sensor id (string)
    - `ts?` - sample timestamp (int, milliseconds)
    - `t?, d?, p?, h?, w?, g?, b?, r?` - values (float), at least one value required
