---
layout: post
title: Jersey and Scala
date: '2010-08-02T14:47:00.008-04:00'
author: dub
tags: [scala, web services, xml]
modified_time: '2014-05-25T22:01:07.890-04:00'
blogger_id: tag:blogger.com,1999:blog-8681083740214020499.post-7794478870804755238
blogger_orig_url: https://blog.dub.podval.org/2010/08/jersey-and-scala.html
---

[JAX-RS (JSR 311)](http://jcp.org/en/jsr/detail?id=311) specification describes a way to implement
[RESTful web services](http://www.amazon.com/Restful-Web-Services-Leonard-Richardson/dp/0596529260)
[in Java](http://www.amazon.com/RESTful-Java-Jax-RS-Animal-Guide/dp/0596158041) using annotations.
[Jersey](https://jersey.dev.java.net/) is an implementation of the JAX-RS specification.
There are others, but I currently prefer Jersey.

I am switching to Scala, and although there are some Scala-native REST frameworks - for instance,
[Scalatra](https://github.com/scalatra/scalatra) - I intend to continue to use Jersey. Just as
[James Strachan](http://macstrac.blogspot.com/), author of Scala template engine [Scalate](http://scalate.fusesource.org/),
[advises](http://servicemix.396122.n5.nabble.com/Scalatra-to-SMX4-td3265674.html). By the way, I intend to use Scalate
also :)

One of the nice things about JAX-RS (and Jersey) is: a method that serves a request returns the result entity, and the
implementation take care of serializing it to the client. A rich variety of data types is supported (String, File,
InputStream, etc.).

Scala has XML support built into the language. XML literals can be embedded in the Scala code. Methods that return XML
(with or without XML literals) have return type [scala.xml.Node](http://www.scala-lang.org/api/current/scala/xml/Node.html)
(or Seq[Node]).

```scala
  @GET
  @Produces(Array("text/html"))
  final def getHtml() = {
    <html>
      <head>
        <title>{getTitle()}</title>
        <link rel="stylesheet" type="text/css" href="/system/style.css"/>
      </head>
      <body>
        <h class="title">{getTitle()}</h>
        {getBody()}
      </body>
    </html>
}
```

Jersey knows nothing about scala.xml.Node, but it does not mean that an explicit conversion has to be coded for Scala
XML values. JAX-RS provides an extension mechanism for handling of additional data types:
[MessageBodyWriter](https://jsr311.dev.java.net/nonav/releases/1.1/javax/ws/rs/ext/MessageBodyWriter.html).

Application has to make the MessageBodyWriter known to the JAX-RS implementation ("register a Provider"), but after that
the new data type is handled just as one of the standard ones.

I did not have to write a MessageBodyWriter for Scala: James Strachan already
[did it](http://jersey.576304.n2.nabble.com/added-jersey-scala-library-to-trunk-td2725623.html). I just tuned it for the
types that I need, and republish the results here for easier reference :)

```scala
  import java.io.OutputStream
  import java.lang.annotation.Annotation
  import java.lang.Class
  import java.lang.reflect.Type

  import javax.ws.rs.core.{MultivaluedMap, MediaType}
  import javax.ws.rs.ext.{MessageBodyWriter, Provider}

  import scala.xml.{Node, PrettyPrinter}

  @Provider
  class ScalaXmlNodeMessageBodyWriter extends MessageBodyWriter[Node] {

    def isWriteable(
      aClass: Class[_],
      aType: Type,
      annotations: Array[Annotation],
      mediaType: MediaType) =
    {
      classOf[Node].isAssignableFrom(aClass)
    }


    def getSize(
      nodes: Node,
      aClass: Class[_],
      aType: Type,
      annotations: Array[Annotation],
      mediaType: MediaType) =
    {
      -1L
    }


    def writeTo(
      nodes: Node,
      aClass: Class[_],
      aType: Type,
      annotations: Array[Annotation],
      mediaType: MediaType,
      stringObjectMultivaluedMap: MultivaluedMap[String, Object],
      outputStream: OutputStream) : Unit =
    {
      var answer = prettyPrinter.format(nodes)
      outputStream.write(answer.getBytes())
      outputStream.write('\n')
    }


    private val prettyPrinter = new PrettyPrinter(120, 4)
  }
```

I am considering generalizing this to handle Seq[Node], but even in its present state the code does what I need it to do.