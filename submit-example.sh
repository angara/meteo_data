#!/bin/bash

SUBMIT_ENDPOINT=http://rs.angara.net/meteo/_in
API_ENDPOINT=http://rs.angara.net/meteo/api

# default credentials

USERNAME=_
PASSWORD=_

HWID=test
ST=test

T=44
P=999

QS="hwid=${HWID}&t=${T}&p=${P}"

curl -v -u "${USERNAME}":"${PASSWORD}" "${SUBMIT_ENDPOINT}?${QS}" && echo ""

curl -v -u "${USERNAME}":"${PASSWORD}" "${API_ENDPOINT}/station-info?st=${ST}"

#.
