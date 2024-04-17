  * #computer
  * Upgrade in-place: https://docs.fedoraproject.org/en-US/quick-docs/upgrading-fedora-offline/
  * **Boot**
```shell
$ efibootmgr
$ efibootmgr --bootnext XXXX # hex id of the drive you want to boot with
```
  * **Partitions and File Systems**
    * GUID Partition Table:
```plain text
/dev/nvme0n1p1  FAT/EFI  628M used  3%
/dev/nvme0n1p2  EXT4    1.1GB used 22%
/dev/nvme0n1p3  BTRFS
```
    * /etc/fstab:
```plain text
/dev/nvme0n1p1    /boot/efi    vfat    umask=0077,shortname=winnt    0 2
/dev/nvme0n1p2    /boot        ext4    defaults                      1 2
/dev/nvme0n1p3    /            btrfs   subvol=fc35                   0 0
/dev/nvme0n1p3    /home        btrfs   subvol=home,compress=zstd:1   0 0
/dev/nvme0n1p3    /mnt/f36     btrfs   subvol=fc36                   0 0
```
      * (Clean up `/etc/fstab` to use (labels or at least) partition devices as above.)
    * to add/rename btrfs subvolumes:
```shell
$ sudo mount /dev/nvme0n1p3 /mnt
$ cd /mnt
$ sudo btrfs subvolume create f36
($ sudo chmod u+w f36)
$ sudo mv root f35
$ sudo btrfs subvolume list .
$ cd
$ sudo umount /mnt
$ sudo grubby --update-kernel=ALL --args=rootflags=subvol=f35
$ sudo grubby --info=ALL
```
    * btrfs UI
      * https://gitlab.com/btrfs-assistant/btrfs-assistant
  * **hostname**
```shell
$ sudo dnf install mc
$ sudo hostnamectl set-hostname dub.lan.podval.org   # server: box
```
  * **rpmfusion**
```shell
$ sudo dnf install https://mirrors.rpmfusion.org/free/fedora/rpmfusion-free-release-$(rpm -E %fedora).noarch.rpm
$ sudo dnf config-manager --set-enabled rpmfusion-free
($ sudo dnf install https://mirrors.rpmfusion.org/nonfree/fedora/rpmfusion-nonfree-release-$(rpm -E %fedora).noarch.rpm)
($ sudo dnf config-manager --set-enabled rpmfusion-nonfree)
```
  * **vlc**
```shell
$ sudo dnf install vlc
```
  * **Update**
    * Packages:
```shell
$ sudo dnf update
```
    * from Beta
      * If you are using a pre-release of Fedora, you shouldn’t need to do anything to get the final public release, other than updating packages as they become available. When the pre-release is released as final, the `fedora-repos` packages will be updated and your `updates-testing` repository will be disabled. Once this happens (on the release day), it is highly recommended to run `$ sudo dnf distro-sync` in order to align package versions with the current release.
      * Remove unused packages that were not installed manually: `$ sudo dnf autoremove`
  * **Gnome**
    * Install:
```shell
($ sudo dnf install gnome-tweaks)?
($ sudo dnf install gnome-extensions-app)? # enable/disable all extensions
($ sudo dnf install xprop # Gnome/apps use it, but it is not installed by default)
```
    * In `Settings | Keyboard |`:
      * In `Keyboard Shortcuts`:
        * set `Switch windows` to `Alt-Tab`
        * set `Switch to next input source` to `Alt-Space`
      * In  `Input Sources` add Russian (US, phonetic) layout
  * **Chrome**
    * Install:
```shell
$ sudo dnf install google-chrome-stable
```
    * Transplant Chrome profile: `~/.config/google-chrome/Default`
    * Transplant Chrome cache: `~/.cache/google-chrome`
    * Install Gnome Shell Integration Chrome Extension
    * Install Extensions from Chrome
      * Application Menu
      * Places Status Indicator
      * Dash to Dock
      * Unite
      * Windows List
  * **SSH**
    * Transplant:
      * `~/.ssh`
      *  `~/.config/Yubico`
    * Server:
      * In `/etc/ssh/sshd_config`:
        * PasswordAuthentication no
      * Enable and start:
```shell
$ sudo systemctl enable sshd
$ sudo systemctl start sshd
```
  * **Docker** ^SlVFpD61s
    * [Documentation](https://docs.docker.com/reference/)
    * [Fedora Instructions](https://linuxconfig.org/how-to-install-and-configure-docker-ce-moby-engine-on-fedora-32)
    * Install:
```shell
$ sudo dnf install moby-engine docker-compose
$ sudo systemctl enable docker
$ sudo usermod -aG docker $USER
# Test (after restart)
$ docker run hello-world

(GUI: lazydocker
$ sudo dnf copr enable atim/lazydocker
$ sudo dnf install lazydocker)
```
    * Transplant: `~/.docker`
  * **Google Cloud SDK**
    * Install:
```shell
$ sudo tee -a /etc/yum.repos.d/google-cloud-sdk.repo << EOM
[google-cloud-cli]
name=Google Cloud CLI
baseurl=https://packages.cloud.google.com/yum/repos/cloud-sdk-el8-x86_64
enabled=1
gpgcheck=1
repo_gpgcheck=0
gpgkey=https://packages.cloud.google.com/yum/doc/yum-key.gpg
       https://packages.cloud.google.com/yum/doc/rpm-package-key.gpg
EOM

$ sudo dnf install google-cloud-cli google-cloud-cli-app-engine-java
```
    * Transplant:
      * `~/.gsutils`
  * **C**
    * Install:
```shell
$ sudo dnf group install "C Development Tools and Libraries"
```
  * **Calibre**
    * Install:
```shell
$ sudo -v && wget -nv -O- https://download.calibre-ebook.com/linux-installer.sh | sudo sh /dev/stdin
```
    * Transplant:
      * `~/.cache/calibre`
      * `~/.config/calibre`
      * `~/.local/share/calibre-ebook.com`
      * `~/.local/share/calibre-parallel`
  * **Steam**
    * Install:
```shell
$ sudo dnf install steam
```
    * Transplant:
      * `~/.local/share/Steam`
      * `~/.local/share/applications/Portal.desktop`
      * `~/.steam`
  * **rclone**
    * Install:
```shell
$ sudo dnf install rclone
```
    * Transplant:
      * `~/.cache/rclone`
      * `~/.config/rclone`
  * **node**
    * do: 
```shell
$ sudo rpm -e nodejs nodejs-full-i18n
$ curl -sL https://rpm.nodesource.com/setup_18.x | sudo bash -
$ sudo dnf install nodejs yarnpkg
```
  * **Install Packages**
    * System:
```shell
$ sudo dnf install java-11-openjdk-devel
$ sudo dnf install solaar # Logitech mice
```
    * Leonid:/
```shell
$ sudo dnf install git-filter-repo
```
    * Nina:
```shell
$ sudo dnf install gimp
$ sudo dnf install inkscape
($ sudo dnf install kolourpaint)
($ sudo dnf install krita)
($ sudo dnf install mypaint)
# WebStorm/JetBrains Toolbox
GDevelop
(OBS Studion)
# Skype Zoom Telegram
```
  * **Transplant ~/ dot-files**
    * .aws
    * `.cache`
      * JetBrains
      * wally
    * `.config`
      * JetBrains
    * .ghconfig
    * .gnupg?
    * .gradle?
    * .grubhub
    * .gsutils
    * .java
    * .jbr
    * `.local`
      * bin
        * qmk
      * share
        * JetBrains
        * applications
          * jetbrains-*.desktop
    * .m2?
    * .minecraft
    * .pip
    * bin
    * minecraft-launcher
    * .bash_profile
    * .bashrc
    * .boto
    * .gitconfig
  * ## DNSMASQ
    * To use dnsmasq started by the NetworkManager, under /etc/NetworkManager/:
    * In /conf.d/dnsmasq.conf:
    *   [main]
    *   dns=dnsmasq
    *     # addn-hosts=/etc/hosts # to read /etc/hosts
    * In /dnsmasq.d/no-conflicts.conf:
    *   To avoid conflicts with another DNS server (e.g., another copy of dnsmasq):
    *   listen-address=127.0.0.1
    *   bind-interfaces
    *   domain-needed
    * libvirtd runs internal dnsmasq that *MAY* be a problem; to disable:
    * # virsh net-autostart --network default --disable
    * (see https://docs.openstack.org/mitaka/networking-guide/misc-libvirt.html)]
    * [Wally: https://github.com/zsa/wally/wiki/Linux-install
    * Oryx: https://github.com/zsa/wally/wiki/Live-training-on-Linux
    * # groupadd plugdev; usermod -aG plugdev $USER
  * ## Manually Install
    * Printers
      * https://wiki.debian.org/CUPSDriverlessPrinting
      * Brother drivers (using their tool) for MFC-J5930DW: http://support.brother.com/g/b/downloadend.aspx?c=us&lang=en&prod=mfcj5930dw_us_eu&os=127&dlid=dlf006893_000&flang=4&type3=625
    * Configure printers.
    * Epson scanner thing: iscan-gt-x770-bundle-1.0.1.x64.rpm
    * JetBrains Toolbox; IntelliJ (it installs the license).
    * [Oxygen]
  * ## http
    * link **/var/www/podval.org** -> /mnt/data/var/www/podval.org
    * link to /var/config/etc/httpd/conf.d/local.conf and /var/config/etc/httpd/conf.local.d
    * Copy /etc/httpd/conf.d/sds.podval.org.conf.
    * # systemctl enable httpd**; **systemctl start httpd