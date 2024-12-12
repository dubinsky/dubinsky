---
title: Home Assistant
---
I heard about [Home Assistant](https://www.home-assistant.io/) home automation project before: open source, integrates with [everything](https://www.home-assistant.io/integrations/)... But it wasn't until I replaced my HVAC system recently that I decided to see what it can do.

## Installation

I installed Home Assistant using Docker; this installation method is called [Home Assistant Container](https://www.home-assistant.io/installation/linux#docker-compose); there is a warning concerning it: "This installation method **does not have access to add-ons**", but so far I did not run into any problems.

<details>
  <summary>My /home/homeassistant/homeassistant/docker-compose.yaml</summary>
  <pre language="yaml"><code>	
	version: '3'
	services:
	  homeassistant:
	    container_name: homeassistant
	    image: "ghcr.io/home-assistant/home-assistant:stable"
	    volumes:
	      - /home/homeassistant/homeassistant/config:/config
	      - /etc/localtime:/etc/localtime:ro
	      - /run/dbus:/run/dbus:ro
	    restart: unless-stopped
	    privileged: true
	    network_mode: host
  </code></pre>
</details>

## Viessmann Boiler

To bring my new a Viessmann's [Vitodens 100-W](https://www.viessmann-us.com/en/products/vitodens/vitodens-100-b1he.html) boiler under the control of Home Assistant, I:
- installed Viessmann's ViCare application on my phone;
- used it to connect the boiler to Viessmann's cloud over WiFi;
- followed instructions on the [ViCare integration](https://www.home-assistant.io/integrations/vicare), and it just worked!

A few month later I noticed that there are things that I can do using the ViCare application that I can't do in Home Assistant. This is probably a [bug](https://github.com/home-assistant/core/issues/126447); I hope it gets fixed soon, so that I can switch from ViCare to Home Assistant completely :)

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

I want Z-Wave support.

From the list of Z-Wave controllers Home Assistant supports](https://www.home-assistant.io/docs/z-wave/controllers/), I picked "Zooz 800 Series Z-Wave Long Range S2 USB Stick ZST39 LR" ($37 on Amazon).

In the zwave-js-ui (see below), my Z-Wave USB stick reports firmware version v1.40 and SDK version v7.22.0, which seem fine. ZOOZ firmware [page](https://www.support.getzooz.com/kb/article/1158-zooz-ota-firmware-files/) has the latest firmware; ZOOZ firmware update [instructions](https://www.support.getzooz.com/kb/article/1276-how-to-perform-an-otw-firmware-update-on-your-zst39-800-long-range-z-wave-stick/) caution against updating the firmware using Home Assistant itself.

Since my Home Assistant runs in a Docker container, I had to install a separate zwave-js server; instead of the UI-less minimalist image https://hub.docker.com/r/kpine/zwave-js-server, I went with the official image that also has a very nice UI: https://github.com/zwave-js/zwave-js-ui/blob/master/docker/docker-compose.yml. Note: `zwave-js-ui` used to be called `jwazvejsmqtt`; some [instructions]( https://www.homeautomationguy.io/blog/docker-tips/installing-z-wave-js-with-docker-and-home-assistant) still use the old name...

<details>
<summary>My /home/homeassistant/zwave-js-ui/docker-compose.yaml</summary>

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
      - SESSION_SECRET=cqlrsdfiosiold92384pojmwdalsqk1q2324987zsxkcjy
      - ZWAVEJS_EXTERNAL_CONFIG=/usr/src/app/store/.config-db
      # Uncomment if you want logs time and dates to match your timezone instead of UTC
      # Available at https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
      #- TZ=America/New_York
    networks:
      - zwave
    devices:
      # Do not use /dev/ttyUSBX serial devices, as those mappings can change over time.
      # Instead, use the /dev/serial/by-id/X serial device for your Z-Wave stick.
      - '/dev/serial/by-id/usb-Zooz_800_Z-Wave_Stick_533D004242-if00:/dev/zwave'
    volumes:
      - '/home/homeassistant/zwave-js-ui/config:/usr/src/app/store:Z'
    ports:
      - '8091:8091' # port for web interface
      - '3000:3000' # port for Z-Wave JS websocket server
networks:
  zwave:
```
</details>

With zwave-js UI running on port 8091, I followed [instructions](https://zwave-js.github.io/zwave-js-ui/#/homeassistant/homeassistant-officia): 
- in Settings / Home Assistant, enabled "WS Server"
- kept MQTT discovery disabled
- disabled MQTT gateway
- used circular arrows icons to generate random keys (S2_Unauthenticated etc.)

It [looks like](https://www.home-assistant.io/integrations/zwave_js) I [do not need to do anything](https://zwave-js.github.io/zwave-js-ui//#/getting-started/quick-start) about Home Assistant Z-Wave add-on; on the Home Assistant side, I added Z-Wave integration with the default web-socket settings - and that's it!

## Thermostat

I have a Honeywell T6 Pro model [TH6210U2001](https://www.honeywellhome.com/us/en/support/air/thermostats/programmable-thermostats/t6-pro-programmable-thermostat-up-to-3-heat-2-cool-th6320u2008-u/) thermostat; it does not have remote capabilities, so I can not integrate it with Home Assistant, but it turns out that other models in the Honeywell T6 Pro lineup *do* work with Home Assistant, and I do not seem to need to re-wire anything, since all T6 models have the same mounting plate! 

Bitten by the "local control, no cloud accounts" bug, I rejected WiFi models [TH6220WF2006/U](https://www.honeywellhome.com/us/en/products/air/thermostats/wifi-thermostats/t6-pro-smart-thermostat-multi-stage-2-heat-2-cool-th6220wf2006-u/) and [TH6320WF2003/U](https://www.honeywellhome.com/us/en/products/air/thermostats/wifi-thermostats/t6-pro-smart-thermostat-multi-stage-3-heat-2-cool-th6320wf2003-u/), which require a Honeywell Cloud account, and settled on Z-Wave model [TH6320ZW2003-U](https://www.honeywellhome.com/us/en/products/air/thermostats/programmable-thermostats/t6-pro-z-wave-thermostat-th6320zw2003-u/) ($128 on Amazon).

Thermostat replacement went without a hitch, and I was able to add it to Z-Wave controller even though the my Z-Wave stick is plugged into a server in the attic,two floors above the thermostat with a few walls in-between - beautiful!

## Zigbee

I bought Sonoff Zigbee 3.0 USB Dongle Plus-E Gateway ($33 on Amazon). Integrating it into Home Assistant turned out to be much simpler than the [official instructions](https://www.home-assistant.io/integrations/zha/) for [ZHA](https://www.home-assistant.io/integrations/zha/) (Zigbee Home Automation) make it sound: I plugged the dongle into the machine where Home Assistant is running and restarted the Home Assistant; it discovered the dongle, and when I agreed to integrate it, everything got configured automatically!

I also bought a Sonoff Zigbee Indoor Temperature and Humidity Sensor, SNZB-02D LCD ($20 on Amazon). Told ZHA to add a new device, pressed the pairing button on the device for 5 seconds - and that was it!

## Virtualization

Although I did manage to add Z-Wave support via additional Docker containers, I'd like to have the ability to install Home Assistant Add-Ons seamlessly, which I [can not do](https://www.home-assistant.io/installation/#advanced-installation-methods) while running Home Assistant Container... I am not about to dedicate a machine (even a Raspberry Pi) to running Home Assistant OS - but I can dedicate a *virtual* machine to it!

For my Home Assistant virtual machine to be accessible from other machines on my network (including my phone), I need a special network setup; see "Network" in [[Virtual Machines]]. In the following, network interface set up for this purpose is named "mac0".

Official instructions for installing Home Assistant in a virtual machine are [available](https://www.home-assistant.io/installation/alternative); followed them:
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

I created a [backup](https://www.home-assistant.io/integrations/backup/) of my current Home Assistant Container installation, and used it in [onboarding](https://www.home-assistant.io/getting-started/onboarding/) of the new virtual machine installation.

For the dongles (Z-Wave and Zigbee) to be visible to the Home Assistant inside the VM, I added them in "Add Hardware" of the virt-manager.
Same for the host Bluetooth controller - and it still does not work! See https://www.reddit.com/r/VFIO/comments/wbsqy1/how_to_fix_onboard_intel_bluetooth_error_code_10/...

## RTL 433

https://github.com/merbanan/rtl_433

sudo dnf install rtl-433

just run

see https://www.instructables.com/rtl-sdr-on-Ubuntu/

$ lsusb

0bda:2832 Realtek Semiconductor Corp. RTL2832U DVB-T

/etc/udev/rules.d/20.rtlsdr.rules:

```
SUBSYSTEM=="usb", ATTRS{idVendor}=="0bda", ATTRS{idProduct}=="2832", GROUP="homeassistant", MODE="0666", SYMLINK+="rtl_sdr"
```

sudo udevadm control --reload

plug in the dongle

added it to the virtual machine hardware


added MQTT integration with the Mosquitto broker

Home Assistant / Settings / People / Add Person: mqtt with password mqtt

https://github.com/home-assistant/addons/blob/master/mosquitto/DOCS.md



[https://1projectaweek.com/blog/2023/8/7/rtl433-home-assistant-and-cheap-flood-sensors-oh-my](https://1projectaweek.com/blog/2023/8/7/rtl433-home-assistant-and-cheap-flood-sensors-oh-my)

https://github.com/pbkhrv/rtl_433-hass-addons

Home Assistant: Settings / Add-ons / Add-on store / dots / Repositories: add https://github.com/pbkhrv/rtl_433-hass-addons

installed rtl_433_next add-on

on Home Assistant, created (using the terminal add-on) a file `/config/rtl_433/config` with:
```
output      mqtt://homeassistant:1883,user=mqtt,pass=mqtt
protocol    20
convert     si
```

limiting protocols processed to prevent appearance of tire pressure sensor devices from cars passing by ;)

configured the file into the add-on
started the rtl_433_next add-on

installed rtl_433 MQTT Auto Discovery (next) add-on
configured it: host homeassistant, user mqtt, password mqtt

istant: Settings / Add-ons / Add-on store / dots / Repositories: add https://github.com/GollumDom/addon-repository

installed MQTT Explorer add-on
saved the connection to homeassistant:1883,user=mqtt,pass=mqtt
used it to prune parasitic tire pressure monitor sensor devices



## Further Plans

I can add Bluetooth Low Energy temperature, humidity and other [sensors](https://esphome.io/components/sensor/xiaomi_ble.html#lywsd03mmc) with either [ESP32 Tracking Hub](https://esphome.io/components/esp32_ble_tracker) or [Bluetooth Proxy](https://esphome.io/components/bluetooth_proxy).

Can I replace my Honeywell HumidiPRO H6062 with something compatible with Home Assistant?

Subscribe to Home Assistant Cloud.

Wall-mounted touch screen with a Raspberry Pi in kiosk mode as a control panel:
- https://www.waveshare.com/product/displays/lcd-oled/lcd-oled-1.htm
- https://vilros.com/products/official-raspberry-pi-7-touchscreen-with-pi-4-compatible-case?variant=31280949264478&currency=USD&utm_medium=product_sync&utm_source=google&utm_content=sag_organic&utm_campaign=sag_organic&tw_source=google&tw_adid=90618046730&tw_campaign=316142330&gad_source=1&gclid=Cj0KCQjwmOm3BhC8ARIsAOSbapWslnnQY7N7Kb0L3loPB1ldWZ4WqcxF31JmNy2KGIKE0dK6rzzWZ34aAkr1EALw_wcB

## TODO

re-order, with installation options together

table of content

[https://www.home-assistant.io/integrations/zwave_js/#how-do-i-switch-between-the-official-z-wave-js-add-on-and-the-z-wave-js-ui-add-on](https://www.home-assistant.io/integrations/zwave_js/#how-do-i-switch-between-the-official-z-wave-js-add-on-and-the-z-wave-js-ui-add-on)

https://www.home-assistant.io/dashboards/sections/#creating-a-sections-view

https://samakroyd.com/2024/09/25/home-assistant-weve-done-smart-home-what-about-smart-garden/

https://itead.cc/product/sonoff-zigbee-human-presence-sensor/

Added and configured https://github.com/sabeechen/hassio-google-drive-backup