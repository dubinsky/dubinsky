---
layout: post
title: 'Bintray Gradle plugin'
author: Leonid Dubinsky
tags: [bintray, gradle]
date: '2020-06-29'
---

* TOC
{:toc}
## Introduction, или ["блядский пиздец"](https://github.com/bintray/gradle-bintray-plugin/issues/302) ##

[GitHub](https://github.com/) is important for software development: almost all the sources are there.
[Bintray](https://bintray.com/) is also important: almost all packages are in
[JCenter](https://bintray.com/beta/#/bintray/jcenter) -
at least until everybody switches to [GitHub Packages](https://github.com/features/packages) :)
It is thus unpleasant that Bintray website is unpolished and its functionality confusing - but
at least it works.

For developers using Gradle, Bintray [Gradle plugin](https://github.com/bintray/gradle-bintray-plugin) is
important: it is the way to upload the packages to JCenter. It is thus unpleasant that the plugin
neglects to update some of the package attributes when they are changed in the Gradle build script, or that
it uses deprecated Gradle APIs, resulting in build warnings - but at least it works (for now; plugin v1.8.5 is
incompatible with the upcoming Gradle 7).

It is sad that such an important plugin seems to be neglected: updates and bug-fixes are very
[rare](https://github.com/bintray/gradle-bintray-plugin/commits/master),
[documentation](https://github.com/bintray/gradle-bintray-plugin) is incomplete,
[examples](https://github.com/bintray/bintray-examples) are obsolete (Gradle 2?!) etc. - but
at least it works.

In this post, I focus on a setting where the plugin actually breaks, and describe some workarounds for
the breakage. This setting is: Gradle project with multiple subprojects where more than one subproject
uploads its artifacts to Bintray. This is far from being an edge case: it is how things are for
the [fans](https://danluu.com/monorepo/) of
[monorepos](https://www.bitquabit.com/post/unorthodocs-abandon-your-dvcs-and-return-to-sanity/) :)

## 'Cannot cast' ##

Let's say you have in your Gradle project at least two subprojects that need to upload artifacts
to Bintray. You configure Bintray plugin for each of them. Yes, it leads to code duplication, since
most of the plugin configuration is the same for all the artifacts produced by the monorepo
(I describe a way to cut down on this code duplication in the last section of this post),
but at least it works, right? Well, not always!
Sometimes, when you run `$ ./gradlew bintrayUpload` you get an error message
from `bintrayPublish` task of one of your Bintray-uploading subprojects:  
```
Cannot cast object 'task ':bintrayUpload''
with class 'com.jfrog.bintray.gradle.tasks.BintrayUploadTask_Decorated'
  to class 'com.jfrog.bintray.gradle.tasks.BintrayUploadTask'
```

It does not happen for all subprojects, and conditions triggering this error are not clear.
One scenario that triggers this bug is: a subproject that configures both the Bintray plugin
and Gradle Plugin Portal
plugin [plugin](https://guides.gradle.org/publishing-plugins-to-gradle-plugin-portal/).

Sometimes the class the error mentions is different, but it is always a failure to cast from a
subclass to superclass for one of the Bintray plugin's classes.

It turns out that Bintray plugin running in one subproject triggers `bintrayPublish` task in
*other* subprojects where it is configured, even when it shouldn't (e.g., subprojects A and B
are configured with Bintray plugin, and subproject B depends on subproject A;
`$ ./gradlew :A:bintrayUpload` triggers `bintrayPublish` task in subproject B, which fails).

This failure results from the fact that plugins are run with per-project classloaders, so
class C loaded by the Bintray plugin in one subproject is not the same as class C loaded by
in another subproject. So if Bintray plugin reaches out to a different subproject for
whatever reason (e.g., that other subproject configures or uses some Maven publications),
everything breaks.  

Thi classloader issue has been known since at least 2015, as this very helpful
[post](http://forums.jfrog.org/Gradle-plugin-and-multi-projects-tp7580521p7580523.html)
by HughG_TMVSE shows, so it seems unlikely that Bintray will fix the real problem of
cross-subproject reach. But there is a workaround: ensure that Bintray plugin classes
are loaded on the root project.

So, in addition to declaring Bintray plugin version in the `settings.gradle` file:
```groovy
pluginManagement {
  plugins {
    id 'com.jfrog.bintray' version '1.8.5'
  }
}
```
and configuring it on subprojects that actually need it:
```groovy
plugins {
  id 'com.jfrog.bintray'
}
```
you need to also declare the plugin in the root project (without applying it, since
it is not used nor configured in the root project): 
```groovy
plugins {
  id 'com.jfrog.bintray' apply false
}
```

This "solution", although a part of the folklore, is not mentioned in the Bintray plugin documentation,
so everybody has to re-discover it for themselves, wasting a lot (at least in my case) time.
This is bad enough, but what is worse is - this workaround should not be necessary at all;
the underlying problem with the plugin should be fixed instead.     

## 'BuildStepsExecutionException: INSTANCE' ##

As the result of the above workaround, Bintray plugin's classes are on the `buildEnvironment` classpath
of every subproject, even ones that do not need or use that plugin. This increases chances of
version conflicts between dependencies of plugins applied to the same subproject.   

Indeed, there is such a conflict betweem Bintray plugin and 
[JIB Gradle plugin](https://github.com/GoogleContainerTools/jib/tree/master/jib-gradle-plugin)
(great tool for Docker-conteinerizing applications). Specifically, current version of the JIB plugin -
v2.4.0 - uses Apache HTTP client (`org.apache.httpcomponents:httpclient`) v4.5.10
(via `com.google.http-client:google-http-client:1.34.0`); current Bintray plugin - v1.8.5 - also
uses Apache HTTP client, but an older version: v4.2.1.

Since Bintray plugin (and its dependencies) is added to the classpath in the root project (earlier),
and JIB plugin (and its dependencies) are added to the classpath in the subprojects that actually use
it (later), Apache HTTP client that JIB calls gets downgraded from v4.5.10 to v4.2.1. Turns out, JIB
actually *needs* the later version of the Apache HTTP client, and this downgrade results
in an error when running `$ ./gradlew jibDockerBuild`:
```
  com.google.cloud.tools.jib.plugins.common.BuildStepsExecutionException:
    INSTANCE
```
Of course, since Gradle daemon caches loaded classes, this error looks differently on the subsequent runs:
```
  com.google.cloud.tools.jib.plugins.common.BuildStepsExecutionException:
    Could not initialize class
      org.apache.http.conn.ssl.SSLConnectionSocketFactory
```
To get back to the the clean break, you need to stop the daemons with `$ ./gradlew --stop` (or run Gradle
with `--no-daemon` to begin with).

Thankfully, Bintray plugin does work with the later version of the Apache HTTP client, so the workaround
here is to force *upgrade* of it for the Bintray plugin (instead of letting it downgrade JIB).
To do that, the following `buildscript` block needs to be added to the beginning of the root `build.gradle`: 

```groovy
buildscript {
  dependencies {
    classpath ("org.apache.httpcomponents:httpclient") {
      version {
        strictly '4.5.10'
      }
    }
  }
}
```

## Sharing Bintray configuration ##

Assuming you managed to get Bintray plugin working with multiple subprojects using it,
you'll notice that there is a lot of duplication: most of the necessary Bintray-related
configuration is the same for all subprojects. To share configuration between subprojects,
I created a file `library.gradle` in the root directory of the overall Gradle project and
applied it to all subprojects:

```groovy
subprojects {
  apply from: '../library.gradle'
}
```
The file defines a function `configureLibrary()`, which takes a list of tags as a parameter
(everything else is either hard-coded in the function or retrieved from the subproject where it is called);
in each subproject that needs to upload its artifacts to Bintray, I call this function:  
```groovy
configureLibrary(['<TAG1>', '<TAG2>', '<TAG3>'])
```
and apply two plugins:
```groovy
plugins {
  id 'maven-publish'
  id 'com.jfrog.bintray'
}
```

It is possible that plugin applications could be moved into the `configureLibrary()` function,
but I prefer to use the "new" `plugins` DSL instead of the `apply plugin`, and declare all the
subproject's plugins upfront. 

This is how the `library.gradle` file looks:

```groovy
ext.configureLibrary = {
  final List<String> tags
    ->
  final String projectName = project.name
  final String projectDescription = project.description
  final String projectGroup = project.group
  // I like my artifac names to be prefixed:
  final String projectArtifact = "<ARTIFACT-PREFIX>-$projectName"
  final String projectVersion = project.version
  final String gitHubRepository = "<GitHub USER or ORGANIZATION>/<GitHub REPOSITORY>"
  final String gitHubRepositoryUrl = "https://github.com/$gitHubRepository"
  final String codeUrl = "$gitHubRepositoryUrl/tree/master/$projectName"
  final String orgName = '<ORGANIZATION NAME>'
  final String orgUrl = '<ORGANIZATION WEBSITE>'

  // Because why not?
  jar {
    manifest {
      attributes(
        'Implementation-Title'  : projectDescription,
        'Implementation-Version': projectVersion
      )
    }
  }

  jar.archiveBaseName.set(projectArtifact)

  // Gradle plugin [publishing?] plugin, if it is applied,
  // adds tasks publishPluginJar and publishPluginJavaDocsJar
  // that create sources and javadoc archives;
  // attempts to use them as artifacts in the Maven publication failed,
  // so I make my own, replacing javadoc with ScalaDoc :)

  task sourceJar(type: Jar) {
    from sourceSets.main.allSource
    archiveClassifier.set('sources')
  }

  task scaladocJar(type: Jar) {
    from scaladoc.destinationDir
    archiveClassifier.set('scaladoc')
  }
  scaladocJar.dependsOn scaladoc

  publishing {
    publications {
      bintrayMavenPublication(MavenPublication) {
        groupId projectGroup
        artifactId projectArtifact
        version projectVersion

        from components.java

        artifact sourceJar
        artifact scaladocJar

        pom {
          name = projectName
          description = projectDescription
          url = codeUrl
          scm {
            url = "$gitHubRepositoryUrl"
            connection = "scm:git:git://github.com/${gitHubRepository}.git"
            developerConnection = "scm:git:ssh://github.com/${gitHubRepository}.git"
          }
          licenses {
            license {
              name = 'The Apache Software License, Version 2.0'
              url = 'http://www.apache.org/licenses/LICENSE-2.0.txt'
              distribution = 'repo'
              comments = 'A business-friendly OSS license'
            }
          }
          organization {
            name = orgName
            url = orgUrl
          }
          developers {
            developer {
              id = '<YOUR ID>'
              name = '<YOUR NAME>'
              email = '<YOUR EMAIL>'
              organization = orgName
              organizationUrl = orgUrl
              timezone = '-5'
            }
          }
        }
      }
    }
  }

  bintray {
    user         = '<Bintray USER>'
    key          = findProperty('bintrayApiKey')
    publications = ['bintrayMavenPublication']
    dryRun       = false
    publish      = true
    override     = true
    pkg {
      repo                  = projectGroup
      name                  = projectArtifact
      desc                  = projectDescription
      websiteUrl            = codeUrl
      issueTrackerUrl       = "${gitHubRepositoryUrl}/issues"
      vcsUrl                = "${gitHubRepositoryUrl}.git"
      githubRepo            = gitHubRepository
      licenses              = ['Apache-2.0']
      labels                = tags
      publicDownloadNumbers = true
      // Note: there seems to be no way to supply per-module CHANGELOG.md -
      // and package creation fails unless there is an overall one...
      githubReleaseNotesFile= 'CHANGELOG.md'
      version { name        = projectVersion }
    }
  }
}
```
