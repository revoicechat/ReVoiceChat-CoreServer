# Host to install ReVoiceChat-CoreServer

## Install Java 25

```sh
wget https://download.oracle.com/java/25/latest/jdk-25_linux-x64_bin.deb
```

```sh 
sudo dpkg -i jdk-25_linux-x64_bin.deb
```

```sh
java --version
```

## Install and configure PostGreSQL

```sh 
sudo apt install postgresql
```

```sh 
sudo -i -u postgres
```

```sh 
psql
```

```sql
CREATE USER revoicechat_user WITH PASSWORD 'secure_password';
```

```sql
CREATE DATABASE revoicechat_db OWNER = revoicechat_user;
```

`exit` to quit psql

```exit``` to quit postgres user

## Clone this repository (if not already done)

For this guide, we will use `/srv/rvc` but you can use any directory (don't forget to change `/srv/rvc` to your path)

```sh
git clone https://github.com/revoicechat/ReVoiceChat-CoreServer
```

## generate rsa key for JWT tokens

```sh
cd ReVoiceChat-CoreServer/
./scripts/generate_jwtKey.sh
```

it will generate two file in `/jwt`

## Configure server.properties

Copy `server.exemple.properties` to `server.properties`

```sh 
cp ./server.exemple.properties ./server.properties
```

Edit `./server.properties`

```sh
nano ./server.properties
```

Edit `quarkus.datasource.username` to the psql username you added earlier
Add psql password to `quarkus.datasource.password`

Your CoreServer is now setup !