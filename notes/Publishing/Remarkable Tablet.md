---
tags:
  - sysadmin
---
- Setup
  - SSH password is shown on the tablet under Settings | Help | Copyright and licenses.

  - upload files via USB web interface (when enabled under "Storage" and the tablet connected) at http://10.11.99.1/
    (see [File Transfer](https://remarkablewiki.com/tips/file_transfer))
  - the tablet's LAN name is in the private note; over USB the address above is reMarkable's, not the house LAN
  - put the public SSH key on the tablet and configured local SSH so that `$ ssh remarkable` works (see
    [SSH](https://remarkablewiki.com/tech/ssh))
  - works in `mc` as a shell link
  - mount over SSH: `$ sshfs remarkable:/home/root <mountpoint>` (see
    [Linux Mount](https://remarkablewiki.com/tips/sshmount))
  - [rclone](https://remarkablewiki.com/tips/rclone)?
- Resources
  - https://my.remarkable.com
  - [Calibre Companion](http://faq.calibrecompanion.co/)
  - [Tips](https://remarkablewiki.com/tips/start)
  - [Cloud Document Sync](https://www.einkpads.com/products/ep-binder)  $20
- [KoReader Support](https://github.com/koreader/koreader/issues/6792)
  - [review](https://www.reddit.com/r/RemarkableTablet/comments/ks80ao/koreader_gamechanger_this_completes_the/)
  - KoReader [toggler](https://github.com/ddvk/remarkable-autoinstall/tree/master/rm2)
  - [toltec](https://github.com/toltec-dev/toltec)
    - opkg install rm2fb
    - opkg install [oxide](https://github.com/Eeems/oxide/releases)
      - [config](https://github.com/toltec-dev/toltec/blob/c7f3198b2b7cb83e86cb4e0b237a4a1a8c1acfa8/package/oxide/package)
      - [bug](https://github.com/Eeems/oxide/issues/149)
      - [gesture daemon](https://rmkit.dev/apps/genie)