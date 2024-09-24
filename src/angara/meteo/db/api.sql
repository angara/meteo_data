
-- :name select-active-stations :? :*
with lts as (
  select distinct on(st_id) st_id, ts as last_ts from meteo_last 
  where ts > :after-ts order by st_id, ts desc
)
select st.*, lts.last_ts from meteo_stations st 
inner join lts on (st.st_id = lts.st_id)
where st.publ and 
    ((st ilike :search) or (title ilike :search) or (descr ilike :search))
order by last_ts desc 
limit :limit offset :offset 
;


-- :name select-last :? :*
select ml.*, st.st from meteo_last ml
join meteo_stations st on (ml.st_id = st.st_id)
where st.st in (:v*:st-list) and ml.ts > :after-ts
;

-- :name station-hourly-avg :? :*
select date_trunc('hour', ts) as ts, vt, avg(fval) as avg 
from meteo_data 
where	st_id = :st-id and ts >= :ts-beg and ts < :ts-end and vt != 'b'
group by date_trunc('hour', ts), vt order by ts, vt
;
