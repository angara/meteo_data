#!/bin/bash

ENDPOINT=http://rs.angara.net/meteo/_in

USERNAME=_
PASSWORD=_

HWID=test

T=44
P=999

QS="hwid=${HWID}&t=${T}&p=${P}"

curl -v -u "${USERNAME}":"${PASSWORD}" "${ENDPOINT}?${QS}" && echo ""

#.
