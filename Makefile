.EXPORT_ALL_VARIABLES:
SHELL = bash

.PHONY: dev build clean version

all: clean build deploy # restart

# # #

APP_NAME   = meteo_data
VER_MAJOR  = 4
VER_MINOR  = 0
MAIN_CLASS = angara.meteo.main

dev:
	bash -c "set -a && source .env && \
	 clj -J-Dbuild_info.appname=${APP_NAME} -J-Dbuild_info.version=${VER_MAJOR}.${VER_MINOR}.DEV -M:dev:nrepl"

javac:
	@clj -T:build javac

run:
	set -a && source ../conf/meteo.env && java -jar target/meteo_data.jar

build:
	@mkdir -p ./target/resources
	@clj -T:build uberjar

deploy:
	scp target/meteo_data.jar angara:/app/meteo_data/
	# scp run.sh meteo_data.service angara:/app/meteo_data/

restart:
	ssh angara "ps ax | grep 'java -jar meteo-data.jar' | grep -v grep | awk '{ print \$$1 }' | xargs kill "

clean:
	rm -rf ./target

outdated:
	@(clojure -Tantq outdated || exit 0)

#.
