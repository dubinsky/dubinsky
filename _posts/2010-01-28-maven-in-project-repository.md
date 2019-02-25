---
layout: post
title: Maven in-project repository
date: '2010-01-28T14:24:00.004-05:00'
author: dub
tags:
- maven
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
(thank you +[vmp](http://www.blogger.com/profile/18172568217105448018) for the idea
and +[Jeremy](http://www.blogger.com/profile/06300558549133430072) for the generatePom tip!):

Install the artifact in the local repository with
```
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
