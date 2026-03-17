---
title: Framework Laptop 13
---
In October 2025, I decided to try [Framework Laptop 13](https://frame.work/laptop13).

CPU - Intel:
- Ultra 5 125H
- Ultra 7 **155H** - $310 more than 125H, 6+8 cores instead of 4+8, 4.8GHz instead of 4.5Ghz - possibly worth it;
- Ultra 7 165H - $400 more than 155H, same number of cores, 5.0GHz instead of 4.8GHz - not worth it;

Display: 2.8K
Bezel: Black
Ports: USB-Cx4 (Aluminum, Black, Orange, Green), USB-Ax2, HDMI, DisplayPort, Ethernet, Micro SD, SD, Storage 250G

Total: $1,474

RAM:
- 2x48G DDR5-5600 SO-DIMM: $480 @Framework, $260 elsewhere (e.g. **CT2K48G56C46S5**).

SSD:
- WD_BLACK SN850X 4TB $350 @Framework, $265 elsewhere
- Samsung 990 Pro 4TB  $280
- Latest Samsung **9100** Pro 4TB to swap for the 990 Pro in my desktop: $400

## Refresh Rate
I also got a 6K 32" monitor - [ASUS PA32QCV](https://www.asus.com/us/displays-desktops/monitors/proart/proart-display-6k-pa32qcv/).

When connected to the laptop via a DisplayPort cable, 6016x3384 resolution works at 60Hz;
with the ThunderBolt cable, resolution is 30Hz!

It is possible to configure the monitor to use DisplayPort cable to expose USB devices plugged into the monitor,
but power to the laptop can be supplied only via the Thunderbolt cable, and I strongly prefer using only one cable :)

I did a few things:
- plugged and unplugged both cables in different combinations;
- rebooted the laptop a number of times;
- configured the monitor to use DisplayPort for the USB up-link (there does not seem to be an option to restrict Thunderbolt to USB 2);
- updated laptop firmware with `fwupdmgr` -
and now everything seems to work with the correct refresh rate and resolution ;)
- 