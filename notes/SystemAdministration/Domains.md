---
title: Domains
---
## Square Space

For a while, I registered my domains with Google Domains and used Google Domains DNS.
In 2023, Google sold its domain registrar business to Square Space, and all my domains were migrated there.

Square Space charges more for the domains than Google Domains used to charge - and more than some registrars that sell domain registrations at cost do, which is a small problem.

Square Space does not support Dynamic DNS, which is a problem.

Some of my domains were imported into Google Cloud Domains at the time of migration; I did not get migration letters for them from Square Space, and they did not appear in the Square Space management UI, but the registrar for them did change to Square Space!
This is a big problem, since I now can not manage those domains anywhere. I did not find any discussion of such a problem in Square Space help documents or anywhere else.

Which made me think: why don't I move all my domains to a different registrar?

Of course, I have no idea which ones are any good ;)

## Cloudflare
A handy article [Domain Registrars which Developers Recommend](https://newsletter.pragmaticengineer.com/p/domain-registrars-which-developers) turned up; it seems that developers recommend [Cloudflare](https://www.cloudflare.com/en-gb/products/registrar/), [Namecheap](https://www.namecheap.com/), and [Porkbun](https://porkbun.com/) the most; [Hover](https://www.hover.com/) and [Tucows](https://tucowsdomains.com/) much less.

Porkbun looks cool, but has to many pigs for my taste ;) It also seems to use Cloudflare DNS.

Cloudflare is very popular, supports Dynamic DNS, and probably has Terraform provider written for it, which I *might* find useful at some point. So, let's try it.

Of course, Square Space does not provide any way to export DNS settings, so there is going to be a lot of manual data recording and entering :(

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

For real domains, set [redirect](https://developers.cloudflare.com/rules/url-forwarding/examples/redirect-root-to-www/) from the apex domain to `www` (CNAME record for the apex domain ("@") pointing to `www` works with GitHub but not with ghs.googlehosted.com).

For alias domains, set [redirect](https://developers.cloudflare.com/fundamentals/manage-domains/redirect-domain/) to the real domain.

- [ ] verify apex -> www works
- [ ] verify MX records
- [ ] verify DKIM records (https://support.google.com/a/answer/174124?hl=en&src=supportwidget0&authuser=0)
- [ ] set my Voice number on all registrations

- delete Square Space account
- remove all mentions of domains etc. in all infrastructures and pulumi

## Dynamic DNS

Among other interesting things, CloudFlare *does* [support](https://developers.cloudflare.com/dns/manage-dns-records/how-to/managing-dynamic-ip-addresses/) Dynamic DNS.

I am running UniFi, which does have some sort of Dynamic DNS update facility, but it seems to have [issues](https://community.ui.com/questions/Cloudflare-Dynamic-DNS-options/49714e4f-6442-4bf7-8ee7-153806bbb005) with CloudFlare.

Apparently, [[Home Assistant]] [can do it also](https://www.home-assistant.io/integrations/cloudflare/) - but why bother? I am running [[ProxMox]], so adding a dedicated Dynamic DNS client seems the way to go ;)

I found a nice CloudFlare DDNS [updater](https://github.com/favonia/cloudflare-ddns) - with the additional attraction that its author, [favonia](https://github.com/favonia), is [familiar](https://homotopytypetheory.org/author/favonia/) from the HoTT scene :)

I thought that the client is [not available](https://github.com/favonia/cloudflare-ddns/issues/1013) as a ProxMox LXC container.
Since I have virtual machine for running Docker containers, I can use that to run it with Docker or Docker compose. After testing it with Docker, I configured a `docker-compose.yaml` and started it with `docker compose up -d`. Here is `docker-compose.yaml`:

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

Turns out, the client [is](https://community-scripts.github.io/ProxmoxVE/scripts?id=cloudflare-ddns&category=Network+%26+Firewall)
available as an LXC after all - and that is how I run it now.

Configuration file is in `/etc/systemd/system/cloudflare-ddns.service`; for changes to take: `systemctl restart cloudflare-ddns`.

## CloudFlare Tunnel

CloudFlare supports a better mechanism for accessing services remotely: [CloudFlare Tunnels](https://developers.cloudflare.com/cloudflare-one/connections/connect-networks/) [SSH](https://developers.cloudflare.com/cloudflare-one/connections/connect-networks/use-cases/ssh/ssh-infrastructure-access/); there is a ProxMox LXC for [that](https://community-scripts.github.io/ProxmoxVE/scripts?id=cloudflared&category=Network+%26+Firewall) :)

Configuration file is in `/usr/local/etc/cloudflared/config.yml` - which does not exist even after the tunnel was started in a restart-on-boot mode...

- created a CloudFlare ZeroTrust team "podvalorg" ("podval" was taken :())
- choze the $0/month plan
- set billing
- Networks | Tunnels | Add Tunnel
- created tunnel "keefe39"

[[TODO]] configure SSH etc.