---
layout: post
title: Creating executable JARs with Maven
date: '2009-10-18T15:50:00.027-04:00'
author: dub
tags:
- maven
- java
modified_time: '2013-04-25T15:21:13.777-04:00'
blogger_id: tag:blogger.com,1999:blog-8681083740214020499.post-2506818829173082354
blogger_orig_url: https://blog.dub.podval.org/2009/10/creating-executable-jars-with-maven.html
---

### Maven Assembly Plugin ###

I wanted to make a self-contained executable JAR for the project mentioned in a previous
[post](/2009/08/30/consuming-zenfolio.html). I used
[Assembly](http://www.sonatype.com/books/maven-book/reference/assemblies.html) plugin. Some of the transitive
dependencies are not really needed in my case, and I did not want to include them. Assembly plugin provides full control
over the contents of the archive through the assembly descriptor. Also, exclusions can be used on the direct
dependencies to control the dependencies set, and then the assembly can be created using predefined
"jar-with-dependencies" descriptor.

### One-JAR™ ###

Resulting archive then contains what I want, but all the dependency JARs are unpacked. That causes confusion (and,
potentially, proliferation of files with the same names :)). I'd like to put dependencies' JARs into a "lib" folder in
the executable archive, and use JAR classloader mechanisms to load classes from the jars-within-the-jar.

I am far from the first to think that this approach should just work. But it does not: built-in classloader won't open
the nested JARs! There is a enhancement [request](http://bugs.sun.com/view_bug.do?bug_id=4648386) that's been filed
years ago, and eventually this may start working as expected, but not yet.

It turned out that I did not have to write my own classloader. P. Simon Tuffs already created  a wonderful solution:
One-JAR™[http://one-jar.sourceforge.net/] classloader.

I want to use java.util.ServiceLoader in my project, for dynamic discovery of service providers. I was worried about it
not working with the One-Jar approach. Everything works just fine!


### Maven and One-JAR™ ###

I used Brian Oxley's [advice](http://binkley.blogspot.com/2006/12/making-one-jar-with-maven.html) on how to integrate
Maven with One Jar using Assembly plugin. Dependency on One-Jar has to be added:
```xml
<dependency>
    <groupid>com.simontuffs</groupid>
    <artifactid>one-jar</artifactid>
    <version>0.97</version>
</dependency>
```

I couldn't find One-Jar in any Maven repository, so it has to be loaded into your corporate repository, or to a
[in-project](/2010/01/28/maven-in-project-repository.html) repository.

Here is my Assembly plugin stanza:
```xml
<plugin>
    <artifactId>maven-assembly-plugin</artifactId>
    <configuration>
        <descriptors>
            <descriptor>src/main/assembly/assembly.xml</descriptor>
        </descriptors>
        <archive>
            <manifest>
                <!-- For One Jar executable, mainClass is the One Jar entry point: -->
                <mainClass>com.simontuffs.onejar.Boot</mainClass>
            </manifest>
            <manifestEntries>
                <one-jar-main-class>FQN OF THE REAL MAIN CLASS</one-jar-main-class>
            </manifestEntries>
        </archive>
    </configuration>
    <executions>
        <execution>
            <id>make-assembly</id>
            <phase>package</phase>
            <goals>
                <goal>single</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

And the assembly descriptor has the following:

```xml
<assembly>
  <id>one-jar</id>

  <formats>
    <format>jar</format>
  </formats>

  <includeBaseDirectory>false</includeBaseDirectory>

  <dependencySets>
    <!-- One Jar classloader itself -->
    <dependencySet>
        <outputDirectory/>
        <unpack>true</unpack>
        <includes>
            <include>com.simontuffs:one-jar</include>
        </includes>
        <unpackOptions>
            <includes>
                <include>OneJar.class</include>
                <include>com/**</include>
                <include>doc/**</include>
            </includes>
        </unpackOptions>
    </dependencySet>

    <!-- Project JAR -->
    <dependencySet>
        <outputDirectory>main</outputDirectory>
        <includes>
          <include>${groupId}:${artifactId}</include>
        </includes>
        <!--<outputFileNameMapping>main.jar</outputFileNameMapping>-->
    </dependencySet>

    <!-- Dependencies -->
    <dependencySet>
        <outputDirectory>lib</outputDirectory>
        <useTransitiveDependencies>true</useTransitiveDependencies>
        <excludes>
            <exclude>com.simontuffs:one-jar</exclude>
            <exclude>${groupId}:${artifactId}</exclude>
        </excludes>
    </dependencySet>
  </dependencySets>
</assembly>
```

Interestingly enough, the assembly descriptor is completely project-independent, and can be reused in other projects.

### One-Jar Maven plugin ###

There is also a Maven2 [plugin](http://code.google.com/p/onejar-maven-plugin/) that integrates with One Jar. To use it,
additional plugin repository needs to be configured. On the other hand, dependency on One-Jar does not need to be
declared. Assembly descriptor and assembly plugin are not needed either.

Resulting configuration is short and simple. Here is the plugin repository configuration:
```xml
<pluginRepositories>
    <pluginRepository>
        <id>onejar-maven-plugin.googlecode.com</id>
        <url>http://onejar-maven-plugin.googlecode.com/svn/mavenrepo</url>
    </pluginRepository>
</pluginRepositories>
```

And this is how I configured the One-Jar plugin:
```xml
<plugin>
    <groupId>org.dstovall</groupId>
    <artifactId>onejar-maven-plugin</artifactId>
    <version>1.4.1</version>
    <executions>
        <execution>
            <configuration>
                <mainClass>.....</mainClass>
            </configuration>
            <goals>
                <goal>one-jar</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### Interoperation with Guice ###

Running Guice from within One-Jar produces a nasty warning about class loaders. The solution (until One-Jar is fixed) is
to [disable](http://code.google.com/p/google-guice/wiki/ClassLoading) Guice's bridge class loader. This solution is
[described](http://sourceforge.net/projects/one-jar/forums/forum/380844/topic/3542317) by Niall Gallagher.

```java
System.setProperty("guice.custom.loader", "false");
```
It seems that starting with version 0.97 (supported by One-Jar plugin staring with version 1.4.4) the issue has been
fixed!

### Interoperation with JNA ###

I heard about some issues in the past, but right now One-Jar and JNA seem to work well together.
