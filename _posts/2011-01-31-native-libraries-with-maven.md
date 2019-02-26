---
layout: post
title: Native Libraries with Maven
date: '2011-01-31T14:34:00.002-05:00'
author: Leonid Dubinsky
tags:
- maven
modified_time: '2013-04-25T14:59:36.599-04:00'
blogger_id: tag:blogger.com,1999:blog-8681083740214020499.post-122451127561287518
blogger_orig_url: https://blog.dub.podval.org/2011/01/native-libraries-with-maven.html
---

Maven supports the "so" packaging. When "so"-packaged dependency is used in a Maven project, the native library has to
be:
- available to the unit tests;
- available during execution of classes (from within the IDE);
- packaged into the application jar so that it is available during production execution.

Turns out that a fair amount of POM tweaking is needed to achieve all this. As my other Maven-related posts, this one
documents the solution that I found.

### Unpack the libraries from the dependencies ###

Define a property pointing to the directory where the native libraries will be unpacked, e.g.:
```xml
<properties>
    <lib.directory>${project.build.directory}/so</lib.directory>
</properties>
```
Unpack the libraries from the dependencies, e.g.:
```xml
<plugin>
    <groupid>org.apache.maven.plugins</groupid>
    <artifactid>maven-dependency-plugin</artifactid>
    <executions>
        <execution>
            <id>copy</id>
            <phase>process-classes</phase>
            <goals>
                <goal>copy</goal>
            </goals>
            <configuration>
                <artifactitems>
                    <artifactitem>
                        <groupid>org.podval.group</groupid>
                        <artifactid>native-library</artifactid>
                        <type>so</type>
                        <version>1.0-SNAPSHOT</version>

                        <overwrite>true</overwrite>
                        <outputdirectory>${lib.directory}</outputdirectory>
                        <destfilename>${so.</destfilename>
                    </artifactitem>
                </artifactitems>
            </configuration>
        </execution>
    </executions>            
</plugin>
```
The chosen phase has to be early enough to make the libraries available during "execute", "test" and "package" activities.<br />

### Set up unit tests ###

java.library.path has to be set for the native libraries to be available to the unit tests:
```xml
<plugin>
    <groupid>org.apache.maven.plugins</groupid>
    <artifactid>maven-surefire-plugin</artifactid>
    <configuration>
        <argline>-Djava.library.path=${lib.directory}</argline>
    </configuration>
</plugin>
```

Note that "argLine" is used, and not "systemProperties" (or its more modern replacement "systemPropertyVariables"),
since even with the forked execution (forkMode="once", which is the default)  surefire plugin does not set the
java.library.path early enough for it to matter ;) Actually, execution of individual tests from within the NetBeans IDE
works with the "systemProperties" (but not with "systemPropertyVariables"), but I need "mvn test" to just work...

### Set up class execution ###

I do not yet know how to tweak java.library.path for execution of the project classes. Attempts to affect configuration
of the exec-maven-plugin and pass appropriate argument were unsuccessful.

### Package the libraries ###

One-Jar takes care of unpacking the libraries from the jar and putting them where loadLibrary() can will them. All that
needs to be done is telling One-Jar *which* libraries to pack:
```xml
<plugin>
    <groupid>org.dstovall</groupid>
    <artifactid>onejar-maven-plugin</artifactid>
    <executions>
        <execution>
            <configuration>
                ...
                <binlibs>
                    <fileset>
                        <directory>${lib.directory}</directory>
                        <includes>
                            <include>${so.name}</include>
                        </includes>
                    </fileset>
                </binlibs>
            </configuration>
            ...
        </execution>
    </executions>
</plugin>            
```

If One-Jar is *not* being used to package the executable, things get trickier ;)

### Procedurality of it all ###

Most of the necessary steps have a very "procedural", Ant-like feeling to them and go against the - purported -
declarative style of Maven. I do not think that Maven is Object-Oriented enough for this sequence of interrelated steps
to be packageable as a unit (unless it is a special plugin?). In fact, I do not understand why doesn't Maven do all (or
at least most) of the steps itself - after all, I *do* declare an *so* dependency?!

Am I missing something?

## Comments ##

> **[venkat](https://www.blogger.com/profile/04911001746709246957)** Friday, June 29, 2012 11:39:00 PM
> 
> Thanks for the blog post. The native files are copied to target/libs directory. But, I don't see them in the jar file.
> Is this expected ?

  **Leonid Dubinsky** Wednesday, July 11, 2012 1:23:00 PM

  Native libraries should be inside "binlib" directory in the OneJar jar.

**Anonymous** Tuesday, February 26, 2013 12:01:00 PM

A typo there: needs to be "argLine" instead of "argline" in surefire configuration.
