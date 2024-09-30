#!/bin/bash

ENDPOINT=http://127.0.0.1:8004/meteo/_in
AUTHORIZATION="Authorization: Basic Xzpf"

TS=`date +%s`000

QS="hwid=test&t=33&p=999&ts="$TS

echo "${AUTHORIZATION}" "${ENDPOINT}?${QS}"

curl -H "${AUTHORIZATION}" "${ENDPOINT}?${QS}" && echo ""

# echo "should fail"
#
# curl -v -H "${AUTHORIZATION}" "${ENDPOINT}?${QS}" && echo ""

# wrk -t 10 -H "Authorization: Basic Xzpf" "http://localhost:8004/meteo/_in?hwid=test&t1&p=999"

# for i in 1 2 3; do ./submit-test-2.sh; done
# Authorization: Basic Xzpf http://127.0.0.1:8004/meteo/_in?hwid=test&t=33&p=999&ts=1727665094000
# {"st":"test","ts":"2024-09-30T02:58:14Z","ok":["t: 33.0","p: 999.0"]}
# Authorization: Basic Xzpf http://127.0.0.1:8004/meteo/_in?hwid=test&t=33&p=999&ts=1727665094000
# {"st":"test","ts":"2024-09-30T02:58:14Z","err":["old timestamp - t","old timestamp - p"]}
# Authorization: Basic Xzpf http://127.0.0.1:8004/meteo/_in?hwid=test&t=33&p=999&ts=1727665094000
# {"st":"test","ts":"2024-09-30T02:58:14Z","err":["old timestamp - t","old timestamp - p"]}

#.
