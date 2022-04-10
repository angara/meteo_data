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
  station     varchar(80)   not null,
  correct     boolean       not null,   -- record is correct
  mtype       varchar(20)   not null,   -- [temp,press,humid,wind,gust,wdir, txt]
  num         decimal(12,5),
  txt         varchar(80)
);

create index meteo_data_ts_idx on meteo_data (ts, station);

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
  mtype      varchar(20) not null,
  num        decimal(12,5),
  txt        varchar(80)
);

create unique index meteo_last_idx on meteo_last(stid, ts, mtype);

--
--

create table meteo_sensors (
  hwid       varchar(80) not null primary key,


)