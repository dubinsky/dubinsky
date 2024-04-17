  * Input formats that matter:
    * [[DocBook]]
    * [[TEI]]
    * [[Markdown]]
    * MathML
  * Output formats that matter:
    * HTML/CSS
    * EPUB
    * PDF
  * [[Markdown]] use flexmark to handle Markdown embedded in [[DocBook]] and [[TEI]]
  * MathML should be handled by MathJax, not JEuclid
    * I did FOP plugin
    * do openhtmltopdf plugin?
    * package plugins so that they do not need Gradle scaffolding and can be run on the server
  * PDF
    * the only free FO-based engine: FOP
    * Walsh says CSS engines are the future (link)
    * free engines:
      * [Headless Chrome](https://developers.google.com/web/updates/2017/04/headless-chrome)
        * [paperplane blog-post](https://www.paperplane.app/blog/modern-html-to-pdf-conversion-2019/)
        * [Chrome DevTools Protocol](https://chromedevtools.github.io/devtools-protocol/)
        * [puppeteer](https://github.com/puppeteer/puppeteer)
          * [MathJax with Puppeteer](https://github.com/mathjax/MathJax-demos-node/tree/master/puppeteer)
          * [Google Cloud Functions](https://cloud.google.com/functions)
        * [playwright](https://playwright.dev/)
        * [CSS Paged Media Module Level 3](https://drafts.csswg.org/css-page-3/)
          * [page-break](https://css-tricks.com/almanac/properties/p/page-break/)
          * [paged.js](https://www.pagedjs.org/)
      * [openhtmltopdf](https://github.com/danfickle/openhtmltopdf)
        * is this better than headless Chrome in any way?
      * [pandoc](https://pandoc.org/releases.html)
    * If I go with HTML+CSS -> PDF, I do not need XSLT at all.
      *  I do not need to transform "387 DocBook elements" (link to Walsh); I only need to deal with a handful (just as I did for TEI).
      * I do need to chunk for the HTML output (Walsh link), but it is probably easier in a normal programming language like Scala.
      * I do need to break PDF into pages (with alternating margins etc.), add header/footer etc. CSS Paged Meadia should be enough?
      * I do need to make index, table of content and possibly cross-references, for which I need to know page numbers. How?
  * **NO XSLT!**
    * Inspired by CETEIcean (convert non-trivially only a few select elements) and Walsh (HTML+CSS -> PDF), I come to conclusion that XSLT is not needed to produce HTML/EPUB nor PDF output. 
    * While XML (TEI, DocBook) is absolutely the right format for semantic mark-up of texts (JSON is crap at this, and we gain nothing by using HTML5 directly), I 
do not see any advantages in using XSLT over a normal programming language:
      * efficiency is not that important - and is probably better with Scala anyway;
      * portability is not important for me, but the smaller and more understandable the code is, the easier it is to port it;
      * flexibility is probably better when using normal programming language compared to XSLT parameters/customizations.
    * Although JavaScript is a given as the front-end language of the web, the need to *write* in it is not - witness GWT, Links,  TypeScript, Scala.js. There are advantages in using the same programming language throughout, regardless of the "polyglot" mantras of the modern software development. And although attempts to hide complexity in other areas were misguided (CORBA, ActiveX, Java Remote), here, I think, minimizing the number of languages one has to use is a pure win.
    * This probably also applies to XQuery, which fascinated me a while back: bringing computation closer to the data only works when one uses XML database (who heard of them in the cloud?), and language-neutral access can be handled by a clear API.