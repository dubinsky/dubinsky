---
title: Omarchy
---
* TOC
{:toc}
## Set Up
`Omarchy | Setup | DNS`: use DHCP

In `/etc/systemd/network/20-ethernet.network` and `/etc/systemd/network/20-wlan.network` add:
```
[Network]
UseDomains=yes
```

`Omarchy | Update | Firmware`

`Omarchy | Install | Service | Bitwarden`

To be able to log into Google account and synchronize bookmarks etc., run:
`Omarchy | Install | Service | Chromium Account`

## Install
```shell
$ mise use java 21
$ sudo pacman -S direnv
$ sudo pacman -S mc
$ sudo pacman -S solaar
$ sudo pacman -S rclone
$ sudo pacman -S syncthing
$ sudo pacman -S calibre
$ sudo yay -S zotero
$ sudo pacman -S pulumi
$ pulumi plugin install language scala 0.5.0 --server github://api.github.com/VirtusLab/besom
$ sudo pacman -R 1password-cli 1password-beta
```

## DirEnv
To [hook direnv into the shell](https://direnv.net/docs/hook.html),
add the following line at the end of the `~/.bashrc` file:
```shell
eval "$(direnv hook bash)"
```

To add `direnv` status to the prompt, in `~/.config/starship.toml` add:
```toml
[direnv]
disabled = false
format = "[$symbol$allowed]($style)"
symbol = "e"
allowed_msg = ""
not_allowed_msg = "✗"
```
and set:
```toml
format = "[$directory$git_branch$git_status]($style)$direnv$character"
```
## Yubikey
Install Yubikey utilities:
```shell
$ sudo pacman -S yubikey-personalization-gui yubikey-manager
```
Install dependencies required by `ssh-add` to work with Yubikey:
```shell
$ sudo pacman -S libfido2
```

Enable SSH agent:
```shell
$ systemctl enable --now --user ssh-agent.service
```

In `~/.config/uwsm/env`, add:
```shell
export SSH_AUTH_SOCK="$XDG_RUNTIME_DIR/ssh-agent.socket"
```
Restart the system.

To add Yubikey credentials to SSH:
```shell
$ ssh-add -K
```

To list credentials added to the agent:
```shell
$ ssh-add -L
```

## Obsidian
Enable community plugins.

For Obsidian to follow theme changes, its theme needs to be manually set to `Omarchy`; this theme becomes available in an Obsidian vault only after Omarchy theme was changed at least once after Obsidian vault was opened.

TODO there is no way to increase the font size of the user interface that I can see
## Monitors
Omarchy [manual](https://learn.omacom.io/2/the-omarchy-manual/86/monitors) mentions a TUI utility for setting up monitors - [Hyprmon](https://github.com/erans/hyprmon/) - but I found it easy enough to configure monitors by editing `~/.config/hypr/monitors.conf` directly.

In fact, the only lines I have there are:
```
env = GDK_SCALE,2
monitor=,preferred,auto,auto
```
and everything works fine with both my Framework laptop 13 OLED screen and my 6K Asus monitor :)

To make adjustments independently of the port the monitor is plugged in, use monitor description as a name instead.

To list connected monitors:
```shell
$ hyprctl monitors all
```

## ZSA Moonlander Keyboard
Follow [Keymapp installation](https://github.com/zsa/wally/wiki/Linux-install) instructions:

Download [latest Keymapp](https://oryx.nyc3.cdn.digitaloceanspaces.com/keymapp/keymapp-latest.tar.gz) and unpack it into `~/.local/share/Keymapp`.

Add to `~/.bash_profile`:
```shell
# Keymapp
export PATH="$PATH:/home/dub/.local/share/Keymapp"
```
Log out and back in for the `PATH` to change :)

Install required dependencies:
```shell
sudo pacman -S libusb webkit2gtk-4.1 gtk3
```

Create `/etc/udev/rules.d/50-zsa.rules` with:
```
# Rules for Oryx web flashing and live training
KERNEL=="hidraw*", ATTRS{idVendor}=="16c0", MODE="0664", GROUP="plugdev"
KERNEL=="hidraw*", ATTRS{idVendor}=="3297", MODE="0664", GROUP="plugdev"

# Keymapp / Wally Flashing rules for the Moonlander and Planck EZ
SUBSYSTEMS=="usb", ATTRS{idVendor}=="0483", ATTRS{idProduct}=="df11", MODE:="0666", SYMLINK+="stm32_dfu"
# Keymapp Flashing rules for the Voyager
SUBSYSTEMS=="usb", ATTRS{idVendor}=="3297", MODE:="0666", SYMLINK+="ignition_dfu"
```

Then:
```shell
$ sudo groupadd plugdev
$ sudo usermod -aG plugdev $USER
$ sudo udevadm control --reload
$ sudo udevadm trigger
```
Log out and back in for the group to apply :)

TODO add desktop entry and/or key binding...
## Fonts

Terminal: font is configured in  `~/.config/alacritty/alacritty.toml`:
```toml
[font]
normal = { family = "JetBrainsMono Nerd Font" }
size = 20
```
There is a TUI for changing the terminal font, but not for changing font size.

Top bar (`Waybar`): font is configured in `~/.config/waybar/style.css`:
```css
* {
  font-family: 'JetBrainsMono Nerd Font';
  font-size: 20px;
}
```
## Keyboard Layouts
[Omarchy Manual](https://learn.omacom.io/2/the-omarchy-manual/78/keyboard-mouse-trackpad) explains that keyboard layout can be added in `~/.config/hypr/input.conf`:

```
input {
  # Use multiple keyboard layouts and switch between them with Left Alt + Right Alt
  kb_layout = us,ru(phonetic)
  kb_options = compose:caps,grp:alts_toggle
```

To list available keyboard layouts:
```shell
$ localectl list-keymaps
```

The list of available toggles:
```shell
$ grep -hroE 'grp:.*toggle ' /usr/share/X11/xkb/rules/ | sort -u
```
Omarchy Manual also [points](https://learn.omacom.io/2/the-omarchy-manual/67/faq) 
to [instructions](https://github.com/basecamp/omarchy/discussions/111)
on how to add layout indicator and selector to the top bar
in `~/.config/waybar/config.jsonc`:

```json
"modules-right": [
  "hyprland/language",
],
"hyprland/language": {
  "format": "{}",
  "format-en": "US",
  "format-ru": "RU",
  "on-click": "hyprctl switchxkblayout <keyboard-device-name> next"
}
```

Keyboard device name can be determined by:
```shell
$ hyprctl devices
```

`<keyboard-device-name>` can also be `current` or `all`.

Styling of the above is configured in
`~/.config/waybar/style.css`:
```css
#language {
  min-width: 12px;
  margin: 0 7.5px;
}
```

To restart Waybar:
```shell
$ pkill waybar && hyprctl dispatch exec waybar
```

## JetBrains
JetBrains IDEs are available from AUR, but the best way to install and update them is via JetBrains Toolbox, which is also available from AUR :)

JetBrains IDEs now support Wayland natively, but it is not enabled by default; to [turn it on](https://youtrack.jetbrains.com/articles/SUPPORT-A-1166/Linux-how-to-turn-on-off-native-Wayland-mode): select `Help | Edit custom VM options` and add `-Dawt.toolkit.name=WLToolkit`.

I added a keyboard binding for the IDE in `~/.config/hypr/bindings.conf`:
```
bindd = SUPER SHIFT, J, IntelliJ Idea, exec, omarchy-launch-or-focus "^idea$" idea
```

When JetBrains IDEs are running on Wayland, their pop-ups are do not get focused, so pop-up where the IDE asks if you trust a project you are trying to open for a first time is not visible and there is no way to say "yes" (or even see the pop-up window) :(

To fix this, windows rules [need](https://github.com/basecamp/omarchy/discussions/100) to be [added](https://notes.dsebastien.net/30+Areas/33+Permanent+notes/33.02+Content/How+to+install+IntelliJ+IDEA+on+Omarchy). 

Create `~/.config/hypr/jetbrains.conf` with:
```
# Fix all dialogs in Jetbrains products
windowrulev2 = tag +jb, class:^jetbrains-.+$,floating:1
windowrulev2 = stayfocused, tag:jb
windowrulev2 = noinitialfocus, tag:jb
windowrulev2 = focusonactivate,class:^jetbrains-(?!toolbox)

# center the pops excepting context menu
windowrulev2 = move 30% 30%,class:^jetbrains-(?!toolbox),title:^(?!win.*),floating:1
windowrulev2 = size 40% 40%,class:^jetbrains-(?!toolbox),title:^(?!win.*),floating:1

# Fix tooltips (always have a title of `win.<id>`)
# Fix for sidebar menus being unclickable
windowrulev2 = noinitialfocus, class:^(.*jetbrains.*)$, title:^(win.*)$
windowrulev2 = nofocus, class:^(.*jetbrains.*)$, title:^(win.*)$
# Fix tab dragging (always have a single space character as their title)
windowrulev2 = noinitialfocus, class:^(.*jetbrains.*)$, title:^\\s$
windowrulev2 = nofocus, class:^(.*jetbrains.*)$, title:^\\s$
# Additional fixes for tab dragging
windowrulev2 = tag +jb, class:^jetbrains-.+$,floating:1
windowrulev2 = stayfocused, tag:jb
windowrulev2 = noinitialfocus, tag:jb
```

In `~/.config/hypr/hyprland.conf`, add:
```
# JetBrains IDEs
source = ~/.config/hypr/jetbrains.conf
```

Note: since Omarchy itself ships some windows rules in `~.local/share/omarchy/default/hypr/apps/jetbrains.conf`, it is not clear which (if any) of the above rules are actually needed...
## Bitwarden

Change keyboard binding from `1Password` to `Bitwarden` in
`~/.config/hypr/bindings.conf`:
```properties
# from:
bindd = SUPER SHIFT, SLASH, Passwords, exec, uwsm-app -- 1password
# to:
bindd = SUPER SHIFT, SLASH, Passwords, exec, uwsm-app -- bitwarden-desktop
```

TODO there does not seem any way to configure the font 

TODO
- .gnupg
- google cloud - gsutils
- devpod
- chirp
- oscar
- asciidoctor
- jekyll