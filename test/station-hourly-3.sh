#!/bin/bash

ENDPOINT=http://127.0.0.1:8004/meteo/api/station-hourly
ENDPOINT=http://rs.angara.net/meteo/api/station-hourly

AUTHORIZATION="Authorization: Basic Xzpf"
QS="st=apik_tunka&ts-end=2024-09-23T22:00:00Z"

echo "${AUTHORIZATION}" "${ENDPOINT}?${QS}"

curl -H "${AUTHORIZATION}" "${ENDPOINT}?${QS}" | jq .

#.
