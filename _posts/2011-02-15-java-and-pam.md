---
layout: post
title: Java and PAM
date: '2011-02-15T12:22:00.003-05:00'
author: Leonid Dubinsky
tags: [authentication, java, linux]
modified_time: '2011-10-28T09:07:38.766-04:00'
blogger_id: tag:blogger.com,1999:blog-8681083740214020499.post-6310548426443273906
blogger_orig_url: https://blog.dub.podval.org/2011/02/java-and-pam.html
---

Sometimes, there is a need, from within a web application written in Java and running on Linux, to authenticate a user
as a system user. There are packages that provide Java bindings to the password file, but since everyone uses PAM,
that does not cut it.

JAAS is Java's PAM analogue. But only conceptually: there is no PAM JAAS "module"! I do not *need* such a module, but
if one was there, I could use it to authenticate the users.

I never understood why such a module is not part of the JAAS release, but it isn't, and so the problem of authenticating
as a system user is not solved by JAAS :(

### Related work ###

While researching the area, I found two projects:

#### SysAuth ####

[SysyAuth](http://www.scribblin.gs/software/sysauth.html) by Matthew M. Lavy is posited as a simple alternative to JAAS.
In addition to PAM, it supports Windows authentication. It uses hard-coded PAM service, which needs to be configured for
it to work. It is not clear what license does it have.

#### jpam ####
[jpam](http://jpam.sourceforge.net/) by Greg Luck, Barrow Kwan, Jon Eaves supports assorted Linux/Unix flavors. It
provides JAAS module, but does not support operations other than authentication.

### My Attempts ###

Over the years I attempted - more than once - to write the code to do the authentication. Native library (libpam) has to
be interfaced to, so the usual unpleasantness associated with JNI and native code layer apply :) I prefer to minimize
amount of native code and do as much as possible in Java. PAM "conversation" involves callbacks, which I prefer to
handle in Java, not in the native wrapper itself. Native code has to work both ways: Java to native and back.

### The Solution ###

Somewhere around 2008-9 I (re)discovered [JNA](http://en.wikipedia.org/wiki/Java_Native_Access) (I heard about the
library that became JNA in 1998). It allows to interface with native libraries without writing any native code! And it
can handle callbacks! It seemed clear that this is the way to go!

It gets better: [Kohsuke Kawaguchi](http://kohsuke.org/) (of
[JAXB](http://en.wikipedia.org/wiki/Java_Architecture_for_XML_Binding) and
[Hudson/Jenkins](http://en.wikipedia.org/wiki/Hudson_(software)) fame) already went down this way! Among many libraries
he produced is [libpam4j](https://github.com/kohsuke/libpam4j), a Java/PAM bindings using JNA!

Kohsuke's code is readable and well-researched. Some things I would have done differently from Kohsuke. Actually, since
the library is very small (thanks to JNA), every time I need something like that, I just adopt it :)

And here we see, that if you wait long enough, someone will do what you were thinking of doing ;)
