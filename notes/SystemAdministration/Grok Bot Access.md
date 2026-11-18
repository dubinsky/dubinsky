---
tags: [sysadmin]
---

How Grok Bot reaches the homelab.
Checked 2026-11-18, read-only.
The bot's own `ssh` config is not on this desktop, so the alias lines below are the intended client config, and the
allow-list is what the jump host actually enforces.
No key material is written here.
Neither assistant makes a change without my approval.

Private details (the container address, the tailnet name, and the hosts on the allow-list) are in the private vault
`dubinsky/dubinsky-private`, at this same path.

## Path

Grok Bot is off the LAN.
Its SSH aliases `pve`, `docker`, and `ha` use `ProxyJump` through `grokbot@homelab-tailscale`.

`homelab-tailscale` is the Proxmox Tailscale container.
See [[ProxMox]].

The key file name is `~/.ssh/grokbot_homelab`.
The jump host has one key in `/home/grokbot/.ssh/authorized_keys`.

## What that user can do

The shell is `/usr/sbin/nologin`.
There is no sudoers entry.
`sshd` itself allows TCP forwarding.
The key options are what limit it: `restrict`, `port-forwarding`, and `permitopen` to port 22 only.
The hosts are Proxmox (`pve`), the Docker VM (`docker`), Home Assistant (`ha`), and UniFi OS.
An `unifi` alias was not part of the intended client config.
The addresses are in the private note.

`restrict` turns off a pty, agent forwarding, and the other extras.
`port-forwarding` turns forwarding back on, and `permitopen` is the whole list.

## This is not the subnet route

The Tailscale container still advertises one subnet route, and that route is what my own machines use off the LAN.
They then SSH as they do on the LAN, with the [[YubiKey]].
Grok Bot does not get that route.
Its key cannot open the rest of the LAN, and it cannot open anything except port 22 on those four hosts.

## Revoke

Delete `/home/grokbot/.ssh/authorized_keys` on the Tailscale container.
That removes the only key.
The user account can stay.

## Assistants

Homelab owns Proxmox, and a weekly read-only update check on Sundays.
Cloud Infrastructure owns the three Cloudflare accounts: Podval, OpenTorah, and CognoMath.
See [[Cloud Infrastructure]] and [[Domains]].
Neither makes changes without my approval.
