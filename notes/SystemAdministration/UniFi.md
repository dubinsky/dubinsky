---
tags:
  - computer
  - sysadmin
---
[UniFi Help](https://help.ui.com/hc/en-us/categories/200320654)

I used to run UniFi in [[Docker]] using various images:
- https://github.com/jacobalberty/unifi-docker
- https://github.com/11notes/docker-unifi
- https://github.com/linuxserver/docker-unifi-network-application

I now run it in an LXC container in [[ProxMox]].
The gateway is a USG.
The controller is UniFi OS Server in that LXC, not a cloud console and not the PoE switch.

The UI is on port **11443**, not the old **8443**.

Host units `uosserver.service` / `uosserver-updater.service` (user `uosserver`).
`uosserver-updater.service` is the app updater, and `uosserver` has no update command.
Data on the LXC: `/var/lib/uosserver/`.
App data in the container: `/usr/lib/unifi/data/`.
Inform/STUN is published via pasta: `8080`, `3478/udp`, `11443` to `443`, and `8444`, `8880`–`8882`, `5671`, `6789`,
`9543`.
That list was written 2026-11-11 and was not re-checked port by port.

## Setup

### Basic
- choose "Advanced Setup" to avoid logging into a UniFi cloud account
- Settings > System > Advanced > Inform Host > Override with the address of the machine running `unifi`
- The name chosen at first setup is not necessarily the live Network identity or the live site name.
  Check both.

### Devices

Move devices from the old controller:

Reset to the factory state with a paperclip; it may be necessary to `$ set-inform http://<controller host>:8080/inform`
on the device; default SSH credentials - ubnt/ubnt

Reset to the factory state with a `syswrapper.sh restore-default` command on the device.

### Networks

One LAN, no extra VLANs.

Two Wi‑Fi networks:

- people Wi‑Fi, currently **2.4 + 5 GHz**, because the Samsung fridge is 2.4-only and is still on this network
- IoT, **2.4 GHz only** (boiler, dishwasher, the ratgdo)

### Later (do not start until I ask)

- **Move the refrigerator to the IoT network.** SmartThings has no Wi‑Fi picker for this fridge.
  AP path (Door Alarm, hold Fridge until `AP`) tried 2026-08-19 and failed.
  Next try: power the fridge off (or unplug a minute), then AP again, with the phone on the IoT network.
  Reclaim if “already registered”.
  Do not delete the device in SmartThings first.
- Then set people Wi‑Fi back to **5 GHz only** (disable 2.4 on that SSID).
  Confirm UniFi shows the refrigerator on the IoT network first.
  Same item lives under [[Home Assistant]] TODO.

### SSH
Enable SSH for UniFi devices:
- in the UniFi Console | Settings | System | Application Configuration | Device SSH Authentication
- retrieve auto-generated SSH password
- use it: `ssh -o PubkeyAcceptedKeyTypes=ssh-rsa -o HostKeyAlgorithms=ssh-rsa -o RequiredRSASize=1024 <device-ip>` (see
  https://kcore.org/2023/03/27/ssh-unifi-fedora-37/)

## Dynamic DNS
It ran for a while on [[ProxMox]] and was never used.
It is gone, in favour of Tailscale.
See [[Domains]].

### Port forwarding

No port forwards are configured.
UPnP is off.
An old note proposed forwarding 22, 80, and 443 to a LAN host.
That was never applied.
Remote admin is Tailscale, not port forwarding.
See [[Domains]].

### Addresses

Fixed addresses for UniFi's own devices are still a TODO: the gear has addresses, and I have not written down a
procedure I trust for pinning the devices themselves.
