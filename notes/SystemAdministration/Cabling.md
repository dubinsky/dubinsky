---
title: Cabling
---
Fall 2025

* TOC
{:toc}

## Cables
CAT6 seems to be sufficient for my needs: 10G up to 55 metres; I even have some left from the previous fishing ;)
## Rack
StarTech.com  23"-41" D  24" W

| Model            | Units | Height | Price |
| ---------------- | ----- | ------ | ----- |
| 4POSTRACK25U     | 25    | 48"    | $327  |
| 4POSTRACK18U     | 18    | 35.6"  | $323  |
| **4POSTRACK15U** | 15    | 30.3"  | $315  |
| 4POSTRACK12U     | 12    | 25.1"  | $250  |
| 4POSTRACK8U      | 8     | 18"    | $240  |
[15U](https://www.amazon.com/gp/product/B084PB7GL8/) $314 at Amazon; B&H does not carry StarTech.
## Patch Panel
With insertable keystones, not like the one I have.
Got one with 16 ports, so that keystones have some space; if I need more, I'll just get another one.

Want to get a 24-port one with cable bracket/ties; I can just plug the unused holes...

Neat patch cover?
## RJ45
I need CAT6 POE rated ones!

Punch-down keystones for cable runs, pass-through ones for a few devices in the rack, pass-through RJ45 plugs.

Looked at:
- [Panduit NetKey](https://www.amazon.com/Panduit-NK688MBU-Q-NetKey-Category-Module) (Mini-Com only works with Panduit faceplates etc.)
- [Monoprice](https://www.monoprice.com/product?p_id=5384
- CableMatters (Vertical Cable at Amazon?)
- TrueCable
- Vertical Cable VMax
- Everest EasyJack
- Simply45
- Leviton
- Belden Revconnect

Decided to go with trueCable keystones, couplers and plugs.

Did not go for a specialized crimper: I am going to just punch them down.

## Switch
I need another switch, to gradually move the cables from the current patch panel to the new one on the other side of the attic,
and to add new cables for the cameras, video doorbell etc.

Ubiquity Networks, of course.

- [Pro XG 10 PoE](https://www.bhphotovideo.com/c/product/1894532-REG/ubiquiti_networks_usw_pro_xg_10_poe_pro_xg_10_poe.html) 10x10G POE+++ $699 at B&H  [$699 at Amazon](https://www.amazon.com/Ubiquiti-Pro-XG-Ethernet-Switch/dp/B0FKM2HYMP)
- [Pro HD 24 POE](https://www.bhphotovideo.com/c/product/1880836-REG/ubiquiti_networks_usw_pro_hd_24_poe_pro_hd_24_poe.html) 22x2.5G POE++ 2x10G PoE++ $999 at B&H
- [Pro Max 16 PoE](https://store.ui.com/us/en/products/usw-pro-max-16-poe) 12x1G POE+ 4x2.5G POE++ [$399 at Amazon](https://www.amazon.com/USW-Pro-Max-16-PoE-16-Port-Uplinks-QALYNX-Flexible)
- [Pro Max 24 POE](https://store.ui.com/us/en/category/all-switching/products/usw-pro-max-24-poe) 8x1G POE+ 8x1G POE++ 8x2.5G POE++ [$799 at B&H](https://www.bhphotovideo.com/c/product/1803250-REG/ubiquiti_networks_usw_pro_max_24_poe_pro_max_24_port_poe.html) [$799 at Amazon](https://www.amazon.com/Ubiquiti-USW-Pro-Max-24-PoE-2-5G-Ethernet-Power/dp/B0CW3MCS5L/)

I do not need too many ports: 10 is probably enough.

I do want to be able to use the flagship access points like U7 Pro XGS, which requires POE++. 

I do not need 10G speed: my WAN uplink is going to be 1G max, my Framework Laptop 13's Ethernet expansion card is 2.5G, my 2024 desktop's Ethernet is 5G, and it is unlikely that wireless clients will be able to benefit from 10G connection between themselves :)

So, Pro XG 10 POE is probably a waste, as is Pro HD 24 POE.

Pro Max 16 PoE seems enough, but it comes with an external power supply and requires an optional rack mount kit...

I am going with Pro Max 24 POE :)
## Access Points
Ubiquity U7 (WiFi 7)

-  **U7 Pro XGS**  $299  10G 8 streams POE++
- U7 Pro Max $279  2.5G 8 streams POE+

- U7 Pro XG $199  10G 6 streams POE+
- U7 Pro $189 2.5G 6 streams POE+