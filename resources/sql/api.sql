
-- :name select-active-stations :? :*
with lts as (
  select distinct on(st_id) st_id, ts as last_ts from meteo_last 
  where ts > :after-ts order by st_id, ts desc
)
select st.*, lts.last_ts from meteo_stations st 
inner join lts on (st.st_id = lts.st_id)
where st.publ
order by last_ts desc 
limit :limit offset :offset 
;


-- :name select-last :? :*
select ml.*, st.st from meteo_last ml
join meteo_stations st on (ml.st_id = st.st_id)
where st.st in (:v*:st-list) and ml.ts > :after-ts
;

