#!/bin/bash

ENDPOINT=http://127.0.0.1:8004/meteo/api/station-hourly
AUTHORIZATION="Authorization: Basic Xzpf"
QS="st=uiii"

echo "${AUTHORIZATION}" "${ENDPOINT}?${QS}"

curl -H "${AUTHORIZATION}" "${ENDPOINT}?${QS}" | jq .

#.
