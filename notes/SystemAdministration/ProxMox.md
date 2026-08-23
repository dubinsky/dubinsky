---
tags:
  - sysadmin
---
Standalone node `proxmox` (`proxmox.lan.podval.org`, `192.168.1.40`). PVE **9.2.11** (kernel **7.0.14-12-pve**; keep previous **7.0.14-5**). Hardware: i9-12900K (24 threads), 64G RAM, boot NVMe `KINGSTON SKC3000D2048G`. Bridge `vmbr0` on `enp3s0`, `192.168.1.40/24`, gw `192.168.1.1`. Host iGPU is Intel AlderLake-S GT1, `/dev/dri/renderD128` on the **hypervisor** (not passed to the docker VM) — see [[Frigate]] § iGPU passthrough.

PVE storage: `local` (dir `/var/lib/vz`, ISO/backup) and `local-lvm` (thin pool `pve/data` on the NVMe).

## Guests

| ID | Type | Name | LAN IPv4 | Role |
|---|---|---|---|---|
| 100 | VM | haos | 192.168.1.209 | [[Home Assistant]] OS. `ssh ha`. 4G RAM, 2 cores, 32G disk |
| 101 | VM | docker | 192.168.1.187 | [[Docker]] / [[DevPod]] / [[Frigate]]. `ssh docker`. 32G RAM, 16 cores (`cpu: host`), 100G disk |
| 103 | LXC | cloudflare-ddns | 192.168.1.235 | Dynamic DNS (`k39.podval.org`). 3G disk |
| 104 | LXC | cloudflared | 192.168.1.236 | Cloudflare Tunnel |
| 105 | LXC | unifi-os-server | 192.168.1.184 | [[UniFi]] OS (controller). **Not** the switch at 192.168.1.101 |

All guests: `onboot: 1`, `vmbr0`, community-script tags. LXC 103/104 unprivileged + nesting. No snapshots when last inventoried.

HAOS USB passthrough (do not steal these off VM 100):

- `0bda:2832` Realtek RTL2832U (rtl_433)
- `303a:831a` Nabu Casa ZBT-2 (Zigbee)
- `303a:4001` Nabu Casa ZWA-2 (Z-Wave)

Docker VM hostname `docker`. **Does not use virtiofs** (no `virtiofs` / `fsN` in `101.conf`). **Do not attach it** — it hangs UEFI boot. Frigate is NFS from `/mnt/data/apps/frigate` to `192.168.1.187` only; guest mounts `/frigate`.

## Community Scripts

There is a lot of extremely helpful scripts for installing various things on ProxMox: https://community-scripts.github.io/ProxmoxVE/scripts.
## Post-install
```shell
# bash -c "$(wget -qLO - https://github.com/community-scripts/ProxmoxVE/raw/main/misc/post-pve-install.sh)"
```
## Dynamic DNS

I use `cloudflare-ddns` LXC (103). Binary `/usr/local/bin/cloudflare-ddns` + `/etc/cloudflare-ddns.env` (mode 600). Do **not** restore `go run …@latest` — that filled the 3G disk.
## Running Docker Containers

As recommended, I use a [[Virtual Machines]] to run [[Docker]] containers.

In ProxMox shell, run the [community script](https://community-scripts.github.io/ProxmoxVE/scripts?id=docker-vm):
```shell
$ bash -c "$(wget -qLO - https://github.com/community-scripts/ProxmoxVE/raw/main/vm/docker-vm.sh)"
```

I use this VM to run [[DevPod]] workspaces locally - and to run `docker compose` stacks like [[Frigate]].

Passing the host iGPU into that VM for Frigate VAAPI is planned (not done): see [[Frigate]] § iGPU passthrough. It needs a PVE reboot. Do not start it until I ask.

TODO in July 2026 suddenly this VM started hanging up on start! - possibly because of the virtfs?

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

That LV sat **on the same thin pool as the VMs**. Filling it could make the pool read-only and stall guests. Media (Audio, Books, Music, OpenTorah, Pictures, Videos) moved to `/mnt/data`. On 2026-08-21 the empty filesystem was unmounted, the fstab line dropped, and `pve/store` removed (`lvremove`). Thin pool `pve/data` dropped from **~50%** to **~3%**. There is no `/mnt/store` anymore. Do not recreate it.

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

`/mnt/data` is Btrfs RAID1 label `Big Data` on `/dev/sdc`+`/dev/sdd` (2×4T WD Red). Photo/media (including Calibre under `Books/`) + [[Frigate]] NFS source. **~25% used**. `Pictures/originals` uses the gphoto-sync layout `YYYY/MM/DD` (renamed from `YYYY/YYYY-MM/YYYY-MM-DD` on 2026-08-21).

`sda`/`sdb` (2×2T WD): leftover `md127` superblocks from the old `/mnt/data`. Array is **stopped**; `/etc/mdadm/mdadm.conf` has `ARRAY <ignore> UUID=dbc353c9:9eddf842:f209850f:8c30d5ea` and `AUTO -all`. Do not start it. Superblocks not wiped.

`sde` (500G) old backup disk; not mounted. No vzdump/PBS jobs; `/var/lib/vz/dump` is empty. Later: scheduled `vzdump` of 100/101/105 (and the small LXC if wanted) onto `sde` or PBS — not onto `local-lvm` next to the guests.

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