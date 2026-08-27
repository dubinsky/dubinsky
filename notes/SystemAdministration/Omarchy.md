My notes on dotfiles in general and tweaks specific to [[Omarchy]] are in [[dotfiles]].

* TOC
{:toc}

## Links

- [Omarchy](https://omarchy.org/)
- [Omarchy Manual](https://learn.omacom.io/2/the-omarchy-manual)
- [Hyprland](https://wiki.hypr.land/)
- [Arch](https://wiki.archlinux.org/title/Main_page)
- [Omacom](https://learn.omacom.io/3/omacom)

A critique: [A Word on Omarchy](https://xn--gckvb8fzb.com/a-word-on-omarchy/?s=03).

## Screensaver

Idle timings live in `~/.config/omarchy/shell.json` (`idle.screensaver` / `idle.lock`, seconds from idle). On this machine: 10 minutes then 15 minutes lock.

`omarchy-screensaver` exits as soon as it is not the active window. `omarchy-launch-screensaver` restores the previously focused monitor after spawning, which refocuses the previous window, so the screensaver dies in about two seconds and the idle service logs `screensaver-dismissed`. Workaround in `~/.config/hypr/hyprland.lua`: `stay_focused` on `org.omarchy.screensaver`. Dismiss with a key.

## Grok Bot

There is no official Linux desktop app ([docs](https://docs.x.ai/grok-bot/get-started)). This machine runs the unofficial [glorics/grok-bot-linux](https://github.com/glorics/grok-bot-linux) AppImage (Windows payload on Linux Electron). Not in [[dotfiles]] bootstrap; not a pacman/AUR package. Installed 2026-08-27 at **0.28.1** (in-app version 0.28.0). Needs `fuse2`.

| What | Where |
|---|---|
| Live binary | `~/Applications/GrokBot-current.AppImage` |
| Versioned copy | `~/Applications/Grok_Bot_<ver>_x86_64.AppImage` |
| Command | `grok-bot` (`~/.local/bin/grok-bot`; `--no-sandbox`) |
| Menu | `~/.local/share/applications/grok-bot.desktop` |

`omarchy update` does not touch it. To upgrade: quit the running app, download a newer AppImage from [releases](https://github.com/glorics/grok-bot-linux/releases), check the SHA256 on that page, then:

```shell
$ git clone --depth 1 --branch vNEW https://github.com/glorics/grok-bot-linux.git
$ ./grok-bot-linux/scripts/install-linux.sh /path/to/Grok_Bot_NEW_x86_64.AppImage
```

Not Grok Build (`grok`).
