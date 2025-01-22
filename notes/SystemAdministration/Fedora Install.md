---
title: Fedora Install
tags:
  - computer
  - sysadmin
---
Upgrade in-place: https://docs.fedoraproject.org/en-US/quick-docs/upgrading-fedora-offline/

* TOC
{:toc}

## hostname
```shell
$ sudo hostnamectl set-hostname dub.lan.podval.org
```

## Update
```shell
$ sudo dnf update
```

## Gnome Settings
In `Settings | Keyboard |`:
- In `Input Sources` add layout `Russian (phonetic)`
- In `Keyboard Shortcuts`:
    - set `Switch windows` to `Alt-Tab` (by default, it is `Switch applications`)
    - set `Switch to next input source` to `Alt-Space`

Starting with Fedora 38, GDM suspends the machine; to turn this off - say, for a server - do:
```shell
$ sudo -u gdm dbus-run-session gsettings set org.gnome.settings-daemon.plugins.power sleep-inactive-ac-timeout 0
```
(see https://discussion.fedoraproject.org/t/gnome-suspends-after-15-minutes-of-user-inactivity-even-on-ac-power/79801) - and in Fedora 41 it no longer works...
## Chrome

- Add `Google Chrome` YUM repository (download and install Chrome)
- Install: `$ sudo dnf install google-chrome-stable`
- Settings | Apps | Default Apps | Web - choose Chrome!
- Install Gnome Shell Integration Chrome Extension
- Install Gnome Extensions from Chrome:
  - Apps Menu
  - Dash to Dock
  - Places Status Indicator
  - TopHat


## Google Cloud SDK
Install:
```shell
$ sudo tee -a /etc/yum.repos.d/google-cloud-sdk.repo << EOM
[google-cloud-cli]
name=Google Cloud CLI
baseurl=https://packages.cloud.google.com/yum/repos/cloud-sdk-el9-x86_64
enabled=1
gpgcheck=1
repo_gpgcheck=0
gpgkey=https://packages.cloud.google.com/yum/doc/yum-key.gpg
       https://packages.cloud.google.com/yum/doc/rpm-package-key.gpg
EOM

$ sudo dnf install google-cloud-cli google-cloud-cli-app-engine-java
```

Transplant:
  - `~/.gsutils`

## Calibre
Install:
```shell
$ sudo -v && wget -nv -O- https://download.calibre-ebook.com/linux-installer.sh | sudo sh /dev/stdin
```
   
## node
Install: 
```shell
$ sudo rpm -e nodejs nodejs-full-i18n
$ curl -sL https://rpm.nodesource.com/setup_18.x | sudo bash -
$ sudo dnf install nodejs yarnpkg
```

## Install Packages
### System
```shell
$ sudo dnf install mc
$ sudo dnf install memtest86+
$ sudo dnf install btrfs-assistant
$ sudo dnf install direnv
$ sudo dnf install java-21-openjdk-devel
$ sudo dnf install solaar            # Logitech mice
$ sudo dnf install rclone
$ sudo dnf install syncthing
$ sudo dnf install vlc
$ sudo dnf install steam
$ sudo dnf install chirp             # handheld radio
$ sudo dnf install git-filter-repo
$ sudo dnf group install "C Development Tools and Libraries"
```
### Nina
```shell
$ sudo dnf install gimp
$ sudo dnf install inkscape
($ sudo dnf install kolourpaint)
($ sudo dnf install krita)
($ sudo dnf install mypaint)
# WebStorm
GDevelop
(OBS Studion)
# Skype Zoom Telegram
```

## DNSMASQ
  To use dnsmasq started by the NetworkManager, under /etc/NetworkManager/:
  - In /conf.d/dnsmasq.conf:
  ```
  [main]
  dns=dnsmasq
    # addn-hosts=/etc/hosts # to read /etc/hosts
  ```
  - In /dnsmasq.d/no-conflicts.conf:
    - To avoid conflicts with another DNS server (e.g., another copy of dnsmasq):
  ```
  listen-address=127.0.0.1
  bind-interfaces
  domain-needed
  ```
  - libvirtd runs internal dnsmasq that *MAY* be a problem; to disable:
```
virsh net-autostart --network default --disable
```

(see https://docs.openstack.org/mitaka/networking-guide/misc-libvirt.html)]
  
## Manually Install

[[Docker]]
[[Obsidian]]
[[Zotero]]
[[Keyboards]] Utilities
Solaar autostart
devpod
pulumi
### Printers
- https://wiki.debian.org/CUPSDriverlessPrinting
- Brother drivers (using their tool) for MFC-J5930DW: http://support.brother.com/g/b/downloadend.aspx?c=us&lang=en&prod=mfcj5930dw_us_eu&os=127&dlid=dlf006893_000&flang=4&type3=625
- Configure printers.
- Epson scanner thing: iscan-gt-x770-bundle-1.0.1.x64.rpm

### JetBrains
- JetBrains Toolbox
- IntelliJ (it installs the license).

