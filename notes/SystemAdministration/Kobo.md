---
title: Kobo
tags:
  - sysadmin
---
- Installed [NickelMenu for Kobo](https://github.com/pgaskin/NickelMenu)
- [KOReader installation](https://github.com/koreader/koreader/wiki/Installation-on-Kobo-devices)
- [Zotero for KOReader](https://github.com/stelzch/zotero.koplugin/)
- Kobo De-DR plugin: [Obok](https://www.epubor.com/calibre-kobo-drm-removal-plugin-obokplugin.html); []releases](https://github.com/noDRM/DeDRM_tools/releases)
- [Calibre Web with Kobo](https://github.com/janeczku/calibre-web/wiki/Kobo-Integration)
  - startup via systemd: https://github.com/janeczku/calibre-web/wiki/Setup-Service-on-Linux#start-calibre-web-as-service-under-linux-with-systemd
    -  Selinux: https://serverfault.com/questions/1032597/selinux-is-preventing-from-execute-access-on-the-file-centos
      - sudo semanage fcontext -a -t bin_t /home/dub/.local/bin
      - sudo restorecon -r -v /home/dub/.local/bin
  - On the device, in `.kobo/Kobo/eReader.conf`
    - default: `api_endpoint=https://storeapi.kobo.com`
- [kepubify](https://pgaskin.net/kepubify/dl/)
- [Kobo Customizations](https://code.mendhak.com/kobo-customizations/) by Mendhak