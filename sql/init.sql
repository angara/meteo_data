--
--
create database meteo;
create user meteo with password 'qwe123';
\c meteo
create extension postgis;


--
--
create table meteo_data (
  ts          timestamptz   not null,
  stid        varchar(80)   not null,
  correct     boolean       not null,   -- record is correct
  vtype       varchar(20)   not null,   -- [temp,press,humid,wind,gust,wdir, extra]
  value       decimal(12,5),
  extra       jsonb
);

create index meteo_data_ts_idx on meteo_data (ts, stid);


--
--
create table meteo_stations (
  stid        varchar(80) not null primary key,
  title       varchar(200),
  descr       varchar(2000),
  active      boolean not null default 'f',
  created_at  timestamptz not null default CURRENT_TIMESTAMP,
  location    geography(point),
  elevation   decimal(8,2),
  note        jsonb
);

create index meteo_stations_location_ids on meteo_stations using gist(location);


-- last measurements for each station
--
create table meteo_last (
  stid       varchar(80) not null,
  ts         timestamptz not null,
  vtype      varchar(20) not null,
  value      decimal(12,5),
  extra      jsonb
);

create unique index meteo_last_idx on meteo_last(stid, ts, vtype);


-- sensor to station mapping, authentcation, handler type, extra params
--
create table meteo_sensors (
  hwid       varchar(80) not null primary key,
  stid       varchar(80),
  auth       varchar(200),  -- Authorizaion header value
  handler    varchar(20),   -- "default|pdm|metar|psw|no_pass"
  params     jsonb          -- {vtypes:[temp,press,humid,wind,gust,wdir]} -- allowed value types
);

