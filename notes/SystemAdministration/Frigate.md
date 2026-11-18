[Frigate](https://github.com/blakeblackshear/frigate) is a popular NVR; it:
- decodes video streams from cameras
- detects and publishes events
- integrates with [[Home Assistant]]

Frigate can be installed in the [[Home Assistant]] itself; I do not want to do that because Frigate can be used without
Home Assistant - and because I want to be able to record the camera feeds on a disk that does not belong to Home
Assistant.

Frigate [documentation](https://frigate.video/) advises against running Frigate in a ProxMox LXC, and suggests a VM
instead; I did try using a [community script](https://community-scripts.org/scripts/frigate) that creates an LXC as
discussed in [discussions](https://github.com/blakeblackshear/frigate/discussions/5773) - and decided to run it in my
`docker` VM because:
- it seems simpler
- two-way talk works out of the box when I use VM but not when I use LXC

My Reolink Doorbell Wi-Fi had ONVIF and RTSP enabled out-of-the-box; those settings are under
`Network | Advanced | Server`.

I used Grok to configure Frigate ;)

Private details (camera address, stream paths, zone coordinates, the image tag and rollback backups) are in the private
vault `dubinsky/dubinsky-private`, at this same path.

## Where it runs

On the Docker VM, not inside [[Home Assistant]].
Config and recordings live on the Proxmox host at `/mnt/data/apps/frigate` and are NFS-exported only to that VM (mounted
at `/frigate`, NFS4, `_netdev,nofail,x-systemd.automount`).
The UniFi reservation is on the Docker VM.
There is no separate client named `frigate`.

`restart: unless-stopped` is not enough after a VM reboot.
On 2026-09-01 **12:45 EDT** the [[ProxMox]] host reset uncleanly (not a guest `reboot`); `startall` brought docker back,
dockerd started Frigate while IPv4 to PVE was still `ENETUNREACH`, `mount.nfs` failed, bind-mount of `/frigate/config`
failed (`mkdir: no such device`), and the container stayed **exited** until 2026-09-03.
HA's Frigate integration went `setup_retry`, occupancy sensors went unavailable, and **Doorbell Notification** died (`frigate.create_event`
missing — that abort also skipped `notify.phones`).
Recovery: `docker start frigate` once NFS is mounted, then reload the HA Frigate config entry.
To survive the next reboot, `frigate-ensure.service` on the docker VM (`/usr/local/sbin/start-frigate.sh`) retries every
15 s until `/frigate` is a mountpoint and the container is running.

The UI is on port 8971, not port 5000.
The address is in the private note.

```
/mnt/data/apps/frigate/          # PVE NFS; docker mounts this at /frigate
  compose.yml                    # restart: unless-stopped; binds config + storage
  .env                           # doorbell + MQTT passwords (not in yadm)
  config/config.yaml             # cameras + go2rtc (secrets as {FRIGATE_*} placeholders)
  config/*.bak.*                 # timestamped copies after edits
  storage/                       # recordings, clips, snapshots
```

The image is pinned in `compose.yml`, not floating on `:stable`.
CPU only (no Coral).
Do not hand-edit `version` — leaving the old value is what triggers migration.
A local rollback tag and dated backups from the upgrade are on the docker VM.
The image tag, the config schema version, the rollback tag, and the backup names are in the private note.
Ports 8971, 8554 (go2rtc RTSP), 8555 WebRTC.
MQTT goes to the Home Assistant Mosquitto broker.
The host, the username, and where the password is kept are in the private note.
The password is not in either vault.
Nginx logs `using uninitialized "go2rtc_stream_proxy" variable` on API requests (including HA `/api/stats`).
Harmless: those requests still return 200, and detect/record/MQTT work.

This tree is not in yadm and not in HA Google Drive Backup.
Safe to copy off-box: `compose.yml` and `config/config.yaml`.
Do not copy `.env`, `config/.jwt_secret`, or `config/frigate.db*`.
To roll back, restore the dated config and database backups named in the private note and point compose at the local
rollback tag.
`docker compose -f /frigate/compose.yml up -d` (never `compose down`).
Later: include this dataset in PVE/PBS, or rsync the two safe files somewhere tracked.

Do **not** attach virtiofs to VM 101 (UEFI hang).
NFS only.

## Doorbell

One camera: a Reolink doorbell.
The hostname, MAC, and reservation are in the private note.
HTTP is enabled on the camera.
go2rtc takes the camera's main and sub streams.
Two-way talk is **only** on the sub stream, and that stream has no `ffmpeg:` prefix.
That is the stream the Home Assistant card uses.
Do not put talk on the main stream (one backchannel).
Frigate consumes the local go2rtc restreams.
The paths are in the private note.

Detect **on**, 480×640 on the sub stream; record+audio on main.
Review **alerts = person only**, and `review.alerts.required_zones: [stoop]`.
Zone `stoop` is the brick landing and the brick stairs, not the sidewalk or the street.
The coordinates are in the private note.
On 0.17, `objects.filters.person.required_zones` was invalid (safe-mode).
The 0.18 migrator left `review.alerts.required_zones: [stoop]` in place and added `enabled` / `friendly_name` on the
person and motion masks; coordinates were unchanged.
YAML stayed block-style (not a single-line `{...}` document).
Talk is still the raw RTSP line on `doorbell_sub` only (no `ffmpeg:` prefix, no `#hardware`).
Sidewalk people can still be detections / `doorbell_person_occupancy`; they are not Review **alerts**.
For HA phone notify to match, trigger on the stoop occupancy entity (not generic person occupancy).

A person object mask covers the left rail at the top of the stairs.
The rectangle is in the private note.
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

- The host iGPU (Intel, driver `i915`).
  The PCI id is in the private note.
- The render node exists on the hypervisor only.
- IOMMU group 0 is the GPU alone.
- DRM connectors are disconnected (the host is headless).
- GVT-g is not on this generation, and SR-IOV vGPU is not on this SKU, so this is full PCI passthrough into the Docker
  VM.
- Do **not** attach virtiofs to that VM (UEFI hang).
  NFS stays.

The Docker VM today is OVMF, `cpu: host`, no `hostpci`.
Frigate `compose.yml` already has the render node commented out.
`config.yaml` has no `ffmpeg.hwaccel_args`.
Do not pass the HDMI audio device.
Its id is in the private note.

**Blast radius:** Phase 0 needs a **PVE host reboot** (HA, docker, Cloudflare, UniFi OS all drop).
After passthrough the host has no iGPU; PVE web UI is fine; a monitor on the PVE box will stay blank.
Intel iGPU reset risk: a **guest** reboot can wedge `00:02.0` until the **host** reboots (`rombar=0`, no `x-vga`).

### Phase 0 — host IOMMU + vfio (PVE reboot)

On `pve`:

1. Backup `/etc/default/grub` and `/etc/pve/qemu-server/101.conf` (`/root/101.conf.bak.$(date +%Y%m%d%H%M%S)`).
2. GRUB: `intel_iommu=on iommu=pt`.
3. `/etc/modprobe.d/vfio.conf` binds vfio-pci to the host iGPU id from the private note, with `softdep` so `i915` and
   `xe` load after vfio-pci.
4. Load `vfio`, `vfio_pci`, `vfio_iommu_type1` at boot.
5. `update-initramfs -u && update-grub && reboot`.
6. After reboot the iGPU's kernel driver is `vfio-pci`, and it is still alone in IOMMU group 0.

HAOS USB radios stay on VM 100.

### Phase 1 — give the GPU to VM 101

1. Shut the Docker VM down.
   Do not destroy it.
2. Switch it to **q35** (needed for PCIe `hostpci`) and pass the host iGPU with `pcie=1,rombar=0`.
   The bus address is in the private note.
   Do **not** set `x-vga=1` unless the guest cannot see the device.
3. Start the VM.
   The guest should show the iGPU on `i915`, and `/dev/dri/renderD128` should exist (not only QEMU `card0`).

### Phase 2 — Frigate VAAPI

Timestamped backups of `compose.yml` and `config.yaml` on `/mnt/data/apps/frigate`:

1. Uncomment in compose:
   ```
   devices:
     - /dev/dri/renderD128:/dev/dri/renderD128
   ```
   If the container cannot open the node, `group_add` the guest `render` GID.
   Do not set `privileged` unless that fails.
2. In `config.yaml` ffmpeg: `hwaccel_args: preset-vaapi` (fallback `preset-intel-qsv-h264`).
3. `docker compose -f /frigate/compose.yml up -d` — do not `compose down`.
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
