---
title: Domains
---
For a while, I registered my domains with Google Domains and used Google Domains DNS.
In 2023, Google sold its domain registrar business to Square Space, and all my domains were migrated there.

Square Space does not support Dynamic DNS, which is a small problem.

Square Space charges more for the domains than Google Domains used to charge - and more than some registrars that sell domain registrations at cost do, which is a small problem.

Some of my domains were imported into Google Cloud Domains at the time of migration; I did not get migration letters for them from Square Space, and they did not appear in the Square Space management UI, but the registrar for them did change to Square Space!
This is a big problem, since I now can not manage those domains anywhere. I did not find any discussion of such a problem in Square Space help documents or anywhere else.

Which made me think: why don't I move all my domains to a different registrar? Of course, I have no idea which ones are any good ;)

A handy article [Domain Registrars which Developers Recommend](https://newsletter.pragmaticengineer.com/p/domain-registrars-which-developers) turned up; it seems that developers recommend [Cloudflare](https://www.cloudflare.com/en-gb/products/registrar/), [Namecheap](https://www.namecheap.com/), and [Porkbun](https://porkbun.com/) the most; [Hover](https://www.hover.com/) and [Tucows](https://tucowsdomains.com/) much less.

Porkbun looks cool, but has to many pigs for my tastes ;) It also seems to use Cloudflare DNS.

Cloudflare is very popular, supports Dynamic DNS, and probably has Terraform provider written for it, which I *might* find useful at some point. So, let's try it.

Of course, Square Space does not provide any way to export DNS settings, so there is going to be a lot of manual data recording and entering :(

And I really hope that Google Workspace services attached to some of my domains will not break :)

Let's go!

For each organization:
- [sign up for Cloudflare](https://dash.cloudflare.com/sign-up) using admin Google email
- add billing

[A step-by-step guide to transferring domains to Cloudflare](https://blog.cloudflare.com/a-step-by-step-guide-to-transferring-domains-to-cloudflare)

[Domains](https://developers.cloudflare.com/fundamentals/manage-domains/)

[DNS records](https://developers.cloudflare.com/dns/manage-dns-records)

For each domain:
- old registrar: unlock
- old registrar: disable DNSSEC
- Cloudflare: [add domain](https://developers.cloudflare.com/fundamentals/get-started/setup/add-site/)
- Cloudflare: verify DNS records:
	- turn off proxying
	- delete old name servers
	- delete domainconnect record pointing to Square Space or Google
	- delete Google domain verification record
- old registrar: replace name servers with the Cloudflare-assigned ones
- Cloudflare: notifies when the domain is active
- old registrar: get transfer authorization code
- Cloudflare: [initiate transfer](https://dash.cloudflare.com/?to=/:account/domains/transfer)

dub@cognomath.org
from Google Cloud Domains:
- [x] cognomath.app: transfer pending;
- [ ] cognomath.com: transfer pending;
- [x] cognomath.games: transfer pending;
- [ ] cognomath.net: transfer pending;
- [x] cognomath.org: transfer pending;
- [ ] congomath.com: transfer pending;
- [ ] congomath.org: transfer pending;

from Square Space:
- [ ] mathworlds.org: transfer pending;

dub@podval.org
- [ ] podval.dev: transfer pending;
- [ ] podval.org: transfer pending;

dub@opentorah.org
- [ ] opentorah.org - from Google Cloud Domains: transfer pending;
- [ ] alter-rebbe.org: transfer pending;
- [ ] chabad.dev: transfer pending;
- [ ] opentorah.dev: transfer pending;
- [ ] chumashquestions.org: transfer pending;
- [ ] jewish-calendar.org: transfer pending;

Note: domain verification records are missing for cogno/congo-math and chumashquestions.org - but they all are listed as verified!
- [ ] remove Google domain verification records

- [ ] add domain forwarding rules www <-> @, alias domains...
- [ ] check A and AAAA records
- [ ] set up Dynamic DNS for k39.podval.org
- [ ] verify MX records
- [ ] verify DKIM records (https://support.google.com/a/answer/174124?hl=en&src=supportwidget0&authuser=0)
- [ ] set my Voice number on all registrations

- disconnect Square Space
- remove all mentions of domains etc. in all infrastructures and pulumi
