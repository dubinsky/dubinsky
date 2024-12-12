---
title: UniFi
tags: [computer]
---
[UniFi Help](https://help.ui.com/hc/en-us/categories/200320654)

See [[Docker]].

Sources of the images:
- https://github.com/jacobalberty/unifi-docker
- https://github.com/11notes/docker-unifi
- https://github.com/linuxserver/docker-unifi-network-application

I used `jacobalberty` image previously; switched to `linuxserver` now.

- create `unifi` user: `$ sudo useradd unifi`.
- put into `init-mongo.sh`:
```shell
#!/bin/bash

if which mongosh > /dev/null 2>&1; then
  mongo_init_bin='mongosh'
else
  mongo_init_bin='mongo'
fi
"${mongo_init_bin}" <<EOF
use ${MONGO_AUTHSOURCE}
db.auth("${MONGO_INITDB_ROOT_USERNAME}", "${MONGO_INITDB_ROOT_PASSWORD}")
db.createUser({
  user: "${MONGO_USER}",
  pwd: "${MONGO_PASS}",
  roles: [
    { db: "${MONGO_DBNAME}", role: "dbOwner" },
    { db: "${MONGO_DBNAME}_stat", role: "dbOwner" }
  ]
})
```
- put into `docker-compose.yaml`:
```yaml
services:
  unifi-db:
    image: docker.io/mongo:7.0.12
    container_name: unifi-db
    environment:
      - MONGO_INITDB_ROOT_USERNAME=root
      - MONGO_INITDB_ROOT_PASSWORD=password
      - MONGO_AUTHSOURCE=admin
      - MONGO_USER=unifi
      - MONGO_PASS=unifi
      - MONGO_DBNAME=unifi
    volumes:
      - /home/unifi/mongo-db-data/:/data/db:Z
      - /home/unifi/mongo-db-config-data/:/data/configdb:Z
      - /home/unifi/init-mongo.sh:/docker-entrypoint-initdb.d/init-mongo.sh:ro,Z
    restart: unless-stopped

  unifi-network-application:
    image: lscr.io/linuxserver/unifi-network-application:latest
    container_name: unifi-network-application
    environment:
      - TZ=America/New_York
      - MONGO_USER=unifi
      - MONGO_PASS=unifi
      - MONGO_HOST=unifi-db
      - MONGO_PORT=27017
      - MONGO_DBNAME=unifi
      - MONGO_AUTHSOURCE=admin
    volumes:
      - /home/unifi/unifi-controller-config:/config:Z
    ports:
      - 8443:8443
      - 3478:3478/udp
      - 10001:10001/udp
      - 8080:8080
    restart: unless-stopped
```
- run `$ docker-compose up -d`

TODO there still seems to be some empty Docker-managed volume hanging around...
## Setup

### Basic
- name: k39-3
- choose "Advanced Setup" to avoid logging into Unifi UI account
- admin: dub/whatwhen
- Settings > System > Advanced > Inform Host > Override with the IP address of the machine running `unifi`

### Devices

Move devices from the old controller:

Reset to the factory state with a paperclip; it may be necessary to `$ set-inform http://<controller host>:8080/inform` on the device; default SSH credentials - ubnt/ubnt

Reset to the factory state with a `syswrapper.sh restore-default` command on the device.

### Networks

- LAN: 192.168.1.0/24
	- Settings | Network | Default | Advanced | Manual | DHCP | DHCP Service Management | Domain Name: "lan.podval.org"
- WiFi:
	- podval-u: all the bands
	- podval-2g: 2G only

### SSH
Enable SSH for UniFi devices:
- in the UniFi Console | Settings | System | Application Configuration | Device SSH Authentication
- retrieve auto-generated SSH password (dlczW8IWQI7YmIdk)
- use it: `ssh -o PubkeyAcceptedKeyTypes=ssh-rsa -o HostKeyAlgorithms=ssh-rsa -o RequiredRSASize=1024 192.168.1.157` (see https://kcore.org/2023/03/27/ssh-unifi-fedora-37/)
### Port forwarding

TODO
I'in the UniFi Console | Settings | Security | Port Forwarding, forward to the `gatekeeper`:
- 22 - ssh
- 80 - http
- 443 - https
### Static Addresses

Addresses on the 192.168.1.* network (*.lan.podval.org).

TODO HOW DO I ASSIGN STATIC ADDRESSES TO UniFi DEVICES?

| Address | Name                     |
| ------- | ------------------------ |
| 1       | USG-3P (gateway)         |
| 2       | dub                      |
| 4       | OBi202                   |
| 21      | printer                  |
| 22      | printer-colour2          |
| 30      | TV                       |
| 31      | gatekeeper               |
| 32      | gatekeeper-wifi          |
| 33      | dub-phone                |
| 34      | nina                     |
| 35      | (nina-wifi)              |
| 36      | nina-phone               |
| 40      | bedroom speaker          |
| 41      | Anova oven               |
| 71      | UAP-nanoHD               |
| 72      | UAP-AC-Pro               |
| 78      | dub-wifi                 |
| 156     | US-8-60W                 |
| 158     | Viessmann-2224 podval-2g |
| 200     | turingpi                 |
| 201     | cube1                    |
| 202     | cube2                    |
| 203     | cube3                    |
| 204     | cube4                    |
