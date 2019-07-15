---
layout: post
title: 'Publishing Papers on the Web: v2'
author: Leonid Dubinsky
tags: [docbook, oxygen, mathjax]
date: '2019-05-06T11:40:00.000-04:00'
modified_time: '2019-07-14T23:30:00.000-05:00'
---
* TOC
{:toc}
## DocBook ##

Some things changed since I [last wrote on this subject](http://dub.podval.org/2011/11/07/publishing-papers-on-web.html):
- it is easy to publish static websites or blogs (like this one) on GutHub [Pages](https://pages.github.com/);
- syntactic highlighting using [MarkDown](https://daringfireball.net/projects/markdown/) is trivial compared to what had
  to be done using - for example - Blogger;
- GitHub Pages natively supports [Jekyll](https://jekyllrb.com/) and quite a few of the Jekyll extensions, so I don't
  even have to check in generated files;
- MarkDown editing is available in IDEs like [IntelliJ Idea](https://www.jetbrains.com/idea/)
  and authoring editors like [Oxygen](https://www.oxygenxml.com/);
- tutorial generators like [Tut](https://github.com/tpolecat/tut) make possible inclusion of code output in the
 published pages;

On the other hand, to make editing the text easier, MarkDown simplifies presentation markup - but removes most of the
semantic markup features and general include mechanisms supported by the traditional XML-based systems.
That makes creation of indexes, glossaries and bibliographies and inclusion of programmatically-generated data either
impossible or dependent on the specific static site generator. For example, it is possible to use MathJax on GitHub
Pages, but to include one MarkDown file in another one has to resort to Jekyll-specific `include-relative` - which may
not even be supported on GitHub pages. 
   
As a result, producing reasonable-quality PDF from MarkDown sources is non-trivial.
For instance, conversion of a recent book published as a series of
[blog posts](https://bartoszmilewski.com/2014/10/28/category-theory-for-programmers-the-preface/)
required a lot of [steps](https://github.com/hmemcpy/milewski-ctfp-pdf/) (and manual intervention).
An even more recent [book](https://plfa.github.io/), written natively in Jekyll,
doesn't have a PDF conversion, and, [according](https://github.com/plfa/plfa.github.io/issues/106)
to the author of the elaborate machinery that generates the book's website:
"This should be possible, and I have created similar setups in the past... but we're not currently
working on this."

I want to be able to publish in both HTML and PDF (preferably, also in EPUB).
Input format of choice to satisfy this requirement is - as it was then - [DocBook](https://docbook.org/). 
It also allows me to easily include tables generated programmatically in my documents.


## FOP ##

I do use non-free tools like Oxygen for editing, but no commercial
tools should be required to process DocBook documents I produce to PDF or any other format.

Traditionally, DocBook is processed using XSLT [stylesheets](https://github.com/docbook/xslt10-stylesheets)
executed by free edition of [Saxon](http://www.saxonica.com/products/products.xml) into HTML, XSL-FO and EPUB,
and [FOP](https://xmlgraphics.apache.org/fop/) to further process XSL-FO to PDF.

Norman Walsh, inventor od DocBook, seems to think that the future is
[CSS styling to PDF](https://so.nwalsh.com/2019/01/10/printingDocBook), but with no free CSS rendering
engines available, I am going to continue to use FOP for PDF publishing.


## Gradle Plugin ##

Previously I used an excellent [Maven](https://maven.apache.org/) DocBook 
[plugin](https://github.com/mimil/docbkx-tools) that automates a lot of the DocBook workflow and configuration.

Since I started using that plugin, FOP became better (with fonts, among other things), but plugin did not get updated
for later versions of FOP.

Since then I switched to [Gradle](https://gradle.org/); I did not find any suitable Gradle DocBook plugins,
but I did find some Gradle scripts, e.g. [one by Aristedes Maniatis](https://gist.github.com/ari/4156d967d54289f4abf6).
Scripts are not as full-featured as the Maven plugin, do not always work with current versions of FOP and friends, are
harder to configure, lead to code duplication if used in multiple projects and so on, so I decided to write a
[Gradle replacement](https://github.com/dubinsky/podval-docbook-gradle) for the original Maven plugin.
This Gradle plugin mainly follows the ideas from the original Maven plugin, but makes a few original contributions:
- configuration is externalized using XML catalog and XSL files with parameter definitions, making it feasible to
  reproduce the results using tools like Oxygen;
- server-side MathJax is supported for typesetting math in PDF;
- non-MathML math is allowed in DocBook files.   


## DocBook XSLT ##

Original DocBook XSLT stylesheets are in XSLT 1.0; v1.79.2 has been out for a while, but doesn't seem to have
made it into Maven repositories, so I use v1.79.1.
 
Norman Walsh is slowly rewriting the stylesheets in XSLT 2.0
[XSLT 2.0 stylesheets](https://github.com/docbook/xslt20-stylesheets). This rewrite does not support XSL-FO (see above)
or EPUB; the only supported output format is (X)HTML. Some of the parameters that affect the processing are
different from - and not documented as well as - the ones in the XSLT 1.0 stylesheets.

My Gradle plugin supports using the new XSLT 2.0 stylesheets for HTML publishing.  


## Saxon ##

It became harder to find a free version of Saxon :)

On http://saxon.sourceforge.net/ it says:
> even if your stylesheets only require XSLT 1.0, it is probably better to run them under Saxon 9.x

Saxon-HE had support for XSLT 1.0 up to v9.7, then dropped it, then - allegedly - reinstated it
by popular demand in v9.8.0.7. It doesn't seem fixed to me; when I use v9.8.0-10, I see a lot of
```text
XPST0008: Variable... has not been declared (or its declaration is not in scope)
```

When I use v9.7.0-21 I do not, but it still doesn't work; I get (from DocBook XSLT):
```text
Don't know how to chunk with Saxonica
Error at char 16 in xsl:value-of/@select on line 84 column 63 of chunker.xsl:
XTMM9000: Processing terminated by xsl:message at line 84 in chunker.xsl
```

It seems that the extensions that DocBook XSLT stylesheets used to produce multiple output files are
not supported even by Saxon-HE versions that supports XSLT 1.0. I had to downgrade to a version of Saxon
that is an XSLT 1.0 processor with the needed extensions. That seems to be v6.5.5, but it is not available
from Maven Central or JCenter, so I had to go with v6.5.3 (or v6.5.2 if I need to debug Saxon: sources for
v6.5.3 are not available from Maven repositoties).

Saxon 6 returns immutable DOM, so Saxon 9 is used where attributes need to be set on the results of a transform.


## JEuclid FOP Plugin ##

I want to be able to use math in my DocBook documents - and have it rendered in HTML, EPUB and PDF.

Standard modern way of doing that is [MathML](https://www.w3.org/Math/).
Although native MathML support in the web browsers is uneven, actively developed JavaScript
library [MathJax](https://www.mathjax.org/) takes care of all browsers (but probably not
all EPUB readers). It also supports formulas in LaTeX.

FOP does not support MathML natively, but can be configured to use a plug-in that does -
[JEuclid](http://jeuclid.sourceforge.net/jeuclid-fop/) by [Max Berger](https://github.com/maxberger).

JEuclid is no longer maintained (latest version - 3.1.9 - was released on 2010-02-12),
doesn't integrate with current FOP (latest release targets FOP 0.95; current version of FOP is 2.3),
and doesn't work with Java 9 [at all](https://github.com/danfickle/openhtmltopdf/issues/161).

Fortunately, there exists a [fork](https://github.com/rototor/jeuclid) of JEuclid
by [Emmeran Seehuber](https://github.com/rototor) that brings the codebase up-to-date in some
respects: it now builds on JDK 9 and uses more current versions of some dependencies
(for example, Batik). Unfortunately, that fork removed the FOP extension, since it wasn't useful for the project
(details are in the [discussion](https://github.com/danfickle/openhtmltopdf/issues/161) that
led to the fork). Fortunately, using unchanged original Java sources of the JEuclid plugin with the
forked JEuclid works! I guess FOP still supports the original extension mechanism :)

I submitted a [pull request](https://github.com/rototor/jeuclid/pull/5) restoring FOP plugin to the fork's author;
it is included in the 3.1.14 release.

I wrote to the Oxygen team suggesting to update JEuclid they included with Oxygen to a version that actually works with
current FOP; they replied:
> Thank you for recommending this project. We will take a look at it and 
> see if it is possible to use it.

Although FOP JEuclid plugin code works as is, I made one change: I use explicit programmatic
configuration instead of leaving classpath provider-configuration crumbs for the the ServiceLoader
(see JEuclidFopFactoryConfigurator class). The reason I did this (besides my general dislike
for magic) is: I need to be able to disable JEuclid based on the configuration of my plugin in
the Gradle build file, which isn't possible if it self-enables just by being on the classpath. 

Here are the names and contents of the provider-configuration files that JEuclid FOP plugin
had under `META-INF/services` (for informational purposes):

| Name                                                        | Contents                                                     |
|-------------------------------------------------------------|--------------------------------------------------------------|
| org.papache.fop.fo.ElementMapping                           | net.sourceforge.jeuclid.fop.JEuclidElementMapping            |
| org.apache.fop.render.XMLHandler                            | net.sourceforge.jeuclid.fop.JEuclidXMLHandler                |
| org.apache.xmlgraphics.image.loader.spi.ImageConverter      | net.sourceforge.jeuclid.xmlgraphics.ImageConverterMathML2G2D |
| org.apache.xmlgraphics.image.loader.spi.ImageLoaderFactory  | net.sourceforge.jeuclid.xmlgraphics.ImageLoaderFactoryMathML |
| org.apache.xmlgraphics.image.loader.spi.ImagePreloader      | net.sourceforge.jeuclid.xmlgraphics.PreloaderMathML          |

I'd probably clean this up upstream a bit more - add documentation about configuring FopFactory to use the plugin,
remove warning suppression that I added in my pull request, add Implementation-Version to the manifest and so on,
but since it turned out to be possible to use MathJax for PDF typesetting - I'll let it be :) 

## MathJax FOP plugin ##

MathJax is actively developed and does a great job typesetting math in the browser, but to use it with FOP I need to run
it outside of the browser. Fortunately, MathJax team releases MathJax for Node.js:
[mathjax-node](https://docs.mathjax.org/en/latest/advanced/mathjax-node.html).

To use it, my Gradle plugin has to support Node and `npm`. There is a Gradle Node
[plugin](https://github.com/srs/gradle-node-plugin) by srs, but I did not use it directly because its tasks are not
reusable unless the plugin is applied to the project, and I do not want to apply Node plugin to every project that uses
DocBook. Also, I want to be able to run npm from within my code without creating tasks.
I rewrote parts of that plugin needed for my scenario; result is under 200 lines.

Since MathJax supports - in addition to MathML - also LaTeX (and AsciiMath), it is tempting to provide support for
non-MathML math in DocBook, preferably without the need to frame every piece of math in boilerplate
`informalequation`/`inlineequation` tags. I am aware of one previous attempts to handle
[in-line math in DocBook](http://ricardo.ecn.wfu.edu/~cottrell/dbtexmath/about.pdf), but chose a different approach:
non-MathML math is wrapped into appropriate DocBook `equation` element with nested MathML `math` element that has 
proprietary attribute indicating the flavour of math, and my MathJax FOP plugin typesets the math accordingly. My plugin
also uses an XML filter to recognize math embedded in the DocBook document by its delimiters and wrap it as described
above (for PDF processing only).

Some digging was required to ensure that SVG that MathJax produces is sized correctly for FOP/Batik to insert it
into the resulting PDF document. Reading of the code that creates SVG and sets its
[sizes](https://github.com/mathjax/MathJax/blob/master/unpacked/jax/output/SVG/jax.js)
made clear that:
- viewBox sizes are in milli-ems, and thus can be converted to millipoints by scaling by the fontSize
  (MathJax internally assumes em to be 10 points);
- viewbox minY is negative SVG height, and viewBox height is SVG height + SVG depth,
  so depth (descent) can be calculated as viewbox height + viewbox minY;
- vertical-align (in exs) in the style attribute is not depth, so I don't need to use it;
- viewport sizes (in exs) are calculated from viewbox sizes, so I don't need to use them;
- MathJax assumes ex height of 430.554 milli-ems (WTF?!), while Batik assumes ex height of 500 milli-ems,
  so before handing the SVG image to Batik, I need to convert viewport sizes to units that are interpreted
  the same way by MathJax and Batik: points.


## Java-to-Node.js Bindings ##

There is a Java binding for V8 and Node.js: [J2V8](https://github.com/eclipsesource/J2V8), but it:
- did not ship for anything other than Android since 2017, and even that 
  [without Node.js support](https://github.com/eclipsesource/J2V8/issues/441);
- uses old version of Node.js (7.4.0; current is 12.0.0);
- between v4.6.0 and v4.8.0, there is no version that available for all platforms with which `mathjax-node` actually
works:
```
mathjax-node/lib/main.js:163: SyntaxError:
Block-scoped declarations (let, const, function, class) not yet supported outside strict mode
for (let key in paths) {
```        
As a result, I have to use v4.8.0, which probably works only on Linux (and even then, not on Ubuntu).

Also, because JavaScript is single-threaded, and I am too lazy to spend time coding the "worker" approach from the
J2V8 documentation for limited gain, I ended up instantiating new V8 runtime for every typesetting call. Even then,
it isn't entirely stable: some times, JVM crashes; some times, stopping Gradle daemon helps etc.  

That is why DocBook plugin tries to use J2V8 only if it is specifically configured, and even then if it fails to load it
falls back to calling Node in an external process for each typesetting.   


## Weird Stuff ##

Some peculiarities in XML processing were encountered:
- if an attribute is in the element's default namespace (e.g., `display` attribute of the MathML `math` element),
it has to be looked up with an empty namespace URI or it wouldn't be found;
- if an attribute is in the element's default namespace,
it has to be added with an empty namespace URI, or a new namespace with the same URI will be declared
(causing `SAXParseException`s down the line); 
- `XMLObj.setAttributes()` sets namespace on an attribute only if it already saw the declaration of that namespace,
so attributes had to be pre-processed for the namespaces to work correctly;
- there seems to be some confusion with the `rewriteURI` form: Catalog DTD requires 'uriIdStartString' attribute
(and that is what IntelliJ wants), but XMLResolver looks for the 'uriStartString' attribute (and this seems to work
in Oxygen).


If SVG element has attribute `xmlns="http://www.w3.org/2000/svg"`, but not `version="1.1"`, I get: 
```
java.lang.ClassCastException: org.apache.batik.dom.GenericElement cannot be cast to org.apache.batik.anim.dom.SVGOMElement
at org.apache.fop.fo.extensions.svg.SVGElement.getDimension(SVGElement.java:134)
```

Turns out that `ImageImplRegistry` instance used by `FopFactory` is global (`defaultInstance`), so pre-loaders etc.
registered on one FopFactory interfere with another. Since `ImageManager` is not settable on the `FopFactory` or
`FopFactoryConfig`, and `ImageImplRegistry` is not settable on the `ImageManager`, I had to write and use
`FopFactoryConfigProxy` to counteract the effect of this invisible global. 


## Future ##

Extending DocBook math support to the non-MathML math using an XML filter made me think that it may be possible to
recover some of the benefits of using MarkDown (simpler markup, like tutorial generators and such) via preprocessing
MarkDown markup in the input file using MarkDown parser like
[`CommonMark` parser](https://github.com/commonmark/commonmark-spec). This is non-trivial, for instance, because
MarkDown can have multiple interlinked files, but warrants further thought.  