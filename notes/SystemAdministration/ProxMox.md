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

## Running Docker Containers

In ProxMox shell, run the [community script](https://community-scripts.github.io/ProxmoxVE/scripts?id=docker-vm):
```shell
$ bash -c "$(wget -qLO - https://github.com/community-scripts/ProxmoxVE/raw/main/vm/docker-vm.sh)"
```

To make arrow keys etc. work when connected to the container over SSH and such,
inside the container make the symbolic link from `/bin/sh` to `/bin/bash`.

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

To [mount](https://pve.proxmox.com/wiki/Linux_Container#_bind_mount_points) a directory from the host in a container, in the ProxMox shell:
```shell
$ pct set <container id> -mp0 /path/on/host,mp=/path/in/container
```
## RAID File Store
I added a bunch of hard disks to my ProxMox box and created a BTRFS RAID; this is where I want to store my photographs and other media.

I perused:
- [Arch Btrfs documentation](https://wiki.archlinux.org/title/Btrfs)
- [ProxMox BTRFS documentation](https://pve.proxmox.com/wiki/BTRFS)
- [Create Btrfs Storage Pool on Proxmox Manually](https://blog.fernvenue.com/archives/create-btrfs-storage-pool-on-proxmox-manually/)

If at some point I decide to add my RAID as srorage to ProxMox:
- [Storage](https://pve.proxmox.com/wiki/Storage)
- [Storage: Directory](https://pve.proxmox.com/wiki/Storage:_Directory)
- [Storage: BTRFS](https://pve.proxmox.com/wiki/Storage:_BTRFS)

ProxMox shell commands for the record:
```shell
## get rid of the stale madm RAID membership metadata
# wipefs -af /dev/sdc
# wipefs -af /dev/sdd
## reboot for ProxMox to re-read disks metadata
## make the RAID filesystem
# mkfs.btrfs -draid1 -mraid1 /dev/sdc /dev/sdd -L "Big Data"
## get UUID etc.
# btrfs filesystem show /dev/sdc # or /sdd
## create mountpoint
# mkdir /mnt/data
## in /etc/fstab, add:
UUID=<UUID> /mnt/data btrfs defaults 0 1
# systemctl daemon-reload
## mount
# mount /mnt/data
```
