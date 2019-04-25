---
layout: post
title: XML and Scala
date: '2011-03-24T11:48:00.021-04:00'
author: Leonid Dubinsky
tags: 
modified_time: '2014-05-26T16:11:46.573-04:00'
blogger_id: tag:blogger.com,1999:blog-8681083740214020499.post-2463497025338849164
blogger_orig_url: https://blog.dub.podval.org/2011/03/xml-and-scala.html
---

There seems to be an XML backlash going on (see "Ongoing" ... "Meh"). I understand that in many cases XML might not be
as appropriate as it was hyped to be:
- configuration, if it does not need to be externalizable; annotations in Java and Scala are more convenient;
- for data transfer to Web applications Json might be the right thing;
- HTML5 (now just HTML) superseded XHTML etc.

But for document processing, XML is the way!

XML is everywhere, and support for XML processing, including parsing, is available for all languages and platforms.

Scala has very good support for XML, including built-in XML parser and access to the platform-provided parsers.

Its author, Burak Emir says:

If you deal with XML, you can make your life simpler by using scala.xml. By now I finally think that xml is a big
mistake, but I still stand by the API which follows several standards closely. It is all explained in the scala.xml book.

In an excellent piece ["Working with Scala’s XML Support"](http://www.codecommit.com/blog/scala/working-with-scalas-xml-support)
Daniel Spiewak quotes (and agrees with) Martin Odersky saying that he half-regrets inclusion of XML support in Scala,
but agrees that it turned out to be very useful ;)

XML Support in Scala is not without problems, as Spiewak notes. There are
["gotchas"](http://blog.markfeeney.com/2011/03/scala-xml-gotchas.html). I remember a long post explicating all the
problems, but I misplaced it :( Here is a an illustration how non-trivial is something that should be completely
trivial: [http://stackoverflow.com/questions/2569580/how-to-change-attribute-on-scala-xml-element](http://stackoverflow.com/questions/2569580/how-to-change-attribute-on-scala-xml-element).

- Pattern matching on attributes does not work
- Built-in parser is problematic, but can be replaced (even with TagSoup :))
- Built-in transformations are not good
- XPATH support is not great

[GData: I can't take it anymore](http://blog.bolinfest.com/2011/05/gdata-i-cant-take-it-anymore.html)

Hortsman: say no to XSLT!

[Scales](http://scala-scales.googlecode.com/svn/sites/snapshots/scales/scales-utils_2.8.1/0.1/index.html)

[Anti-XML](https://github.com/djspiewak/anti-xml)

[http://weblogs.java.net/blog/cayhorstmann/archive/2011/12/12/sordid-tale-xml-catalogs](http://weblogs.java.net/blog/cayhorstmann/archive/2011/12/12/sordid-tale-xml-catalogs)

[Efficient Semi-structured Queries in Scala using XQuery Shipping](http://infoscience.epfl.ch/record/85493/files/Scala_XQuery.pdf)

[XQS](https://github.com/fancellu/xqs)

XQuery: close to the data.

Wolfgang: application in XQuery (AtomicWiki). With interface code and custom functions in  Java/Scala, this can be done. But:
- no static typing;
- no objects (manual encoding is not the real thing :))
- no exceptions (before XQuery 1.1)
- no concurrency
- no integration with libraries

So, I'd rather "send data" :) I want to write in Scala - but process XML!

From Java: parsing, XPath (only v 1.0) and XSLT APIs are cumbersome; DOM - impossible to work with; JDom sucks and no
XPATH... XQuery - XML literals with expression escapes - rules!

Interfacing with XQuery via XQJ gives: pipelining, iteration, XPATH, parsing, XSLT (through non-standard functions), literals...

In pure Scala - literals with escapes into Scala, XPath library, comprehensions!

Scala is good for XML processing *not* because of the built-in support.

I am sure that when I'll need to "send the query", it'll be doable in Scala, and "pipelining", promissed by
proprietary(!) Saxon & Exist, did not work, but has better chance of being implementable in Scala, I think.

TEI people started on the HTML serialization: permalink.gmane.org/gmane/text.tei.general/11589

They say that microformats and RDF are past; microdata is the future: schema.org; www.w3.org/TR/HTML5/microdata
