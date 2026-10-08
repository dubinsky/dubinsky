---
tags: [sysadmin]
---
* TOC
{:toc}

## Square Space

For a while, I registered my domains with Google Domains and used Google Domains DNS.
In 2023, Google sold its domain registrar business to Square Space, and all my domains were migrated there.

Square Space charges more for the domains than Google Domains used to charge - and more than some registrars that sell
domain registrations at cost do, which is a small problem.

Square Space does not support Dynamic DNS, which is a problem.

Some of my domains were imported into Google Cloud Domains at the time of migration; I did not get migration letters for
them from Square Space, and they did not appear in the Square Space management UI, but the registrar for them did change
to Square Space!
This is a big problem, since I now can not manage those domains anywhere.
I did not find any discussion of such a problem in Square Space help documents or anywhere else.

Which made me think: why don't I move all my domains to a different registrar?

Of course, I have no idea which ones are any good ;)

## Cloudflare
A handy article
[Domain Registrars which Developers Recommend](https://newsletter.pragmaticengineer.com/p/domain-registrars-which-developers)
turned up; it seems that developers recommend [Cloudflare](https://www.cloudflare.com/en-gb/products/registrar/),
[Namecheap](https://www.namecheap.com/), and [Porkbun](https://porkbun.com/) the most; [Hover](https://www.hover.com/)
and [Tucows](https://tucowsdomains.com/) much less.

Porkbun looks cool, but has to many pigs for my taste ;) It also seems to use Cloudflare DNS.

Cloudflare is very popular, supports Dynamic DNS, and probably has Terraform provider written for it, which I *might*
find useful at some point.
So, let's try it.

Of course, Square Space does not provide any way to export DNS settings, so there is going to be a lot of manual data
recording and entering :(

And I really hope that Google Workspace services attached to some of my domains will not break :)

Let's go!

## Migration
For each organization (dub@podval.org, dub@opentorah.org, dub@cognomath.org):
- [sign up for Cloudflare](https://dash.cloudflare.com/sign-up) using admin Google email
- add billing

Documentation:
- [A step-by-step guide to transferring domains to Cloudflare](https://blog.cloudflare.com/a-step-by-step-guide-to-transferring-domains-to-cloudflare)
- [Domains](https://developers.cloudflare.com/fundamentals/manage-domains/)
- [DNS records](https://developers.cloudflare.com/dns/manage-dns-records)

For each domain:
- old registrar: unlock
- old registrar: disable DNSSEC
- Cloudflare: [add domain](https://developers.cloudflare.com/fundamentals/get-started/setup/add-site/)
- Cloudflare: verify DNS records:
	- turn off proxying
	- delete old name servers
	- delete domainconnect record pointing to Square Space or Google
	- delete Google domain verification record
	- delete apex domain A records pointing to:
		- Square Space (198.49.23.144 etc.)
		- Google (216.239.32.21, 216.239.34.21, 216.239.36.21, 216.239.38.21, 2001:4860:4802:32::15, 2001:4860:4802:34::15, 2001:4860:4802:36::15, 2001:4860:4802:38::15)
- old registrar: replace name servers with the Cloudflare-assigned ones
- Cloudflare: notifies when the domain is active
- old registrar: get transfer authorization code
- Cloudflare: [initiate transfer](https://dash.cloudflare.com/?to=/:account/domains/transfer)

## Pulumi library

Three organizations use `org.podval.tools:org.podval.tools.pulumi` ([github.com/dubinsky/pulumi](https://github.com/dubinsky/pulumi),
checkout `~/Podval/pulumi`) from their `infra` repositories — GCP org, buckets, groups, and Cloudflare DNS:

| Org | Checkout | Cloudflare account | Pulumi stack |
|---|---|---|---|
| Podval | `~/Podval/infra` | `dub@podval.org` | `podval-infra` / `dev` |
| OpenTorah | `~/OpenTorah/infra` | `dub@opentorah.org` | `opentorah-infra` / `dev` |
| CognoMath | `~/CognoMath/infra` | `dub@cognomath.org` | `cognomath-infra` / `dev` |

Each `settings.gradle` composite-`includeBuild`s the sibling pulumi checkout when it exists (`-PpulumiDir=` to
override).
If that directory is missing, Gradle resolves the published artifact from Maven Central.
Published version is `0.5.0`; local composite is `0.5.1` (alias / Google-apex redirects).

Every zone also gets `min_tls_version` 1.2, DNSSEC, and CAA.
Workspace zones get DMARC `p=none`; names without mail get SPF `-all` and DMARC `p=reject`.

`DnsZone` patterns:

- **GitHub Pages** (`www` → `*.github.io`): orange-cloud CNAME `@` → `www` → GitHub + Always Use HTTPS (same as
  `alter-rebbe.org`).
  Extra CNAMEs stay DNS-only.
  Cloudflare terminates TLS (SSL Full, not strict).
- **Google Sites** (`www` → `ghs.googlehosted.com`): CNAME-flattened apex does not get a working HTTPS cert.
  Orange-cloud dummy `A 192.0.2.1` on `@` and a Single Redirect (`http.host`) to `https://www.<domain>`.
  `www` stays DNS-only to Google.
- **Auxiliary** (`DnsZone.alias`): orange-cloud dummy A on `@` and `www`, Single Redirect to the canonical
  `https://www.<real>`.
  The zone entrypoint ruleset is named `default` (one per phase; renaming it 400s).
  Live auxiliary rulesets are still named `redirect` except `cognomath.org` (`default`); Pulumi `ignoreChanges` on
  `name` keeps stacks clean.

- [x] GitHub apex → www, orange-cloud + Always Use HTTPS (`podval.org`, `opentorah.org`, `chumashquestions.org`,
      `mathworlds.org`, `alter-rebbe.org`)
- [x] Google Sites apex → www (`cognomath.org`)
- [x] auxiliary 301s: `podval.dev`; OpenTorah `opentorah.dev` / `chabad.dev` / `jewish-calendar.org`; CognoMath
      `cognomath.com` `.net` `.app` `.games` and both `congomath.*`
- [x] MX / DKIM on Workspace zones (`smtp.google.com`, `google._domainkey`)
- [x] DMARC: Workspace `_dmarc` `p=none` `rua=mailto:dub@<domain>`; mail-less names SPF `-all` + `_dmarc` `p=reject`
- [x] CAA `issue` / `issuewild` for `pki.goog` and `letsencrypt.org`
- [x] DNSSEC on every zone (`status` goes `pending` then `active`; Cloudflare registrar publishes DS)
- [x] `min_tls_version` 1.2 on every zone
- [x] Voice number on registrar contacts (all three orgs)
- [x] Square Space accounts deleted (registrar is Cloudflare)

- remove all mentions of domains etc. in all infrastructures and pulumi

## Current state (podval)

Account `dub@podval.org`.
Registrar for `podval.org` is Cloudflare (WHOIS); zone metadata still remembers Squarespace as the previous registrar.
Nameservers `joel.ns.cloudflare.com` / `paislee.ns.cloudflare.com`.
Pulumi stack `podval-infra` / `dev` is clean.

| Zone | Role |
|---|---|
| `podval.org` | GitHub Pages, orange-cloud CNAME `@` → `www` → `dubinsky.github.io` + Always Use HTTPS. Extra CNAMEs stay grey: Workspace `ghs.googlehosted.com`, `dub` → GitHub Pages, `nina` → `ghs.google.com` (Blogger), `ha` + `_acme-challenge.ha` → Nabu Casa, `ssh`/`sds` → `k39.podval.org`. |
| `podval.dev` | Auxiliary: dummy A + 301 → `https://www.podval.org` |

`k39.podval.org` A record is **not** in Pulumi. [[ProxMox]]
LXC 103 (`cloudflare-ddns`) updates it.
Keep it that way.

`http://www.podval.org/` is now Cloudflare Always Use HTTPS (301 to `https://`).

## Current state (OpenTorah)

Account `dub@opentorah.org`.
Pulumi stack `opentorah-infra` / `dev` is clean.
`opentorah/opentorah` Pages custom domain is only `www.opentorah.org` (not `jewish-calendar.org`).

| Zone | Role |
|---|---|
| `opentorah.org` | GitHub Pages, orange-cloud + Always Use HTTPS, same as `alter-rebbe.org` |
| `chumashquestions.org` | GitHub Pages, orange-cloud + Always Use HTTPS. Single Redirect `/book.pdf` → `https://www.chumashquestions.org/book/book.pdf` (301). |
| `alter-rebbe.org` | GitHub Pages, orange-cloud + Always Use HTTPS; extra `app` → Workspace, `store`/`facsimiles` → GCS |
| `opentorah.dev`, `chabad.dev`, `jewish-calendar.org` | Auxiliary: dummy A + 301 → `https://www.opentorah.org` |

No Blogger `ghs.google.com`.
No Dynamic DNS A records.

## Current state (CognoMath)

Account `dub@cognomath.org`.
Pulumi stack `cognomath-infra` / `dev` is clean (redirect ruleset for `cognomath.org` is the zone entrypoint named
`default`).

| Zone | Role |
|---|---|
| `cognomath.org` | Google Sites `www` → `ghs.googlehosted.com`; apex dummy A + 301 → `https://www.cognomath.org`. Extra: `blog` → Google, `games`/`treasure-hunt` → GCS. MX/DKIM as usual. |
| `mathworlds.org` | GitHub Pages (`cognomath.github.io`), orange-cloud + Always Use HTTPS |
| `cognomath.com` `.net` `.app` `.games`, `congomath.org` `.com` | Auxiliary: dummy A + 301 → `https://www.cognomath.org` |

## API tokens

Do not put token values here.

Pulumi tokens (`~/Podval/Keys/podval-cloudflare-api-token.txt`, `~/OpenTorah/Keys/opentorah-cloudflare-api-token.txt`,
`~/CognoMath/Keys/cognomath-cloudflare-api-token.txt`) are **account-owned** (user-token verify 401; account-token
verify 200).
That is the right kind for automation — keep them; do not switch to user tokens.
Each `.envrc` sets `CLOUDFLARE_API_TOKEN`.
Grants are consistent enough to run `DnsZone`: Zone DNS Edit, Zone Edit, Zone Settings Edit, and Single Redirects
(rulesets work).
`GET /zones` also reports `#waf:edit` / Page Shield / SSL Read — WAF package APIs 403, tunnels list as empty 200; those
are not extra checkboxes that matter.
Page Rules API returns 1011 for account-owned tokens; unused (Single Redirects instead).

All three Pulumi tokens can PATCH `always_use_https` (CognoMath used to 403; it no longer does).
CognoMath can also read Universal SSL / certificate packs; Podval and OpenTorah 403 those — extra on CognoMath, not
required.
No expiry.

Other tokens:

- DDNS on [[ProxMox]] LXC 103: DNS Edit on `podval.org` only.
  Keep zone-scoped; account-owned if it is not already.
- OpenTorah worker token: GitHub Actions on `opentorah/alter-rebbe.org` deploys Worker `alter-rebbe-aliases` (`site-publisher.yml`
  → repo secret `CLOUDFLARE_API_TOKEN`).
  The live Worker does not use the token; only **redeploy** does.
  Local copy: `~/OpenTorah/Keys/alter-rebbe-org-cloudflare-worker-api-token.txt`.
  Account token UI (2026-09): **Manage Account → Account API Tokens**.
  Workers grant is role **Editor** on **Specified Workers** → `alter-rebbe-aliases` (legacy name was Account → Workers
  Scripts → Edit).
  Also **Zone → Workers Routes → Write** on `alter-rebbe.org`.
  Put the value in both the Keys file and that GitHub secret.
  Do not reuse the Pulumi token.

## Dynamic DNS

Among other interesting things, CloudFlare *does*
[support](https://developers.cloudflare.com/dns/manage-dns-records/how-to/managing-dynamic-ip-addresses/) Dynamic DNS.

I am running UniFi, which does have some sort of Dynamic DNS update facility, but it seems to have
[issues](https://community.ui.com/questions/Cloudflare-Dynamic-DNS-options/49714e4f-6442-4bf7-8ee7-153806bbb005) with
CloudFlare.

Apparently, [[Home Assistant]] [can do it also](https://www.home-assistant.io/integrations/cloudflare/) - but why
bother?
I am running [[ProxMox]], so adding a dedicated Dynamic DNS client seems the way to go ;)

I found a nice CloudFlare DDNS [updater](https://github.com/favonia/cloudflare-ddns) - with the additional attraction
that its author, [favonia](https://github.com/favonia), is [familiar](https://homotopytypetheory.org/author/favonia/)
from the HoTT scene :)

I thought that the client is [not available](https://github.com/favonia/cloudflare-ddns/issues/1013) as a ProxMox LXC
container.
Since I have virtual machine for running Docker containers, I can use that to run it with Docker or Docker compose.
After testing it with Docker, I configured a `docker-compose.yaml` and started it with `docker compose up -d`.
Here is `docker-compose.yaml`:

```yaml
services:
  cloudflare-ddns:
    image: favonia/cloudflare-ddns:latest
    network_mode: host
    restart: always
    read_only: true
    cap_drop: [all]
    security_opt: [no-new-privileges:true]
    environment:
      - CLOUDFLARE_API_TOKEN=...
      - DOMAINS=k39.podval.org
      - IP6_PROVIDER=none
```

Turns out, the client
[is](https://community-scripts.github.io/ProxmoxVE/scripts?id=cloudflare-ddns&category=Network+%26+Firewall) available
as an LXC after all - and that is how I run it now.

Configuration file is in `/etc/systemd/system/cloudflare-ddns.service`; for changes to take:
`systemctl restart cloudflare-ddns`.

## Remote SSH (not Cloudflare Tunnel)

LAN SSH is `ssh pve` / `ha` / `docker` / `unifi` (YubiKey).
`k39.podval.org` is DDNS for the home IP if something still needs a public name.

A Cloudflare Tunnel LXC (104, `cloudflared`, `.236`) and Zero Trust tunnel `keefe39` (team `podvalorg`) were started and
never given a `config.yml`.
LXC 104 was destroyed 2026-09-16 (`/root/104.conf.bak.*` on PVE).
`keefe39` is gone (API list of live and deleted tunnels on the Podval account is empty).

Do not use Cloudflare Tunnel for admin SSH.
The subnet router is [[ProxMox]] LXC 106 (`tailscale`, `192.168.1.237`, tailnet `100.82.39.37`).
It advertises `192.168.1.0/24`.
That route still has to be enabled for this machine in the Tailscale admin console.
A laptop off the LAN cannot use it until then.
It is not an exit node and Tailscale SSH is off, so admin login stays normal SSH plus the YubiKey, and port 22 is not
published.
A laptop off the LAN accepts the route and uses `ssh pve` / `ha` / `docker` / `unifi` as on the LAN.