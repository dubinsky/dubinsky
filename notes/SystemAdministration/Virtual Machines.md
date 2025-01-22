---
title: Virtual Machines
tags:
  - sysadmin
---
At last, I have a use for virtual machines: run [[Home Assistant]] in one (on [[ProxMox]])!

## USB and Bluetooth

I need Home Assistant running in a virtual machine to be able to access various USB dongles (Z-Wave, Zigbee) plugged into the host machine; this is easy to do with "Add Hardware" for each USB device needed.

I would also like Home Assistant to have access to Bluetooth controller of the host, but although it looks like just another USB device, just adding it to the virtual machine did not work: Home Assistant does not detect any Bluetooth controllers...

See https://www.reddit.com/r/VFIO/comments/wbsqy1/how_to_fix_onboard_intel_bluetooth_error_code_10/...

In the XML of the virtual machine overview:
- added attribute `xmlns:qemu="http://libvirt.org/schemas/domain/qemu/1.0"` to the root element `domain`;
- as a last child of that same `domain` element, added:
```xml
  <qemu:capabilities>
    <qemu:del capability="usb-host.hostdevice"/>
  </qemu:capabilities>
```

I get the following at the virtual machine startup:
```
spice-client-error-quark: Could not auto-redirect Intel Corp AX201 Bluetooth
[8087:0026] at 1-8: could not claim interface 1 (configuration 1):
LIBUSB_ERROR_NO_DEVICE (0)
```

... but Home Asssistant *does* see the Bluetooth controller...

## Network

I want the virtual machine running on my lab server to be available from other machines on my network. This is such an obvious problem that one would think the answer is easy to find... Various instructions [explained](https://www.zenarmor.com/docs/linux-tutorials/how-to-configure-network-bridge-on-linux) how to create a [network bridge](https://www.redhat.com/sysadmin/setup-network-bridge-VM) - but neglected to mention that adding ("slaving") the physical interface to this bridge disrupts the network of the host machine... I guess everybody has multiple physical interfaces on their servers, but it seems such a waste...

Turns out, what I need is a `macvlan` network interface, which *looks* like physical interface with its own MAC address etc., but is in reality a sub-interface of a physical interface. This way, it can be configured as the interface for the virtual machine, which then gets an address on my network - which is what I want!

With this approach, virtual machine is not accessible from the host machine itself, but I don't need that anyway, and if I ever do, there is a way: create *another* one of those and add it to the host machine!

This is [what I did](https://stackoverflow.com/questions/24783600/how-to-create-a-macvlan-network-interface-in-the-same-network-as-the-host) to try this out (my physical interface is `eno2`, and the `macvlan` I created is `mac0`):
```shell
# ip link add link eno2 mac0 type macvlan mode bridge
# ip link set dev mac0 up
# dhclient -v mac0
```

To make the interface persist through reboots, I used [nmcli](https://networkmanager.dev/docs/api/latest/nmcli.html) (connection settings are described [separately](https://networkmanager.pages.freedesktop.org/NetworkManager/NetworkManager/nm-settings-nmcli.html)):
```shell
# nmcli connection add \
  connection.id mac0 \
  connection.interface-name mac0 \
  connection.type macvlan \
  macvlan.mode bridge \
  macvlan.parent eno2 \
  ipv4.method auto

# nmcli connection up mac0
```

TODO This probably can be used to turn Docker container applications into hosts on the network too!

When I migrated to [[ProxMox]], which sets the network up automatically so that the virtual machines are accessible from the outside (and uses bridge, I think, and not macvlan), this became obsolete, so I did `mcli connection del mac0`