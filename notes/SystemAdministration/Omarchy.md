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
