---
layout: post
title: The File System Problem
date: '2013-06-02T18:43:00.000-04:00'
author: Leonid Dubinsky
tags: [scala]
modified_time: '2013-06-02T18:43:05.591-04:00'
blogger_id: tag:blogger.com,1999:blog-8681083740214020499.post-1500864965174868633
blogger_orig_url: https://blog.dub.podval.org/2013/06/the-file-system-problem.html
---

# In praise of Scala traits or, design of a file-system-like class hierarchy. #

Sometimes one need to code an hierarchical store. For instance, a hierarchy of photo albums or a
music collection. There are things specific to photos that do not apply to tunes, but a lot of
the features are common. Our goal is to capture common characteristics of a file-system-like
situation in an extendable way, so that a specific application can be derived from the common
base - without code duplication.

We have a set of types that evolve together: Connection, Folder (non-leaf node), Photo (or Tune;
in general - Item). Connection hosts functionality that does not belong in a specific Folder or Item.

Another example in need of family polymorphism is a calendar: Year. Month, Day and Moment reference each other and
evolve together.

## Folder classification ##

A Folder can be root or non-root.

Each non-root Folder has a reference to its parent; it is undefined for the root Folder.

Root Folder has a reference to the Connection this Folder belongs to; it is undefined for the non-root Folders.

Some Folders can contain Items, some can't. Some can contain other Folders, some can't.

A list of Items and sub-Folders of a Folder can be retrieved from it.

Traditional approach is to enforce the classification at runtime, e.g. enforce emptiness of the subfolder list on a
no-subfolders folder; it is desirable to use involve the typesystem a bit more :)

An attempt to express folder classification in Java results in code duplication. With type evolution, which in Java can
only be expressed via generics, this duplication becomes even worse.

In addition, generics-based declarations are unwieldy since all types in the family reference one another, and return
types of methods need to evolve.

In Scala, mixins help dealing with code duplication, but generics-based declarations are still
unwieldy, and it is not clear how to "tie the knot" so that no casts are needed. Even when
abstract type members are used, making declarations cleaner, "knot tying" is an issue. Both
approaches (generics and abstract type members) are helped somewhat by the self-types, but some
casts remain :(


Cake Pattern?

Miles Sabin?

"Family Polymorphism" (E. Ernst)

["Family Polymorphism in Scala"](http://www.familie-kneissl.org/Members/martin/blog/family-polymorphism-in-scala)

[http://ctp.di.fct.unl.pt/mei/pmp/teoricas/08.html](http://ctp.di.fct.unl.pt/mei/pmp/teoricas/08.html)

"Scalable Component Abstractions" (Odersky, Zenger)

["Objects and Modules - Two Sides of the Same Coin?" (Odersky)](http://events.inf.ed.ac.uk/Milner2012/slides/Odersky/Odersky.pdf)

