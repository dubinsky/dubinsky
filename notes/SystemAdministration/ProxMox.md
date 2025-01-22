---
title: ProxMox
tags:
  - sysadmin
---
## Community Scripts

There is a lot of extremely helpful scripts for installing various things on ProxMox: https://community-scripts.github.io/ProxmoxVE/scripts.
## Post-install
```shell
# bash -c "$(wget -qLO - https://github.com/community-scripts/ProxmoxVE/raw/main/misc/post-pve-install.sh)"
```

## UniFi

In ProxMox shell, run the [community script](https://community-scripts.github.io/ProxmoxVE/scripts?id=unifi):
```shell
# bash -c "$(wget -qLO - https://github.com/community-scripts/ProxmoxVE/raw/main/ct/unifi.sh)"
```
## Home Assistant

In ProxMox shell, run the [community script](https://community-scripts.github.io/ProxmoxVE/scripts?id=haos-vm):
```shell
# bash -c "$(wget -qLO - https://github.com/community-scripts/ProxmoxVE/raw/main/vm/haos-vm.sh)"
```

See also a nice [guide](https://www.derekseaman.com/2023/10/home-assistant-proxmox-ve-8-0-quick-start-guide-2.html).

## Running Docker Containers

In ProxMox shell, run the [community script](https://community-scripts.github.io/ProxmoxVE/scripts?id=docker-vm):
```shell
$ bash -c "$(wget -qLO - https://github.com/community-scripts/ProxmoxVE/raw/main/vm/docker-vm.sh)"
```

## File Store

In ProxMox shell:
```shell
# lvcreate -V1T -T pve/data -n store
# mkfs.ext4 /dev/mapper/pve-store
# mkdir /mnt/store
# mount /dev/pve/store /mnt/store # does not persist
```

In `/etc/fstab` on ProxMox, add:
```
/dev/pve/store /mnt/store ext4 0 1
```

## PhotoPrism
Did not have much luck with this one, but...

In ProxMox shell, run the [community script](https://community-scripts.github.io/ProxmoxVE/scripts?id=photoprism):
```
$ bash -c "$(wget -qLO - https://github.com/community-scripts/ProxmoxVE/raw/main/ct/photoprism.sh)"
```
Mount directory with the original photos in the PhotoPrism container:
```shell
# pct set <container id> -mp0 /mnt/store/Pictures/originals,mp=/opt/photoprism/photos/originals
```

Log in: admin/changeme

Library | Index
  All originals
  Complete Rescan
  START

## Emby
Did not have much luck with this one, but...

In ProxMox sell, run the [community script](https://community-scripts.github.io/ProxmoxVE/scripts?id=emby):
```shell
$ bash -c "$(wget -qLO - https://github.com/community-scripts/ProxmoxVE/raw/main/ct/emby.sh)"
```

Mount the file store in the Emby container:
```shell
# pct set <container id> -mp0 /mnt/store,mp=/mnt/store
```
