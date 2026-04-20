---
title: Jekyll
---

I did some re-organizing of my notes March 2026, and started getting:

```text
/usr/lib/ruby/gems/3.4.0/gems/jekyll-wikirefs-0.0.16/lib/jekyll-wikirefs/util/wikiref.rb:224:in
 'Jekyll::WikiRefs::WikiLinkInline#context_fm_data': undefined method 'url' for nil (NoMethodError)
'url' => self.context_doc.url,
                     ^^^^
from /usr/lib/ruby/gems/3.4.0/gems/jekyll-wikirefs-0.0.16/lib/jekyll-wikirefs/util/link_index.rb:73:in 'block in Jekyll::WikiRefs::LinkIndex#populate'
from /usr/lib/ruby/gems/3.4.0/gems/jekyll-wikirefs-0.0.16/lib/jekyll-wikirefs/util/link_index.rb:67:in 'Array#each'
from /usr/lib/ruby/gems/3.4.0/gems/jekyll-wikirefs-0.0.16/lib/jekyll-wikirefs/util/link_index.rb:67:in 'Jekyll::WikiRefs::LinkIndex#populate'
from /usr/lib/ruby/gems/3.4.0/gems/jekyll-wikirefs-0.0.16/lib/jekyll-wikirefs/plugins/generator.rb:28:in 'block in Jekyll::WikiRefs::Generator#generate'
from /usr/lib/ruby/gems/3.4.0/gems/jekyll-wikirefs-0.0.16/lib/jekyll-wikirefs/plugins/generator.rb:25:in 'Array#each'
from /usr/lib/ruby/gems/3.4.0/gems/jekyll-wikirefs-0.0.16/lib/jekyll-wikirefs/plugins/generator.rb:25:in 'Jekyll::WikiRefs::Generator#generate'
```

Turned out, use of `[[TODO]]` in any of the `notes/XXX/index.md` files causes this...

I should just write my own site generator ;)
