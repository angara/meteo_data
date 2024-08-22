#!/bin/bash

set -a && source ../conf/meteo.env && java -jar meteo_data.jar | tee -a ../log/meteo_data.log

#.
