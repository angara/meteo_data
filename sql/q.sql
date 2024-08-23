
@set start = '2014-01-01'
@set end   = '2014-02-01'
@set vt = 't'


select date_trunc('hour', ts) as ts, 
       min(fval) as min, 
       avg(fval) as avg, 
       max(fval) as max 
from meteo_data 
where
	st_id = 34 and
	vt = 't' and
	ts >= ${start}::timestamptz and 
	ts < ${end}::timestamptz
group by date_trunc('hour', ts)
;


-- -- -- --


with created as (
	select distinct on(st_id) st_id, ts from meteo_data order by st_id, ts
)
update meteo_stations m 
	set created_at = c.ts, publ = 't' 
	from (select * from created) as c
	where m.st_id = c.st_id
;


with finished as (
	select distinct on(st_id) st_id, ts from meteo_data order by st_id, ts desc
)
update meteo_stations m 
	set closed_at = c.ts, publ = 'f' 
	from (select * from finished where ts < '2024-01-01'::timestamptz) as c
	where m.st_id = c.st_id
;

-- -- -- --


alter table meteo_last add column delta float not null;


with d as (
  select coalesce(avg(fval), 0) as delta from meteo_data 
  where st_id= and vt= and ts < now and ts > now - 'interval 1 hour' -- and vt != 'b'
)
insert into meteo_last (st_id, vt, ts, fval, delta) values (?, ?, ?, ?, d.delta)
on conflict (st_id, vt) do update set ts=?, fval=?, dalta=d.delta;


-- -- -- --
