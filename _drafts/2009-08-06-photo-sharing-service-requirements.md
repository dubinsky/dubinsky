---
layout: post
title: Photo Sharing Service Requirements
date: '2009-08-06T13:41:00.004-04:00'
author: Leonid Dubinsky
tags: [photo]
modified_time: '2014-05-30T18:39:32.374-04:00'
blogger_id: tag:blogger.com,1999:blog-8681083740214020499.post-6224358028394546483
blogger_orig_url: https://blog.dub.podval.org/2009/08/photo-sharing-service-requirements.html
---

The time has come to describe my "search" for the perfect photo sharing!

My workflow:
- store pictures on my computer;
- classify them into "events";
- select the deserving ones and publish;
- I do not edit, but I do retain RAW files for occasional high-quality print.

I want to be able to view my photos and publish the ones I want to publish online, not from a dedicated computer. It
would be nice to store all the pictures online, for backup.

It is expected from a photo-sharing installation that a visitor should be able to leave comments and order prints. Thus,
user authentication and integration with third-party services is required.

I tried rolling my own between 2000 and 2007. In the process I learned a lot about browser image caching peculiarities;
image metadata storage formats; extraction of images from RAW files; etc. At various points various alpha versions of my
code were actually serving pictures, but in the end it turned out that I can not deliver a solution for our family's
photo sharing needs :(

I looked into self-hosted packages like gallery and jalbum. They did not work out for various reasons (not just "not
made here"): poor support for RAW files; poor support for event grouping or album organisation etc.

I looked into web-based services, including - but not limited to: Flickr, Fotki, Zenflio, Picasa.

My requirements:
- support for my workflow
- unlimited storage
- storage of RAW files
- minimal editing capabilities (rotation)
- support for print ordering with cropping
- support for folder hierarchy with flexible access control, so that I can store the photos in a private hierarchy, and publish to the open one
- subsets and links (for publication from the timeline)
- ability to replace a photo with a different version
- ability to put a photo into multiple albums without duplication, so that if I reprocess and replace a photo in the hierarchy of originals, it changes wherever it is published (effectively, allow links to photos (and albums)) [This feature, combined with authentication/authorization issues, proved difficult to do right in my attempts to roll my own.]
- sorting of photos - and albums - by the date taken
- tagging photos with people, places and events
- practical programmatic access (API)
- convenient mass upload (ideally - with synchronization of hierarchy)

Of the services I tried:
- none support storing RAW files
- Picasa Web Albums charges for storage, and there is no unlimited
- Picasa Web Albums does not support folders hierarchy, links or photo replacement
- Zenfolio does not sort groups of albums - or albums themselves - among themselves by the date taken

Looking at programs like Shotwell, I realized that events should not just be tags as I thought previously, but that the
directory structure should reflect them directly! Which means that I do not really need all that complex an hierarchy
(and links); I just need a way to select photos that I want published from the timeline of events! Well, some
hierarchy - ability to group albums by year - *is* useful, and maybe Picasa Web Albums will support it some day :) As
for links, I think tags will replace them for me.

### Zenfolio ###

Zenfolio offers unlimited storage and arbitrary multi-level hierarchy.

Zenfolio does not have tools for grouping photos into events, so my wife did it manually for a few thousand pictures...

Zenfolio's uploader was slow and buggy, and there was no easy way to upload hundreds of photos from a directory
structure. So I wrote my own using Zenfolio API (It wasn't a walk in the park; see TODO). Zenfolio refused to give me a
free developer account, so I used my tools to do an XML dump of the metadata (painstakingly entered by my wife) and
cancelled the account.

### Picasa Web Albums ###

In the end, I chose Picasa Web Albums, although it failed more of my requirements than any other service. My reasons:
- people tags;
- potential integration with other Google services;
- potentially fast progress. 

With the advent of Google+, the hopes for further integration with other Google services started to pan out. As part of
this development, Picasa Web Albums API will eventually be superseded by something new.

Development of Picasa Web Albums turned out to be very slow. So slow indeed that some fell that the service is
abandoned. With Google+ the pace picked up.

## History ##

Since Fall of 2006 I am using a "hosted" Google account: dub@podval.org. For a long time, "hosted" Google accounts like
mine did not provide all the services that "normal" Google accounts did, like Reader or Picasa Web Albums. To publish my
photos on Picasa, I had to have a "normal" account too, which was messy, even with a trick that was available to
"associate" both of the accounts.

It was especially messy for photos: when I tagged a photo stored in my "normal" account with a new person, a contact
was created in that account, but not in my main "hosted" account. On the other hand, when I added a contact to my
"hosted" account, it did not become available for tagging my photos. This forced me to start working on a contacts
synchronizer - and look at Google APIs.

Finally, in the end of 2010, Google switched to a "new infrastructure", and (almost) all the services - including Picasa
Web Albums - became available to hosted accounts! For a while there was no way to buy more storage for a hosted Picasa
account, making it unusable for me, since I have more photos that would fit in the default allotment, but that
restriction went away around March of 2011.

Now it was finally possible to store my photos in my main, "hosted" account. I did not want to have to reconstruct my
published photos in the new account manually, though. This is when I started re-purposing my Zenfolio code (that was
reasonably general anyways) to synchronize among two Picasa Web Albums accounts (and encountered a "new" GData client
for Java). Data about faces and people tags did not seem to be available through the APIs (TODO: Issue #, although see
TODO...), so I braced myself to the need to re-tag everything once I move the pictures.

In the beginning of April 2011, Google introduced a way to migrate photo albums from one account to another. I asked for
my photos to be migrated, and they were!

There was a problem, though: some of the people tags become orphaned. The contacts they reference disappeared or merged
with other contacts. All my attempts to remove the tags completely and start tagging anew - which becomes more
attractive as the Goggle+ photo functionality develops, including tagging - failed; I still do not know how to get rid
of the troublesome hanging contact references, and my "All People" tab loads forever and is completely useless.

I now envision a workflow where the photos are uploaded and stored on my server and automatically grouped into events.
Then, through a web-interface, I tweak the grouping, name the events, rotate what needs to be rotated, and select which
photos need to be published. Publishing to PWA is done automatically, and in such a way that if I tweak something in
PWA, the changes are synchronized back to my local disk store. This is the direction my synchronization code is evolving
in; in the end, I migh realize the 10+ year old dream of a perfect photo sharing yeat :)

My code for synchronizing photos between local disk structure and Picasa Web Albums (with rotting Zenfolio support) is
on GitHub: [podval-photo-sync](https://github.com/dubinsky/podval-photo-sync). Code for extracting metadata from image
files: [podval-imageio](https://github.com/dubinsky/podval-imageio).
