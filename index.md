---
layout: page
title: Home
---

{% avatar dubinsky size=100 %}

[My resume](https://www.linkedin.com/in/leoniddubinsky/)

[Photos](https://photos.google.com/albums)

{% for repository in site.github.public_repositories %}
  * [{{ repository.name }}]({{ repository.html_url }})
{% endfor %}
