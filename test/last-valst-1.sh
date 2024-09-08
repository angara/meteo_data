#!/bin/bash

ENDPOINT=http://127.0.0.1:8004/meteo/api/last-vals
AUTHORIZATION="Authorization: Basic Xzpf"
QS="st=uiii&st=uuee"

echo "${AUTHORIZATION}" "${ENDPOINT}?${QS}"

curl -v -H "${AUTHORIZATION}" "${ENDPOINT}?${QS}" && echo ""

# echo "should fail"
#
# curl -v -H "${AUTHORIZATION}" "${ENDPOINT}?${QS}" && echo ""

# wrk -t 10 -H "Authorization: Basic Xzpf" "http://localhost:8004/meteo/_in?hwid=test&t1&p=999"

#.
