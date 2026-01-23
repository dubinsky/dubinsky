---
title: Home Assistant
tags:
  - sysadmin
---
* TOC
{:toc}

I heard about [Home Assistant](https://www.home-assistant.io/) home automation project before: open source, integrates with [everything](https://www.home-assistant.io/integrations/)... But it wasn't until I replaced my HVAC system recently that I decided to see what it can do. And it is great!
## Installation

Home Assistant documentation describes a number of ways to [install](https://www.home-assistant.io/installation/) it.

### Dedicated Hardware - not the one
One approach is to use dedicated Raspberry Pi-based hardware, and the purpose-built units offered for sale look great; this is probably the best way for those who do not have a home server; I do though, and prefer to run Home Assistant on it - along with other things.

### Docker Containers - not the one
One way to run Home Assistant on a machine not dedicated to it is to use Docker containers; this installation method is called [Home Assistant Container](https://www.home-assistant.io/installation/linux#docker-compose). Setting up Home Assistant this way is simple enough - here is the `docker-compose.yaml`:
```yaml    
version: '3'
services:
  homeassistant:
    restart: unless-stopped
    container_name: homeassistant
    image: "ghcr.io/home-assistant/home-assistant:stable"
    privileged: true
    network_mode: host
    volumes:
      - /home/homeassistant/config:/config
      - /etc/localtime:/etc/localtime:ro
      - /run/dbus:/run/dbus:ro
```

One issue with this approach is that there is no one-click updates: new image needs to be pulled explicitly with a Docker command; another is - no access to add-ons: each add-on needs to be installed manually using a separate `docker-compose.yaml`

Here, for example, is one for Z-Wave:
```yaml
version: '3.7'
services:
  zwave-js-ui:
    container_name: zwave-js-ui
    image: zwavejs/zwave-js-ui:latest
    restart: unless-stopped
    tty: true
    stop_signal: SIGINT
    environment:
      - SESSION_SECRET=...
      - ZWAVEJS_EXTERNAL_CONFIG=/usr/src/app/store/.config-db
    networks:
      - zwave
    devices:
      # Do not use /dev/ttyUSBX serial devices,
      # as those mappings can change over time.
      # Instead, use the /dev/serial/by-id/X serial device for your Z-Wave stick.
      - '/dev/serial/by-id/...:/dev/zwave'
    volumes:
      - '/home/homeassistant/zwave-js-ui/config:/usr/src/app/store:Z'
    ports:
      - '8091:8091' # port for web interface
      - '3000:3000' # port for Z-Wave JS websocket server
networks:
  zwave:
```

(Instead of the UI-less minimalist image https://hub.docker.com/r/kpine/zwave-js-server, I went with the official image that also has a very nice UI: https://github.com/zwave-js/zwave-js-ui/blob/master/docker/docker-compose.yml. Note: `zwave-js-ui` used to be called `jwavejsmqtt`; some [instructions]( https://www.homeautomationguy.io/blog/docker-tips/installing-z-wave-js-with-docker-and-home-assistant) still use the old name...)

With `zwave-js-ui` now running on port 8091, I followed [instructions](https://zwave-js.github.io/zwave-js-ui/#/homeassistant/homeassistant-officia): 
- in Settings / Home Assistant, enabled "WS Server"
- kept MQTT discovery disabled
- disabled MQTT gateway
- used circular arrows icons to generate random keys (S2_Unauthenticated etc.)

It [looks like](https://www.home-assistant.io/integrations/zwave_js) I [do not need to do anything](https://zwave-js.github.io/zwave-js-ui//#/getting-started/quick-start) about Home Assistant Z-Wave add-on; on the Home Assistant side, I added Z-Wave integration with the default web-socket settings - and that's it!

Appealing to the control freaks who love doing everything themselves, this approach quickly becomes inconvenient - one needs to hand-craft or find a working `docker-compose.yaml` file for each add-on, and then manually update them all. For those who value convenience, better approach is to let Home Assistant itself take care of the add-on management and updates: it does use Docker internally, and provides UI for managing the add-ons and updating everything. This means - install Home Assistant OS.

So, I want to run Home Assistant OS on a dedicated machine without dedicating my home server to it and without any additional hardware. This means - virtual machines!

### Virtual Machine - the one
#### Manual
One approach is to create Home Assistant virtual machine using `virt-manager`.

For it to be accessible from other machines on my network (including my phone), I need a special network setup; see "Network" in [[Virtual Machines]]. In the following, network interface set up for this purpose is named "mac0".

Official [instructions](https://www.home-assistant.io/installation/alternative) for installing Home Assistant in a virtual machine:
- `wget` the `KVM` (`.qcow2`) image from the [list](https://www.home-assistant.io/installation/alternative#download-the-appropriate-image) to `/home/homeassistant`;
- uncompress resulting file with: `unxz haos_ova-13.1.qcow2.xz`;
- In `virt-manager`: create a new virtual machine;
- "Import existing disk image": browse to the downloaded and uncompressed image;
- "Generic or unknown OS. Usage is not recommended.";
- RAM: 8192 megabytes, 4 CPUs;
- "Customize configuration before install.";
- "Network selection: Macvtap device.": mac0
- Overview / Firmware / UEFI x86_64: /usr/share/edk2/ovmf/OVMF_CODE.fd
- Add Hardware / Channel: device type "unix"; org.qemu.guest_agent.0
- "Begin Installation"

For the various USB dongles (Z-Wave, Zigbee, RTL) to be visible to the Home Assistant inside the virtual machine, they need to be added in "Add Hardware" of the `virt-manager`.

(Bluetooth controller did not work for me though, which is fine really; see https://www.reddit.com/r/VFIO/comments/wbsqy1/how_to_fix_onboard_intel_bluetooth_error_code_10/)

#### ProxMox
Just as running Home Assistant in a virtual machine is more convenient than managing its components manually, it is more convenient to run virtual machines using [[ProxMox]] than setting them up manually:
- ProxMox takes care of the network setup that exposes the virtual machines to the local network using a [bridge](https://pve.proxmox.com/wiki/Network_Configuration#_default_configuration_using_a_bridge)
- there are [community scripts](https://community-scripts.github.io/ProxmoxVE/) that automate setting up virtual machines, including one for the [Home Assistant](https://community-scripts.github.io/ProxmoxVE/scripts?id=haos-vm)
- ProxMox exposes a web-based management UI, so there is no need to run `virt-manager` UI over SSH

## Add-Ons and Integrations
With Home Assistant installed in a virtual machine, adding add-ons is trivial. I use:
- [File Editor](https://github.com/home-assistant/addons/tree/master/configurator)
- [Home Assistant Google Drive Backup](https://github.com/sabeechen/hassio-google-drive-backup)
- [Mosquitto broker](https://github.com/home-assistant/addons/tree/master/mosquitto)
- [MQTT Explorer](http://192.168.1.245:8123/hassio)
- [rtl_433 (next)](https://github.com/pbkhrv/rtl_433-hass-addons/tree/main/rtl_433-next)
- [rtl_433 MQTT Auto Discovery (next)](https://github.com/pbkhrv/rtl_433-hass-addons/tree/main/rtl_433_mqtt_autodiscovery-next)
- [Terminal & SSH](https://github.com/home-assistant/addons/tree/master/ssh)
- [Z-Wave JS](https://github.com/home-assistant/addons/tree/master/zwave_js)
- [Jewish Calendar](https://www.home-assistant.io/integrations/jewish_calendar/)

I also added [https://www.hacs.xyz/](HACS - Home Assistant Community Store).

Integrations I use:
- ESPHome
- MQTT
- Viessmann ViCare
- Z-wave
- Zigbee Home Automation
## Home Assistant Cloud and Backups
Just as it is more convenient to let Home Assistant OS manage itself than configure and update each add-on manually, it is more convenient to pay a little bit to [Nabu Casa](https://www.nabucasa.com/) than setting up remote access [manually](https://www.home-assistant.io/docs/configuration/remote/).

Home Assistant Cloud can be configured to use a custom domain too!

Home Assistant Cloud supports storage of one backup; instead, I went with the [Google Drive Backup Add-On](https://github.com/sabeechen/hassio-google-drive-backup).

## Viessmann Boiler
To bring my new a Viessmann's [Vitodens 100-W](https://www.viessmann-us.com/en/products/vitodens/vitodens-100-b1he.html) boiler under the control of Home Assistant, I:
- installed Viessmann's ViCare application on my phone;
- used it to connect the boiler to Viessmann's cloud over WiFi;
- followed instructions on the [ViCare integration](https://www.home-assistant.io/integrations/vicare), and it just worked!

A few month later I noticed that there are things that I can do using the ViCare application that I can't do in Home Assistant. I filed a [bug](https://github.com/home-assistant/core/issues/126447) - and it got fixed (thank you, [@CFenner](https://github.com/CFenner)!). Now I can switch from ViCare to Home Assistant completely :)

On July 10, 2025 I got a message from Viessmann about a Domain Change for the Viessmann API https://​api.​viessmann.​com to https://​api.​viessmann-climatesolutions.​com; not clear at this point what, if anything, I need to do about this...

## Garage Door Opener
When I replaced my garage door openers in 2015, I wanted to be able to open the doors with my phone - just for fun! But it turned out that I needed some WiFi accessory that wasn't compatible with my model of the opener or some such...

Over the years, I though about building some Raspberry Pi-based thingy to control the garage door opener, but since I do not really need it, and interfacing with the opener properly did not seem trivial, I did nothing ;)

Recently, I saw a post [I added a ratgdo to my garage door, and I don’t know why I waited so long](https://arstechnica.com/gadgets/2024/09/i-home-automated-my-garage-door-finally-with-a-ratgdo/). Turns out, one guy *completely* solved the garage door opener integration, and how! The product is compatible with [any garage door opener](https://github.com/PaulWieland/ratgdo/wiki) and [any home automation approach](https://ratcloud.llc/pages/installation)! And it's completely local: my data doesn't get shipped to the Garage Door Opener Manufacturer's cloud! So, I:
- ordered a [ratgdo v2.53i kit](https://ratcloud.llc/products/ratgdo-v2-5i-kit) for $45 and [ratgdo v2.53i holster](https://ratcloud.llc/products/ratgdo-v2-53i-mount) for $9;
- got them in a couple of days;
- flashed the [ESPHome firmware](https://ratgdo.github.io/esphome-ratgdo/) recommended for use with Home Assistant;
- the tool itself added my board to my Home Assistant;
- in Home Assistant's settings, set "Allow the device to perform Home Assistant actions";
- powered down my garage door opener;
- [moved](https://ratcloud.llc/pages/installation) wires for the door button and obstruction sensors from the opener to the ratgo;
- [connected](https://ratcloud.llc/pages/installation) ratgo to the opener using supplied three-wire harness;
- powered ratgo up with the supplied USB brick and cable;
- powered the opener back up - and it just worked!

I am not sure how the sales of this little device can possibly fund continuous improvements to both hardware and firmware *and* research involved, especially since one needs to be a *little bit* of a geek to buy it...

I am in awe: one person developed and continues to improve a solution that completely covers this application area ;)

Professionally done labor of love - way to go!!

## Z-Wave
From the list of Z-Wave controllers Home Assistant supports](https://www.home-assistant.io/docs/z-wave/controllers/), I picked "Zooz 800 Series Z-Wave Long Range S2 USB Stick ZST39 LR" ($37 on Amazon).

In the zwave-js-ui, my Z-Wave USB stick reports firmware version v1.40 and SDK version v7.22.0, which seem fine. ZOOZ firmware [page](https://www.support.getzooz.com/kb/article/1158-zooz-ota-firmware-files/) has the latest firmware; ZOOZ firmware update [instructions](https://www.support.getzooz.com/kb/article/1276-how-to-perform-an-otw-firmware-update-on-your-zst39-800-long-range-z-wave-stick/) caution against updating the firmware using Home Assistant itself.

By now, I switched to [Home Assistant Connect ZWA-2](https://www.home-assistant.io/connect/zwa-2/).
## Thermostat
I have a Honeywell T6 Pro model [TH6210U2001](https://www.honeywellhome.com/us/en/support/air/thermostats/programmable-thermostats/t6-pro-programmable-thermostat-up-to-3-heat-2-cool-th6320u2008-u/) thermostat; it does not have remote capabilities, so I can not integrate it with Home Assistant, but it turns out that other models in the Honeywell T6 Pro lineup *do* work with Home Assistant, and I do not need to re-wire anything, since all T6 models have the same mounting plate! 

Bitten by the "local control, no cloud accounts" bug, I rejected WiFi models [TH6220WF2006/U](https://www.honeywellhome.com/us/en/products/air/thermostats/wifi-thermostats/t6-pro-smart-thermostat-multi-stage-2-heat-2-cool-th6220wf2006-u/) and [TH6320WF2003/U](https://www.honeywellhome.com/us/en/products/air/thermostats/wifi-thermostats/t6-pro-smart-thermostat-multi-stage-3-heat-2-cool-th6320wf2003-u/), which require a Honeywell Cloud account, and settled on Z-Wave model [TH6320ZW2003-U](https://www.honeywellhome.com/us/en/products/air/thermostats/programmable-thermostats/t6-pro-z-wave-thermostat-th6320zw2003-u/) ($128 on Amazon).

Thermostat replacement went without a hitch, and I was able to add it to Z-Wave controller even though the my Z-Wave stick is plugged into a server in the attic,two floors above the thermostat with a few walls in-between - beautiful!

## Zigbee
I bought Sonoff Zigbee 3.0 USB Dongle Plus-E Gateway ($33 on Amazon). Integrating it into Home Assistant turned out to be much simpler than the [official instructions](https://www.home-assistant.io/integrations/zha/) for [ZHA](https://www.home-assistant.io/integrations/zha/) (Zigbee Home Automation) make it sound: I plugged the dongle into the machine where Home Assistant is running and restarted the Home Assistant; it discovered the dongle, and when I agreed to integrate it, everything got configured automatically!

I also bought a Sonoff Zigbee Indoor Temperature and Humidity Sensor, SNZB-02D LCD ($20 on Amazon). Told ZHA to add a new device, pressed the pairing button on the device for 5 seconds - and that was it!

## RTL 433
I have a little LCD device with three additional sensors that send data to the screen. Turns out, there is a library that handles the protocols such devices use: [rtl_433](https://github.com/merbanan/rtl_433)! Of course, to decode the messages the sensors transmit, we first need to receive them using an RTL-SDR USB dongle.

Receiver/decoder can be run stand-alone, but does not have to when used in Home Assistant:
- add the RTL-SDR USB dongle to the virtual machine running Home Assistant
- `Home Assistant | Settings | People | Add Person` mqtt with password mqtt
- add [Mosquitto Broker](https://github.com/home-assistant/addons/blob/master/mosquitto/DOCS.md) add-on
- start the add-on
- add MQTT integration with the [Mosquitto broker](https://www.home-assistant.io/integrations/mqtt/)
- add [rtl_433_next](https://github.com/pbkhrv/rtl_433-hass-addons/tree/main/rtl_433-next) add-on

Using File Editor (Configurator) add-on, create `/config/rtl_433/config` file with:
```
output      mqtt://homeassistant:1883,user=mqtt,pass=mqtt
protocol    20
convert     si
```

Only the protocol my devices use is enabled; limiting protocols processed to prevent appearance of tire pressure sensor devices from cars passing by ;)

- configure the file into the add-on
- start the add-on
- add [rtl_433 MQTT Auto Discovery (next)]()https://github.com/pbkhrv/rtl_433-hass-addons/tree/main/rtl_433_mqtt_autodiscovery-next add-on
- configure it: host homeassistant, user mqtt, password mqtt
- start the add-on
- add [MQTT Explorer](https://github.com/GollumDom/addon-repository/tree/master/mqtt-explorer) add-on
- configure the connection to homeassistant:1883,user=mqtt,pass=mqtt
- start the add-on

## Garden

Added a [SONOFF Sprinkler Timer Zigbee 3.0](https://www.amazon.com/dp/B0D5B8S8N8); since it is far from my server in the attic, placed a [Zigbee plug](https://www.amazon.com/dp/B09KNDM4VV?th=1) on the inside of the wall where the garden faucet is :) 

## Security
I want to replace my door bell with a video one and install some cameras; I am thinking [Reolink](https://reolink.com/us/product/reolink-video-doorbell-wifi/), which [integrates](https://www.home-assistant.io/integrations/reolink/) with Home Assistant - although it seems that some additional [trickery](https://github.com/AlexxIT/WebRTC) is needed for audio... Also, the head-to-toe version (white, not black) does not seem to be available...

## Floor Plans

Make floor plans with:
- SweetHouse3D
- RoomSketcher
- FereeCAD

Integrate with Home Assistant with:
- https://github.com/ExperienceLovelace/ha-floorplan
- https://github.com/johnnyo21/floorplan_3d
- https://github.com/shmuelzon/home-assistant-floor-plan

## Timed Fan Off
 - https://gist.github.com/Blackshome/9f9785d7aa0ba7978fa6515a2d73d192

## Panel
- https://community.home-assistant.io/t/esp32-p4-4-wi-fi-6-touch-panel-with-built-in-voice-the-next-gen-wall-panel/895470/40
- https://github.com/alaltitov/Waveshare-ESP32-P4-86-Panel-ETH-2RO/tree/dev
## TODO

[https://www.home-assistant.io/integrations/zwave_js/#how-do-i-switch-between-the-official-z-wave-js-add-on-and-the-z-wave-js-ui-add-on](https://www.home-assistant.io/integrations/zwave_js/#how-do-i-switch-between-the-official-z-wave-js-add-on-and-the-z-wave-js-ui-add-on)

https://www.home-assistant.io/dashboards/sections/#creating-a-sections-view

Can I replace my Honeywell HumidiPRO H6062 with something compatible with Home Assistant?

https://samakroyd.com/2024/09/25/home-assistant-weve-done-smart-home-what-about-smart-garden/

https://itead.cc/product/sonoff-zigbee-human-presence-sensor/

[https://1projectaweek.com/blog/2023/8/7/rtl433-home-assistant-and-cheap-flood-sensors-oh-my](https://1projectaweek.com/blog/2023/8/7/rtl433-home-assistant-and-cheap-flood-sensors-oh-my)

https://github.com/dummylabs/thewatchman

