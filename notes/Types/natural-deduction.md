---
layout: post
title: natural-deduction
date: '2019-04-24T18:40:00.000-04:00'
author: Leonid Dubinsky
tags: []
math: true
---

## Natural Deduction ##

[wikipedia](https://en.wikipedia.org/wiki/Natural_deduction)

### Judgements ###
- "$A$ is a proposition" ($A \; prop$)
- "(proposition) $A$ is true" ($A \; true$)
- "term $M$ has type $T$" ($M : T$)

### Inference Rules ###

Allow deduction of judgements from premises.

Come in flavors: formation, introduction, elimination, computation.


Proposition formation rule for conjunction:

$$ \frac {A \; prop \quad B \; prop}{A \land B \; prop} \land_F $$

Conjunction introduction rule:

$$ \frac {A \; prop \quad B \; prop \quad A \; true \quad B \; true}{(A \land B) \; true} \land_I $$

or, if it is clear that A and B are propositions:

$$ \frac {A \; true \quad B \; true}{(A \land B) \; true} \land_I $$

or, if the judgement is clear:

$$ \frac {A \quad B}{(A \land B)} \land_I $$

Conjunction elimination rule(s):

$$ \frac {A \land B} A \land_E $$


#### Prawitz ####

Assumption (premis $A$ of a hypothetical derivation of $C$): $ C (A) $

$$ \frac {A \quad B}{(A \land B)} \land_I \qquad \qquad \frac {A \land B} A {\land_E}_1 \qquad \frac {A \land B} B {\land_E}_2 $$

$$ \frac A { A \lor B } {\lor_I}_1 \qquad \frac B { A \lor B } {\lor_I}_2 \qquad \qquad \frac { A \lor B \quad C (A) \quad C (B) } C \lor_E $$

$$ \frac { B (A) } { A \Rightarrow B }  \Rightarrow_I \qquad \qquad  \frac { A \quad A \Rightarrow B } B \Rightarrow_E $$

TODO quantifiers

$$ \frac \bot A \bot^I \qquad \qquad \frac { \bot (~ A) } A \bot^C $$

## References ##

["Natural Deduction: A Proof-Theoretical Study"](https://www.amazon.com/Natural-Deduction-Proof-Theoretical-Study-Mathematics/dp/0486446557),
  Dag Prawitz, 1965
  
"Logical Basis of Metaphysics", Dummet, 1976

“Intuitionistic Type Theory”, Martin-Lof, 1984.

"Constructive Mathematics and Computer Programming" (Martin-Lof)

“Programming in Martin-Lof Type Theory”, Nordstrom et al, 1990.

“Martin-Lof’s Type Theory”, Nordstrom et al, 1991.

“Type Theory and Functional Programming”, Thompson, 1991. ([errata](http://www.cs.kent.ac.uk/people/staff/sjt/TTFP/errata.html))

“Syntax and Semantics of Dependent Types”, Hofmann, 1997.

“Logical Frameworks - A Brief Introduction”, Pfenning, 2002.

“Computation and Reasoning: a Type Theory for Computer Science”. Luo, 1994. (Not found online)

"A unifying theory of dependent types" (Luo)

"Towards a practical programming language based on dependent type theory", Norell, 2007

"Why Dependent Types Matter" (McBride)

"The Power of Pi" (Oury, Swierstra)

"The Road to Dependent Types" (Pope)

"Propositions as Types" (Wadler)

"Proofs and Types" (Girard)

"On the roles of types in mathematics" (de Bruijn)

"Lectures on the Curry-Howard Isomorphism" (Sorensen)

"Lambda Calculus Timeline" (Dana Scott)

"Lambda Calculi with Types" (Barendregt)

"History of Lambda-calculus and Combinatory Logic" (Hindley)

"From Sets to Types to Categories to Sets" (Awodey)

"Equality and dependent type theory" (Coquand)

"Type Theory and Formal Proof", Nederpelt & Geuvers, 2014

"When is one thing equal to some other thing?" (Mazur)

"Isomorphism is Equality" (Coquand)
