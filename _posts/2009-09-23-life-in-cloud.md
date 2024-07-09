---
layout: post
title: Life in the Cloud
date: '2009-09-23T08:30:00.003-04:00'
author: dub
tags: [life-in-the-cloud]
modified_time: '2015-02-13T11:33:36.880-05:00'
blogger_id: tag:blogger.com,1999:blog-8681083740214020499.post-4471311622524428152
blogger_orig_url: https://blog.dub.podval.org/2009/09/life-in-cloud.html
---

I want to try "living in the cloud" - no packages installed on my server.

### Google ###

I prefer to use Google services in general - for simplicity.
I strongly prefer to use Google services for the collaborative functions, because I can express permissions in terms
of Google ids (which everybody has). Also, there is a potential for the integration between the services that are
owned by the same company - although Google does not always develops this potential ;)

### Email ###

In the beginning I realized that I want to be able to access my email from multiple computers. Which means that all the
mail had to be kept on the server. Although that is possible with POP, finding anything among years of accumulated email
is only practical when it can be filed in folders, which POP does not support. It turned out that there is an
alternative: IMAP. IMAP has its drawbacks: email application has to be configured on every machine; search across the
folders is slow etc., but it is a clear winner over POP.
  
I registered my domain - podval.org - in the Spring of 1999. Some of my friends and relatives have accounts on my domain.
When email for my domain was hosted on the server in my house, I ran an IMAP server on it :) (In fact, I liked IMAP so
much that I even worked for a while on a JavaMail-structured IMAP server of my own.)
  
GMail account that I acquired in the Spring of 2006 was initially forwarding to that machine.
  
In the Fall of 2006, after a broadband service outage, I reconfigured my mail server to accept mail from my
brother-in-law's server across the street. A mistake in configuration was exploited by spammers, and my broadband
provider cut me off until I closed the open email relay I inadvertently created. I was pressed for time: email addressed
to all my users was about to bounce! Then I remembered that Google just opened for testing a new service: Google Apps
for your Domain! I applied, was approved, and moved the email flow to Google in one day.
  
After the move, I can access my email from any computer without any setup. Search capabilities, compared to IMAP,
improved drastically. Switch from folder hierarchy to one-level labels what somewhat trying, but GMail now supports
multiple levels of labels anyway :)

### Contacts ###

Google Contacts seems to be more than sufficient for my needs.

### Calendar ###

Google calendar seems to be more than sufficient for my needs, although alternatives like Sunrise Calendar may be interesting.

### Documents ###

Google Drive (previously Google Docs) is the IMAP for documents that allows collaboration. Some people find the Google
Drive documents, spreadsheets and presentations lacking in features compared to Microsoft Office. For my needs, it is
more than sufficient.

### Blogger ###

Blogger seems to be (one of) the best blogging services.
It is simple to use, and I use it for publishing my short and medium-sized notes.
  
[Jekyll](http://jekyllrb.com/) has some geek appeal (especially since all my [code](https://github.com/dubinsky) is at
[GitHub](https://github.com/)), but I want to have comments in the blog (and do not like
[closed-source](http://dumbmatter.com/2011/08/jekyll-and-other-static-site-generators-are-currently-harmful-to-the-free-open-source-software-movement/)
[Disqus](https://disqus.com/)), so I am sticking with [Blogger](https://draft.blogger.com/home) for now, although
syntactic [highlighting](http://alexgorbatchev.com/SyntaxHighlighter/) of code is more difficult.
  
For longer, more structured notes, I tried using Google Sites wiki, although it is not the most advanced or mature
(and mangles the links, which is not acceptable).
  
Technical differences between current wiki and blog services are minimal.
Both support, to some extent: collaborative editing; history of changes; comments;
changes feed and syndication; widgets etc.
  
Some wiki packages have features that support longer posts or posts related to
other posts (unlike the blog's loosely collection of posts) - things like structured editing.
It seems natural that the two flavors of services should be merged.
Thus, "bliki" - a blog/wiki hybrid. Allegedly, MoinMoin is one of the packages attempting
the merge. Google services, for some reason, are not merging. Yet?
  
Currently, I am of the opinion that Google Sites in general is not suitable for publishing structured text
(like papers). What is then?

### Publishing ###

For papers, I need styling, source control, formulae, bibliography etc. It seems that DocBook is the way. (Possibly,
with CiteULike integration :))

### News ###

The best way to read the news (feeds) from various sites that I found is Google Reader. Even old-fashioned mailing lists
can be read this way, if they are on [GMane.org](http://gmane.org/). Reader also allows me to "share" things that I like
from my feeds or - with the help of the "Note in Reader" bookmarklet - anywhere, producing a - what else? - feed of my
shared items. Anyone interested in them can just subscribe to my feed, and I do not have to email interesting links any
more :)
  
Well, Google first removed the subscribable out-feed from the Reader and replaced it with the indiscriminate push
of Google+ - step in the wrong direction, and then killed reader altogether! I moved to [Feedly](http://www.feedly.com/)

### Bookmarks ###

The best way to handle bookmarks that I found is Delicious. Tagged, searchable, social bookmarking! There is even Chrome
integration: [Delicious Tools](https://chrome.google.com/webstore/detail/gclkcflnjahgejhappicbhcpllkpakej). In the end
of 2010, the future of the service became uncertain. Google bookmarks still suck, and there is no good integration
between Google Bookmarks and Chrome Bookmarks...

I switched to [Pinboard](https://pinboard.in/) in 2013; it does not have social features, but I do not need them either.
(As a bonus, it has an interesting pricing model.)

### Note-taking ###

Research: Google Keep (and not Evernote :)); how about a merge between bookmarks, news and research?

### Tasks List ###

The canonical book on managing the task lists is ["Getting Things Done"](http://www.amazon.com/Getting-Things-Done-Stress-Free-Productivity/dp/0142000280)
by David Allen; his approach makes a lot of sense, but I am not (yet?) ready to a full GTD application (lifestyle?).
On the other hand, I do not want to continue keeping my lists of things to do on paper :) On the third hand, I want
something that is specific to task lists, not a general-purpose note-taker like Evernote.

I want a simple todo-list application where:
- I can have multiple lists
- I can make a sub-item, a subsub-item etc.

Bonus:
- Integration with Google Calendar
- Tags

There are two Google applications in this area: Google Tasks and Google Keep.

Google Keep is trying to compete with Evernote, a full-featured note-taking application, which I probably should check
out (again), but as of September 2013 Google Keep does not allow hierarchy (sub-items), so it's out.

Google Tasks is an older application, which makes it less likely to receive updates, but it does what I need it to do.
The UI is not the smoothest, and by default it is not a full-window one; big UI is accessible at
https://mail.google.com/tasks/canvas. There is a good (third-party) Android application for Google Tasks:
[GTasks](https://play.google.com/store/apps/details?id=org.dayup.gtask). Goggle Tasks integrates with Google Calendar,
but does not support tags.

There is an interesting alternative: [workflowy](https://workflowy.com/); it is limited to 500 items, though, and to get
more one has to pay $50/year or refer someone and get 2500 more items. There is no Android application yet.

There are many more alternatives, for example: Wunderlist (not more advanced than Google Tasks, but not from Google);
Remember The Milk (too complex; sync allowed only once per day - unless you buy a "pro" account for $25/year);
conqu (synchronization costs $53/year); Doit.im (one sync a day - or $20/year); Any.do (not a full GTD application);
Toodledo (not a full GTD application?); Todoist (not a full GTD application?; calendar integration only in paid version,
$29/year); producteev (not a full GTD application?); zendone; Asana.

IQTell is a full GTD application and has email, calendar and Evernote integrations. I used it while it was free, but it
is too complex and difficult to use.

My current choice: TickTick.

### Shopping List ###

My requirements:
- *Shared Shopping List*: my wife marks what we need; I see it in the store and mark what I got;
- *Web Support*: all functionality is available in the browser;
- *Mobile Support*: all functionality is available on the phone;
- *Checklists*: convenient way to keep checklists of things we buy regularly and selecting what we ran out of so that it
   appears on the shopping list.

Bonus:
- associating items with stores
- barcode scanning
- pictures

Out of Milk and Our Groceries do not have checklists; Rainbow Shopping and Mighty Grocery do not have a web interface;
Remember The Milk is not really a shopping list application; ZipList does not have checklist functionality in the mobile
app; GroceryIQ has issues with sync...

That leaves nothing :(

### Music ###

I use Google Music for my music, lectures, audio books etc.

### Photos ###
I use Google's Picasa Web Albums for my photos. It gradually merges with Google Plus Photos.

At some point, I'll document my criteria for an ideal photo service is.

### Money ###

To look at the transactions and balances of all accounts in one place, I previously used GnuCash on Linux.
Current best alternative seems to be [Mint](https://www.mint.com/) from Intuit.

### Diary ###

For my diary I tend to use a Rhodia Web Notebook; if I wanted an online diary/journal, I'd probably go with
[Penzu](http://penzu.com/)... Definetely not LiveJournal!

### Genealogy ###

For things like family tree, there was once a Linux application called Gramps.
Nowadays, there is a wide variety of online applications.
For my very limited use, I prefer [Geni](http://www.geni.com/).

### Code ###

The best way to present code snippets in the Blogger posts is [SyntaxHighlighter](http://alexgorbatchev.com/wiki/SyntaxHighlighter)
from Alex Gorbatchev. (Although there are other [opinions](http://code.sshrin.com/2011/01/code-syntax-highlighting-on-blogger.html).)
XML (and HTML) need to be escaped for posting on Blogger (still?). One service that can be used for that is available at
[centricle.com](http://centricle.com/tools/html-entities/).

### Source Control ###

There are many services that provide source control services, mainly with a modern system like Mercurial or Git (and not
Subversion or - horror! - cvs). Many are "forges", i.e. in addition to the repositories they provide issue tracker and
wiki:
- [GitHub](https://github.com/) - not just Git; they wrote [hg-git](http://hg-git.github.com/) extension that lets one
use GitHub with Mercurial.
- [BitBucket](https://bitbucket.org/) - I do not like [Atlassian](http://www.atlassian.com/), although they have everything.
- [Google Code](http://code.google.com/projecthosting/) - interface is not great; looks a little abandoned...

It is possible to host sources at more than one service; see
["Dual bitbucket/github citizenship"](http://www.serpentine.com/blog/2010/10/10/dual-bitbucketgithub-citizenship/) by
[Bryan O’Sullivan](http://www.serpentine.com/blog/author/admin/).

I keep my [code](https://github.com/dubinsky) at GitHub and use Mercurial.

If Homotopy Type Theory libraries and [textbook](https://github.com/HoTT/HoTT) are being developed on GitHub,
it should be enough for me :)

### Continuous Integration ###

Cloud-based continuous integration services are harder to find, but Cloudbees - the company behind Jenkins - 
hosts such a service (integrated with GitHub through post-commit triggers). I [use](https://podval.ci.cloudbees.com/) it.

### IDE ###

An IDE with Java and Scala support remains one of the few applications that have to be installed locally (on a
reasonably powerful machine). This may change in the future, when cloud-based IDEs mature:
- Cloud9 - Javascript only, but integrated with GitHub
- shiftIde
- Google Brightly
- Codenvy

### Running in the Cloud ###
- GAE
- Amazon
- CloudBees
- Heroku
- Cosm
