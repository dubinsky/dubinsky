---
layout: post
title: Java Daemon
date: '2011-03-17T12:15:00.006-04:00'
author: Leonid Dubinsky
tags: [java]
modified_time: '2011-06-03T10:35:44.928-04:00'
blogger_id: tag:blogger.com,1999:blog-8681083740214020499.post-3839280446042927778
blogger_orig_url: https://blog.dub.podval.org/2011/03/java-daemon.html
---

More than once I needed to start a Java program in a daemon mode.

I am aware of the existence of the packages like [Apache Commons Daemon](http://commons.apache.org/daemon/) and
[Tanuki Service Wrapper](http://wrapper.tanukisoftware.com/doc/english/product-overview.html). I never understood what
benefits they have over native-libraries-free pure-Java solutions (at least, in simple cases and not on Windows :)).
Yes, it is somewhat tricky to record a pid in a pid file and such, by I prefer not to use native libraries - unless I
have to.

Recently, I had to do this again. It turned out that FuseJ - Java-to-Fuse binding - is broken in background mode, so I
had to improvise... But the reason is less important than the result :)

My colleague Dima pointed me towards a 2005 [article](http://barelyenough.org/blog/2005/03/java-daemon/) by
[Peter Williams](http://barelyenough.org/blog/author/peter-williams/) on the subject. And it turned out to be such a
very thorough and thought-through article, that I just wanted to record it here ;)

One of the techniques mentioned in the article is: close standard input of a program being daemonized (`<&-` on the
command line) and start it in the background (`&` at the end of the command line); then close standard output and error
from within it. I understand why he does not want to close all standard file descriptors: he wants to be able to log to
console during the startup sequence. I did not understand why does he bother closing the standard input from the command
line - and not from within the process?

I am starting to understand that, I think. I tried a simplified approach - just `&` on the command line; close
everything from within the process. It works fine on Fedora 14 under bash: shell prints the pid of the backgrounded
process and continues; the process logs to console and to log file, then closes everything and runs as a daemon. On
CentOs under tcsh the results are very different: the shell prints the pid, but after I press <enter>, the shell prints
"Suspended (tty output)"; nothing gets logged to console; only the first line gets logged to the log file; the process
is not running.

I suspect that the process is wedged because standard input is still open when the process is backgrounded. Since tcsh
understood the same close-the-standard-input unprintable (`<&-`) that bash does, I use non-bash-specific way to detach
standard input from the keyboard on the command line: `< /dev/null`.
