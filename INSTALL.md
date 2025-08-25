# Host to install ReVoiceChat-CoreServer

## Install Java 21

```sh
wget https://download.oracle.com/java/21/latest/jdk-21_linux-x64_bin.deb
```

```sh 
sudo dpkg -i jdk-21_linux-x64_bin.deb
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
CREATE
USER revoicechat_user WITH PASSWORD 'secure_password';
```

```sql
CREATE
DATABASE revoicechat_db OWNER = revoicechat_user;
```

`exit` to quit psql

```exit``` to quit postgres user

## Clone this repository

For this guide, we will use `/srv/rvc` but you can use any directory (don't forget to change `/srv/rvc` to your path)

```sh
git clone https://github.com/revoicechat/ReVoiceChat-server
```

## generate rsa key for JWT tokens

```sh
./scripts/generate_jwtKey.sh
```

it will generate two file in `/jwt`

## Configure server.properties

Copy `server.exemple.properties` to `app/server.properties`

```sh 
cp ./server.exemple.properties ./app/server.properties
```

Edit `./app/server.properties`
