My notes on dotfiles in general and tweaks specific to [[Omarchy]] are in [[dotfiles]].

* TOC {:toc}

## Links

- [Omarchy](https://omarchy.org/)
- [Omarchy Manual](https://learn.omacom.io/2/the-omarchy-manual)
- [Hyprland](https://wiki.hypr.land/)
- [Arch](https://wiki.archlinux.org/title/Main_page)
- [Omacom](https://learn.omacom.io/3/omacom)

A critique: [A Word on Omarchy](https://xn--gckvb8fzb.com/a-word-on-omarchy/?s=03).

## Patron

Class 256 (Mountain Hairpin) since 2026-09-03.

<p>
<a href="https://omarchy.org/"><img alt="Omarchy Class 256 Patron" width="300" height="300" src="https://omarchy.org/assets/images/badges/256/preview.webp"/></a>
</p>

## Screensaver

Idle timings live in `~/.config/omarchy/shell.json` (`idle.screensaver` / `idle.lock`, seconds from idle).
On this machine: 10 minutes then 15 minutes lock.

`omarchy-screensaver` exits as soon as it is not the active window.
`omarchy-launch-screensaver` restores the previously focused monitor after spawning, which refocuses the previous
window, so the screensaver dies in about two seconds and the idle service logs `screensaver-dismissed`.
Workaround in `~/.config/hypr/hyprland.lua`: `stay_focused` on `org.omarchy.screensaver`.
Dismiss with a key.

## Grok Bot

Official Linux desktop AppImage ([docs](https://docs.x.ai/grok-bot/get-started); downloads at
[x.ai/bot](https://x.ai/bot)).
Not in [[dotfiles]] bootstrap.
Needs `fuse2`.

The in-app updater replaces the AppImage in place and does not change its name.
The file is `~/Applications/Grok_Bot.AppImage`, with no version in the name, so a later update cannot leave a stale
version in the filename.
`fuse2` is installed.
The Omarchy package `grok-bot` is not installed.

An Omarchy repo package (menu **AI → Install → Grok Bot**) was installed 2026-09-14 and removed 2026-09-16.
The package repacks the official `.deb` of the same release.
Stay on the AppImage.
Under `/opt/Grok Bot/` the app finds no `/var/lib/dpkg/info/grok-bot.list`, treats the install as RPM, and the update
dialog offers an RPM download.
**AI → Remove → Grok Bot** also deletes `~/.config/Grok Bot`.

| What | Where |
|---|---|
| Live binary | `~/Applications/Grok_Bot.AppImage` |
| Integrator | `~/.local/share/grok-bot/appimage` → that file |
| Command | `grok-bot` (`~/.local/bin/grok-bot`) |
| Menu | `~/.local/share/applications/grok-bot.desktop` |
| Profile | `~/.config/Grok Bot` |

Start from the app menu or `grok-bot`.
The app checks for updates on its own.
Upgrade in-app: **Settings → Updates → Check for Updates**, then **Restart to Update**.
`omarchy update` does not touch it.
Its mise step updates Grok Build (`grok`).

Not Grok Build (`grok`).
