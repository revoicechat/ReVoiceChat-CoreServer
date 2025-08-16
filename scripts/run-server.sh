#!/bin/bash
# TODO - remove de dev profile once postgres is setup
screen -dmS rvc-server -L -Logfile latest.log ./mvnw spring-boot:run -pl "app" -Dspring-boot.run.profiles=dev,linux