# RevoiceChat-Server

![RevoiceChat](https://img.shields.io/badge/Revoice-Chat-1E90E7.svg)

This repository provide the source code of server-side of `RevoiceChat`

## Run the app

### Prerequisite

 - JDK 21

### How to :
```sh
git clone https://github.com/revoicechat/ReVoiceChat-server.git
cd ./ReVoiceChat-server
./mvnw spring-boot:run -pl "app"
```

for server purpose, you can copy the `revoicechat-core-server.properties` file on the root of the server
and complete it with your postgres database

## Voice (VoIP)

### Prerequisite
The following ports need to be open and forwarded to your server :
- 3478 TCP/UDP (TURN)
- 49152–65535 UDP (Voice data)
