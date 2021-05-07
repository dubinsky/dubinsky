---
layout: post
title: Maven in-project repository
date: '2010-01-28T14:24:00.004-05:00'
author: dub
tags: [maven]
modified_time: '2013-06-20T16:19:10.864-04:00'
blogger_id: tag:blogger.com,1999:blog-8681083740214020499.post-343750537477523036
blogger_orig_url: https://blog.dub.podval.org/2010/01/maven-in-project-repository.html
---

### Why in-project dependencies? ###

Maven handles dependencies of the project very well. It finds the artifacts in the configured repositories, retrieves
their POMs and resolves transitive dependencies.

In some cases, artifacts are not available in well-known public repositories. For instance:
- artifact is proprietary
- nobody bothered packaging the artifact (e.g., Saxon9)
- artifact is available, but its sources are not

### The Nexus solution ###

One solution to such problems is to run an instance of (Nexus) repository manager, upload the missing artifacts there
and configure it in you project's POM. (Actually, a repository manager should be used even when all artifacts are
available in public repositories, but that is a different story :)).

But what if this is not an option? What if - for political or technical reasons - you really need to carry some
dependencies with your project? How do you do it?

### The "system scope" attempt ###

It is possible to describe in-project dependencies using "system" scope and URL that references a jar co-located with
the project. Unfortunately, system-scoped dependencies break the transitive dependency resolution of Maven. (Also, if
your in-project dependency is needed only for tests, there is no way to specify a "test" scope for it...)

### The in-project repository ###

To embed a Maven repository in the project:

Create a subdirectory "lib" in the project directory.

Define this subdirectory as a repository in the project's POM:
```xml
<repository>
    <id>lib</id>
    <name>lib</name>
    <releases>
        <enabled>true</enabled>
        <checksumPolicy>ignore</checksumPolicy>
    </releases>
    <snapshots>
        <enabled>false</enabled>
    </snapshots>
    <url>file://${project.basedir}/lib</url>
</repository>
```

(Above assumes that you want to turn off the SHA1 checksum checking for your in-project repository.)

For an artifact with groupId x.y.z, content of the "lib" subdirectory should be:
```
lib
  |-- x
      |-- y
          |-- z
              |-- ${artifactId}
                  |-- ${version}
                      |-- ${artifactId}-${version}.pom
                      |-- ${artifactId}-${version}.pom.sha1
                      |-- ${artifactId}-${version}.jar
                      |-- ${artifactId}-${version}.jar.sha1
```

POMs for in-project dependency artifacts are not, strictly speaking, necessary. But if you do not have them, Maven will
attempt to retrieve the POMs from the other repositories (including central) for each build.

SHA1 checksums are not necessary either - if you indicate in the repository definition (see below) that they should be
ignored.

This structure can be created manually - or using Maven itself, thus reducing the risk of typos
(thank you @[vmp](http://www.blogger.com/profile/18172568217105448018) for the idea
and @[Jeremy](http://www.blogger.com/profile/06300558549133430072) for the generatePom tip!):

Install the artifact in the local repository with
```shell
mvn install:install-file -Dfile=myArtifact.jar -DgroupId=x.y.z -DartifactId=${artifactId} -Dversion=${version} -Dpackaging=jar -DgeneratePom=true
```
and then copy resulting files from the local repository into the in-project one.

### Obsolete "legacy" layout ###

It is somewhat simpler to use the Maven1 repository layout. That means that under lib directory there are directories
for each groupId that you need:
```
lib
  |-- ${groupId}
      |-- poms
          |-- ${artifactId}-${version}.pom
      |-- jars
          |-- ${artifactId}-${version}.jar
      |-- java-sources
          |-- ${artifactId}-${version}-sources.jar
```
Sub-directory "java-sources" is needed only if you want to publish the sources.

In the repository definition, add:
```xml
<repository>
   <layout>legacy</layout>
</repository>
```

With Maven 3, "legacy" layout is going away, so the default layout has to be used.

### Modules ###

For multi-module projects, a "lib" repository declared in the parent POM is inherited by each module. Artifacts are
being looked up in the "lib" folder under each module. Where to put aftifacts depends on the build order: the first
module that needs an artifact has to contain it in the "lib" folder; figuring this out is tedious - and empty "lib"
folders left around by the build are not pretty :)

One approach to deal with that is to declare the "lib" repository in a separate module dedicated to the in-project
repository. That module contains all the artifacts in its "lib" folder and declares them as dependencies, so that
the local Maven repository is populated during the build of the module.

The modules that actually use an artifact carried in the in-project repository can declare a dependency on the "lib"
module, ensuring that the needed artifact will be present in the local repository prior to the module's build.
Unfortunately, *all* of the in-project artifacts will transitively become the module's dependencies.

Or: leave the "lib" module out of the dependencies of the rest of the modules, and ensure that the local repository is
populated by running a build of that module manually (once on the empty system and every time the in-project
dependencies change).

### Mirrors ###

For this to work when using a mirror, in-project artifacts have to be excluded from mirroring. One way to do it is:
in the mirror definition's "mirrorOf" element, put "external:*" instead of (too inclusive) "*". (Thanks for the tip,
anonymous commenter!)


## Comments ##

> **[tmoreira](https://www.blogger.com/profile/02746102746554973322)** Wednesday, February 22, 2012 9:12:00 AM
>
> Nice post! Fits like a charm on my current project.

  **Leonid Dubinsky** Wednesday, February 22, 2012 3:12:00 PM

  Thanks!


> **[par](http://stackoverflow.com/users/312594/par)** Wednesday, February 22, 2012 11:41:00 PM
>
> Pro post. Seems interesting that tmoreira2020 and I both needed this info on the same day.
> I found this via a stackoverflow article, you might consider reposting this in that thread as it's so useful.
>
> [http://stackoverflow.com/questions/364114/can-i-add-jars-to-maven-2-build-classpath-without-installing-them](http://stackoverflow.com/questions/364114/can-i-add-jars-to-maven-2-build-classpath-without-installing-them)

  **Leonid Dubinsky** Thursday, February 23, 2012 12:52:00 PM

  Thanks! You are right, since the post is helpful, it probably should be on stackoverflow, but the question is closed for
  casual answers. I need to get some reputation first :)

> **[vmp](https://www.blogger.com/profile/18172568217105448018)** Saturday, March 31, 2012 9:17:00 AM
>
> Great post, thanks! I think this is the best solution and I am using it already. Just a couple minor corrections that I
> found with trial and fail, since I am an absolute noob in maven.
>
> The folder structure for groupId=x.y.z and artifactId=artefact will be x/y/z/artifact/version (the artifact folder was
> missing from the post.
>
> Also, if the lib directory is at the same level with the pom.xml then the url will be file://${project.basedir}/lib
>
> Btw, after spending about 5 minutes to manually create the correct folder structure and produce the sha1 checksums, I
> found a nice "trick".
>
> Install the artifact in the local repository using the install plugin:
> ```
> mvn install:install-file -Dfile=myArtifact.jar -DgroupId=com.example.group -DartifactId=myArtifact -Dversion=1.0 -Dpackaging=jar
> ```
> and then just go to your local repository and copy the produced files. It just reduces the risk of typos.
>
> Thanks!


  **Leonid Dubinsky** Monday, April 09, 2012 12:24:00 PM

  Thank you for your kind words and suggestions (which I incorporated into the post).

> **[Rajiv Nair](https://www.blogger.com/profile/11026523356003215779)** Tuesday, June 12, 2012 8:46:00 PM
> 
> Thank you so much!! We're doing a big move to maven from ant and your solution really helps get rid of a large part of
> my daily pains :)

  **Leonid Dubinsky** Tuesday, June 26, 2012 6:42:00 PM

  Thanks!


> **[chris](https://www.blogger.com/profile/06710695481203187688)** Tuesday, June 26, 2012 6:41:00 AM
> 
> very nice article.....it helped me a lot.....

  **Leonid Dubinsky** Tuesday, June 26, 2012 6:43:00 PM

  Thanks!

> **Erwin** Wednesday, September 05, 2012 5:38:00 AM
> 
> Went the systemPath way...until I read you blog post. Thanks!

  **Leonid Dubinsky** Thursday, October 18, 2012 4:57:00 PM

  Thanks! Glad it helped.

> **[Răzvan Rotaru](https://www.blogger.com/profile/01144730032959489977)** Friday, September 21, 2012 9:07:00 AM
> 
> I'm not the first to say this, but thanks. This is exactly what I was looking for.


  **Leonid Dubinsky** Thursday, October 18, 2012 4:58:00 PM

  Thank you for your kind words!

> **[nathan](http://nathan.seedoftruth.net/)** Monday, October 22, 2012 1:05:00 AM
> 
> Awesome! Had issues at first because I was using nested modules and had everything in the child project, however after
> moving the repo folder and info to the parent, and adding the dependency to the child pom then it worked.

  **Leonid Dubinsky** Monday, November 12, 2012 4:50:00 PM

  Thanks! Glad it worked :)


> **[Siva Prasad Reddy](https://www.blogger.com/profile/04244498738186957602)** Monday, December 10, 2012 7:10:00 AM
>
> Leonid Dubinsky,
> Java community is moving ahead because of the good, passionate developers like you who shares knowledge with the community.
>
> Thanks for the info.
>
> Cheers,
> Siva
> www.sivalabs.in

  **Leonid Dubinsky** Sunday, December 16, 2012 9:25:00 PM

  Thank you for your kind words!


> **[stef](https://www.blogger.com/profile/01286838048317579381)** Monday, December 10, 2012 7:33:00 AM
>
> +1 for the Nexus solution. That is what we are using.

  **Leonid Dubinsky** Sunday, December 16, 2012 9:29:00 PM

  As you should. Sometimes it is not practical, and then in-project repository is the way to go for the repeatable,
  no-configuration-needed builds.


> **Anonymous** Tuesday, December 18, 2012 8:58:00 AM
>
> It's not working into follwing case: if you have "mirrors" section into your settings.xml file.
> ```xml
> <mirrors>
>   <mirror>
>     <mirrorOf>*</mirrorOf>
>     <url>some.url</url>
>     <id>some.id</id>
>   </mirror>
> </mirrors>
> ```

  **[Ajay](http://www.google.com/)** Monday, April 22, 2013 2:26:00 AM

  Hi, 
  what is reason for that, if I use with mirror tag. 
  It true that when I remove the mirros tag then it works fine.
  But I need mirror tag as well as this soluntion.

  **Anonymous** Tuesday, May 07, 2013 9:55:00 AM

  Please, see my comment above about mirror and in-project repo. You can use "external:*" to avoid mirroring private repo.
  Denis.


> **[Jeremy](https://www.blogger.com/profile/06300558549133430072)** Sunday, December 30, 2012 4:46:00 PM
>
> Thanks! One additional tip if you are using the mvn install:install-file mechanism and/or if the JAR file you want to
> use doesn't have a POM you can use the `-DgeneratePom=true` parameter. For instance I'm using db4o:
> 
> ```
> mvn install:install-file -DgroupId="com.db4o" -DartifactId="db4o-all" -Dversion="8.0.249.16098" -Dfile=db4o-all-8.0.249.16098.jar -Dpackaging=jar -DgeneratePom=true
> ```
> 
> Thanks for the tip on creating an in project dependency!

  **Leonid Dubinsky** Sunday, December 30, 2012 11:08:00 PM

  Added "generatePom" to the post. Thanks!

**Anonymous** Tuesday, May 07, 2013 9:03:00 AM

Also, if your company uses some kind of Maven proxy (ex. Nexus) to mirror all public repos (like central), you can
connect it in your local maven settings.xml
  
[http://maven.apache.org/guides/mini/guide-mirror-settings.html](http://maven.apache.org/guides/mini/guide-mirror-settings.html)

But be careful: in case of in-project repo you have to set mirror for `"external:*"`, not `"*"`. Otherwise maven will
not be able to find artefacts from in-project repo.
