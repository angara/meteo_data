#!/bin/bash

set -a && source ../conf/meteo.env && java -jar meteo-data.jar | tee -a ../log/meteo_data.log

#.
