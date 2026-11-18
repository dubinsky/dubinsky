The Cloud Infrastructure Grok Bot runs Pulumi for Podval, OpenTorah, and CognoMath on its own computer.
It runs only `pulumi preview`, and `pulumi preview --refresh` for drift checks.
I run everything that writes on this desktop: `pulumi up`, refresh, import, and state surgery, with the `terraform`
keys.
Code changes go through pull requests.
Domain and DNS state stays in [[Domains]].
The library stays in [[Pulumi]].

For a while, what Grok Bot does now on its own computer happened in a dedicated container in the homelab.
That was more complex but not safer.

* TOC {:toc}

## Changes

When I ask the cloud Grok Bot for a change, it has a Cursor cloud agent open a pull request.
The pull request is in the library or an infra repo.
The repos are `dubinsky/podval-infra`, `opentorah/infra`, and `cognomath/infra`.
The library is `dubinsky/pulumi`.
Each commit uses that org's identity.
The name is Leonid Dubinsky.
Podval and the `dubinsky/pulumi` library use `dub@podval.org`.
OpenTorah uses `dub@opentorah.org`.
CognoMath uses `dub@cognomath.org`.
I review and merge.
I publish the library to Maven Central myself.
A library pull request has to merge before an infra pull request that depends on it.
The bot does not run `pulumi up`.

## Credentials

`.envrc` in each infra repo uses `terraform-<project>.json` and `<org>-cloudflare-api-token.txt` when those files are
present.
That is this desktop.
Otherwise it falls back to `grok-bot-<org>-infra.json` and `grok-bot-<org>-cloudflare-api-token.txt`.
That is the bot's computer.
`<org>` is `podval`, `opentorah`, or `cognomath`.
On this desktop the files are in `~/<Org>/Keys/`.

| Org | Desktop, when present | Bot fallback |
|---|---|---|
| Podval | `terraform-podval-infra.json`, `podval-cloudflare-api-token.txt` | `grok-bot-podval-infra.json`, `grok-bot-podval-cloudflare-api-token.txt` |
| OpenTorah | `terraform-opentorah-infra.json`, `opentorah-cloudflare-api-token.txt` | `grok-bot-opentorah-infra.json`, `grok-bot-opentorah-cloudflare-api-token.txt` |
| CognoMath | `terraform-cognomath-infra.json`, `cognomath-cloudflare-api-token.txt` | `grok-bot-cognomath-infra.json`, `grok-bot-cognomath-cloudflare-api-token.txt` |

The desktop runs Pulumi as `terraform@<project>.iam.gserviceaccount.com`.
The bot runs as `grok-bot@<project>.iam.gserviceaccount.com`.
At org level, `grok-bot` has only `roles/browser`, `roles/iam.securityReviewer`, `roles/iam.serviceAccountViewer`,
`roles/serviceusage.serviceUsageViewer`, `roles/billing.viewer`, and `roles/storage.bucketViewer`.
On the state bucket it also has `roles/storage.objectViewer`.
It has no `roles/iam.serviceAccountKeyAdmin` on itself.
State is in `gs://state.podval.org`, `gs://state.opentorah.org`, and `gs://state.cognomath.org`.

The grok-bot Cloudflare token on each account is read-only.
The grants are Zone, DNS, Zone Settings, and Single Redirect Read.
Write attempts were verified to be denied.

Each org's `grok-bot` service account has the Google Workspace Groups Reader admin role.
`grok-bot@podval-infra.iam.gserviceaccount.com` is assigned in the podval.org Workspace,
`grok-bot@opentorah-infra.iam.gserviceaccount.com` in opentorah.org, and
`grok-bot@cognomath-infra.iam.gserviceaccount.com` in cognomath.org.
Each org's own service account is assigned in that org's own Admin console, under Account > Admin roles > Groups Reader >
Admins assigned > Assign service accounts.
The role may take a few minutes to take effect.
With it, `direnv exec . pulumi preview --refresh` runs clean on all three dev stacks.

The desktop installs `pulumi-language-scala` from `github://api.github.com/VirtusLab/besom`.
GCP and Cloudflare are pinned in each stack config (`gcp:version`, `cloudflare:version`) and in the Besom plugin
coordinates.
The installed versions are in the private vault `dubinsky/dubinsky-private`, at this same path.
The shared program sets the same version on the provider, on each resource, and on the GCP invokes, because the
resources use the default provider.
The selected stack is not stored in git.
On a new checkout, `pulumi stack select organization/<project>/dev` once.

## Besom provider update

A newer Pulumi plugin binary is not a Besom upgrade.
The Besom jar's `plugin.json` names the plugin the program was generated for.
The coordinates installed now are in the private note.
Those jars were the newest on Maven Central on 2026-11-11.
Upstream plugins can be newer and still have no Besom jar.

Bump `besom-core_3`, `besom-gcp_3`, and `besom-cloudflare_3` together in `~/Podval/pulumi` and in each infra
`build.gradle`.
The infra builds also depend on published `org.podval.tools.pulumi`.
`includeBuild` of `~/Podval/pulumi` replaces that when the checkout is present.

Change the version literals in `gcpResourceOptions` and `cloudflareResourceOptions`.
Besom accepts a string literal there and rejects a `val`.
Set `gcp:version` and `cloudflare:version` in each `Pulumi.dev.yaml` to those same versions.
The resources use the default provider, so the version has to be on each resource.
The provider resource option alone leaves the old `default_<version>` provider in state.
GCP lookups use `gcpInvokeOptions`.
There are no Cloudflare lookups.

Install that plugin on this desktop with `pulumi plugin install resource <name> <version>`.
`direnv exec . pulumi preview` for Podval, OpenTorah, and CognoMath.
I run `pulumi up` on this desktop for that diff.
A GCP provider upgrade can replace groups.
`Group.initialGroupConfig` is ignored for that reason.

State stays in the buckets from the desktop `pulumi up`.
The bot does not run `pulumi up`.
Remove an old plugin only after state no longer names it.
A stack that still records the old version makes the next preview download that plugin again.
