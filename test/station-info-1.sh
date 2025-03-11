#!/bin/bash

ENDPOINT=http://127.0.0.1:8004/meteo/api/station-info
ENDPOINT=http://rs.angara.net/meteo/api/station-info

AUTHORIZATION="Authorization: Basic Xzpf"
QS="st=uiii"

echo "${AUTHORIZATION}" "${ENDPOINT}?${QS}"

curl -H "${AUTHORIZATION}" "${ENDPOINT}?${QS}" | jq .

# curl -v -H "${AUTHORIZATION}" "${ENDPOINT}?${QS}" && echo ""

# wrk -t 10 -H "Authorization: Basic Xzpf" "http://localhost:8004/meteo/_in?hwid=test&t1&p=999"

#.
