---
title: dotfiles
---
Around year 2000, I heard, saw, and - I vaguely recall - used myself a bare repository approach to store my configuration files. Since I change my desktop computer approximately every 6-7 years on the average, it did not make sense to formalize it.

Now that I use [[Omarchy]] on my laptop, and am likely to install it on my [[Desktop 2024]] too, maybe it is time to try again ;)

The options I see are:
- [GNU stow](https://www.gnu.org/software/stow/); although recommended in the Omarchy Manual, its symlink-based approach does not appeal to me;
- bare GIT repository: there are many [guides](https://www.atlassian.com/git/tutorials/dotfiles), but I'd like something that helps with creating and maintaining the dotfiles repository;
- [yadm](https://yadm.io/) seems to be a thin enough wrapper around a GIT repository which does help with routine tasks;
- [chezmoi](https://www.chezmoi.io/) gets a lot of praise for better templating than `yadm`, but since I don't even understand what templating is, let alone need it, I am going to go with `yadm` for now :)

`yadm` stores the dotfiles repository in `$HOME/.local/share/yadm/repo.git`.

I might store secrets in my `dotfiles` repository: Cloudflare API tokens, Google Cloud Platform Keys etc.; `yadm` provides [its own way](https://yadm.io/docs/encryption#) of doing that, but there are approaches that are not yadm-specific:
- [git-crypt](https://github.com/AGWA/git-crypt)
- [transcrypt](https://github.com/elasticdog/transcrypt)
- [git-secret](https://github.com/sobolevn/git-secret)
- [SOPS](https://github.com/getsops/sops)

`yadm` supports `transcrypt`  and `git-crypt` directly.

There is a way to add README to the `yadm` repository without polluting the home directory: https://github.com/yadm-dev/yadm/issues/93

