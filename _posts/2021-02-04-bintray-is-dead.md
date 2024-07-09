---
layout: post
title: Bintray is dead - long live Maven Central!
author: Leonid Dubinsky
tags:
  - bintray
  - gradle
  - maven-central
date: 2021-02-04
---
Notes on my Maven Central setup.

As of 2024, Maven Central is moving towards a new way of publishing - [Central Portal](https://central.sonatype.org/register/central-portal/#releasing-to-central);
there is still no official Gradle support and no clear way to move verified namespaces from the old way (now called "legacy OSSRH") to the new, but the old way still works (although a token must now be used instead of the web credentials).

It is also still possible to request the old way of publishing to be enabled for the new projects, but registration and namespace verification is completely different from what is described below ;)

* TOC
{:toc}
## Introduction ##

Yesterday JFrog [announced](https://jfrog.com/blog/into-the-sunset-bintray-jcenter-gocenter-and-chartcenter/)
that [Bintray/JCenter](https://bintray.com/beta/#/bintray/jcenter?tab=packages) will go away
in three months. Although they do provide free [JFrog Platform Cloud](https://jfrog.com/pricing/#sass)
subscriptions for Open Source developers, and I got [one](https://dub.jfrog.io/),
I do not see how I can use it to distribute my artifacts.
Besides, as I [wrote before](http://dub.podval.org/2020/06/29/bintray-gradle-plugin.html),
Bintray seems to be abandonware...

I am not ready to switch to the non-traditional [JitPack](https://jitpack.io/) (yet?),
so [Maven Central](https://central.sonatype.org/) seems to be the way to go.

One reason I did not use it before because it has a reputation of being slow, and the way it
is struggling, returns `405 Not Allowed` and keeps being restarted 
right now, just a day after the JCenter announcement, seems to confirm that.
At least they report the [status](https://status.maven.org/) of things :)
I hope that with everybody moving there maybe Sombody Will Do Something about it and
eventually things will improve.

Another reason I didn't use it before is: it is supposed to be more difficult to use than
JCenter, with all their signing requirements and such. This turned out to be not that hard ;)

These are my notes on what did I have to do to switch to Maven Central.
Official (but somewhat outdated) documentation is available:
- [OSSRG Guide](https://central.sonatype.org/pages/ossrh-guide.html)
- [Working with PGP Signatures](https://central.sonatype.org/pages/working-with-pgp-signatures.html)
- [Publishing to Maven Central](https://github.com/chhh/sonatype-ossrh-parent/blob/master/publishing-to-maven-central.md)

## Account ##

I signed up for Sonatype at https://issues.sonatype.org/secure/Signup!default.jspa
with dub@podval.org email address.

Originally, I used the same credentials for deploying artifacts are use to log into https://oss.sonatype.org/ (OSSRH). In early 2024, those stopped working: now, a token needs to be [generated](https://central.sonatype.org/publish/generate-token/#releasing-to-central) and used [supplied](https://central.sonatype.org/publish/publish-gradle/#releasing-to-central), together with its password, as credentials.

To make credentials available to Gradle, I put them into `~/.gradle/gradle.properties`:
```properties
mavenCentralUsername=dub
mavenCentralPassword=...
```

To the `publishing` block of the Gradle build file I added:
```groovy
repositories {
  maven {
    name = 'mavenCentral'
    url = version.endsWith('SNAPSHOT') ?
      'https://oss.sonatype.org/content/repositories/snapshots' :
      'https://oss.sonatype.org/service/local/staging/deploy/maven2'

    // Note: this will use mavenCentralUsername and mavenCentralPassword properties - if they are available
    credentials(PasswordCredentials)
  }
}
```

## Namespace ##

I claimed the "namespace" (group id) `org.podval.tools` by opening a special 
kind of ticket via https://issues.sonatype.org/secure/CreateIssue.jspa?issuetype=21&pid=10134

I had to verify that I own the domain `podval.org` by adding a DNS TXT record referencing
the ticket. I did ask if I should claim `org.podval` instead of the `org.podval.tools`,
but it seems that nobody read my question - probably because initial verification is handled by
robots :). But I then asked specifically to wide the scope
of my "staging profile" that resulted from this [ticket](https://issues.sonatype.org/browse/OSSRH-63919),
and [Joel Orlina](https://issues.sonatype.org/secure/ViewProfile.jspa?name=jorlina)
did it immediately!

In addition to `podval.org`, I control the domain (and GitHub organization) `opentorah.org`.
I want to sign artifacts in it with a key associated with an appropriate email address 
dub@opentorah.org. It seems that Maven Central verification does not require for the signing
key to be associated with email address of the Sonatype account, so I didn't have to create
another Sonatype account, and [claimed](https://issues.sonatype.org/browse/OSSRH-64024) the
`org.opentorah` namespace under the same one!

## GPG Keys and Signing ##

I do not normally use (or have) a GPG key, so I had to make one (actually, two).
First, I made sure that permissions on the `~/.gnupg` are tight:
```shell
$ chown -R $(whoami) ~/.gnupg/
$ chmod 700 ~/.gnupg/*
$ chmod 700 ~/.gnupg
```
(Some advice on the Internet suggests `600`, but it is wrong: gpg2 won't be able to read
the secret keys...)

Generated GPG key for dub@podval.org:
```shell
$ gpg2 --generate-key ... whatwhen
```
Sent the public key to the keyserver so that Maven Central could verify it:
```shell
$ gpg2 --keyserver keys.gnupg.net --send-key EA493E02
```
That didn't seem to work, so I manually submitted at http://pool.sks-keyservers.net 
the output of
```shell
$ gpg2 --armor --export dub@podval.org
```

All the above was repeated for a dub@opentorah.org key.

By default, Gradle's [Signing Plugin](https://docs.gradle.org/current/userguide/signing_plugin.html)
uses properties (signing.keyId, signing.password, signing.secretKeyRingFile) to access
the key. I prefer to use explicit configuration, but more importantly, I need the flexibility
to use different keys to sign the artifacts of different projects.

I got the keys into a form suitable for `gradle.properties`: run
```shell
$ gpg2 --armor --export-secret-keys dub@podval.org | awk '1' ORS='\\n\\\n'
```
and removed the final `\n\`.

I then added to `~/.gradle/gradle.properties`:
```properties
gnupg.dub-podval-org.key=<see above>
gnupg.dub-podval-org.password=...
gnupg.dub-opentorah-org.key=<see above>
gnupg.dub-opentorah-org.password=...
```

To the Gradle build file, I added:
```groovy
signing {
  useInMemoryPgpKeys(
    findProperty('gnupg.dub-podval-org.key'),
    findProperty('gnupg.dub-podval-org.password')
  )
  sign publishing.publications.library
}
```

## Releasing ##

After artifacts are deployed to the staging repository:
- log into Nexus at https://oss.sonatype.org/
- "close" the staging repository (verify that their requirements are satisfied)
- "release" it
  

If this is a first release in this namespace, comment on the namespace claim ticket so that Sonatype 
can start synchronizing the artifacts to Maven Central. Formy second namespace I received this reply:
> Central sync is activated for org.opentorah.
> After you successfully release, your component will be published to Central,
> typically within 10 minutes, though updates to search.maven.org can take up to two hours.

In this context, "Central" means https://repo1.maven.org/maven2/


TODO nexus Gradle plugin(s)
//id 'io.codearte.nexus-staging' version '0.22.0' ? or something else that gets credentials from maven-publish?
