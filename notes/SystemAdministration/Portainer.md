---
title: Portainer
---
Not sure if this is of any use to me, but I do deploy things like Unifi and Home Assistant using docker compose, so...

https://github.com/portainer/portainer
https://github.com/docker/awesome-compose/blob/master/portainer/compose.yaml

My `/home/portainer/docker-compose.yaml`:
```yaml
services:
  portainer:
    image: portainer/portainer-ce:alpine
    container_name: portainer
    privileged: true
    command: -H unix:///var/run/docker.sock
    ports:
      - "9000:9000"
    volumes:
      - "/var/run/docker.sock:/var/run/docker.sock"
      - "/home/portainer/portainer_data:/data"
    restart: always

```

Started playing with Plex: https://github.com/plexinc/pms-docker