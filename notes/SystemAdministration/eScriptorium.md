---
tags:
  - sysadmin
---
[eScriptorium](https://gitlab.com/scripta/escriptorium) is the review UI for the archival HTR pipeline.
The engine is Kraken.
Both run on [[ProxMox]] LXC **107** (`escriptorium`, static `192.168.1.238`).
SSH is `root@192.168.1.238` with the YubiKey key.

## Why an LXC

On 2026-10-14 this was chosen over the `docker` VM (101) and over a new VM.
Proxmox's own advice is to run Docker in a VM, and that is how [[Frigate]] is installed.
A VM on this host pins its whole RAM allocation.
Guest 101 is 32G and 16 cores, and on that day Frigate was its only container: about 1.9G RAM and a load around 2 cores.
An LXC uses RAM only while it needs it.
107's ceiling is 16G and it sits near 1.2G when idle.

The same day a Compose probe cleared the usual Docker-in-LXC failures.
Postgres 16 and Redis 7 started under Docker Compose.
Postgres data was on the btrfs work dir.
The alpine postgres user is host uid `100070`.
A row was still there after the database container restarted.
Redis answered `PING` on a published port from the Proxmox host.
AppArmor stayed confined.
`nesting=1,keyctl=1` was enough.
No switch to a VM.

## Guest

Debian 13, unprivileged, `onboot: 1`.
8 cores, 16G RAM, 512M swap, 32G rootfs on `local-lvm`.
`cpuunits: 50`, so this guest loses to VM 101 when the CPUs are busy.
`features: nesting=1,keyctl=1` and `/dev/net/tun`.
The TUN device is there so Tailscale can be added the way LXC 106 was.
`.238` is outside the UniFi DHCP pool (`.100`–`.199`), so it has no fixed-IP reservation.

Docker is Debian `docker.io` 26.1.5 and Compose 2.26.1.
Storage driver `overlay2`, cgroup driver `systemd`.

## Storage

Facsimiles are bind-mounted read-only:

`/mnt/data/OpenTorah/facsimiles.alter-rebbe.org` → `/srv/facsimiles`

That tree is about 7.3G and 3,035 JPEGs, owned by `dub:dub` and world-readable, so the shifted container root can read
it.
The pipeline must not write there.

Work dir, read-write, host uid `100000` (root inside the container):

`/mnt/data/apps/htr` → `/srv/htr`

Compose data for eScriptorium (Postgres, media, models, checkpoints) belongs on `/srv/htr`, not on the 32G rootfs.
Do not create a large thin LV for this.
`pve/store` was removed because a full thin LV can stall the guests.

## Stack

eScriptorium v26.07 is running from a detached clone at `/srv/htr/escriptorium` (`f3f97f5`).
The Compose file in the clone is unchanged.
Local settings are `variables.env` (mode 600, gitignored) and `docker-compose.override.yml`.
The override pins `registry.gitlab.com/scripta/escriptorium:v26.07`, sets Kraken training and inference to CPU, and
turns Flower off so port 5555 is not published.
The only published port is `8080`.
LAN check: `http://192.168.1.238:8080/health`.

Named volumes for static files, media, and Postgres are bind mounts under `/srv/htr/data`.
Docker image layers stay on the 32G rootfs (`overlay2`).
Do not move the Docker data-root onto the btrfs bind.

Each app process sits near 600MB idle because the image loads Kraken.
Hard limits: web 1536m, channelserver 1024m, celery-live and celery-low-priority 1280m, celery-main and celery-gpu
2560m, intensive inference 1536m, celery-jvm 2048m, Postgres 1536m.
A 768m web limit OOM-killed the container when a second Python process started.

The Django superuser is `dub` (`dub@podval.org`).
The password is only in `/root/escriptorium-admin.txt` and `variables.env` inside the container.

## Tailscale

Tailscale 1.104.1 is installed and logged in.
`/dev/net/tun` is passed in.
The node is `escriptorium` at `100.81.96.9`, MagicDNS `escriptorium.tailc87f26.ts.net`.
Login was `tailscale up --hostname=escriptorium --ssh=false --accept-dns=false`: no subnet routes, no Tailscale SSH, and
LAN DNS stays `192.168.1.1`.
`tailscale serve --bg 8080` fronts the UI at `https://escriptorium.tailc87f26.ts.net` (tailnet only, proxy to
`127.0.0.1:8080`).
Leave Tailscale Funnel off.
Funnel would publish that same URL on the public internet.
The LAN URL `http://192.168.1.238:8080` still answers.
Do not move this stack onto VM 101.

## Kraken CLI

Kraken 7.1.1 and ketos are on the container with `pipx install --global 'kraken==7.1.*'`, Python 3.13.5.
The binaries are `/usr/local/bin/kraken` and `/usr/local/bin/ketos`.
The venv is `/opt/pipx/venvs/kraken` on the 32G rootfs.
After that install about 11G of the rootfs was free.
Lightning is pinned at 2.6.1 by this Kraken release, with Torch 2.14.0.
Do not upgrade Lightning on its own.
The eScriptorium image also contains Kraken.
Host-side models use `XDG_DATA_HOME=/srv/htr/xdg`.
PP-OCRv6 small (`10.5281/zenodo.21788405`) is stored there.

## Backups

`/usr/local/sbin/escriptorium-pgdump` writes a custom-format `pg_dump` into `/srv/htr/backups` (mode 600) and keeps 14
files.
`escriptorium-pgdump.timer` fires at 21:30 America/New_York.
The first dump, `escriptorium-20261014-224636.dump`, listed its tables with `pg_restore -l`.

## Phase 0 timing

On 2026-10-14 a 20-page CPU batch finished in 7 minutes 24 seconds of wall time, about 22 seconds per page.
That is one process at `nice -n 10`, `--device cpu --threads 4`, `segment -bl`, and `ocr -B 8 --num-line-workers 2`,
with PP-OCRv6 small.
The model load is inside that figure.
Peak resident memory was 2.3 GB.
`pages.txt` holds four JPEGs from each of lvia, niab, rgada, rgia, and books, each between 200KB and 6MB.
Text direction was Kraken's default, `horizontal-lr`.
One page, `rgada/.../3140/279-2.jpg`, produced no lines.
The output directory has 19 files because two inputs share the basename `000-1.jpg` and the second overwrote the first.
The log still records a recognition write for all 20.
