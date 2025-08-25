#!/bin/bash
./mvnw spring-boot:run -pl "app" -Dspring-boot.run.profiles=linux,pg,create-false-user