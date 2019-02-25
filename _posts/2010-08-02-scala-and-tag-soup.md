---
layout: post
title: Scala and Tag Soup
date: '2010-08-02T15:16:00.009-04:00'
author: dub
tags:
- scala
- xml
modified_time: '2015-02-15T00:39:16.509-05:00'
blogger_id: tag:blogger.com,1999:blog-8681083740214020499.post-5712231194807527541
blogger_orig_url: https://blog.dub.podval.org/2010/08/scala-and-tag-soup.html
---

Scala has [very good](http://weblogs.java.net/blog/cayhorstmann/archive/2010/05/16/xml-processing-scala) support for XML,
including built-in XML parser and access to the platform-provided parsers.

Sometimes the documents that need to be parsed are in HTML, and XML parsers can not handle that, since the rules of
element nesting and attribute values are different. [TagSoup](http://home.ccil.org/~cowan/XML/tagsoup/) package by John
Cowan is designed to bridge the gap. The question is: how to hook TagSoup into Scala's XML parsing?

Google search came back with two relevant results:
[How to use TagSoup with Scala XML?](http://scala-programming-language.1934581.n4.nabble.com/How-to-use-TagSoup-with-Scala-XML-td1940874.html#a1940874)
and [Processing real world HTML as if it were XML in scala](http://www.hars.de/2009/01/html-as-xml-in-scala.html),
both by Florian Hars.

Unless I am missing something, at least in Scala 2.8 there is a simpler solution:
```scala
import scala.xml.{Elem, XML}
  import scala.xml.factory.XMLLoader
    
  import org.ccil.cowan.tagsoup.jaxp.SAXFactoryImpl
    
    
  object TagSoupXmlLoader {
    
    private val factory = new SAXFactoryImpl()
  
    
    def get(): XMLLoader[Elem] = {
      XML.withSAXParser(factory.newSAXParser())
    }
  }
```

Strictly speaking, the class is not needed; one-liner
```scala
XML.withSAXParser(new org.ccil.cowan.tagsoup.jaxp.SAXFactoryImpl().newSAXParser())
```
is all it takes! But, the object provides the scope where to put the code that configures features of the SAXFactoryImpl
if such a need ever arises :)
