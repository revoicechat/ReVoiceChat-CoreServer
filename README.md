![logo_black_screen.png#gh-dark-mode-only](logo-dark-mode.png#gh-dark-mode-only)
![logo_white_screen.png#gh-light-mode-only](logo-light-mode.png#gh-light-mode-only)

![RevoiceChat](https://img.shields.io/badge/RevoiceChat-Core%20Server-1E90E7?style=for-the-badge)

This repository provide the source code of server-side of `RevoiceChat`

## Run the app

### Prerequisite

 - JDK 21
 - PostGreSQL

### [How to install](INSTALL.md)

### Run as an app (.jar)

 - copy the jar where you want and rename it `revoicechat-app.jar`
 - copy the `server.properties` in the same place of `revoicechat-app.jar`
 - run `./scripts/build-app.sh`
 - run `./scripts/run-app.sh`

### Run as a service (systemd)
- Copy `rvc-core.service.example` to `rvc-core.service`
- Edit `rvc-core.service` and change `WorkingDirectory` and `ExecStart` path
- Link the service file : `sudo systemctl link /[YOUR-PATH]/rvc-core.service`
- Enable service : `sudo systemctl enable rvc-core.service`
- Start service : `sudo systemctl start rvc-core.service`
- Check service status : `sudo systemctl status rvc-core.service`

Expected output : 
```log 
* rvc-core.service - ReVoiceChat Core Server
     Loaded: loaded (/etc/systemd/system/rvc-core.service; enabled; preset: enabled)
     Active: active (running) since Sun 2025-08-24 12:14:11 UTC; 1s ago
```

## License

This project is licensed under the GNU General Public License v3.0 - see the [LICENSE](LICENSE) file for details.

Copyright (C) 2025 RevoiceChat.fr