---
title: Kobo
tags:
  - sysadmin
---
## Background
Since 2011 both me and my wife use [Kobo](https://www.kobo.com/) devices for reading e-books.

I have a full-size [[Onyx Boox]] device for working with research papers in PDF; Kobo devices are used mostly for EPUBs.

I use [Calibre](https://calibre-ebook.com/) to manage our library and to side-load books to our Kobo devices.

I use [rclone](https://rclone.org/) to back the library up to Google Drive.

I use [Obok](https://github.com/Satsuoni/DeDRM_tools) plugin for Calibre to back up books that we buy from Kobo ([previous](https://github.com/noDRM/DeDRM_tools); [original](https://www.epubor.com/calibre-kobo-drm-removal-plugin-obokplugin.html)).

## Non-Latin Metadata Search
In 2026, my wife asked me how to search her Kobo device for a book *by author in Russian*?

Turns out, stock Kobo software does not have a Russian keyboard, so the answer is "you can't"! Since that answer is not satisfactory, I started looking for ways to get the ability to search using Russian keyboard.

There used to be [Kobo Hacks](https://pip.cat/khd/) and [Kobo Patches]([https://www.mobileread.com/forums/tags.php?tag=kobopatch](https://github.com/pgaskin/kobopatch-patches)) that replaced extended character set on the stock Kobo keyboard with Russian, but they do not seem to work with the 2025 Kobo firmware. So it seems that *the only way to get Russian keyboard for searches is to switch from stock Kobo reader to some other reader*!

## Alternative Readers
There seem to be two alternative readers available for Kobo: [KOReader](https://github.com/koreader/koreader) and [Plato](https://github.com/baskerville/plato).
One does not have to choose what to install: the same [installer](https://github.com/koreader/koreader/wiki/Installation-on-Kobo-devices) can be used to install [both](https://www.mobileread.com/forums/showthread.php?t=314220):
- download latest KOReader, Plato, or combined [one-click installer](https://www.mobileread.com/forums/showpost.php?p=3797095&postcount=1)
- download latest [installation script](https://www.mobileread.com/forums/showpost.php?p=3797096&postcount=2)
- unzip installation script archive alongside the reader(s)
- connect your device
- run installation script

Plato has a very clean interface, and I'd probably choose it over KOReader even if it misses some non-essential features, but it seems to be missing *the* feature that started this quest for alternatives: ability to use both English and Russian keyboards in search!
By editing Plato's `Settings.toml` file on the device Plato is installed on, I can *choose* between English and Russian keyboards - but I can't have *both* :(

So, it is KOReader or no Russian...

## Web Apps
There is a number of web apps available that let you browse your library, organize it, and read books; two examples are [Calibre Web](https://github.com/janeczku/calibre-web) and [Booklore](https://booklore.org/).

Both support a standard protocol for browsing, searching and downloading books directly from your device: [OPDS](https://opds.io/). Plato does not support it, but KOReader does.

KOreader does not support selecting multiple books for download, so the convenience of being able to download books from your library onto your device from anywhere (assuming your self-hosted instance of the web app is accessible from the Internet) is tampered by the inconvenience of doing it one book at a time.

Although Booklore supports a special "Kobo shelf" where you put all the books you want on your device, this only works with the stock Kobo reader - which does not support searches in Russian...

The *real* deal-breaker with OPDS is two-fold:
- KOReader search by Calibre metadata does not include books downloaded over OPDS - only books placed on the device by Calibre;
- all books downloaded over OPDS end up in the same directory, not in per-author sub-directories like they do when placed on the device by Calibre, so you can not even *browse* the books on your device by author!

So, to retain the ability to search *and* browse books by author one can not use OPDS and must use Calibre to put the books on the device. Of course, this also removes the one-book-at-a-time restriction ;)

With Calibre being the way to deliver the books to the device, attractiveness of web apps for me drops precipitously: I can not switch to the library management via web app, so nice features like better metadata retrievers, magic shelves and such are of no use.

## Conclusion
Somewhat surprisingly, in 2026, even though e-readers are common for many years now, and even though numerous open-source projects in this area are available, you do not have much flexibility in your choice of the "reading stack": if you use a Kobo device and want to be able to search book metadata on your device using a *non-Latin script*:
- you *must* use KOReader to read your books.
- you *must* use Calibre to put your books on your device;
- web apps for managing your library are of not much value.
