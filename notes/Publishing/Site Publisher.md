[Old blog](https://leonid-dubinsky.blogspot.com)

See [[Jekyll]], where the sordid saga started.

- ask Obsidian developers to make file moves visible to Git if the vault is a Git repository


```mermaid
flowchart TB

A[Hard] -->|Text| B(Round)
B --> C{Decision}
C -->|One| D[Result 1]
C -->|Two| E[Result 2]
```

## Jekyll finally pissed me off

When I listed TODO link  unresistable to any programmer urge to
write certain kinds of systems, I neglected to mention
a static site generator.

I've been using Jekyll since I migrated my blog from Blogger TODO when;
I then moved my notes from Roam into Obsidian
TODO when link and merged them into the static site with my blog;
that required customizing my Jekyll setup
TODO move Jekyll customization notes into the Jekyll page and link it here.

In March 2026, my site stopped publishing;
error messages were meaningless, and it took a lot of time
to figure out what is the problem by deleting files until it went away.
It turned out that Jekyll plugin I used for handling wiki links TODO link
breaks when an `index` page contains a wiki link (e.g., a [[TODO]]).
Previous communications with the author of the plugin made it clear
that he considers Jekyll to be a legacy system
(but it remained unclear what is the suggested replacement).

At this point, I looked at a few of the modern static site generators
like 11ty TODO link - and decided to write my own ;)

## New Possibilities

It occurred to me that if I write my own site generator,
I can make it Obsidian-aware:

- it can read Obsidian configuration
- it can publish daily notes as dated blog posts

It also occurred to me that I can make alter-rebbe.org TODO link,
a site where I publish some archive documents in the TEI format
static (again) -
if I add support for TEI to my generator:

- link resolution
- facsimiles
- multi-lingual names

It also occurred to me that I can add publishing to X Articles -
if X ever makes an API for that. This would allow me
to own my blog - and at the same time use X ;)

## Farewell, Jekyll

Events of 2026:

- Mar 17: My Jekyll setup (wiki-refs plugin) broke down
- Mar 20: I created my site publisher repository
- I became aware of ZIO Blocks Yaml released on Mar 11 and ZIO Blocks Markdown released on Jan 30
- Mar 22: I filed ZIO Blocks pull request
- Apr 30: I switched my personal website to use my site publisher vis GitHub Actions Workflow
- May 04: I removed all traces of Jekyll from my personal website
- May 06: I became aware of the ZIO Blocks HTML that was added on Apr 25-May 01, and started switching to it from my home-grown HTML DSL
- May 07: switched
- June/July: working on TEI support
- July/August: Asciidoc support and chunking
- Aug 7: PDF generation (with Grok's help)
- Aug 9: switching OpenTorah website from Asciidoctor and Jekyll to site generator

The first milestone for this project is to replace Jekyll on my personal website.
In under two month and a little less than 2000 lines of Scala code,
I have:

- no support for paginating the post list, which I did not use with Jekyll anyway;
- instead of the Ruby code highlighter Rouge, my publisher uses Highlights.js; it loads only the language modules used in the page; TODO unsupported language mapping...

- while I did manage to list children pages using Jekyll,
I never figured out how to list subdirectories;
with my own publisher this is not a problem ;)

- Jekyll ignores Markdown files without front matter; I do not;
- wiki links
- link resolution
- front-matter defaults auto-added pages;
- header pages are configured in their frontmatter, with FA icons (and defaults) ;)
- transclusion
- directory navigation: up, prev, next;
- path navigation;
- insertion of missing index pages;

Write about all the plugins I used:

- jekyll-optional-front-matter
- jekyll-sitemap
- jekyll-feed
- jekyll-mentions
- jekyll-avatar
- jekyll-wikirefs

## Design

Opinionated, because I am writing this for myself:

- everything is written in Scala
- no plugins
- no SCSS
- no template languages
- no layouts

The layout is Minima-inspired CSS and HTML, not a Jekyll theme engine.

Code lives in `site-publisher`. Dialect conversion sits next to `XxxMarkup`; shared IR and resolution sit in `markup/` (`Citation`, `Bibliography`, `Footnote`, `Glossary`, `Section`, …). There is no `feature/` package.

### Pipeline

`Site` coordinates. `Pages` scans the source tree and builds the page graph, including synthetic pages (`/posts`, `/tags`, `/errors`, `sitemap.xml`, …).

Per document:

1. Dialect `Markup` (`md` / `adoc` / `html` / `tei` / `docbook`) plus `FrontMatter` → `Xml.Element`
2. Dialect converters emit shared IR (leftover soup on `XxxMarkup`: `quoteblock`, `[!tip]`, TEI `cit`, DocBook `sidebar`, …)
3. HTML-shaped leftovers → IR in `HtmlIr.normalize` (`Aside`, `Quote`, `Strike`, `Figure`, `PdfEmbed`, `Video`). `HtmlMarkup.process` is title + nest sections + that pass; Markdown and AsciiDoc finish there. TEI and DocBook do not: leftovers are still native names until their converters run.
4. `PageContent.apply` prepares once: sections/ids, internal-link marks, wiki embed (images, audio, video, PDF), footnote harvest
5. `PageContent.markupContent` resolves per chunk: select XML, append referenced footnotes, resolve citations, resolve links and tooltips, inject TOC
6. Minima-inspired HTML → write (`textContent` or copy assets)

`.xml` files are disambiguated by root element (`TEI`, DocBook `article` / `book` / …).

### SEO

`Seo.head` is the `{% seo %}` stand-in: derived `<head>` tags from site config and front matter, no extra keys. Document title is `Page | Site title` (home, or when they match, is just the site title). `og:title` / `twitter:title` stay the page title. Description and author fall back to the site. Canonical and `og:url` are `site.url` + path. `og:type` is `article` when the page has a date, else `website`. JSON-LD `@type` is `WebSite` (home), `BlogPosting` (dated/post), or `WebPage`; the article `itemtype` matches. Generator is this publisher (`https://github.com/dubinsky/site-publisher`), including Atom `<generator>`. No images, Facebook, or webmaster proofs.

Page `description` is also the feed `<summary>` and the `/posts` list teaser (`p.post-excerpt`) when set. Not an auto-excerpt of the body; site `description` is not a fallback there.

### Markup

Supported: [[Markdown]], [[AsciiDoc]], HTML, [[TEI]], [[DocBook]].

Cross-markup transclusion means stylesheets (and MathJax etc.) are included even when a page’s source dialect would not need them — unless we later compute the set of markups actually used.

### Front matter

Internal (in the markup file) or external (same name, `.yaml` / `.yml`). Both present is an error.

A file is markup if its extension is associated with a dialect (`.md`, `.adoc`, …) or it is `.xml` whose root element is associated with a dialect. TODO: front-matter boolean `asset` to declare that a file is *not* markup.

[[Jekyll]] ignored Markdown without front matter; this publisher does not.

### Page names and wiki links

[[Jekyll]] used front-matter titles and ignored file names; I needed an Obsidian plugin to copy names into titles. This publisher uses both file name and front-matter title for link resolution, so the title property is only needed when it differs from the file name.

Obsidian uses file names and ignores the front-matter title; a plugin would be needed for the reverse.

Wiki links (`[[…]]`), internal link resolution, backlinks, aliases (TODO). Open questions about Obsidian wiki links: case sensitivity, file name vs title vs document title, ambiguous names, line wrapping, agglutination.

### Directories and posts

Directory pages, navigation (up/prev/next), path navigation, missing index pages, header pages with FA icons from front matter.

Posts: `_posts/`, `_drafts/`, Obsidian daily-notes folder (from `.obsidian`). Auto-post vs permalink. Filename convention `YYYY-MM-DD-title`. `_posts` is emptied out of the directory listing (`Posts.isDirectoryEmptiedOut`).

### Chunking

I used `asciidoctor-multipage` [extension](https://github.com/owenh000/asciidoctor-multipage) to split AsciiDoc into per-section HTML. It has long-known [issues](https://github.com/owenh000/asciidoctor-multipage/issues/46), (footnotes) and only works for AsciiDoc.

The publisher chunks any supported markup. The term is `chunking` (DocBook XSLT), not `multi-page`.

### Paging

Same page-graph idea as chunking (extra pages, `path.add`, header prev/next), but only the synthetic `/posts` listing is paged, and the cut is **list items**, not sections. Site config `paginate-posts: N` in `_site_config.yml` (omitted or &lt; 1 is off). No front-matter `paginate`, no authored-list paging, no Liquid `paginator`. Extra batch pages are registered after the tree scan so `Posts.posts` is complete. `Posts.batchContent` slices `ul.post-list`; page 1 stays `/posts.html`; further batches are `/posts/2.html`, …. A `nav.pagination` sits under the list. `rel=prev/next` is the pager sequence.

Not Jekyll’s `/page/:num/` index layout — that would move `/posts.html`.

### Sections and TOC

Canonical IR: nested `div.section` with a heading (HTML `hN` nested after convert; TEI already nested, heading is `tei-head`; DocBook `section` / `sectN` / nested `chapter` become `div`, heading is `db-title`). Permalinks and missing ids are added on that IR (`Section.normalize`). `xml:id` is copied to `id`. TOC walks through non-section wrappers; a heading need not be the first child (`pb`/`fw` before `head`).

Document title (`process` second value, same as HTML `h1` / DocBook `db-title`): TEI `titleStmt/title` (`tei-title` after `Xml2Html`; `@type="main"` if several); `store` / `collection` child `title`. Not body `head`, not `bibl`/`cit` titles, not entity names. Stripped from the tree. Empty `titleStmt` (common in the archive) leaves `Page.title` to front matter then the file name.

Kramdown `{:toc}` is a TOC placeholder in Markdown.

### DocBook

Same shape as TEI: `.xml` files, root-element disambiguation, `Xml2Html("db")` then dialect converters, no `HtmlIr.normalize`. Prefix `db` (`title` → `db-title`). Claimed roots: `article`, `book`, `chapter`, `appendix`, `part`, `set`, `preface`, `refentry`, `topic` — not `section`. Nested `section` / `sect1`–`sect5` / `simplesect` / `chapter` / `appendix` / `preface` rename to `div` then `Section.mark`; a claimed root is not renamed (so a `chapter` file stays `<chapter>`). Document title is the root or `info`/`articleinfo`/… `title`, stripped from the body (HTML `h1` analog). CALS `tgroup` is unwrapped; `row`/`entry` become `tr`/`td` (class `entry` kept). No DocBook XSLT and no `org.podval.docbook` package.

IR converters run in a second pass so IR `class` is not prefixed to `db-class`: `footnote` / `footnoteref`, `glosslist` / `glossary`, `variablelist` (plain `dl`), `bibliography` / `citation` / `biblioref`, `programlisting` / `code` / `literal`, `co` / `calloutlist`, `note`/`tip`/`warning`/`caution`/`important`, `sidebar`, `blockquote`/`epigraph`, `emphasis` roles (`bold`, `strikethrough`), `figure` / `imagedata`, `videodata`. No task lists, wiki links, or PDF embeds. DocBook 4 (no namespace) and 5 (default `xmlns="http://docbook.org/ns/docbook"`) both match on local names. `link` is renamed to `a` without class `link` (that class is the section permalink).

### Footnotes

IR: stub `span.footnote-link` with `footnote-correlation-id`; body `span.footnote` with the same id. Harvest numbers in document-link order, strip bodies, append referenced bodies after chunk select, turn stubs into numbered `<a>`s.

Tooltips: `Tip.attachTip` returns `span.footnote-ref` containing the `<a>` and `span.footnote-tip` as siblings. Recurse only into the tip (`attachTips = false`) so links in the note resolve without nested tips.

AsciiDoc leftover `div#footnotes` and Markdown `div.footnotes` are stripped as spurious containers.

### Task lists

IR is GFM-shaped, not FlexMark or Asciidoctor soup:

```html
<ul class="task-list">
  <li class="task-list-item">
    <input type="checkbox" class="task-list-item-checkbox" disabled checked>
    item text
  </li>
</ul>
```

`TaskList` is IR only (classes, `checkbox` / `asItem` / `asList`). Markdown leftovers (FlexMark `li.task-list-item`, checkbox, `&nbsp;`) convert in `MarkdownMarkup`. AsciiDoc leftovers (`ul.checklist`; default html5 `✓`/`❏`, `%interactive` `<input>`, `icons=font` Font Awesome) convert in `AsciiDocMarkup.cleanup`. Mixed lists: only task items get `task-list-item`; the parent gets `task-list` if it has any. CSS in `layout.css` styles only these classes. HTML that is already IR is left alone.

### Callouts

IR: `span.callout` with `data-value` and the number as text, in the verbatim block; `ol.callout-list` of `li` annotations. Not harvested like footnotes (the list is already next to the listing). CSS styles only these classes; markers are `user-select: none` so copy-paste from a listing omits them.

AsciiDoc leftovers (`b.conum`, `i.conum` + guard `<b>(1)</b>`, `div.colist` wrapping `ol` or a table when `icons` is set) convert in `AsciiDocMarkup.cleanup`. HTML that is already IR is left alone.

Not feasible as Markdown (or TEI) syntax. A fenced block is opaque — CommonMark does not parse inlines inside it — so `<1>` or `<.>` is just source text: it stays in copy-paste, fights the highlighter, and is not bound to a following list. An ordered list after the fence is just a list. What Markdown and Obsidian call a “callout” is an admonition (`> [!NOTE]`), a different feature. Quarto’s code annotations (`# <1>` in a fence plus an ordered list) copy AsciiDoc but are Quarto-only; FlexMark and Obsidian do not understand them. Line numbers (Pandoc `.numberLines`) and line highlighting (Docusaurus / VitePress `{1,3-5}`) number or highlight lines; they do not attach explanations. TEI has no listing-callout convention either.

### Admonitions

IR: `div.admonition` with `data-type` (lowercase) and `div.admonition-title`; Obsidian `+`/`-` fold is `details`/`summary` (`open` when `+`). CSS styles only these classes (accent from `data-type`). Not harvested. HTML that is already IR is left alone.

AsciiDoc leftovers (`div.admonitionblock` table, `td.icon` / `td.content`, optional content `div.title`) convert in `AsciiDocMarkup.cleanup`. Markdown: FlexMark blockquote whose first `<p>` starts with `[!type]` (Obsidian core callouts; no plugin) converts in `MarkdownMarkup.convert`. Type is kept as written (Obsidian `important` is not collapsed to `tip`).

### Asides

IR: `<aside class="aside">`, optional `div.aside-title`. No `data-type`. CSS styles only these classes. HTML that is already IR is left alone.

AsciiDoc leftovers (`div.sidebarblock`; `div.content` already unwrapped as spurious; optional `div.title`) convert in `AsciiDocMarkup.cleanup`. Bare HTML `<aside>` (Markdown HTML blocks and `.html` pages) gets `class="aside"` in `HtmlIr.normalize`. No Markdown/Obsidian/TEI source syntax.

### Quotes

IR: `<blockquote class="quote">`, optional `div.quote-title` and `footer.quote-attribution` (`cite` kept as-is). CSS styles only these classes (the decorative opening mark stays on `blockquote`). Not harvested. HTML that is already IR is left alone.

AsciiDoc leftovers (`div.quoteblock`; optional `div.title`; inner `blockquote`; optional `div.attribution`) convert in `AsciiDocMarkup.cleanup` — `quoteblock` is not unwrapped as a spurious wrapper, or title and attribution would become siblings of the quote. Markdown CommonMark `>` and bare HTML `<blockquote>` get `class="quote"` in `HtmlIr.normalize` (after Obsidian `[!type]` has already become an admonition). A Markdown em-dash line is not treated as attribution.

TEI leftovers (`quote`; `cit` grouping `quote`/`q` with `bibl`/`biblStruct`/`ref`) convert in `TeiMarkup.process` second pass so IR `class` is not prefixed. Inner `bibl` on a `quote` becomes attribution. Bare `q` stays HTML `q`. `@who`/`@source` are not turned into attribution text. Standalone `bibl` is not a quote.

### Strikethrough

IR is HTML `<del>` (user-agent line-through; no extra class). FlexMark `~~` emits it once `StrikethroughExtension` is on. AsciiDoc leftover `span`/`mark.line-through` and HTML `<s>` become `<del>` in `HtmlIr.normalize`. TEI `<del>` is already the element. Not harvested.

### Figures

IR: `<figure class="figure">`, optional `figcaption.figure-caption`. Not harvested. HTML that is already IR is left alone. Inline images stay `<img>`.

AsciiDoc leftovers (`div.imageblock`; `div.content` already unwrapped; optional `div.title` after the image) convert in `AsciiDocMarkup.cleanup`. Markdown/HTML: a `<p>` whose only child is `<img>` (or a lone linked `<img>`) becomes a figure in `HtmlIr.normalize`; `title` on the image is the caption and is removed from the `<img>`. TEI: `<graphic url>` becomes `<img src>` in the first pass; `<figure>` with `<head>`/`<figDesc>` converts in the second pass so IR `class` is not prefixed. Wiki `![[image]]` embeds stay `<img>` (resolved after convert).

### PDF embeds

IR: `div.pdf-embed` wrapping `<object type="application/pdf" data="…">` (inner fallback `<a>`) and a sibling `p.pdf-embed-link`. Not a figure. Not harvested. Print CSS hides the `object` so Chromium `pdf: true` does not snapshot the inner viewer; the sibling link remains.

Bare HTML `<object type="application/pdf">` is wrapped in `HtmlIr.normalize`. Wiki `![[file.pdf]]` becomes this IR in `WikiLink.embed` (after convert; fragment `page=` / `height=`). `#page=N` is put on `data` and `href`. Height is `--pdf-embed-height` on the wrapper (digits → `px`). No PDF.js. No `<iframe>` / `<embed>`.

### Video

IR: local file is `<video class="video" controls>` with an inner fallback `<a>`; YouTube/Vimeo is `<iframe class="video-embed">` (`src` kept). Not harvested. Print CSS hides both.

AsciiDoc leftovers (`div.videoblock`; optional `div.title`; inner `video` or player `iframe`) convert in `AsciiDocMarkup.cleanup`. A title becomes Figure IR around the player. Bare HTML `<video>` gets `class` / `controls` / fallback link in `HtmlIr.normalize`; a YouTube/Vimeo iframe gets `class="video-embed"` (other iframes are left alone). Wiki `![[file.mp4]]` (`webm` / `ogv` / `m4v`) becomes the local IR in `WikiLink.embed`. No JS player.

### Glossary

Harvested from markup-neutral HTML: `class="glossary"` on the list, `class="glossary-item"` with an `id` on each term. Links to those ids get definition tooltips (`span.glossary-ref` / `span.glossary-tip`), same `Tip` as footnotes. Print and `html.glossary-expand` inline the definition in parentheses.

Term id is the `id` on `<dt>` if present, otherwise the term text with spaces turned into hyphens (`Alter Rebbe` → `Alter-Rebbe`).

### Bibliography

Two kinds, usable together on one page. Ids do not collide: citeproc entries are `#bibl-{key}`; native entries keep the authored id.

**External.** Dialect syntax → `Citation` IR (`span.citation` / `span.citation-item` with `data-key`, optional `data-locator`, `data-mode`; empty `div.bibliography` placeholder). Then a **per-document** BibTeX file plus citeproc-java (CSL). No site-level bibliography file or style; both `bibliography` and `csl` are required on the document’s front matter. Locale is page `lang`, else site `lang`, else `en-US`. `.bib` files are ignored at scan (`*.bib` in `Ignore.internal`) so they are not published; citeproc still reads them from the source tree. Un-ignore in `_site_ignore` to copy one to the site. Cited-only list: `Bibliography.resolve` fills an empty placeholder, or appends if there is none; it does not replace a native list. Unknown keys → `span.unresolved-citation` and a page error (not reported on chunks). Resolved in-text cites become `a.citation` linking to `#bibl-{key}` on the matching `csl-entry` (first key if several).

AsciiDoc: Java extensions (`cite`, `citenp`, `bibliography::[]`); no Ruby gem. The inline-macro regexp allows an empty target so `cite:[key]` matches; positional attributes are all joined (not just `1`).

Markdown: Pandoc citation syntax scanned after FlexMark; no extra extension.

TEI: `ref`/`ptr` `@cRef` (optional `@n` locator) → the same `Citation` stubs. Empty `div type="bibliography"` is the placeholder. A bare `@target` that is a bib key and is *not* a native `listBibl` id is also a stub.

**Internal.** Authored list harvested like glossary (`BibliographyItem`, `class="bibliography-item"` with `id`). Links to those ids get `a.citation` and a `citation-tip` of the entry. Glossary tip wins if the same id is also a glossary term.

AsciiDoc: `[bibliography]` (`div.ulist.bibliography`) converts before the `ulist` wrapper is unwrapped, so the class is not lost. `[[[id]]]` empty anchors are hoisted onto `li.bibliography-item`; `<<id>>` is an ordinary xref.

TEI: in-document `listBibl` / `bibl` / `biblStruct` (not in `teiHeader`) become `class="bibliography"` / `bibliography-item` with `xml:id` copied to `id`. `ref`/`ptr` `@target="#id"` stay internal links. Empty pointers get `@n` or the id as text. A bare `@target` that matches a `listBibl` id is rewritten to `#id`. `cit` with a `quote` stays Quote IR; `cit` that is only a pointer is not a blockquote. Authored `listBibl` is kept (not cited-only, not auto-appended).

### PDF

Markup-independent. TODO details.

## TODO

- chunked root: index.html; issues with: naming, parents, suppress listing (needed for TEI too), landing page choice
- treat `<img>` as a link element also?
- sort the pages in transclusion order, extract sections and blocks,  transclude, and style the transclusions;
- Maybe configure FlexMark to not convert general transclusion Markdown links (if it does now)
- handle categories; they can be wiki links?!
- auto-create category pages
- auto-create tag pages
- TEI facsimiles
- raw TEI
- package the CLI
- publish site into a bucket

##   Further Research

- add publishing and updating a page to X once X API supports Articles
- Look at https://stephango.com/vault
- Look at https://squidfunk.github.io/mkdocs-material/
- Look at https://github.com/KaTeX/KaTeX[KaTeX] as a MathJax alternative...

