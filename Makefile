.EXPORT_ALL_VARIABLES:
SHELL = bash

.PHONY: dev build clean version

all: clean build deploy # restart

# # #

APP_NAME   = meteo_data
VER_MAJOR  = 4
VER_MINOR  = 2
MAIN_CLASS = angara.meteo.main

dev: clean javac
	bash -c "set -a && source .env && clj -M:dev:nrepl"

javac:
	@clj -T:build javac

run:
	set -a && source ../conf/meteo.env && java -jar target/meteo_data.jar

build: clean
	@mkdir -p ./target/resources
	@clj -T:build uberjar

deploy:
	scp target/meteo_data.jar angara:/app/meteo_data/
	# scp run.sh meteo_data.service angara:/app/meteo_data/

restart:
	ssh angara "ps ax | grep 'java -jar meteo_data.jar' | grep -v grep | awk '{print \$$1}' | xargs kill "

clean:
	rm -rf ./target

outdated:
	@(clojure -Tantq outdated || exit 0)

#.
