---
tags:
  - sysadmin
---
Standalone Proxmox node.
Private details (addresses, the guest table, disk identifiers) are in the private vault `dubinsky/dubinsky-private`, at
this same path.

PVE storage: `local` (dir `/var/lib/vz`, ISO/backup) and `local-lvm` (thin pool `pve/data` on the boot NVMe).
Host iGPU stays on the hypervisor (not passed to the Docker VM) until [[Frigate]] § iGPU passthrough is started.
Do not start that.
Leave old kernels installed.
Do not `apt autoremove`.

## Guests

One VM for [[Home Assistant]] OS (`ssh ha`): 4G RAM, 2 cores, 32G disk.
One VM for [[Docker]] / [[DevPod]] / [[Frigate]] (`ssh docker`): 8G RAM, 16 cores, 100G disk.
Not 32G.
An LXC for [[UniFi]] OS: privileged, 2 cores, 4G RAM, 20G disk.
An LXC for Tailscale: unprivileged, 1G RAM, 1 core, 4G disk.
An LXC for [[eScriptorium]]: unprivileged, 8 cores, 32G RAM, 512M swap, 32G disk.
Addresses and guest ids are in the private note.
All guests: `onboot: 1`, on the LAN bridge.

The Tailscale and eScriptorium containers are unprivileged, with nesting.
The UniFi OS container is privileged (nesting only).
Checked 2026-11-18.
The Tailscale and eScriptorium containers also have `keyctl=1` and `/dev/net/tun`, so Tailscale can open its tunnel.

The dynamic-DNS container ran for a while and was never used.
It was destroyed 2026-11-18, in favour of Tailscale.
The Cloudflare Tunnel container and the cloud-infrastructure container were destroyed.
Do not recreate them.
See [[Domains]] and [[Cloud Infrastructure]].

Remote access is the Tailscale container.
It does not run Grok, is not an exit node, and does not use Tailscale SSH.
It still advertises a subnet route, and that route is what my own machines use off the LAN.
An assistant does not get the LAN through that route.
The route, the tailnet names, and the assistant allow-list are in the private note.
Assistant access is [[Grok Bot Access]].

Snapshots taken 2026-11-11 are no longer on the host (checked 2026-11-18).
eScriptorium cannot `pct snapshot` because of the bind mounts (facsimiles read-only, work dir read-write).
Use a file backup on the host for that guest.

On **2026-09-01 12:45 EDT** the host itself came back from an **unclean** stop (no `reboot` in the journal, EFI dirty
bit, journal “uncleanly shut down”, guest ext4 recovery).
No UPS/NUT/IPMI log, no `qmreboot`.
`startall` then started the guests that existed then.
Docker/Frigate fallout: [[Frigate]].

HAOS USB passthrough (do not steal these off the Home Assistant VM):

- Realtek RTL2832U (rtl_433), `0bda:2832`
- Nabu Casa ZBT-2 (Zigbee), `303a:831a`
- Nabu Casa ZWA-2 (Z-Wave), `303a:4001`

The Docker VM is Debian, timezone **America/New_York**.
**Does not use virtiofs.** **Do not attach it.** It hangs UEFI boot.
Frigate is NFS from `/mnt/data/apps/frigate` to that VM only.
The guest mounts `/frigate`.

## Community Scripts

There is a lot of extremely helpful scripts for installing various things on ProxMox:
https://community-scripts.github.io/ProxmoxVE/scripts.
## Post-install
```shell
# bash -c "$(wget -qLO - https://github.com/community-scripts/ProxmoxVE/raw/main/misc/post-pve-install.sh)"
```
## Dynamic DNS

An LXC (`cloudflare-ddns`) updated a Cloudflare A record for a while.
Nothing used it.
It is gone, in favour of the Tailscale container.
The name and the address are in the private note.
See [[Domains]].
## Running Docker Containers

As recommended, I use a [[Virtual Machines]] to run [[Docker]] containers.

In ProxMox shell, run the [community script](https://community-scripts.github.io/ProxmoxVE/scripts?id=docker-vm):
```shell
$ bash -c "$(wget -qLO - https://github.com/community-scripts/ProxmoxVE/raw/main/vm/docker-vm.sh)"
```

I use this VM to run [[DevPod]] workspaces locally - and to run `docker compose` stacks like [[Frigate]].

[[eScriptorium]] and Kraken run in their own LXC.
Do not move that stack onto this VM.

Passing the host iGPU into that VM for Frigate VAAPI is planned (not done): see [[Frigate]] § iGPU passthrough.
It needs a PVE reboot.
Do not start it until I ask.

TODO in July 2026 suddenly this VM started hanging up on start! - possibly because of the virtfs?

## Terminal

To make arrow keys etc. work when connected to the container over SSH and such, inside the container make the symbolic
link from `/bin/sh` to `/bin/bash`.

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

That LV sat **on the same thin pool as the VMs**.
Filling it could make the pool read-only and stall guests.
Media (Audio, Books, Music, OpenTorah, Pictures, Videos) moved to `/mnt/data`.
On 2026-08-21 the empty filesystem was unmounted, the fstab line dropped, and `pve/store` removed (`lvremove`).
Thin pool `pve/data` dropped from **~50%** to **~3%**.
There is no `/mnt/store` anymore.
Do not recreate it.

## RAID File Store
I added a bunch of hard disks to my ProxMox box and created a BTRFS RAID; this is where I want to store my photographs
and other media.

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

`/mnt/data` is Btrfs RAID1.
Photo/media (including Calibre under `Books/`) and the [[Frigate]] NFS source live there.
Disk names, the label, and how full it is are in the private note.
`Pictures/originals` uses the gphoto-sync layout `YYYY/MM/DD` (renamed from `YYYY/YYYY-MM/YYYY-MM-DD` on 2026-08-21).

The previous pair of disks still has leftover `md127` superblocks from the old `/mnt/data`.
The array is **stopped**.
`/etc/mdadm/mdadm.conf` ignores it (`AUTO -all`).
The UUID is in the private note.
Do not start it.
Superblocks not wiped.

An old backup disk is not mounted.
No vzdump/PBS jobs.
`/var/lib/vz/dump` is empty.
Later: scheduled `vzdump` of the Home Assistant VM, the Docker VM, and the UniFi container (and the small LXCs if
wanted) onto that disk or PBS, not onto `local-lvm` next to the guests.

## Mount

To [mount](https://pve.proxmox.com/wiki/Linux_Container#_bind_mount_points) a directory from the host in an LXC
container:
- in the ProxMox shell:
```shell
$ pct set <container id> -mp0 /path/on/host,mp=/path/in/container
```
For additional mounts use `-mp1` etc.

To [mount](https://woshub.com/proxmox-shared-host-directory/) a directory from the host in a virtual machine (see
[post](https://forum.proxmox.com/threads/proxmox-8-4-virtiofs-virtiofs-shared-host-folder-for-linux-and-or-windows-guest-vms.167435/)):
- in the ProxMox UI `Datacenter | Directory Mappings` add a mapping from name/tag to the path on the host
- in the settings of the virtual machine `Hardware | Virtiofs` pass it in
- in the guest virtual machine, create a mountpoint
- in the guest: `sudo mount -t virtiofs <tag> <mountpoint>`
- in the guest `/etc/fstab`, add: `<tag> <mountpoint> virtiofs defaults,nofail 0 0`
