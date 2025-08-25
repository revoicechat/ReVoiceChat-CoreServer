#!/bin/bash
screen -dmS rvc-server ./mvnw spring-boot:run -pl "app" -Dspring-boot.run.profiles=linux,pg