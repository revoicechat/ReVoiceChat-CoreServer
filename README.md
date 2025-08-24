# RevoiceChat-Server

![RevoiceChat](https://img.shields.io/badge/Revoice-Chat-1E90E7.svg)

This repository provide the source code of server-side of `RevoiceChat`

## Run the app

### Prerequisite

 - JDK 21
 - PostGreSQL

### [How to install](INSTALL.md)

### Run as an app (.jar)

 - copy the jar where you want and rename it `revoicechat-app.jar`
 - copy the `server.properties` in the same place of `revoicechat-app.jar`
 - run `java -jar ./revoicechat-app.jar --spring.profiles.active=linux,pg`

### Run as a service (systemd)
- Rename `rvc-core.service.example` to `rvc-core.service`
- Edit rvc-core.service and change `WorkingDirectory` and `ExecStart` path
- Link the service file : `sudo systemctl link /[YOUR-PATH]/rvc-core.service`
- Enable service : `sudo systemctl enable rvc-core.service`
- Start service : `sudo systemctl start rvc-core.service`

## Voice (VoIP)

### Prerequisite
The following ports need to be open and forwarded to your server :
- 3478 TCP/UDP (TURN)
- 49152–65535 UDP (Voice data)
