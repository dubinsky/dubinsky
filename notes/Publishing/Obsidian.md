---
tags:
  - publishing
---
The story of migration of my notes from [[Roam]] to [[Obsidian]] is in [[publishing-notes-on-web]].

On [[2026-04-13]], bought Obsidian Sync - and now I have my notes on my phone too!

[[TODO]] buy Obsidian Publish to compare my Site Publisher to it.

[[TODO]] pay for the Catalyst privileges

[[TODO]] once my publisher is working, get rid of the title-inserting plugin and redundant titles (they were needed for
Jekyll); also, clean up repetitive author and empty tags.

## This vault

This repository is my Obsidian vault.
It is `dubinsky/dubinsky`, checked out at `~/Podval/dub.podval.org`, and it is what gets published at dub.podval.org.
Obsidian Sync keeps the vault in sync.
The desktop opens it from `Podval/dub.podval.org`.
See [[dotfiles]] for the theme and the CLI flag.

Grok Bot reads these notes through GitHub.
It edits only by opening a pull request, which I review.
It does not commit to `master`.

Agent inventory is the private repository `dubinsky/homelab`, checked out at `~/Podval/homelab`.
It is not a vault, and it is not published.
Grok Bot reads that repository through GitHub too.
Addresses and device lists are not in this vault.
Passwords, tokens, and keys are not written in either place.

House skills in `~/.grok/skills/` read this vault for the setup and `homelab` for the inventory.
See [[dotfiles]].
