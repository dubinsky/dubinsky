---
tags:
  - sysadmin
---
[eScriptorium](https://gitlab.com/scripta/escriptorium) is the review UI for the archival HTR pipeline.
The engine is Kraken.
Both run on their own [[ProxMox]] container.
SSH is root with the [[YubiKey]] key.

## Why an LXC

On 2026-10-14 this was chosen over the Docker VM and over a new VM.
Proxmox's own advice is to run Docker in a VM, and that is how [[Frigate]] is installed.
A VM on this host pins its whole RAM allocation.
An LXC uses RAM only while it needs it.
The 2026-10-14 note said the Docker VM was 32G and this container's ceiling was 16G.
That was backwards: the Docker VM was 8G RAM and 16 cores, and this container's ceiling is 32G.
On 2026-11-18 that VM was renamed `frigate` and cut to 4G RAM and 4 cores.
The reason still holds.

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

Debian 13, unprivileged, `onboot: 1`, with `nesting`, `keyctl`, and `/dev/net/tun` so Tailscale can run the way the
other Tailscale container does.
`cpuunits: 50`, so this guest loses to the Frigate VM when the CPUs are busy.
8 cores, 32G RAM, 512M swap, 32G rootfs on `local-lvm`.
The address is outside the DHCP pool, so there is no fixed-IP reservation.

Docker is Debian `docker.io` with Compose.
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
Checked 2026-11-18: `/health` returned ok.

Named volumes for static files, media, and Postgres are bind mounts under `/srv/htr/data`.
Docker image layers stay on the 32G rootfs (`overlay2`).
Do not move the Docker data-root onto the btrfs bind.

Each app process sits near 600MB idle because the image loads Kraken.
Hard limits: web 1536m, channelserver 1024m, celery-live and celery-low-priority 1280m, celery-main and celery-gpu
2560m, intensive inference 1536m, celery-jvm 2048m, Postgres 1536m.
A 768m web limit OOM-killed the container when a second Python process started.

The password is not in these notes.

## Tailscale

Tailscale is installed and logged in.
`/dev/net/tun` is passed in.
Login was `tailscale up` with SSH off, no subnet routes, and without accepting Tailscale DNS, so LAN DNS stays the
gateway.
`tailscale serve` fronts the UI for the tailnet only, proxying to local port 8080.
Leave Tailscale Funnel off.
Funnel would publish that URL on the public internet.
The LAN URL still answers.
Do not move this stack onto the Docker VM.

## Kraken CLI

Kraken 7.1.1 and ketos are on the container with `pipx install --global 'kraken==7.1.*'`, Python 3.13.5.
The binaries are `/usr/local/bin/kraken` and `/usr/local/bin/ketos`.
The venv is `/opt/pipx/venvs/kraken` on the 32G rootfs.
Purging `/root/.cache/pip` freed space on that small rootfs.
A Docker image prune would not have helped in any useful way and was not run.
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
