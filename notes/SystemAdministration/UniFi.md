---
title: UniFi
tags:
  - computer
  - sysadmin
---
[UniFi Help](https://help.ui.com/hc/en-us/categories/200320654)

I used to run UniFi in [[Docker]] using various images:
- https://github.com/jacobalberty/unifi-docker
- https://github.com/11notes/docker-unifi
- https://github.com/linuxserver/docker-unifi-network-application

I now run it in a LXC container in [[ProxMox]] :)
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
	- podval-u: all the bands
	- podval-2g: 2G only

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

| Address | Name                     |
| ------- | ------------------------ |
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
| 71      | UAP-nanoHD               |
| 72      | UAP-AC-Pro               |
| 78      | dub-wifi                 |
| 156     | US-8-60W                 |
| 158     | Viessmann-2224 podval-2g |
| 200     | turingpi                 |
| 201     | cube1                    |
| 202     | cube2                    |
| 203     | cube3                    |
| 204     | cube4                    |
