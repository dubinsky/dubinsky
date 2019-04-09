---
layout: post
title: Linux on Lenovo ThinkPad p50
date: '2016-02-07T17:21:00.000-05:00'
author: Leonid Dubinsky
tags: [linux] 
modified_time: '2016-09-25T15:12:23.835-04:00'
blogger_id: tag:blogger.com,1999:blog-8681083740214020499.post-5982252303112752973
blogger_orig_url: https://blog.dub.podval.org/2016/02/linux-on-lenovo-thinkpad-p50.html
---

I am in the process of setting up my new Lenovo ThinkPad [P50](http://shop.lenovo.com/us/en/laptops/thinkpad/p-series/p50/).
Here are some problems I encountered and solutions I found. There is no lasting value to these notes, even as notes to
self: most of these issues will probably just go away soon. I am writing this mainly for the entertainment value.

#### Hardware ####

Since Lenovo RAM and storage upgrade prices are outrageous (2-4 times the market price!), it makes sense to buy their
base RAM and storage options and upgrade with aftermarket parts. On the other hand, their base storage option is a $140
hard drive, which is at this point completely useless. And the next one is a $400 256G SSD, which is around 5 times
overpriced... For me, the decision was made by the fact that Lenovo promised to ship in 10-12 business days, but NewEgg
had pre-configured  model with the CPU I wanted ready to ship. Since NewEgg - unlike Lenovo - doesn't take taxes, their
price was comparable with the Lenovo's even with the "friends and family" discount. I went with NewEgg - and the SSD
option.

I ordered 16G more of RAM and a 512G Samsung 950 Pro [NVMe SSD](http://www.newegg.com/Product/Product.aspx?Item=N82E16820147467)
and installed them myself. I consulted Hardware Maintenance [Manual](https://download.lenovo.com/pccbbs/mobiles_pdf/p50_ug_en.pdf),
and that is how I figured out that I am installing RAM in the wrong slot and the NVMe drive upside down :)

NVMe drives require a special [tray](http://shop.lenovo.com/us/en/itemdetails/4XB0K59917/460/7EF7D50A5A7047049A355BF42AAF3C5C).
I ordered one from Lenovo, but that takes a while. Although it was not clear from the specifications of the model I
ordered what format is the SSD - 2.5" or M.2, my machine came with an M.2 SSD, so I just replaced it with the one I
bought (I'll put the original one back into the machine once the tray arrives).

As [notebookcheck review](http://www.notebookcheck.net/Lenovo-ThinkPad-P50-Workstation-Review.158713.0.html) mentioned,
maintenance cover is "secured by small plastic clips, so you have to be careful not to break them". I used flat metal
thingy from a [toolkit](http://www.amazon.com/gp/product/B00VJYWRKW) I got. The clips survived :)

#### BIOS Update ####

Next, I tried booting into Fedora 23 Live USB stick. After much screen blinking, I got to the command line prompt, where
I found no graphical interface, no running installer and a bunch of errors.

I heard that some bugs were fixed in the later versions of the BIOS, and decided to upgrade it. Since at this point I
didn't have the Windows drive in the machine any longer, and in general I do now want to use Windows, I started looking
for a way to flash the BIOS without Windows.
  
Lenovo publishes a [bootable CD](http://support.lenovo.com/us/en/downloads/ds106109) image with the BIOS updater (which
is a little weird, since many laptops - including P50 - do not have a CD drive :)).
  
I wrote that image onto an USB stick; it didn't boot. Document
[Lenovo Thinkpad BIOS Update with Linux and USB](http://positon.org/lenovo-thinkpad-bios-update-with-linux-and-usb)
reminded me that CD image isn't a bootable disk image - it *contains* bootable disk images. There is a tool for
extracting first such from a CD image - "geteltorito". On Fedora 24, it is not in the “genisoimage” package, but in the
"geteltorito" package.
```  
  $ geteltorito -o bios.img n1eur16w.iso # last argument - Bootable CD image from Lenovo
  $ sudo dd if=bios.img of=/dev/sdX      # where the USB stick is
```
BIOS updated fine, but Fedora Live still didn't boot :(

#### Skylake ####

Among the errors that I saw booting Fedora 23 was "snd_hda_intel …  failed to add i915 component master", which -
although sound-related - mentions "i915", which is Intel integrated graphics driver. Linux kernel version 3.2 in Fedora
23 that I was trying to boot predates the Skylake processor in my machine, and I thought that the problems I was seeing
are caused by the lack of the Skylake support in the 3.2 kernel.
  
I didn't know any easy way of making a bootable Fedora USB stick with a kernel that supports Skylake, so I googled the
topic, and found out (in “[Early Intel Skylake Linux Users May Run Into A Silly Issue](http://www.phoronix.com/scan.php?page=news_item&amp;px=intel-skl-prelim-support)"
by [Michael Larabel](http://www.michaellarabel.com/) that 3.2 kernel *does* support Skylake - when a parameter is added
to the kernel command line: `i915.preliminary_hw_support=1`.

After this, I was able to boot Fedora 23 Live and install it onto my NVMe drive!

Booting into the newly-installed system didn't work, though.

I figured that the installed system also needs the "i915" parameter added to its kernel command line, so I rebooted into
the Fedora 23 Live USB, mounted what I needed and changed "grub.cfg". I know that I am supposed to change
"/etc/default/grub" instead and run grub2-mkconfig, but I need to mount more filesystems and do a "chroot" for that (see
[instructions](http://searchenterpriselinux.techtarget.com/tip/Its-an-easy-fix-to-clean-up-a-GRUB-error-on-your-Linux-server),
and I am too lazy. Also, once newly-install system boots, I update it, and current Fedora 23 kernel - 4.3.4 - seems to
have Skylake support enabled, so there is no need to supply any parameters and touching "/etc/default/grub".

Installed system still didn't boot.

#### EFI ####

I pressed F12 during boot, to see what can be booted. My drive was among the options, but even if I selected it manually,
list of boot options soon reappeared - as if BIOS tried to boot my drive and failed.

This is when I noticed that the drive is listed as "NVMe EFI" drive. I previously verified that BIOS is set to boot both
"legacy" (pre-UEFI) and UEFI OSs, and try "legacy" first. Now it seemed that the BIOS is hinting to me that it will only
boot UEFI systems from my NVMe drive - and Fedora 23 that I just installed there was, of course, installed as a "legacy"
OS!

I probably was supposed to know from UEFI by now - but I didn't :(
  
I found the excellent “[Linux on UEFI](http://www.rodsbooks.com/linux-uefi/): A Quick Installation Guide” by Roderick W.
Smith, and in the very beginning of it I learned that in order for my Linux installation to be UEFI, the installer needs
to boot in UEFI. My USB stick can boot as both "legacy" and UEFI, but since BIOS tries "legacy" first, it boots in
"legacy" mode. I set the BIOS to boot only UEFI - as the text recommended - and my installer booted the way it should
(and much faster - no time wasted trying the thegacy boot)!
  
Last hurdle: since I already had a "legacy" Linux installation on the drive, it had a "legacy" partition table, whereas
UEFI needs a GTP partition table... I used "gdisk" - a GTP flavor of fdisk - from Fedora Live to write a new GTP
partition table.

Fedora 23 installed and working!
