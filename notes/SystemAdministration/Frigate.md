[Frigate](https://github.com/blakeblackshear/frigate) is a popular NVR; it:
- decodes video streams from cameras
- detects and publishes events
- integrates with [[Home Assistant]]

Frigate can be installed in the [[Home Assistant]] itself; I do not want to do that because Frigate can be used without
Home Assistant - and because I want to be able to record the camera feeds on a disk that does not belong to Home
Assistant.

Frigate [documentation](https://frigate.video/) advises against running Frigate in a ProxMox LXC, and suggests a VM
instead; I did try using a [community script](https://community-scripts.org/scripts/frigate) that creates an LXC as
discussed in [discussions](https://github.com/blakeblackshear/frigate/discussions/5773) - and decided to run it on a VM
because:
- it seems simpler
- two-way talk works out of the box when I use VM but not when I use LXC

My Reolink Doorbell Wi-Fi had ONVIF and RTSP enabled out-of-the-box; those settings are under
`Network | Advanced | Server`.

I used Grok to configure Frigate ;)

## Where it runs

On the Frigate VM (Proxmox name `frigate`, guest hostname `frigate`, `ssh frigate`), not inside [[Home Assistant]].
Config and the database live on the VM's own disk at `/srv/frigate`.
Only recordings are on the Proxmox host: `/mnt/data/apps/frigate` is NFS-exported only to that VM (mounted at `/frigate`,
NFS4, `_netdev,nofail,x-systemd.automount`) and Frigate uses its `storage/`.
Until 2026-11 the config and `frigate.db` were on that share too.
I moved them because Frigate's docs warn against keeping its SQLite database on network storage (locking, corruption),
and the recordings are what needs the big disk.
The UniFi reservation is that same DHCP client.
A new client named `frigate` would be a different reservation.

`restart: unless-stopped` is not enough after a VM reboot.
On 2026-09-01 **12:45 EDT** the [[ProxMox]] host reset uncleanly (not a guest `reboot`); `startall` brought docker back,
dockerd started Frigate while IPv4 to PVE was still `ENETUNREACH`, `mount.nfs` failed, bind-mount of `/frigate/config`
failed (`mkdir: no such device`), and the container stayed **exited** until 2026-09-03.
HA's Frigate integration went `setup_retry`, occupancy sensors went unavailable, and **Doorbell Notification** died (`frigate.create_event`
missing — that abort also skipped `notify.phones`).
Recovery: `docker start frigate` once NFS is mounted, then reload the HA Frigate config entry.
To survive the next reboot, `frigate-ensure.service` on this VM (`/usr/local/sbin/start-frigate.sh`) retries every 15 s
until `/frigate` is a mountpoint and the container is running.
It still waits for the mount after the move: without it there is nowhere to record.

The UI is on port 8971, not port 5000.

```
/srv/frigate/                    # on the VM's own disk
  compose.yml                    # restart: unless-stopped; binds config + /frigate/storage
  .env                           # doorbell + MQTT passwords (mode 600; never in git)
  config/config.yaml             # cameras + go2rtc (secrets as {FRIGATE_*} placeholders)
  config/frigate.db              # SQLite: events, review, recordings index
/frigate/                        # NFS from PVE /mnt/data/apps/frigate
  storage/                       # recordings, clips, snapshots
```

The image is pinned to a specific Frigate release in `compose.yml`, not `:stable`; the current version is in the
inventory.
CPU only (no Coral).
Config is `version: 0.18-0` after Frigate's auto-migration.
Do not hand-edit `version` — leaving the old value is what triggers migration.
Before an upgrade, keep the previous image on the VM and dated copies of `compose.yml`, `config.yaml` and `frigate.db`.
Ports 8971, 8554 (go2rtc RTSP), 8555 WebRTC.
MQTT goes to the Home Assistant Mosquitto broker.
The password is not in these notes.
Nginx logs `using uninitialized "go2rtc_stream_proxy" variable` on API requests (including HA `/api/stats`).
Harmless: those requests still return 200, and detect/record/MQTT work.

Backups: `compose.yml` and `config/config.yaml` have no secrets in them, so copies are kept in git in the private
`dubinsky/homelab` inventory repository (`frigate-config/`); after editing either on the VM, copy it there.
`.env`, `config/.jwt_secret` and `config/frigate.db*` are never copied off the VM; the two passwords in `.env` belong in my
password manager.
The database is covered only by Proxmox snapshots of the VM; losing it loses event history, not recordings.
Recordings are not backed up.
Nothing here is in yadm or HA Google Drive Backup.
To roll back an upgrade: put the previous image back in `compose.yml`, restore the dated `config.yaml` and `frigate.db`
if the new version migrated them, and run `docker compose -f /srv/frigate/compose.yml up -d` (never `compose down`).
The exact backup and snapshot names are in the inventory.

Do **not** attach virtiofs to VM 101 (UEFI hang).
NFS only.

## Doorbell

One camera: a Reolink doorbell.
HTTP is enabled on the camera.
go2rtc takes the camera's main and sub streams as HTTP-FLV: `channel0_main.bcs` and `channel0_ext.bcs`.
Two-way talk is **only** on the sub stream (`Preview_01_sub`), and that stream has no `ffmpeg:` prefix.
That is the stream the Home Assistant card uses.
Do not put talk on the main stream (one backchannel).
Frigate consumes the local go2rtc restreams `doorbell_main` and `doorbell_sub` on `127.0.0.1:8554`.

Detect **on**, 480×640 on the sub stream; record+audio on main.
Review **alerts = person only**, and `review.alerts.required_zones: [stoop]`.
Zone `stoop` is the brick landing and the brick stairs, not the sidewalk or the street.
On 0.17, `objects.filters.person.required_zones` was invalid (safe-mode).
The 0.18 migrator left `review.alerts.required_zones: [stoop]` in place and added `enabled` / `friendly_name` on the
person and motion masks; coordinates were unchanged.
YAML stayed block-style (not a single-line `{...}` document).
Talk is still the raw RTSP line on `doorbell_sub` only (no `ffmpeg:` prefix, no `#hardware`).
Sidewalk people can still be detections / `doorbell_person_occupancy`; they are not Review **alerts**.
For HA phone notify to match, trigger on the stoop occupancy entity (not generic person occupancy).

A person object mask covers the left rail at the top of the stairs.
A dark post there was detected as a person (score about 0.57), and the box bottom jumped between the stoop edge and the
upper step.
That made `binary_sensor.stoop_person_occupancy` chatter, and Doorbell Notification fired every few seconds
(2026-10-07).
The mask drops a person whose bottom-center lands on that rail.
Feet on the center of the walkway and on the lower steps stay outside it.

Recording: motion 7 days, alerts/detections 14 days.
Snapshots 14 days.
Button press is Reolink; HA `frigate.create_event` (`visitor`, 30s) plus notify image
`/api/frigate/notifications/<event_id>/snapshot.jpg`.
Person notify uses `/api/frigate/doorbell/person/snapshot.jpg`.
Do not URL-encode the doorbell password in the FLV query string.

To tweak the polygon: Frigate UI → doorbell → Masks / Zones, or edit `coordinates` (relative 0–1).
Restart Frigate after YAML changes.

## iGPU passthrough (planned, not done)

Do **not** execute this until I ask.
One 480×640 doorbell at 5 fps is fine on CPU.
This is headroom for more cameras, and it would silence Frigate’s `Did not detect hwaccel` /
`CPU detectors are not recommended` warnings.

The GPU to pass is the **[[ProxMox]] host** iGPU, not the Arrow Lake desktop:

- The host iGPU is Intel Alder Lake-S GT1, device `8086:4680`, at `0000:00:02.0` on this host, driver `i915`.
- The render node exists on the hypervisor only.
- IOMMU group 0 is the GPU alone.
- DRM connectors are disconnected (the host is headless).
- GVT-g is not on this generation, and SR-IOV vGPU is not on this SKU, so this is full PCI passthrough into the Frigate
  VM.
- Do **not** attach virtiofs to that VM (UEFI hang).
  NFS stays.

The Frigate VM today is OVMF, `cpu: host`, 4G RAM, 4 cores, no `hostpci`.
Frigate `compose.yml` already has the render node commented out.
`config.yaml` has no `ffmpeg.hwaccel_args`.
Do not pass the HDMI audio device (`00:1f.3` on this host).

**Blast radius:** Phase 0 needs a **PVE host reboot** (HA, docker, Cloudflare, UniFi OS all drop).
After passthrough the host has no iGPU; PVE web UI is fine; a monitor on the PVE box will stay blank.
Intel iGPU reset risk: a **guest** reboot can wedge `00:02.0` until the **host** reboots (`rombar=0`, no `x-vga`).

### Phase 0 — host IOMMU + vfio (PVE reboot)

On `pve`:

1. Backup `/etc/default/grub` and `/etc/pve/qemu-server/101.conf` (`/root/101.conf.bak.$(date +%Y%m%d%H%M%S)`).
2. GRUB: `intel_iommu=on iommu=pt`.
3. `/etc/modprobe.d/vfio.conf` binds vfio-pci to `8086:4680`, with `softdep` so `i915` and `xe` load after vfio-pci.
   On this host that line is `options vfio-pci ids=8086:4680`.
4. Load `vfio`, `vfio_pci`, `vfio_iommu_type1` at boot.
5. `update-initramfs -u && update-grub && reboot`.
6. After reboot the iGPU's kernel driver is `vfio-pci`, and it is still alone in IOMMU group 0.

HAOS USB radios stay on VM 100.

### Phase 1 — give the GPU to VM 101

1. Shut the Frigate VM down.
   Do not destroy it.
2. Switch it to **q35** (needed for PCIe `hostpci`) and pass the host iGPU with `pcie=1,rombar=0`.
   On this host: `qm set 101 -machine q35` and `qm set 101 -hostpci0 0000:00:02.0,pcie=1,rombar=0`.
   Do **not** set `x-vga=1` unless the guest cannot see the device.
   The guest's existing VGA is QEMU stdvga (`1234:1111`).
   The passed GPU should show up beside it.
3. Start the VM.
   The guest should show the iGPU on `i915`, and `/dev/dri/renderD128` should exist (not only QEMU `card0`).

### Phase 2 — Frigate VAAPI

Timestamped backups of `compose.yml` and `config.yaml` in `/srv/frigate` on the VM:

1. Uncomment in compose:
   ```
   devices:
     - /dev/dri/renderD128:/dev/dri/renderD128
   ```
   If the container cannot open the node, `group_add` the guest `render` GID.
   Do not set `privileged` unless that fails.
2. In `config.yaml` ffmpeg: `hwaccel_args: preset-vaapi` (fallback `preset-intel-qsv-h264`).
3. `docker compose -f /srv/frigate/compose.yml up -d` — do not `compose down`.
4. Check: logs no longer say `Did not detect hwaccel`; System page shows VAAPI; `docker exec frigate vainfo` lists iHD;
   doorbell still records/detects; Talk stays on `doorbell_sub` only.

### Phase 3 — OpenVINO (later, after VAAPI is boring)

```yaml
detectors:
  ov:
    type: openvino
    device: GPU
```

Keep a `config.yaml.bak.*` so CPU tflite is one restart away.

### Rollback

1. Comment the compose device bind, remove `hwaccel_args` (and OpenVINO); recreate the container — CPU Frigate still
   works with the GPU passed in.
2. Full undo: shutdown 101, delete `hostpci0`, restore host `vfio.conf`/GRUB, reboot PVE so `i915` owns `00:02.0` again.

Cutover when I ask: Phases 0–2 in one evening window.
Phase 3 on a later day.
