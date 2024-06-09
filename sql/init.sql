--
--
create database meteo;
create user meteo with password 'qwe123';
\c meteo


--
--
create table meteo_data (
  ts          timestamptz   not null,
  stid        varchar(40)   not null,
  vtype       varchar(20)   not null,   -- [temp,press,humid,wind,gust,wdir]
  fval        float         not null
);

create index meteo_data_ts_idx on meteo_data(ts);


--
--
create table meteo_stations (
  stid        varchar(40) not null primary key,
  title       varchar(200),
  descr       varchar(2000),
  public      boolean not null default 'f',
  created_at  timestamptz not null default CURRENT_TIMESTAMP,
  lat         float,
  lon         float,
  elevation   float,
  note        jsonb
);


-- last measurements for each station
--
create table meteo_last (
  stid       varchar(40) not null,
  vtype      varchar(20) not null,
  ts         timestamptz not null,
  fval       float       not null
);

create unique index meteo_last_idx on meteo_last(stid, vtype);


-- sensor to station mapping, authentcation, handler type, extra params
--
create table meteo_sensors (
  hwid       varchar(80) not null,
  params     jsonb          -- {
                            --   handler: "default|pdm|metar|psw|no_pass",
                            --   stid: "...",
                            --   authkey: "...",
                            --   vtypes:[temp,press,humid,wind,gust,wdir]  // allowed value types
                            -- }
);

create index meteo_sendors_idx on meteo_last(hwid);
