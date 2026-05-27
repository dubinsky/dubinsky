In the spring of 2026, my blog-plus-notes publishing setup using [[Jekyll]] broke down sufficiently to 
motivate me to write my own static site generator: [[Site Publisher]].

At exactly the same time I became aware of the [ZIO Blocks](https://github.com/zio/zio-blocks) project. ZIO Blocks tries to minimize dependencies of each "block", so it supplies its own parsers and renderers for various formats instead of using the industry-standard ones. It also provides format-specific models (Abstract Syntactic Tree), and a mechanism to encode and decode Scala classes to and from those ASTs.

Although wary of the non-standard parsers, I decided to use ZIO Blocks, since:
- I need ASTs for various formats anyways (e.g., XML)
- I could probably use the Scala-to-AST codecs for my benefit
- it has ZIO in its name ;)

This is my journey with ZIO Blocks.

## JSON
JSON is probably the best-supported format in ZIO Blocks - and the one that I need the least: the only JSON files I need to work with are [[Obsidian]] configuration files, which I read to determine the location of the Obsidian Daily Notes if the site I am generating happens to also be an Obsidian vault.

Specifically, I read one field `folder` from one file `.obsidian/daily-notes.json`. I do not use ZIO Schema codecs, I just parse the file into JSON AST and retrieve the field I need.

ZIO Blocks JSON support that I actually use works flawlessly.

## Yaml
- pester ZIO Blocks people to fix the hard-coded kebab-case bug (https://github.com/dubinsky/site-publisher/issues/6)
- report to ZIO Blocks people enumeration handling bug (https://github.com/dubinsky/site-publisher/issues/7)
- my stashed extra keys will not survive round trip once FrontMatter becomes a case class and `.copy()` is used!
- besides, I stash keys other than the ones in the schema of the `Config`
class, but the codec changes the names: currently - because of the kebab bug, but even once it is fixed, a name mapper could be in effect...

## Markdown

Pipeline:

- Markdown text
- Markdown AST
- HTML text
- HTML AST

I need HTML AST for post-processing like calculating page structure, resolving links to sections and blocks, transclusion, chunking, adding TOC,
and prefer to use it for all post-processing
like HTML layout for the browser and PDF generation.

Converting ZIO Blocks Markdown AST to XML AST proved inconvenient
(e.g., for reasons unknown, there are two flavours of each `Inline` subclass); it also turned out to be impossible, since
Markdown allows embedding HTML,
and since opening and closing tags of the same HTML element
may end up in different Markdown AST nodes,
I can not go directly from Markdown AST to HTML AST -
I have to go through HTML text.

Parsing Markdown with ZIO Blocks parser and writing it to XML text
turned out to be inconvenient: ZIO Blocks Markdown writer
produces unclosed tags like `br` and `hr`,
which are not valid XML and thus can not be parsed into XML AST.
I had to write my own writer for ZIO Blocks Markdown AST.

Converting Markdown text to XML AST using ZIO Blocks Markdown parser
still turned out impossible, since it parses nested lists incorrectly - see https://github.com/zio/zio-blocks/issues/1377.

FlexMark WikiLink extension is useless to me:
- it sets the content of the rendered HTML 'a' element when there is no | in the link;
- it prefixes the href with the dashes corresponding in number to the spaces after the |...

Unlike ZIO Blocks Markdown HTML renderer,
which I had to fork to ensure that resulting HTML is valid XML
(no unclosed tags like 'br', 'hr', 'input' and 'img'; attributes have values etc.),
FlexMark renders valid XML out of the box.
I may need to add some extensions to handle GitHub task lists and such...

I had to switch to the industry-standard FlexMark parser.

It can be configured to
pair some - but not all - of the tags,
so direct Markdown AST to HTML AST is not possible with FlexMark either.

Since I insist on the end result being HTML AST,
invalid XML (e.g., unclosed 'br' elements)
embedded in the Markdown text is not supported.

I process wiki links and Kramdown TOC marker in post-processing.

On May 12, 2026 I found out that ZIO Blocks XML parser
`zio.blocks.schema.xml.XmlReader`
is *useless*: it chokes on entities other than `amp`, `lt`, `gt`, `quot` and `apos`! Fucking amateur hour! I have to switch to something standard and convert the result to ZIO Blocks XML AST; SAX or StAX?

## XML

As a part of the OpenTorah project, I developed a custom
XML pretty-printer for the Scala XML AST;
I need to write one for the ZIO Blocks XML AST,
since the XML writer provided by ZIO Blocks is broken:
when used with "pretty", it

- inserts spaces between book titles and quotes around them;
- destroys the formatting of the code blocks;

when used with "default", in addition to producing
non-human-readable text, it:

- does not insert spaces between text and elements;
- mangles the formatting of the code blocks if there are spaces at the beginning of the lines after the first one.

With my own XML writer, I can also ensure that the empty elements
that cause problems with the browsers when they are self-closed
(script, data, span) are not -
without inserting comments into them, which is ugly.

- I think I should not have to cling quotes and friends to the sibling elements unless either FlexMark writer or XML parser introduce spurious whitespace; if it is the XML parser, and if it really shouldn't, integrate with the industry-standard parser...

See that the following renders as is:


```scala
val escaped = userInput.replace("<", "&lt;").replace(">", "&gt;")
```

On May 12, 2026, while attempting to add FlexMark extensions "Footnotes" and "GFM Task List", I discovered that ZIO Blocks XML
parser chokes on HTML entities like  `&nbsp;`, which the Task List extension
outputs, and alone `&#8617;` ("right arrow curling left emoji")
which the Footnote extension does.
There is no way to turn off entity parsing in the ZIO Blocks XML parser.

I wrote an adaptor that uses StAX XML parse built into the JDK
and produces ZIO Block XML AST. It takes less than a 100 lines
of code.
TODO share with the ZIO Blocks people.

StAX XML parser, just like the ZIO Blocks XML parser,
does not recognize HTML entities - but it can be configured
to not expand the entities, and instead deliver them via StAX:

```scala
val factory: XMLInputFactory = XMLInputFactory.newInstance
factory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false)
```

... except the entities get delivered through a dedicated event,
`EntityReference`, not as `Characters`...

With the real XML parser my fenced code blocks get mangled;
setting another property took care of that:


```scala
factory.setProperty(XMLInputFactory.IS_COALESCING, true)
```

... but counter-acted the one that parses the entities!

So, I need to coalesce the characters myself,
and incorporate entities into the result!


Using the real XML parser, I discovered that the custom `404.html` file that I used on my personal website for the last two
years, and which I found when originally setting up Jekyll,
was not valid XML/HTML:
it has two root elements (`<style>` and `<div>`)!
It is possible that Jekyll's parser does the right thing,
but ZIO Blocks XML parser parses the first root element and silently
ignores the rest, which is _definitely_ not the right thing.

## HTML

- mention ZIO Blocks HTML optional attributes in its documentation.
- if I need prefixed names like `tei:p`, ask ZIO Blocks people to allow them in HTML
