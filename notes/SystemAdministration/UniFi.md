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
Private details (address, SSIDs, the device list, DHCP reservations) are in the private vault
`dubinsky/dubinsky-private`, at this same path.

The UI is on port **11443**, not the old **8443**.
The PoE switch is not the controller.
Versions and the LAN address are in the private note.

Host units `uosserver.service` / `uosserver-updater.service` (user `uosserver`).
`uosserver-updater.service` is the app updater, and `uosserver` has no update command.
Data on the LXC: `/var/lib/uosserver/`.
App data in the container: `/usr/lib/unifi/data/`.
Inform/STUN is published via pasta.
The port list is in the private note.

## Setup

### Basic
- choose "Advanced Setup" to avoid logging into a UniFi cloud account
- Settings > System > Advanced > Inform Host > Override with the address of the machine running `unifi`
- The name used at first setup, and whether it is still the live name, is in the private note.

### Devices

Move devices from the old controller:

Reset to the factory state with a paperclip; it may be necessary to `$ set-inform http://<controller host>:8080/inform`
on the device; default SSH credentials - ubnt/ubnt

Reset to the factory state with a `syswrapper.sh restore-default` command on the device.

### Networks

One LAN, no extra VLANs.
The subnet, the DHCP pool, and the LAN domain are in the private note.

Two Wi‑Fi networks:

- people Wi‑Fi, currently **2.4 + 5 GHz**, because the Samsung fridge is 2.4-only and is still on this network
- IoT, **2.4 GHz only** (boiler, dishwasher, the ratgdo)

The SSID strings are in the private note.

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
Retired 2026-11-18 with the [[ProxMox]] dynamic-DNS container.
The DHCP reservation was removed.
There is no public DDNS name.
Remote access is the Tailscale container.
See [[Domains]].

### Port forwarding

No port forwards are configured (checked 2026-11-18).
UPnP is off.
An old note proposed some.
That list is in the private note, and it was never applied.
Remote admin is not port forwarding.
See [[Domains]].

### Addresses

The hardware list, DHCP reservations, and the WAN address are in the private note.
Fixed addresses for UniFi's own devices are still a TODO: the gear has addresses, and I have not written down a
procedure I trust for pinning the devices themselves.
