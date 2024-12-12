---
title: BTRFS
tags:
  - sysadmin
---
add/rename btrfs subvolumes:
```shell
$ sudo mount /dev/nvme0n1p3 /mnt
$ cd /mnt
$ sudo btrfs subvolume create root
$ sudo mv f35t f36
$ sudo btrfs subvolume list .
$ cd
$ sudo umount /mnt
$ sudo grubby --update-kernel=ALL --args=rootflags=subvol=root
$ sudo grubby --info=ALL
```

btrfs UI: https://gitlab.com/btrfs-assistant/btrfs-assistant
