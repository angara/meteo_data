--
--
-- create database meteo;
-- create user meteo with password 'XXXXXX';
-- \c meteo


-- measurements time series
--
create table meteo_data (
  ts          timestamptz   not null,
  stid        varchar(40)   not null,
  vt          char(1)       not null,
  fval        float         not null
);

comment on column meteo_data.vt is
  'first letter of [Temperature,Pessure,Q-absolute-pressure,Humidity,Bearing,Wind,Gust,Rainfall,Visibility,Dewpoint] in lower case'
  ;

create unique index meteo_data_uniq on meteo_data (ts, stid, vt) include (fval);


-- station information
--
create table meteo_stations (
  stid        varchar(40) not null primary key,
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

-- update meteo_stations set publ='f' where not publ and closed_at is not null;


-- last measurements for each station
--
create table meteo_last (
  stid       varchar(40) not null primary key,
  vt         char(1)     not null,
  ts         timestamptz not null,
  fval       float       not null
);

create unique index meteo_last_idx on meteo_last (stid, vt);


-- hardware sensor station identification
--
create table meteo_clients (
  client_id     varchar(80) not null primary key,
  client_secret varchar(400),
  st_params     jsonb
);

comment on column meteo_clients.st_params is 
  'mapping {hwid:{stid,vts=[t,p,q,h,d,w,g,b,v,r],...}}'
  ;

