#!/bin/bash

ENDPOINT=http://127.0.0.1:8004/meteo/api/active-stations
AUTHORIZATION="Authorization: Basic Xzpf"
QS="lat=52.27&lon=104.28"

echo "${AUTHORIZATION}" "${ENDPOINT}?${QS}"

curl -v -H "${AUTHORIZATION}" "${ENDPOINT}?${QS}" && echo ""

#.
