# Host to install ReVoiceChat-CoreServer

## Install Java 21

```wget https://download.oracle.com/java/21/latest/jdk-21_linux-x64_bin.deb```

```sudo dpkg -i jdk-21_linux-x64_bin.deb```

```java --version```

## Install and configure PostGreSQL

```sudo apt install postgresql```

```sudo -i -u postgres```

```psql```

```CREATE USER revoicechat_user WITH PASSWORD 'secure_password';```

```CREATE DATABASE revoicechat_db OWNER = revoicechat_user;```

```exit``` to quit psql

```exit``` to quit postgres user

## Clone this repository

For this guide, we will use ```/srv/rvc``` but you can use any directory (don't forget to change ```/srv/rvc``` to your path)

```git clone https://github.com/revoicechat/ReVoiceChat-server```

## Configure server.properties

Copy ```server.exemple.properties``` to ```app/server.properties```

```cp ./server.exemple.properties ./app/server.properties```

Edit ```./app/server.properties``` : 

```properties
spring.datasource.url=jdbc:postgresql://127.0.0.1:5432/revoicechat_db
spring.datasource.username=revoicechat_user
spring.datasource.password=secure_password
```
