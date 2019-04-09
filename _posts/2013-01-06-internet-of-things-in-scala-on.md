---
layout: post
title: Internet of Things in Scala on Raspberry Pi
date: '2013-01-06T19:46:00.001-05:00'
author: Leonid Dubinsky
tags: [electronics, scala]
modified_time: '2013-04-15T01:22:18.322-04:00'
blogger_id: tag:blogger.com,1999:blog-8681083740214020499.post-8876003498529001840
blogger_orig_url: https://blog.dub.podval.org/2013/01/internet-of-things-in-scala-on.html
---

I want to connect things like LEDs, buttons, displays, sensors, relays etc. to a computer
([physical computing](http://en.wikipedia.org/wiki/Physical_computing)). I want to program that computer in
[Scala](http://www.scala-lang.org/). And I want to connect to the Internet
([Internet of Things](http://en.wikipedia.org/wiki/Internet_of_Things).
I chose [Raspberry Pi](http://www.raspberrypi.org/) as the platform.

This post details the ways to connect things, the platform choices, and the programming interfaces.

I also wrote some Scala code. The code is very rough:  I spend around three days (during a winter break of 2012)
researching and developing it, but specific things that I needed to work (I2C) already work. In the spirit of
"[release early, release often](http://en.wikipedia.org/wiki/Release_early,_release_often)", I decided to announce it
anyway. The project is hosted on [GitHub](https://github.com/): [podval-iot](https://github.com/dubinsky/podval-iot).

I hate [JNI](http://en.wikipedia.org/wiki/Java_Native_Interface) with its
"[crashing is the default mode of operation](http://stackoverflow.com/a/65237)"; the only native interface that I can
tolerate is [JNA](https://github.com/twall/jna).

I prefer to use standard Linux facilities wherever available, and avoid hardware-specific code, non-standard libraries
and bit-banging. The result should be usable on platforms other than Raspberry Pi.

I do not focus on the [Scala/Java interoperability](http://www.codecommit.com/blog/java/interop-between-java-and-scala),
so it may be difficult to use my library from Java. My rationale for allowing myself to go with Scala is:
- the binding may be cleaner using Scala features
- people who are not ready to switch to Scala can use existing Java bindings
- for some availability of a library they need in Scala may be the last push to switch over - I would be thankful now if
  such a push happened to me few years back ;)

## How to Connect Things to Computers ##

[Physical computing](http://en.wikipedia.org/wiki/Physical_computing) requires connecting devices
(sensors, displays, relays etc.) to a computer or a programmable controller. It is done through
[GPIO](http://en.wikipedia.org/wiki/General_Purpose_Input/Output) (General Purpose Input/Output) pins.

The level of a given pin can be set in software to high or low (digital output). This is sufficient to control LEDs and
other low-current devices. If more current is needed than a GPIO pin can handle, a transistor can be used. To control
high-voltage devices, a relay is called for.

The level of a pin can be read in software  (digital input). This is sufficient to connect buttons and switches, but
requires polling, making it computationally unfeasible. Often, it is possible to configure  interrupts to be triggered
by changes in the pin levels.

There may be configurable [pull-up/pull-down resistors](http://en.wikipedia.org/wiki/Pull-up_resistor) on the pins.

By pulsing the output level of a pin with pulses of controlled width, *average* level of the pin can be controlled
without a DAC - [Pulse-Width Modulation](http://en.wikipedia.org/wiki/Pulse-width_modulation) (PWM).

A pin can have [analog-to-digital converter](http://en.wikipedia.org/wiki/Analog-to-digital_converter) (ADC),
so a level on the pin can be read with more precision than just high or low (analog input). If there is a
[digital-to-analog converter](http://en.wikipedia.org/wiki/Digital-to-analog_converter) (DAC) on the pin, its level can
be set more precisely than just high or low (analog output).

This hardware GPIO functionality is exposed to software through I/O registers.

There are many protocols for connecting devices to (a set of) GPIO pins:
- [I2C](http://en.wikipedia.org/wiki/I2C)/[SMBus](http://en.wikipedia.org/wiki/SMBus) - two-wire interface for attaching
  low-speed peripherals
- [SPI bus](http://en.wikipedia.org/wiki/SPI_bus) - four-wire full-duplex high-speed serial bus
- [1-Wire](http://en.wikipedia.org/wiki/1-Wire): one-wire low-speed interface for attaching sensors and such
- Serial ([UART](http://en.wikipedia.org/wiki/Uart)): for connecting to other boards
  ([microcontrollers](http://en.wikipedia.org/wiki/Microcontrollers), [ZigBee](http://en.wikipedia.org/wiki/Zigbee) radios etc.)

PWM, I2C, SMB, SPI, Serial and 1-Wire can be done in software, using basic GPIO digital input/output capabilities
([bit banging](http://en.wikipedia.org/wiki/Bit-banging)). This is computationally expensive, especially if polling of
the pins is involved, so on a platform that is not fast enough, software-based implementation of such protocols is not
feasible. Also, the protocols are timing-sensitive, so on a platform that is not real-time it can be difficult.

Often, some of the protocols are implemented in hardware of the underlying microcontroller or
[System on a Chip](http://en.wikipedia.org/wiki/System_on_chip) (SoC), and exposed to software through the same
mechanism as the basic GPIO functionality itself.

Some protocols can be implemented in external hardware that is accessed through one of the natively-implemented
protocols. For instance, computationally-intensive 1-Wire protocol can be offloaded to a chip like
[DS2482-100](http://www.maximintegrated.com/datasheet/index.mvp/id/4382) that communicates to the host system over I2c.

Missing ADC or DAC capabilities can be added through the use of external chips: SPI ADC chip
[MCP3008](http://www.microchip.com/wwwproducts/Devices.aspx?dDocName=en010530)
($[3.75](https://www.adafruit.com/products/856)), I2C ADC breakout board [ADS1015](http://www.ti.com/product/ads1015)
($[10](https://www.adafruit.com/products/1083)), I2C DAC breakout board
[MCP4725](http://www.microchip.com/wwwproducts/Devices.aspx?dDocName=en532229) ($[5](https://www.adafruit.com/products/935)).
External ADC/DAC is often more precise than the built-in ones, and sometimes more feature-rich (ADS1015 has
[programmable-gain amplifiers](http://en.wikipedia.org/wiki/Programmable-gain_amplifier)).

External chips can also be used to obtain additional GPIO pins - for instance, an I2C I/O expanders like
[MCP23008](http://www.microchip.com/wwwproducts/Devices.aspx?dDocName=en021393) ($[2](https://www.adafruit.com/products/593)).

## What I Want to Connect ##

Suppose I want to build, for my daughter's room something that can:
- work as an alarm clock
- measure temperature and humidity
- display the measurements locally and
- graph the measurements on [Cosm](https://cosm.com/)
- play some music as a wake-up alarm

Simple temperature sensor like TMP36 ($[2](https://www.adafruit.com/products/165)) neead an "analog in" pin to connect
to, which Raspberry Pi does not have. This is easy to remedy with - say - a SPI MCP3008; see, for example, this
[tutorial](http://learn.adafruit.com/send-raspberry-pi-data-to-cosm). Alternatively, I2C TMP102
($[6](https://www.sparkfun.com/products/9418)) can be used.

Combined temperature/humidity sensors do not use analog interface at all. For instance:
- DHT11 ($[5](https://www.adafruit.com/products/386)) and DHT22 ($[12.50](https://www.adafruit.com/products/385)) have -
  it seems - 1-Wire interface;
- SHT11 ($[35](https://www.adafruit.com/products/246)) uses,
  [according](http://forums.electricimp.com/discussion/comment/2076#Comment_2051) to Hugo of the Electric Imp, "nearly
  like, but not actually I2C";
- SHT21 ($[40](http://www.liquidware.com/shop/show/SEN-SHT/Humidity+and+Temperature+Sensor)) uses I2c.

I am not ready to deal with the tricky timing of the 1-Wire protocol myself, and SHT sensors seem to be more advanced
anyway. I do not want to bit-bang "something like I2C", so I picked SHT21 as my sensor over SHT11, as Hugo recommended.

For the display of time and sensor data I picked nice and bright four-digit seven-segment LED displays from Adafruit
($[10](https://www.adafruit.com/products/880)). They have I2C interface, so only four wires need to be connected for
each. Up to 4 can be attached to the same I2C bus.

For general-purpose interaction with the system, an I2C-based LCD display with buttons
($[25](https://www.adafruit.com/products/1110)) seems to be the way to go.

Should I need to blink some LEDs or switch on the light, GPIO will come handy - or I can use an I2C expander like
MCP23008. That would mean wasting the existing GPIO pins, though, and I am not clear on the support for the interrupts
generated by the expander...

Although I do not need 1-Wire support for this project, I wouldn't mind having it for another project for a friend from
[LechemLabs](http://www.lechemlab.com/): I need to use a bunch of temperature sensors (to measure temperature inside
rising dough), and the best ones (like [DS18B20](http://adafruit.com/products/381)) talk 1-Wire protocol. Actually, that
sensor seems to be the main reason people use 1-Wire :)

It seems that I2C is just about sufficient for my current needs.

[*Aside: I2C.* Since I2C is so convenient, and is present in every computer, the question is - why isn't it
available everywhere? Well, probably because it does not pay to provide additional connector on every machine - a
connector that only a tiny minority of buyers will use. How about a USB-to-I2C dongle, then? Every computer has USB!
And here it is: [LimkM](http://thingm.com/products/linkm.html) ($[30](https://www.sparkfun.com/products/9903)). So, if
I only wanted to connect some hardware to a real computer, this is a possibility. The dongle does not expose the I2C bus
in a standard Linux way, though: it would need to have a kernel module to do that...]

## Internet of Things Platforms ##

Here are some of the hardware platforms that can be used for the Internet of Things projects.

### Arduino ###

A while ago, when I wanted to connect some sensors and such to a computer, I became aware of
[Arduino](http://blog.dub.podval.org/2010/05/arduino.html) - a programmable microcontroller board with GPIO pins.

Arduino Internet connectivity is not cost-efficient. Arduino Uno board costs
$[30](https://www.adafruit.com/products/50). Ethernet shield for Arduino costs
$[45](https://www.sparkfun.com/products/9026).
WiFi shield costs $[85](https://www.sparkfun.com/products/11287)! A ZigBee radio is
$[23](https://www.sparkfun.com/products/8665) (without adaptor board), and requires a gateway for the
Internet connection. Compare to Raspberry Pi Model B with built-in Ethernet port for $35 or Raspberry Pi Model A ($25)
with WiFi dongle ($12)...

Maybe things are a bit better with Arduino Due ($[50](https://www.adafruit.com/products/1076)), since it has a USB port,
but I am not sure.

### Electric Imp ###

Recently, another platform became available - [Electric Imp](http://electricimp.com/).

Allegedly, [Hugo Fiennes](http://www.linkedin.com/pub/hugo-fiennes/0/3a3/110), CEO of Electric Imp, was
frustrated when he wanted to connect a programmable LED strip to the Internet. This frustration brought us the Electric
Imp. (Previously, Hugo was frustrated that he does not have all his music in his car - and built a first in-dash MP3
player - [empeg](http://www.empeg.com/), which I still have in my car :))

This amazing device costs $[30](https://www.sparkfun.com/products/11395) (development board is
$[12.50](https://www.adafruit.com/products/1130)), has a microcontroller, GPIO pins, ADC, I2C. There is no
1-Wire support, and Imp is too slow to do it in software, but Hugo
[promised](http://forums.electricimp.com/discussion/comment/1592#Comment_1592) 1-Wire support done in firmware early in
2013 (that just started).

Electric Imp has built-in WiFi. Not only is it possible to connect it to the Internet, it is *impossible* to
program it in any other way: a [Squirrel](http://www.squirrel-lang.org/) program is compiled in the cloud and downloaded
to the Imp over WiFi!

Hugo says that Imp-based sensor that reports a reading once an hour can work off batteries for more than two years - and
still use ubiquitous WiFi and not - contrary to the industry wisdom - ZigBee!

### Raspberry Pi ###

The board costs $[35](http://www.mcmelectronics.com/product/83-14421) with Ethernet port and 2 USB ports or $25
with no Ethernet and 1 USB port. It has an ARM CPU, 512MB of RAM, GPU capable of full HD, HDMI connector, audio out,
camera connector, GPIO pins, I2C, SPI, PWM, UART. There is no built-in ADC.

There is no built-in WiFi, but because it has USB ports, WiFi is just a $[12](https://www.adafruit.com/products/814)
dongle away.

It runs a flavor of Debian Linux and is thus very flexible. For instance, even for projects where real time is needed,
one does not need the Real Time Clock module ($[17.50](https://www.adafruit.com/products/255)) -
[NTP daemon](http://www.eecis.udel.edu/~mills/ntp/html/ntpd.html) takes care of clock synchronization :)

One argument against a Linux-running board and for a microcontroller is: Linux is not a
[real-time OS](http://en.wikipedia.org/wiki/Real-time_operating_system). Also, languages like Scala and Java (and even
Python, the favorite of the Raspberry Pi community) do garbage collection at unpredictable times (I suspect
that Squirrel that Electrical Imp uses does the same). But - you get multi-threading :)

Some say that it is an [overkill](http://news.ycombinator.com/item?id=4138045) to use Raspberry Pi where you can use a
simple microcontroller. Yes, it will consume more power, but if it is in a room with an electrical outlet, this is not
an issue. Maybe it is just the thought of all those unused CPU cycles and peripherals (like HDMI in a project that does
not use it) that makes people feel it is an overkill....

### Other ###

There are other platforms out there: [BeagleBone](http://beagleboard.org/bone),
[Pinoccio](http://www.indiegogo.com/pinoccio), etc. And I am sure there will be more still :)

## Choosing a Platform ##

Choice of a platform for a particular project is guided by requirements for:
- power consumption
- connectivity
- computing power
- flexibility

A nice comparison of Arduino Uno, BeagleBone and Raspberry Pi is available at
[Digital Diner](http://digitaldiner.blogspot.com/2012/10/arduino-uno-vs-beaglebone-vs-raspberry.html).

For portable, stand-alone devices (like the photo trigger I did not built yet :)) Arduino is probably the best: Electric
Imp is likely not powerful enough, and Raspberry Pi is too power-hungry (although people do use it in portable projects).

For embedded connected sensors and such, Electric imp Imp is probably better than Arduino: Imp's built-in connectivity
is more price-effective. If more computational power is needed, Arduino with Zigbee is an option. Stand-alone
data-logging sensors are easier with an Arduino.

For multimedia and home entertainment centers Raspberry Pi is probably ideal: Electric Imp is not powerful enough for
video, and Arduino requires additional hardware that itself costs more than a Raspberry Pi. And power consumption is not
an issue.

Actually, for any project where power consumption is not an issue, I'd lean towards Raspberry Pi, because my language of
choice is Scala. I do not want to program Arduino in a C++ dialect, nor Imp in Squirrel. Raspberry Pi is an affordable
platform that allows me - theoretically - to participate in the Internet of Things using my language of choice. It is
not powerful enough to actually run a Java/Scala IDE, but as long as the versions of the libraries are the same, code
compiled elsewhere runs fine.

Yes, Linux is not a real-time OS. But you know what? I'll take a pleasant development experience working in a strongly
typed language with functional programming support in a real IDE, even if in the end the clock display will sometimes
miss an update and then jump ahead two seconds.

Libraries written for Raspberry Pi tend to be in Python or C. I do not want to write in Python or C; I want to write in
Scala. I can call into Java libraries from Scala, but I can not call into Python. I can call into C using JNI, but that
requires writing in C, and I do not want that. Besides, I hate JNI.

Thus, to make using Scala practical, I need a native Scala binding to the facilities that I need, using - at most - JNA.

## Raspberry Pi Peripherals and Linux ##

The bare-metal way to get at all the peripherals of Raspberry Pi is through the memory-mapped I/O registers. Official
Broadcom [documentation](http://www.raspberrypi.org/wp-content/uploads/2012/02/BCM2835-ARM-Peripherals.pdf) explains all
the registers of the BCM2835 (this is the chip inside Raspberry Pi; BCM2708 seems to be the name of the family of which
BCM2835 is a member).

An overview of various methods of getting at the peripherals is in the
"[RPi Low-level peripherals](http://elinux.org/RPi_Low-level_peripherals)" tutorial.

It is possible to write a userspace library that uses I/O registers to provide a reasonable interface to some of the
GPIO functionality:
- Python library [RPi.GPIO](http://pypi.python.org/pypi/RPi.GPIO). Supports GPIO. Support for I2C, SPI, PWM, UART, and
  1-Wire is planned.
- [BCM2835](http://www.open.com.au/mikem/bcm2835/) library. Supports GPIO and SPI.

Linux-standard ways of working with the peripherals in Linux userspace - and their support on Raspberry Pi - are:

### I2C/SMB ###

Linux kernel has a driver for the I2C/SMBus: i2c-dev module. Access from userspace is through reads, writes and ioctls
on [/dev/i2c-n](http://www.kernel.org/doc/Documentation/i2c/dev-interface) files. Package
[i2c-tools](http://www.lm-sensors.org/wiki/I2CTools) contains userspace C bindings for I2C, a Python module (python-smb)
and various command-line utilities.

Hardware-specific bits for I2C on BCM2708 were
[written](https://github.com/raspberrypi/linux/blob/rpi-3.6.y/drivers/i2c/busses/i2c-bcm2708.c) by
[Chris Boot](http://www.bootc.net/projects/raspberry-pi-kernel/) &
[Frank Buss](https://github.com/FrankBuss/) and are loaded by default on the
[Occidentalis](http://learn.adafruit.com/adafruit-raspberry-pi-educational-linux-distro/occidentalis-v0-dot-2)
distribution from Adafruit that I use.

### SPI ###

Linux kernel has a driver for the SPI bus: spidev module. Access from userspace is through reads, write and ioctls on
[/dev/spidevB.C](http://www.kernel.org/doc/Documentation/spi/spidev) files. Kernel documentation gives
[examples](http://www.kernel.org/doc/Documentation/spi/spidev_test.c) of use.

Hardware-specific bits for SPI on BCM2708 were
[written](https://github.com/raspberrypi/linux/blob/rpi-3.6.y/drivers/spi/spi-bcm2708.c) by
[Chris Boot](http://www.bootc.net/projects/raspberry-pi-kernel/) and are loaded by default on the
[Occidentalis](http://learn.adafruit.com/adafruit-raspberry-pi-educational-linux-distro/occidentalis-v0-dot-2)
distribution.

### 1-Wire ###

Linux kernel has 1-Wire [support](http://www.kernel.org/doc/Documentation/w1/). Access from userspace is through sysfs
files: /sys/bus/w1/... There is a module that implements 1-Wire on GPIO pins through bit-banging (w1-gpio). Frank Buss
[patched](https://github.com/FrankBuss/linux-1/commit/71871509238d3e7bce4a74cdf616c3f12542acaa) the Raspberry Pi kernel
to [allow](http://www.raspberrypi.org/phpBB3/viewtopic.php?t=6649) for a bit-banged implementation of 1-Wire.
Occidentalis has this patch, but 1-Wire modules are not loaded by default.

If bit-banged 1-Wire turns out to be too computationally expensive, external I2C 1-Wire master like DS2482-100 can be
used. There is a Linux module that supports it (ds2482).

Alternatively, an external programmable device that implements 1-Wire in software can be used (for instance,
[TeensyPi](http://www.teensypi.com/) uses [Teensy](http://www.pjrc.com/teensy/) board), but that looks like an overkill
even to me :)

[OWFS](http://owfs.org/) - 1-Wire File System - is an open-source project that handles all kinds of 1-Wire stuff,
including 1-Wire masters connected via I2C, and
[works on Rasspbery Pi](http://raspberrypi.homelabs.org.uk/i2c-connected-1-wire-masters/).

### GPIO ###

GPIO is supported in Linux through reads and writes of files in
[/sys/class/gpio/](http://www.kernel.org/doc/Documentation/gpio.txt). This interface seems to support PWM and
edge detection too. GPIO pins provided by external chips (like I2C expanders) should work the same way (if a driver for
appropriate chip is loaded).

A new [pinctrl](https://github.com/torvalds/linux/blob/master/Documentation/pinctrl.txt) subsystem that supports pin
functionality is being developed.

This interface is suitable for use from shell, and is allegedly pretty slow, which is understandable: one has to
read/write words like "on" from/to files... But I do not need to bit-bang on the GPIO pins if I have access to I2C; I
only need to be able to detect a button press and blink a LED. The /sys/class/gpio interface is probably fast enough
for that :)

In fact, kernel GPIO documentation says:
> Note that standard kernel drivers exist for common "LEDs and Buttons"GPIO tasks: "leds-gpio" and "gpio_keys",
> respectively. Use those instead of talking directly to the GPIOs; they integrate with kernel frameworks better than
> your userspace code could.

I am not sure what the kernel can do with LEDs that I can not do from the userspace: blink them without software
involvement? I do understand what the kernel can do for the buttons: handle them via interrupts instead of polling, and
do de-bouncing. Maybe that is what gpio_keys module does?  Here is a tutorial on
[detecting GPIO interrupts](http://bec-systems.com/site/281/how-to-implement-an-interrupt-driven-gpio-input-in-linux)
in userspace. I am unclear on the gpio_keys support on Raspberry Pi.

The /sys/class/gpio interface does work on Raspberry Pi. Hardware-specific in
[bcm2708.c](https://github.com/raspberrypi/linux/blob/rpi-3.6.y/arch/arm/mach-bcm2708/bcm2708.c) and
[bcm2708_gpio.c](https://github.com/raspberrypi/linux/blob/rpi-3.6.y/arch/arm/mach-bcm2708/bcm2708_gpio.c)
was written by Broadcom.

### UART ###

Serial interfaces are represented as /dev/ttyX devices on Linux.

Raspberry Pi UART appears as /dev/ttyAMA0 - after it is
[freed](http://learn.adafruit.com/adafruit-nfc-rfid-on-raspberry-pi/freeing-uart-on-the-pi) from other purposes
(console?) it is dedicated to.

## Raspberry Pi Peripherals and Scala ##

### I2C/SMB ###

Peter Simon (rotok) [announced](http://www.raspberrypi.org/phpBB3/viewtopic.php?p=187073#p187073) a Java JNA
[binding](http://simonp.uw.hu/linuxi2c/linuxi2c_java_v0_1.zip) for Linux I2C on 10/3/2012. According to a comment on his
announcement, it is the first Java I2C binding:
> Thank you Peter! People have been looking for a Linux I2C Java binding for years and your code is the first publicly
> available binding, nice!".

The binding uses native calls to open/close/read/write a file and for ioctl. Since there is a way to obtain a native
file descriptor number from a file that was opened from Java (using sun.misc.SharedSecrets), only one (!) JNA call
is really necessary: ioctl (one can hope that one of these years Java will get ioctl, and then no native calls will
be needed). I did my own binding in Scala using this one native call.

For completeness, I plan to expand coverage of my I2C binding beyond the basic functionality that I needed so far, using
i2c-tools as a guide. (I may implement the command-line utilities from i2c-tools also.)

I used Adafruit Python [code](https://github.com/adafruit/Adafruit-Raspberry-Pi-Python-Code.git) as a guide for the
parts from Adafruit (like the 4 digit 7 segment display). For SHT21 that,
[thanks to Hugo](http://forums.electricimp.com/discussion/comment/2280#Comment_2280), already worked with the Electric
Imp, it was a straightforward translation.

I plan to add coverage for more I2C parts, from Adafruit and otherwise, as I use them :)

### GPIO ###

There is a JNI binding for GPIO - [pi4j](http://pi4j.com/), developed by Robert Savage and Chris Walzl. I even tried to
use it, but some JNI parts of it were missing in the Maven repository... At some point, the author invited Peter Simon
to bring his I2C binding into the project, but I think that did not happen and they developed their own approach. I
prefer not to use JNI.

It may be possible to bind using JNA to BCM2835 library or
[WiringPi](https://projects.drogon.net/raspberry-pi/wiringpi/) library (the one pi4j binds to using JNI), but they are
not a part of distribution, and I'd rather be more self-contained.

There is a Java wrapper around /sys/class/gpio: [framboos](https://github.com/jkransen/framboos). It does not use JNI or
JNA, relying on Linux-standard way of working with the GPIO through files. RPi.GPIO also used this approach originally,
but switched to using I/O registers through /dev/mem, and claims that it is faster. For controlling LEDs and buttons,
if leds-gpio and gpio_keys do not work out, I'll use framboos (or, more likely, a Scala binding inspired by it).

To make GPIO fast from Scala, without non-standard libraries or JNI, I need access to Raspberry Pi I/O registers. I may
need it even if speed is not an issue, for alternative function select on the GPIO pins.

According to the documentation, peripherals are mammed into memory starting at physical address 0x20000000.

There are at least two ways of working with the arbitrary memory locations directly from Java (and Scala):
com.sun.jna.Native or sun.misc.Unsafe (an instance of which has to be obtained using
[reflection](http://highlyscalable.wordpress.com/2012/02/02/direct-memory-access-in-java/). Version of JNA that is
currently on Raspberry Pi does not have the methods for direct memory access. Unsafe approach did not work for me: JVM
crashes! This is, probably, because - as  Chris Hatton [notes](http://www.chrishatton.org/archives/88) (referring to the
peripherals area of the memory as the "lower megabyte") - the process has to give Linux some kind of a notice before
accessing arbitrary memory locations.

This is probably why  RPi.GPIO, bcm2835 and WiringPi libraries and the peripherals
[tutorial](http://elinux.org/RPi_Low-level_peripherals#GPIO_Driving_Example_.28C.29) use memory-mapping an area of
/dev/mem file to access low-level peripherals through registers. Tutorial, following the code
[provided](http://www.raspberrypi.org/forum/educational-applications/gertboard/page-4/#p31555) by Gert van Loo and Dom,
maps a block of desired size without pre-allocating anything;  bcm2835 also maps without pre-allocating memory, although
there is a function for page-alligned allocation in the code (which is not called); RPi.GPIO pre-allocates a buffer and
mapps to that part of it that is page-alligned (using "fixed" mode); and so does WiringPi.

There is a method to memory-map a file in pure Java: FileChannel.map(). Unfortunatelly, it does not work for /dev/mem,
since its size is reported as 0, and Java's map() implementation attempts to "extend" the file, with comical results ;)

It seems that calling mmap through JNA is necessary. So far all attempts on my part to make that work failed: the JVM
crashes. It is now a challenge - to figure out a way to do this :)

### SPI and 1-Wire ###

For completeness, I might do SPI and  1-Wire bindings.
