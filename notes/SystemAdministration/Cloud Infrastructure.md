LXC **108** on [[ProxMox]], hostname `cloud-infrastructure`.
This is the shared machine for Pulumi across Podval, OpenTorah, and CognoMath.
I reach it from this desktop with the [[YubiKey]], over Tailscale.
The cloud Grok Bot reaches it from the [[ProxMox]] host with `pct exec`.
Domain and DNS state stays in [[Domains]].
The library stays in [[Pulumi]].

* TOC {:toc}

## Guest

| | |
|---|---|
| ID | 108 |
| LAN | `192.168.1.239/24` on `vmbr0`, gateway `192.168.1.1` |
| UniFi | Fixed reservation for `bc:24:11:16:ef:ac`, name `cloud-infrastructure` |
| Size | 4 cores, 8G RAM, 512M swap, 32G on `local-lvm` |
| OS | Debian 13, unprivileged, nesting, `keyctl=1`, `/dev/net/tun` |
| Boot | `onboot: 1`, timezone `America/New_York` |
| JDK | Debian OpenJDK 25.0.5 |
| Pulumi CLI | 3.270.0 at `/usr/local/bin/pulumi` |

No sudo for `dub`.
Package upgrades run as root from the hypervisor:
`ssh pve "pct exec 108 -- bash -lc 'apt-get update && apt-get upgrade'"`.
That covers Tailscale and OpenJDK.
The Pulumi CLI in `/usr/local/bin` is not a Debian package.
Debian has no Pulumi package, and the desktop copy comes from Arch `pacman`.
The guest has the single `pulumi` binary from the official `3.270.0` Linux tarball, so the version stays pinned to the
desktop.
The upstream install script would unpack a whole release under `~/.pulumi` and follow newer releases.
`apt` does not upgrade this binary.
Language and provider plugins install separately.
Root SSH and password login are off.
`sshd` listens on all addresses, and nftables drops TCP 22 that arrives on `eth0`.
LXC 106 already routes `192.168.1.0/24` onto the tailnet, so a LAN listener would be reachable from there too.
From the host, `pct exec 108` still works when Tailscale is down.

Do not vzdump this guest, and do not leave a `pct snapshot` of it.
The disk holds the Pulumi credentials.
The code is in git and the state is in GCS, so the guest can be rebuilt.
The credentials themselves are copied from the desktop.

## Tailscale

Tailscale 1.104.6, the same package as LXC 106 and 107.
Login command: `tailscale up --hostname=cloud-infrastructure --ssh=false --accept-dns=false`.
No subnet routes, no Tailscale SSH, and LAN DNS stays `192.168.1.1`.
Funnel stays off.
The node is `cloud-infrastructure` at `100.68.230.126`, MagicDNS `cloud-infrastructure.tailc87f26.ts.net`.
It does not accept subnet routes.
From this desktop, the SSH host alias stays `infra`.
It connects to that MagicDNS name as `dub`, with the YubiKey.

## Access

The cloud Grok Bot administers the homelab from its own computer.
That computer's SSH key is on the Proxmox host and the other guests.
It does not join this tailnet.
Its Tailscale client reaches MCP servers granted to `tag:grokbot` and cannot log the computer into the tailnet.
It enters this guest from the host:
`pct exec 108 -- runuser -u dub -- bash -lc 'cd ~/Podval/infra && direnv exec . pulumi preview'`.
`pct exec` is root.
`runuser` runs Pulumi as `dub`.
The same form works in `~/OpenTorah/infra` and `~/CognoMath/infra`.

TCP 22 stays dropped on `eth0`.
LXC 106 advertises `192.168.1.0/24`, so an open port on the LAN address would be reachable from every tailnet device
that accepts that route.
The jump user on LXC 106 is not a way around that drop, and this guest is not on its allow-list.
Tailscale stays on this guest because the desktop login uses it.

## Checkouts

Same paths as the desktop, so the `includeBuild` defaults work without edits.
`~/Podval/infra` uses `../pulumi` and `../podval-gradle`.
`~/OpenTorah/infra` and `~/CognoMath/infra` use `../../Podval/pulumi` and `../../Podval/podval-gradle`.

| Checkout | Remote | Guest access |
|---|---|---|
| `~/Podval/infra` | `dubinsky/podval-infra` | read-only deploy key `cloud-infrastructure` |
| `~/OpenTorah/infra` | `opentorah/infra` | read-only deploy key `cloud-infrastructure` |
| `~/CognoMath/infra` | `cognomath/infra` | read-only deploy key `cloud-infrastructure` |
| `~/Podval/pulumi` | `dubinsky/pulumi` | public HTTPS |
| `~/Podval/podval-gradle` | `dubinsky/podval-gradle` | public HTTPS |

GitHub deploy keys belong to one repository and cannot be reused, so the guest has three read-only keys, all titled
`cloud-infrastructure`.
The private halves are `~/.ssh/podval-infra`, `~/.ssh/opentorah-infra`, and `~/.ssh/cognomath-infra` on the guest.
It can pull those repos.
It cannot push.

Each infra build uses that repo's Gradle wrapper.
`direnv` is hooked for interactive shells.
A non-interactive command has to be `direnv exec . pulumi preview`, because a plain `ssh` command does not load `.envrc`.

## Git identity

Commits use the desktop Git identity.
The name is Leonid Dubinsky.
Podval uses `dub@podval.org`.
OpenTorah uses `dub@opentorah.org`.
CognoMath uses `dub@cognomath.org`.

## Credentials

Six files, mode `0600`, directories mode `0700`.
These are the desktop copies, not a second set.

| Org | Service account key | Cloudflare token |
|---|---|---|
| Podval | `~/Podval/Keys/terraform-podval-infra.json` | `~/Podval/Keys/podval-cloudflare-api-token.txt` |
| OpenTorah | `~/OpenTorah/Keys/terraform-opentorah-infra.json` | `~/OpenTorah/Keys/opentorah-cloudflare-api-token.txt` |
| CognoMath | `~/CognoMath/Keys/terraform-cognomath-infra.json` | `~/CognoMath/Keys/cognomath-cloudflare-api-token.txt` |

The rest of each `Keys` directory stays on the desktop.
`~/.local/bin/copy-cloud-infrastructure-credentials` copies these six files over `ssh infra` and checks that the sizes
match.
It also copies `~/.config/git/config`, `~/OpenTorah/.gitconfig`, and `~/CognoMath/.gitconfig`.
State is in `gs://state.podval.org`, `gs://state.opentorah.org`, and `gs://state.cognomath.org`.

`pulumi preview` is the usual command.
`pulumi up` waits for an explicit go-ahead for that change.
The guest does not enforce that.
The cloud bot is root through `pct exec` on the host.

The desktop bootstrap and the guest both install `pulumi-language-scala` 0.5.2 from
`github://api.github.com/VirtusLab/besom`.
GCP is pinned to `9.0.0`, the plugin in `besom-gcp_3:9.0.0-core.0.5`.
Cloudflare is pinned to `6.20.0`, the plugin in `besom-cloudflare_3:6.20.0-core.0.5`.
Each stack config sets `gcp:version` and `cloudflare:version` to those versions.
The shared program sets the same version on the provider, on each resource, and on the GCP invokes, because the
resources use the default provider.
Desktop and guest both have Cloudflare `6.20.0` and GCP `9.0.0`.
The selected stack is not stored in git.
On a new checkout, `pulumi stack select organization/<project>/dev` once.
`<project>` is `podval-infra`, `opentorah-infra`, or `cognomath-infra`.

## Besom provider update

A newer Pulumi plugin binary is not a Besom upgrade.
The Besom jar's `plugin.json` names the plugin the program was generated for.
`besom-gcp_3:9.0.0-core.0.5` asks for GCP `9.0.0`.
`besom-cloudflare_3:6.20.0-core.0.5` asks for Cloudflare `6.20.0`.
Those were the newest jars on Maven Central on 2026-11-11.
Upstream plugins can be newer and still have no Besom jar.

Bump `besom-core_3`, `besom-gcp_3`, and `besom-cloudflare_3` together in `~/Podval/pulumi` and in each infra
`build.gradle`.
The infra builds also depend on published `org.podval.tools.pulumi`.
`includeBuild` of `~/Podval/pulumi` replaces that when the checkout is present, so the guest does not need a new library
release to compile the pin.

Change the version literals in `gcpResourceOptions` and `cloudflareResourceOptions`.
Besom accepts a string literal there and rejects a `val`.
Set `gcp:version` and `cloudflare:version` in each `Pulumi.dev.yaml` to those same versions.
The resources use the default provider, so the version has to be on each resource.
The provider resource option alone leaves the old `default_<version>` provider in state.
GCP lookups use `gcpInvokeOptions`.
There are no Cloudflare lookups.

Install that plugin on this desktop with `pulumi plugin install resource <name> <version>`.
`direnv exec . pulumi preview` for Podval, OpenTorah, and CognoMath.
`pulumi up` waits for an explicit go-ahead for that diff.
A GCP provider upgrade can replace groups.
`Group.initialGroupConfig` is ignored for that reason.

Then install the same plugin on the guest and remove the older plugin of that name.
The guest gets the code with `git pull` in `~/Podval/pulumi` and the three infra checkouts.
State is already in the buckets from the desktop `pulumi up`.
Do not run `pulumi up` on the guest only to copy state.
`pulumi preview` on the guest should be empty.
Remove an old plugin only after state no longer names it.
A stack that still records the old version makes the next preview download that plugin again.
