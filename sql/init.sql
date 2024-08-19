--
--
-- create database meteo;
-- create user meteo with password 'XXXXXX';
-- \c meteo


-- measurements time series
--
create table meteo_data (
  ts          timestamptz   not null,
  st_id       int           not null,  -- references meteo_stations
  vt          char(1)       not null,
  fval        float         not null
);

comment on column meteo_data.vt is
  'first letter of [Temperature, Pessure, Humidity, Dewpoint, Bearing, Wind, Gust, Rainfall] in lower case'
  ;

create unique index meteo_data_uniq on meteo_data (ts, st_id, vt) include (fval);


-- station information
--
create table meteo_stations (
  st_id       int not null primary key,
  st          varchar(40) not null,
  title       varchar(200),
  descr       varchar(2000),
  publ        boolean not null default 'f',
  created_at  timestamptz not null default CURRENT_TIMESTAMP,
  closed_at   timestamptz,
  lat         float,
  lon         float,
  elev        float,
  note        jsonb
);

create unique index mete_stations_st_idx on meteo_stations(st);

-- update meteo_stations set publ='f' where not publ and closed_at is not null;


-- last measurements for each station
--
create table meteo_last (
  st_id      int         not null,
  vt         char(1)     not null,
  ts         timestamptz not null,
  fval       float       not null,
  delta      float       not null
  -- select coalesce(avg(fval), 0) from meteo_data 
  -- where st_id= and vt= and ts < now and ts > now - 'interval 1 hour'
);

create unique index meteo_last_idx on meteo_last (st_id, vt);


-- hardware sensor station identification
--
create table meteo_sensors (
  auth         varchar(80) not null,
  hwid         varchar(80) not null,
  st           varchar(40) not null,
  params       jsonb  -- {vts?=[t,p,h,d,b,w,g,r], psw?="...", note?:"..."}
);

create unique index meteo_sensors_idx on meteo_sensors (auth, hwid);


-- client identification
--
create table meteo_auth (
  auth    varchar(80) not null primary key,
  params  jsonb    -- {note?:"...", secret="..."}
);

