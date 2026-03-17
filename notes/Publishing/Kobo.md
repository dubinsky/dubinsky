---
title: Kobo
tags:
  - sysadmin
  - ebooks
---
## Background
Since 2011 both me and my wife use [Kobo](https://www.kobo.com/) devices for reading e-books.

I have a full-size [[Onyx Boox]] device for working with research papers in PDF; Kobo devices are used mostly for EPUBs.

I use:
- [Calibre](https://calibre-ebook.com/) to manage our library and to side-load books to our Kobo devices;
- [Obok](https://github.com/Satsuoni/DeDRM_tools) plugin for Calibre to back up books that we buy from Kobo ([previous](https://github.com/noDRM/DeDRM_tools); [original](https://www.epubor.com/calibre-kobo-drm-removal-plugin-obokplugin.html));
- [Quality Check](https://github.com/kiwidude68/calibre_plugins/wiki/Quality-Check) plugin for Calibre;
- [rclone](https://rclone.org/) to back the library up to Google Drive (and to our home server).

## Metadata

I prefer author names being displayed as (and not just sorted by) "Last, First".

If there are multiple authors, the `authors` field looks like `LN1, FN1 & LN2, FN2`.

`Kobo`, assumes that `authors` is structured like `FN1 LN1, FN2 LN2`.

I use `Metadata Plugboard` by [chaley](https://www.mobileread.com/forums/member.php?u=56920) ([2022](https://www.mobileread.com/forums/showpost.php?p=4261374&postcount=4), [2024](https://www.mobileread.com/forums/showpost.php?p=4385592&postcount=14)) to tweak the `authors` field for `KOBOTOUCH` devices:
```
program:
	comma = '';
	res = '';
	for author in $authors separator '&':
		res = strcat(res, comma, swap_around_comma(author));
		comma = ', '
	rof;
	res
```
It flips `LN, FN` into `FN LN` and changes author separator to `,`.

TODO this probably won't work when there is a third, "suffix" component in the name: `FN, LN, Dr.`...

PDF files that have multiple authors listed exactly the way `Kobo` wants still show up all wrong, as if the string listing them that `Kobo` splits is *quoted*: `"FN1 LN1, FN2 LN2"`, so those quotes are displayed as a part of the name!
## Firmware Patches
In 2026, my wife asked me how to search her `Kobo` device for a book *by author in Russian*?
Turns out, stock `Kobo` firmware does not have a Russian keyboard.

There is a project that maintains various firmware patches for `Kobo`: [Kobo Patches](https://github.com/pgaskin/kobopatch-patches); one of the patches (`Cyrillic Keboard`) replaces extended character set on the stock `Kobo` keyboard with Cyrillics.

[Instructions](https://www.mobileread.com/forums/tags.php?tag=kobopatch) for patching the firmware:
- Download and extract the [patches](https://github.com/pgaskin/kobopatch-patches/releases/latest)
- Download Kobo [firmware](https://pgaskin.net/KoboStuff/kobofirmware.html) and put it into the `src` folder
- Adjust `version` and `in` in `kobopatch.yaml` to the firmware version you downloaded
- Enable patches in the files in the `src` folder - or use the overrides in `kobopatch.yaml`
- Run `./kobopatch.sh`
- Copying `KoboRoot.tgz` into the `.kobo` folder of your device and eject it

## Alternative Readers
There seem to be two alternative readers available for Kobo: [KOReader](https://github.com/koreader/koreader) and [Plato](https://github.com/baskerville/plato).
One does not have to choose what to install: the same [installer](https://github.com/koreader/koreader/wiki/Installation-on-Kobo-devices) can be used to install [both](https://www.mobileread.com/forums/showthread.php?t=314220):
- download latest KOReader, Plato, or combined [one-click installer](https://www.mobileread.com/forums/showpost.php?p=3797095&postcount=1)
- download latest [installation script](https://www.mobileread.com/forums/showpost.php?p=3797096&postcount=2)
- unzip installation script archive alongside the reader(s)
- connect your device
- run installation script

Plato has a very clean interface, and I'd probably choose it over KOReader even if it misses some non-essential features, but it lacks the ability to use both English and Russian keyboards in search!
By editing Plato's `Settings.toml` file on the device Plato is installed on, I can *choose* between English and Russian keyboards - but I can't have *both* :(

## Web Apps
There is a number of web apps available that let you browse your library, organize it, and read books; two examples are [Calibre Web](https://github.com/janeczku/calibre-web) and [Booklore](https://booklore.org/).

Both support a standard protocol for browsing, searching and downloading books directly from your device: [OPDS](https://opds.io/). Plato does not support it, but KOReader does.

KOReader does not support selecting multiple books for download, so the convenience of being able to download books from your library onto your device from anywhere (assuming your self-hosted instance of the web app is accessible from the Internet) is tampered by the inconvenience of doing it one book at a time.

Booklore supports a special "Kobo shelf" where you put all the books you want on your device; this only works with the stock Kobo reader, not with KOReader.

The *real* deal-breaker with OPDS is two-fold:
- KOReader search by Calibre metadata does not include books downloaded over OPDS - only books placed on the device by Calibre;
- all books downloaded over OPDS end up in the same directory, not in per-author sub-directories like they do when placed on the device by Calibre, so you can not even *browse* the books on your device by author!

So, to retain the ability to search *and* browse books by author one can not use OPDS and must use Calibre to put the books on the device. Of course, this also removes the one-book-at-a-time restriction ;)

With Calibre being the way to deliver the books to the device, attractiveness of web apps for me drops precipitously: I can not switch to the library management via web app, so nice features like better metadata retrievers, magic shelves and such are of no use.

## Syncthing

https://guissmo.com/blog/how-to-install-syncthing-on-a-kobo/

Connect Kobo to and put public SSH key into KOBO/run/media/dub/KOBOeReader/.adds/koreader/settings/SSH/authorized_keys

On Kobo, run KOReader
- Cog | Network | check "Wi-Fi Connection"
- Cog | Network | SSH Server:
	- check "Login without password"
	- check "SSH server"
	- note the IP address: *address*

Prepare certificates:TODO this is probably not needed; verify
- ssh root@*address* -p 2222
	- mkdir /etc/ssl
	- mkdir /etc/ssl/certs
- scp -P /etc/ssl/certs/ca-certificates.crt root@IP address:/etc/ssl/certs/

Install syncthing:
- download the [syncthing](https://syncthing.net/downloads/) for Linux ARM (32‑bit) and unpack it
- scp -P 2222 syncthing root@IP address:/mnt/onboard/.adds/
- ssh root@*address* -p 2222
	- ip link set lo up (for some reason, it was not up)
	- run syncthing
- scp -P 2222 config.xml root@*address*:/.local/state/syncthing/config.xml
- change gui/address to from 127.0.0.1:8384 to 0.0.0.0:8384
- ssh root@*address* -p 2222
	- run syncthing

Configure the sync:
- browse to http://address:8384 - syncthing UI
	- set user/password (dub/whatwhen)
	- set the default folder to /mn/onboard/SyncThing
	- add device to sync from
	- auto-accept the folders
- start syncthing; in its UI:
	- add kobo device
	- share folders

On Kobo:
`/mnt/onboard/.adds/scripts/syncthing-start`:
```shell
#!/bin/sh
/mnt/onboard/.adds/syncthing serve &
```
`/mnt/onboard/.adds/scripts/syncthing-stop`:
```shell
#!/bin/sh
/usr/bin/pkill syncthing
```
`/mnt/onboard/.adds/nm/syncthing`:
```
menu_item :main    :Start Syncthing    :cmd_spawn         :quiet :exec /mnt/onboard/.adds/scripts/syncthing-start
  chain_always                         :nickel_setting    :enable :force_wifi
  chain_always                         :nickel_wifi       :enable
  chain_always                         :nickel_wifi       :autoconnect_silent
  chain_success                        :cmd_spawn         :quiet :exec /mnt/onboard/.adds/scripts/syncthing-start
menu_item :main    :Stop Syncthing     :cmd_spawn         :quiet :exec /mnt/onboard/.adds/scripts/syncthing-stop
  chain_always                         :nickel_setting    :disable :force_wifi
  chain_always                         :nickel_wifi       :disable
```

Authors of PDF files are displayed badly on Kobo!!

Authors of EPUBs are also not great when not processed by Calibre - can I make it write the metadata into the file itself? Otherwise, syncthing is unusable for epubs...

There seems to be no good way to set author names with titles so that they display correctly both on Kobo (FN LN) and in Calibre (where I use LN FN)...