---
title: dotfiles
---
Notes on dotfiles in general and tweaks specific to [[Omarchy]];
my dotfiles repository is: [https://github.com/dubinsky/dotfiles](https://github.com/dubinsky/dotfiles).

* TOC
{:toc}
## Motivation

Around year 2000, I heard, saw, and - I vaguely recall - used myself
a bare repository approach to store my configuration files.
Since I change my desktop computer only every 6-7 years on the average,
it did not make sense to formalize it.

Now that I use [[Omarchy]] on both my [[Framework Laptop 13]] and [[Desktop 2024]],
maybe it is time to try again ;)

## Approach

The options I see are:
- [GNU stow](https://www.gnu.org/software/stow/):
although recommended in the Omarchy Manual,
its symlink-based approach does not appeal to me;
- bare GIT repository:
there are many [guides](https://www.atlassian.com/git/tutorials/dotfiles),
but I'd like something that helps with creating and maintaining the dotfiles repository;
- [yadm](https://yadm.io/) seems to be a thin enough wrapper around a GIT repository
which does help with routine tasks;
- [chezmoi](https://www.chezmoi.io/) gets a lot of praise for better templating than `yadm`,
but since I don't even understand what templating is, let alone need it,
I am going to go with `yadm` for now :)

`yadm` arranges for the home directory to be the work tree of the dotfiles repository
stored in `.local/share/yadm/repo.git`.

## Installation

To install `yadm` on `Omarchy`:

```shell
$ sudo pacman -S yadm
$ yadm init
$ yadm remote add origin "git@github.com:dubinsky/dotfiles.git"
$ yadm add <file>
$ yadm commit -m "<message>"
$ yadm push
```

## Bootstrap

After checkout, `yadm` [runs](https://yadm.io/docs/bootstrap)
`.config/yadm/bootstrap` executable;
it can also be run explicitly with
```shell
$ yadm bootstrap
```

I use a [script](https://github.com/dubinsky/dotfiles/tree/master/.config/yadm/bootstrap) copied from the `yadm` manual
which runs all the executables in `.config/yadm/bootstrap.d/`.

## Readme

There are [ways](https://github.com/yadm-dev/yadm/issues/93) to add README
to the `yadm` repository without polluting the home directory:
use `sparse checkout` or put `README` under `.github`.

My README is the note you are reading ;)

## Secrets

If I decide to store secrets in my `dotfiles` repository
(Cloudflare API tokens, Google Cloud Platform keys etc.),
`yadm` provides [its own way](https://yadm.io/docs/encryption)
of doing that, but there are approaches that are not `yadm`-specific:

- [git-crypt](https://github.com/AGWA/git-crypt)
- [transcrypt](https://github.com/elasticdog/transcrypt)
- [git-secret](https://github.com/sobolevn/git-secret)
- [SOPS](https://github.com/getsops/sops)

`yadm` supports `transcrypt`  and `git-crypt` directly.

## Clone

On the new machine, run:

```shell
$ yadm clone --bootstrap https://github.com/dubinsky/dotfiles.git
```


yadm does not change pre-existing files;
they can be overwritten with
```shell
$ yadm checkout <file>
```

## Manual Tweaks

### IntelliJ IDEA
Enable native Wayland support:
in `Help | Edit custom VM options` add
```
-Dawt.toolkit.name=WLToolkit
```
(see  JetBrains [support ticket](https://youtrack.jetbrains.com/articles/SUPPORT-A-1166/Linux-how-to-turn-on-off-native-Wayland-mode))

- to increase font size of both the user interface that the editor, set:
  * `Settings | Editor | Font | Size` to 22
  * `Settings | Appearance & Behavior | Appearance | Use custom font`  and `Size` to 22
  *  `Setting | Tools | Terminal | Font Settings | Size` to 22

### Obsidian
- open the vault in `Podval/dub.podval.org`
- change Omarchy theme (this makes Omarchy theme available in Obsidian)
- set theme to `Omarchy` so that Obsidian follows Omarchy theme changes
- enable community plugins
- to increase font size of both the user interface that the editor, set:
  * `Settings | Appearance | Advanced | Zoom level` to 150% and
  * `Settings | Appearance | Font size` to 16

### Zotero
- install [ZotMoove](https://github.com/wileyyugioh/zotmoov) plugin
- install [Better BibTex](https://retorque.re/zotero-better-bibtex/) plugin

### Chromium
- TODO what theme should I use to be able to change font size?
