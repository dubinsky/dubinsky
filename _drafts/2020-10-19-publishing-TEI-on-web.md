---
layout: post
title: 'Publishing TEI on the Web'
author: Leonid Dubinsky
tags: [tei]
date: '2020-10-19T12:40:00.000-04:00'
modified_time: '2020-10-19T12:40:00.000-04:00'
---
* TOC
{:toc}
## TEI ##

Upgraded to 2020/6/18 version with my modifications (including https://github.com/TEIC/CETEIcean/pull/35 and https://github.com/TEIC/CETEIcean/pull/37).

With the removal of the `table` behavior, end-notes in the table cells are no longer being duplicated (e.g. rgada 026). But `cols` attribute on the TEI table cells is no longer handled; this is not perfect for the collection index tables, where it is used for part headers, and *breaks* layout of the table in LVIA 1799 09512. Since `colspan` attribute does not work on not-built-in element `tei-cell` even though `display: table-cell` does, the only solution seems to be to bring the `table` behavior back... See https://github.com/TEIC/CETEIcean/issues/38

Remove endnotes from the data I put into the page headers, or they will be duplicated; see for example http://www.alter-rebbe.org/collections/rgia529/documents/074.html 

At this point, it seems pretty clear that CETEIcean people are not going to fix the breakage they caused, let alone start handling things like `<table><head>` or `role="label"` on the `<row>` and `<cell>` - although maybe I can deal with all that in CSS...

I should just get rid of this disaster altogether (and blog about it) :(

Get rid of CETEIcean:
- [x] generate HTML files with embedded TEI;
- [x] transform select TEI elements to HTML; 
- [x] keep TEI element in the TEI namespace;
- [x] keep TEI element names;
- [x] assign XHTML namespace to the transformed elements;
- [x] restrict TEI CSS to TEI namespace;
- [x] restrict other CSS to XHTML namespace;
- [x] use the transformation on the parts of the facsimile files;
- [x] do not write augmented TEI and HTML wrappers like now;
- [x] save/restore window position;
- [x] remove CETEicean;

Blog (include parts of the `xml` README):
- TEI Boilerplate;
- CETEIcean;
- broken development process;
- static/dynamic;
- official XSLT;
- pre-generation;
- pretty-printing;
- Scala Tags;