
create table meteo_data (
  timestamp   timestamptz   not null,
  station_id  varchar(80)   not null,
  valid       boolean       not null,
  val_type    varchar(20)   not null,   -- t,p,b,w,g,...
  num_val     decimal(12,5),
  str_val     varchar(80)
);

create index meteo_data_ts_idx 
  on meteo_data (timestamp, station_id);

