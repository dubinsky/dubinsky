[Frigate](https://github.com/blakeblackshear/frigate) is a popular NVR; it:
- decodes video streams from cameras
- detects and publishes events
- integrates with [[Home Assistant]]

Frigate can be installed in the [[Home Assistant]] itself; I do not want to do that because Frigate can be used without Home Assistant -and because I want to be able to record the camera feeds on a disk that does not belong to Home Assistant.

Frigate [documentation](https://frigate.video/) advises against running Frigate in a ProxMox LXC, and suggests a VM instead; I did try using a [community script](https://community-scripts.org/scripts/frigate) that creates an LXC as discussed in [discussions](https://github.com/blakeblackshear/frigate/discussions/5773) - and decided to run it in my `docker` VM because:
- it seems simpler
- two-way talk works out of the box when I use VM but not when I use LXC

[[TODO]] stop microphone from hijacking the video with two-way talk

Frigate uses MQTT to publish events; for Home Assistant to feel them, MQTT instance Frigate publishes to has to be the same Home Assistant uses. I'll try to expose and re-use the instant running in my Home Assistant, but it may be cleaner to run MQTT stand-alone on Proxmox.

My Reolink Doorbell Wi-Fi had ONVIF and RTSP enabled out-of-the-box; those settings are under `Network | Advanced | Server`.

Frigate documentation [recommends](https://docs.frigate.video/configuration/camera_specific/#reolink-cameras) setting on Reolink cameras:
- `Device | Stream | Interframe Interval` (which it calls `Interframe Space`) to `1x` for each stream;
- `Device | Stream | Frame Rate Mode` to `Constant`;

When adding Reolink camera I specified:
- IP address
- ONVIF port 8000
- user `admin` and its password


