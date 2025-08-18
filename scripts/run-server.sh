#!/bin/bash
screen -dmS rvc-server -L -Logfile latest.log ./mvnw spring-boot:run -pl "app" -Dspring-boot.run.profiles=linux,pg