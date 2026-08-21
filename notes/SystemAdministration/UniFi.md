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

I now run it in a LXC container in [[ProxMox]] :) Guest **105** (`unifi-os-server`, Debian 13) at `192.168.1.184` (`unifi-os-server.lan.podval.org`). UI: https://192.168.1.184:11443 (**not** :8443). **192.168.1.245** is the USW-Pro-24-PoE, not the controller.

As of 2026-08: UniFi OS Server **5.1.21**, Network **10.5.67**. Host units `uosserver.service` / `uosserver-updater.service` (user `uosserver`). Data on the LXC: `/var/lib/uosserver/`. App data in the container: `/usr/lib/unifi/data/`. Inform/STUN published via pasta: `8080`, `3478/udp`, `11443→443`, plus 8444/8880–8882/5671/6789/9543.

## Setup

### Basic
- name: k39-3
- choose "Advanced Setup" to avoid logging into Unifi UI account
- Settings > System > Advanced > Inform Host > Override with the IP address of the machine running `unifi`

### Devices

Move devices from the old controller:

Reset to the factory state with a paperclip; it may be necessary to `$ set-inform http://<controller host>:8080/inform` on the device; default SSH credentials - ubnt/ubnt

Reset to the factory state with a `syswrapper.sh restore-default` command on the device.

### Networks

- LAN: 192.168.1.0/24
	- Settings | Network | Default | Advanced | Manual | DHCP | DHCP Service Management | Domain Name: "lan.podval.org"
- WiFi:
	- `podval-u`: people Wi‑Fi; currently **2.4 + 5 GHz** so the Samsung fridge can stay online (2.4-only, still on this SSID at `.113`)
	- `podval-2g`: **2.4 GHz IoT** (boiler, dishwasher; ratgdo `.240` is already here)
- One LAN, no extra VLANs. DHCP pool `192.168.1.100–199`.

### Later (do not start until I ask)

- **Move refrigerator to `podval-2g`.** SmartThings has no Wi‑Fi picker for this fridge. AP path (Door Alarm, hold Fridge until `AP`) tried 2026-08-19 and failed. Next try: power the fridge off (or unplug a minute), then AP again; phone on `podval-2g`; Reclaim if “already registered”. Do not delete the device in SmartThings first.
- Then set `podval-u` back to **5 GHz only** (disable 2.4 on that SSID). Confirm UniFi shows `refrigerator` on `podval-2g` first. Same item lives under [[Home Assistant]] TODO.

### SSH
Enable SSH for UniFi devices:
- in the UniFi Console | Settings | System | Application Configuration | Device SSH Authentication
- retrieve auto-generated SSH password
- use it: `ssh -o PubkeyAcceptedKeyTypes=ssh-rsa -o HostKeyAlgorithms=ssh-rsa -o RequiredRSASize=1024 192.168.1.157` (see https://kcore.org/2023/03/27/ssh-unifi-fedora-37/)

## Dynamic DNS
see [[ProxMox#Dynamic DNS]]

### Port forwarding

TODO
I'in the UniFi Console | Settings | Security | Port Forwarding, forward to the `gatekeeper`:
- 22 - ssh
- 80 - http
- 443 - https
### Static Addresses

Addresses on the 192.168.1.* network (*.lan.podval.org).

TODO HOW DO I ASSIGN STATIC ADDRESSES TO UniFi DEVICES?

UniFi devices as of 2026-08 (site `default`, LAN `192.168.1.1/24`). Public WAN on the USG was `73.143.105.42` (same IP `cloudflare-ddns` tracks for `k39.podval.org`).

| Type | Model | Name | Address |
|---|---|---|---|
| ugw | UGW3 | USG 3P | WAN 73.143.105.42 / LAN .1 |
| uap | U7PG2 | AC Pro | 192.168.1.72 |
| uap | U7NHD | Nano HD | 192.168.1.161 |
| usw | US8P60 | US 8 60W | 192.168.1.210 |
| usw | USPM24P | USW Pro Max 24 PoE | 192.168.1.245 |

Older DHCP/static list (some APs/switches have since moved):

| Address | Name                     |
|---------|--------------------------|
| 1       | USG-3P (gateway)         |
| 2       | dub                      |
| 4       | OBi202                   |
| 21      | printer                  |
| 22      | printer-colour2          |
| 30      | TV                       |
| 31      | gatekeeper               |
| 32      | gatekeeper-wifi          |
| 33      | dub-phone                |
| 34      | nina                     |
| 35      | (nina-wifi)              |
| 36      | nina-phone               |
| 40      | bedroom speaker          |
| 71      | UAP-nanoHD (now .161)    |
| 72      | UAP-AC-Pro               |
| 78      | dub-wifi                 |
| 110     | Reolink doorbell (Front) |
| 113     | refrigerator (`podval-u`)|
| 156     | US-8-60W (now .210)      |
| 158     | Viessmann-2224 podval-2g |
| 184     | unifi-os-server          |
| 187     | docker                   |
| 200     | turingpi                 |
| 201     | cube1                    |
| 202     | cube2                    |
| 203     | cube3 (stale for doorbell)|
| 204     | cube4                    |
| 209     | homeassistant            |
| 235     | cloudflare-ddns          |
| 236     | cloudflared              |
| 240     | ratgdo (`podval-2g`)     |
