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

Official Linux desktop AppImage ([docs](https://docs.x.ai/grok-bot/get-started); downloads at [x.ai/bot](https://x.ai/bot)). Not in [[dotfiles]] bootstrap. Needs `fuse2`. Last session 2026-09-16 reported in-app **0.53.0**; on-disk file was `Grok_Bot_0.51.0.AppImage`.

An Omarchy repo package (`grok-bot` 0.29.0, menu **AI → Install → Grok Bot**) was installed 2026-09-14 and removed 2026-09-16: the repo lagged the AppImage. Do not reinstall from that menu until the package version catches up. **AI → Remove → Grok Bot** also deletes `~/.config/Grok Bot`.

| What | Where |
|---|---|
| Live binary | `~/Applications/Grok_Bot_<ver>.AppImage` |
| Integrator | `~/.local/share/grok-bot/appimage` → that file |
| Command | `grok-bot` (`~/.local/bin/grok-bot`) |
| Menu | `~/.local/share/applications/grok-bot.desktop` |
| Profile | `~/.config/Grok Bot` |

Start from the app menu or `grok-bot`. Upgrade in-app: **Settings → Beta → Check for Updates**. `omarchy update` does not touch it.

Not Grok Build (`grok`).
