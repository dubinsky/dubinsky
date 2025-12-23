---
layout: post
title: Where is my code running?
author: Leonid Dubinsky
tags:
  - gradle
date: 2025-12-22
---
In 2017, after writing the same code a number of times, I made a little [library](https://github.com/dubinsky/podval-run) (now archived) for determining from within the code how is the code being run: which build tool, application service or IDE is involved, is it a test run or not etc.

Since then I noticed that:
- I only use one IDE (IntelliJ Idea), which sets property `idea.active` to `true`;
- I do not use any application servers (I just embed a ZIO HTTP server);
- I only use one build tool (Gradle).

Sometimes, when running within a clone of a repository, my code needs a file-system path it, so that it can locate test data, web resources and such. The trick I use is:
- add an empty resource file `anchor.txt` to the repository;
- use `getClass.getResource(".../anchor.txt")` to obtain the `URL` of the resource;
- use `java.nio.file.Paths.get(url.toURI).toFile` to obtain the `File` of the resource;
- use the correct number of `file.getParentFile` invocations to calculate the root of the repository :)