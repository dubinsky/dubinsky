[Frigate](https://github.com/blakeblackshear/frigate) is a popular NVR; it:
- decodes video streams from cameras
- detects and publishes events
- integrates with [[Home Assistant]]

Frigate can be installed in the [[Home Assistant]] itself; I do not want to do that because Frigate can be used without Home Assistant - and because I want to be able to record the camera feeds on a disk that does not belong to Home Assistant.

Frigate [documentation](https://frigate.video/) advises against running Frigate in a ProxMox LXC, and suggests a VM instead; I did try using a [community script](https://community-scripts.org/scripts/frigate) that creates an LXC as discussed in [discussions](https://github.com/blakeblackshear/frigate/discussions/5773) - and decided to run it in my `docker` VM because:
- it seems simpler
- two-way talk works out of the box when I use VM but not when I use LXC

My Reolink Doorbell Wi-Fi had ONVIF and RTSP enabled out-of-the-box; those settings are under `Network | Advanced | Server`.

I used Grok to configure Frigate ;)

## iGPU passthrough (planned, not done)

Do **not** execute this until I ask. One 480×640 doorbell at 5 fps is fine on CPU. This is headroom for more cameras, and it would silence Frigate’s `Did not detect hwaccel` / `CPU detectors are not recommended` warnings.

The GPU to pass is the **[[ProxMox]] host** iGPU, not the Arrow Lake desktop:

- Intel AlderLake-S GT1 `8086:4680` at `0000:00:02.0`, driver `i915`
- `/dev/dri/renderD128` exists on the hypervisor only
- IOMMU **group 0 is the GPU alone** (good)
- DRM connectors are disconnected (PVE is headless)
- GVT-g is not on 12th-gen; SR-IOV vGPU is not on this GT1 SKU → **full PCI passthrough** of `00:02.0` into VM 101
- Do **not** attach virtiofs to 101 (UEFI hang). NFS stays.

VM 101 today: OVMF, `cpu: host`, 16 cores, 32G, **i440fx** (no `machine:` line), QEMU stdvga `1234:1111`, no `hostpci`. Frigate `compose.yml` already has `/dev/dri/renderD128` commented out. `config.yaml` has no `ffmpeg.hwaccel_args`. Do not pass HDMI audio `00:1f.3`.

**Blast radius:** Phase 0 needs a **PVE host reboot** (HA, docker, Cloudflare, UniFi OS all drop). After passthrough the host has no iGPU; PVE web UI is fine; a monitor on the PVE box will stay blank. Intel iGPU reset risk: a **guest** reboot can wedge `00:02.0` until the **host** reboots (`rombar=0`, no `x-vga`).

### Phase 0 — host IOMMU + vfio (PVE reboot)

On `pve`:

1. Backup `/etc/default/grub` and `/etc/pve/qemu-server/101.conf` (`/root/101.conf.bak.$(date +%Y%m%d%H%M%S)`).
2. GRUB: `intel_iommu=on iommu=pt`.
3. `/etc/modprobe.d/vfio.conf`:
   ```
   options vfio-pci ids=8086:4680
   softdep i915 pre: vfio-pci
   softdep xe pre: vfio-pci
   ```
4. Load `vfio`, `vfio_pci`, `vfio_iommu_type1` at boot.
5. `update-initramfs -u && update-grub && reboot`.
6. After reboot: `00:02.0` is `Kernel driver in use: vfio-pci` and still alone in IOMMU group 0.

HAOS USB radios stay on VM 100.

### Phase 1 — give the GPU to VM 101

1. `qm shutdown 101`. Do not `qm destroy`.
2. Switch to **q35** (needed for PCIe `hostpci`):
   ```
   qm set 101 -machine q35
   qm set 101 -hostpci0 0000:00:02.0,pcie=1,rombar=0
   ```
   Do **not** set `x-vga=1` unless the guest cannot see the device.
3. `qm start 101`. Guest-agent: `lspci` shows `8086:4680` with `i915`, and `/dev/dri/renderD128` exists (not only QEMU `card0`).

### Phase 2 — Frigate VAAPI

Timestamped backups of `compose.yml` and `config.yaml` on `/mnt/data/apps/frigate`:

1. Uncomment in compose:
   ```
   devices:
     - /dev/dri/renderD128:/dev/dri/renderD128
   ```
   If the container cannot open the node, `group_add` the guest `render` GID. Do not set `privileged` unless that fails.
2. In `config.yaml` ffmpeg: `hwaccel_args: preset-vaapi` (fallback `preset-intel-qsv-h264`).
3. `docker compose -f /frigate/compose.yml up -d` — do not `compose down`.
4. Check: logs no longer say `Did not detect hwaccel`; System page shows VAAPI; `docker exec frigate vainfo` lists iHD; doorbell still records/detects; Talk stays on `doorbell_sub` only.

### Phase 3 — OpenVINO (later, after VAAPI is boring)

```yaml
detectors:
  ov:
    type: openvino
    device: GPU
```

Keep a `config.yaml.bak.*` so CPU tflite is one restart away.

### Rollback

1. Comment the compose device bind, remove `hwaccel_args` (and OpenVINO); recreate the container — CPU Frigate still works with the GPU passed in.
2. Full undo: shutdown 101, delete `hostpci0`, restore host `vfio.conf`/GRUB, reboot PVE so `i915` owns `00:02.0` again.

Cutover when I ask: Phases 0–2 in one evening window. Phase 3 on a later day.

