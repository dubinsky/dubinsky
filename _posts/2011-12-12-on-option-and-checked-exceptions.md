---
layout: post
title: On Option and checked exceptions
date: '2011-12-12T11:27:00.002-05:00'
author: Leonid Dubinsky
tags: [scala]
modified_time: '2011-12-27T14:45:30.785-05:00'
blogger_id: tag:blogger.com,1999:blog-8681083740214020499.post-7679403351251874158
blogger_orig_url: https://blog.dub.podval.org/2011/12/on-option-and-checked-exceptions.html
---

I went to a Scala [workshop](http://www.artima.com/shop/scala_boston) run by
[Bill Venners](http://www.artima.com/weblogs/index.jsp?blogger=bv) and
[Dick Wall](https://www.artima.com/weblogs/index.jsp?blogger=dickwall). Among other useful tidbits I learned, there
happened an interesting discussion about Option types and exceptions. Here is the gist.

Disck is a proponent of Option types. He said that once he switched from the
[evil nulls](http://www.infoq.com/presentations/Null-References-The-Billion-Dollar-Mistake-Tony-Hoare) to Option, 80% of
the bugs in his code - caused by NullPointerException - went away. I commented that NPEs did not go away, but turned
into whatever gets thrown when you do ".get" on an undefined Option. Dick explained that that the bugs went away because
the use of Option forces the programmer to explicitly deal with the handling of undefined Option values - or the code
won't compile.

Additional benefit of using Option is the availability of "map", "for" and other functional machinery on it, which
allows writing smooth expressions (in a point-less style) uninterrupted by the checks for null. One of the people
present argued that treating Option as a container that can be empty is unnatural. I countered that if one really likes
Java-style code peppered with checks for null, it can be written in Scala - just check ".isDefined" instead :)

Another problem with Option, voiced by a workshop participant, is the need to box the values with "Some", making for
chatty and weird-looking code. [Andy Vysocker](http://ironicallytitled.blogspot.com/) added that introducing implicit
conversion from T to Option[T] (autoboxing) turned out to be a bad idea: since implicit conversions are inserted not
only when a type required is different from the type found, but also when a method used is missing on the type
("pimping"), use of the method present on the Option will trigger insertion of the implicit conversion when it was not
intended, causing incorrect - and confusing - behaviour. (Maybe it should be possible to limit the applicability of an
implicit conversion to one of the two settings?)

Then, Dick mentioned that checked exceptions are looked upon with disfavor by the community. Personally, I am not
completely convinced that checked exceptions are so evil, but I feel the pain that they bring in some settings.
The question I had at this point in Dick's presentation was a question of consistency: if it is a good idea to force the
programmer to deal with special situations explicitly in case of the Option/null, why <u>isn't</u> it a good idea in
case of checked/unchecked exceptions? Checked exceptions are analogous to Option, unchecked - to null, so if you prefer
Option to null, you can not - at the first glance - favor unchecked exceptions over checked ones! Yet "the community",
Dick, and - in part - I do! Are we being inconsistent?!

This is when Bill had the insight:

Since the <u>idea</u> of an exception is to divert control from the code where special situation occurred to the code
where something can reasonably be done about it, - in other words, shield the programmer from the need to deal with  an
exception that he can not handle - it stands to reason that the programmer should be shielded from the need to
<u>know</u> about the <u>possibility</u> of the exception also! Thus, exceptions should <u>always</u> be unchecked!

In other words, forcing the programmer to <u>be explicit</u> about the handling of special situations does make sense if
it is the programmer's responsibility to <u>deal</u> with them - thus, Option is a better alternative to nulls - but not
where it is not. So, preferring unchecked exceptions to the checked ones is not just a logically consistent position -
it is the only one!

(As I mentioned, I am still not completely convinced that checked exceptions do not have their place. Thus I am not
really happy that Scala does not provide any way to express checked expressions (@throws is for calling from Java.)).
On the other hand I understand that since unchecked exceptions do seem to be the preferred case, Scala needs to turn all
exceptions from existing Java libraries into unchecked ones, so it is unclear how to introduce "really checked
exceptions" :))

Dick went on to suggest that exceptions should be abandoned altogether, and replaced by Either: either a value, or a
reason for the failure to produce one. This approach is trivially consistent with the idea of forcing the explicit
handling of the special situations (as exemplified by Option), but I do not think that it is a viable alternative to
exceptions: often, you <u>do</u> want to jump over a bunch of layers of code and deliver the exception to a handler
directly.

On the other hand, Either does seems to be conceptually equivalent - and arguably syntactically better - alternative to
<u>checked</u> exception! If you do not want to jump over code, but want to propagate the failure reason and force the
programmer to deal with it explicitly - Either seems to be cleaner than checked exceptions (which we do not have in
Scala anyway :)).

(Either <u>is</u> (I think) used in Haskell l to represent exceptions, but jumping over stack frame is not really an
option in a purely functional setting :) Also, the pain of explicit propagation of the Either values is alleviated by
monads and syntax sugar, if the memory serves.)

Dick mentioned that just like with Option, there are combinators in Scala that allow computation to be expressed in a
smooth way, without analyzing the intermediary Either values at each step - and still taking them into account.

Bill noted that it is possible to throw the "reason of failure" side of an Either, thus converting from Either to
exceptions (and back :)).

## Conclusions ##
- Use Option, not nulls
- Unchecked exceptions are better than checked
- Use Either where checked exceptions make sense
- Learn the combinators that can be used to write expressions over Either without ifs and cases
