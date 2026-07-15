---
tags:
  - sysadmin
---
## Community Scripts

There is a lot of extremely helpful scripts for installing various things on ProxMox: https://community-scripts.github.io/ProxmoxVE/scripts.
## Post-install
```shell
# bash -c "$(wget -qLO - https://github.com/community-scripts/ProxmoxVE/raw/main/misc/post-pve-install.sh)"
```
## Dynamic DNS

I use `cloudflare-ddns` LXC.
## Running Docker Containers

As recommended, I use a [[Virtual Machines]] to run [[Docker]] containers.

In ProxMox shell, run the [community script](https://community-scripts.github.io/ProxmoxVE/scripts?id=docker-vm):
```shell
$ bash -c "$(wget -qLO - https://github.com/community-scripts/ProxmoxVE/raw/main/vm/docker-vm.sh)"
```

I use this VM to run [[DevPod]] workspaces locally - and to run `docker compose` stacks like [[Frigate]].

TODO in July 2026 suddenly this VM started hanging up on start!

## Terminal

To make arrow keys etc. work when connected to the container over SSH and such,
inside the container make the symbolic link from `/bin/sh` to `/bin/bash`.

## File Store

In ProxMox shell:
```shell
# lvcreate -V1T -T pve/data -n store
# mkfs.ext4 /dev/mapper/pve-store
# mkdir /mnt/store
```
In ProxMox `/etc/fstab`, add:
```fstab
/dev/pve/store /mnt/store ext4 0 1
```
And then mount:
```shell
# mount /mnt/store
```

## RAID File Store
I added a bunch of hard disks to my ProxMox box and created a BTRFS RAID; this is where I want to store my photographs and other media.

I perused:
- [Arch Btrfs documentation](https://wiki.archlinux.org/title/Btrfs)
- [ProxMox BTRFS documentation](https://pve.proxmox.com/wiki/BTRFS)
- [Create Btrfs Storage Pool on Proxmox Manually](https://blog.fernvenue.com/archives/create-btrfs-storage-pool-on-proxmox-manually/)

If at some point I decide to add my RAID as storage to ProxMox:
- [Storage](https://pve.proxmox.com/wiki/Storage)
- [Storage: Directory](https://pve.proxmox.com/wiki/Storage:_Directory)
- [Storage: BTRFS](https://pve.proxmox.com/wiki/Storage:_BTRFS)

If needed, get rid of the stale `madm` RAID membership metadata: in ProxMox shell:
```
# wipefs -af /dev/sdc
# wipefs -af /dev/sdd
## reboot for ProxMox to re-read disks metadata
```

In ProxMox shell:
```shell
## make the RAID filesystem
# mkfs.btrfs -draid1 -mraid1 /dev/sdc /dev/sdd -L "Big Data"
## get UUID etc.
# btrfs filesystem show /dev/sdc # or /sdd
## create mountpoint
# mkdir /mnt/data
```

In ProxMox `/etc/fstab`, add:
```
UUID=<UUID> /mnt/data btrfs defaults 0 1
```
And mount:
```
# systemctl daemon-reload
# mount /mnt/data
```
## Mount

To [mount](https://pve.proxmox.com/wiki/Linux_Container#_bind_mount_points) a directory from the host in an LXC container:
- in the ProxMox shell:
```shell
$ pct set <container id> -mp0 /path/on/host,mp=/path/in/container
```
For additional mounts use `-mp1` etc.

To [mount](https://woshub.com/proxmox-shared-host-directory/) a directory from the host in a virtual machine (see [post](https://forum.proxmox.com/threads/proxmox-8-4-virtiofs-virtiofs-shared-host-folder-for-linux-and-or-windows-guest-vms.167435/)):
- in the ProxMox UI `Datacenter | Directory Mappings` add a mapping from name/tag to the path on the host
- in the settings of the virtual machine `Hardware | Virtiofs` pass it in
- in the guest virtual machine, create a mountpoint
- in the guest: `sudo mount -t virtiofs <tag> <mountpoint>`
- in the guest `/etc/fstab`, add: `<tag> <mountpoint> virtiofs defaults,nofail 0 0`