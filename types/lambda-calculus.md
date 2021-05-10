# Lambda-calculus #

## Definition ##

In untyped lambda-calculus we have terms (t, u...), variables (x, y, z),
abstractions (functions), and applications.
When we get to dependent types, where well-formedness of the terms depends on the
types of the variables, terms will need to be defined using judgements; here a
simple grammar is sufficient; assuming infinite set Variable of variables:

Term ::= Variable | λ Variable . Term | Term Term

Conventions:
  - application associates to the left: tuv means (tu)v;
  - application binds stronger than the abstraction: λx.xy means λx.(xy);
  - abstractions can be grouped: λxyz.xz(yz) means λx.λy.λz.xz(yz).

Variable under λ is _bound_ in its "body"; variable than is not bound is _free_;
term with no free variables is _closed_.
Bound variables can be renamed (α-conversion),
and when substituting a term for a bound variable, care needs to be taken not to
accidentally "capture" a variable that is free in that term. This is a great pain
for the implementer, and various tricks were developed to ease it (de-Brujin
indices, Barendregt convention); we will say no more about it, and just assume
that before substitution all variables that need to be distinct are given fresh names :)

β-reduction is the smallest binary relation →<sub>β</sub> on terms such that:
- (λx.t)u →<sub>β</sub> t[u/x], with substitution t[u/x] the result of replacing
  all free occurrences of x in t by u ((λx.t)u is called a _redex_);
- if t →<sub>β</sub> t', λx.t →<sub>β</sub> λx.t';
- if t →<sub>β</sub> t', tu →<sub>β</sub> t'u;
- if u →<sub>β</sub> u', tu →<sub>β</sub> tu'.

→*<sub>β</sub> (multi-step reduction; reduction path) is the reflexive and transitive
closure of →<sub>β</sub>.

Term that can not be reduced is in _normal form_ (a _value_).

Term t is _weekly normalizing_ if there exist a normal form u such that t →*<sub>β</sub> u.

Term is _strongly normalizing_ when every sequence of reductions will eventually produce a
normal form. Not all terms a strongly normalizing: Ω = (λx.xx)(λx.xx) reduces
to itself. But: if t →*<sub>β</sub> u<sub>1</sub> and t →*<sub>β</sub> u<sub>2</sub>,
there exists such v that u<sub>1</sub> →*<sub>β</sub> v and u<sub>2</sub> →*<sub>β</sub> v
(_confluence_, _Church-Rosser property_).

=<sub>β</sub> (β-equivalence, β-convertability) is the symmetric closure
of →*<sub>β</sub>.

η-reduction: λx.t →<sub>η</sub> t; opposite relation is called η-expansion;
it is clear how define η-equivalence and βη-equivalence.

## Encodings ##

It is possible to encode common types and data structures in lambda calculus.

identity: I = λx.x

boolean 'true': T = λxy.x

boolean 'false': F = λxy.y

if-then-else: if = λbxy.bxy (if T t u →*<sub>β</sub> t etc.)

and = λxy.xyF; or = λxy.xTy; nor = λx.xFT; (logical operations)

pair = λxyb. if b x y 

fst = λp.pT; snd = λp.pF (fst(pair t u) →*<sub>β</sub> t etc.)

(n-tuples can also be defined)

n-th Church numeral: **n** = λfx.f(f(...(fx))) where f is applied n times

succ = λnfx.f(nfx)

add = λmnfx.m succ n

mul = λmnfx.m (add n) 0

exp = λmn.n (mul m) 1

iszero = λnxy.n(λz.y)x

pred = λnfx.n(λgh.h(gf))(λy.x)(λy.y)

sub = λmn.n pred m

leq = λmn.iszero (sub m n)

## Fixpoints ##

u is a _fixpoint_ of term u if t u →*<sub>β</sub> u; in lambda-calculus,
**every** term has a fixpoint, **and** there is a term Y (_fixpoint combinator_)
such that Yt is a fixpoint of t! For example:
- _Curry fixpoint combinator_: Y = λf.(λx.f(xx))(λx.f(xx))
- _Turing fixpoint combinator_: ϴ = (λfx.x(ffx))(λfx.x(ffx))