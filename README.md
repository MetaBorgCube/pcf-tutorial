# A Spoofax tutorial implementing Programming Computable Functions (PCF)

> In computer science, Programming Computable Functions (PCF) is a typed functional language introduced by Gordon Plotkin in 1977, based on previous unpublished material by Dana Scott. It can be considered to be an extended version of the typed lambda calculus or a simplified version of modern typed functional languages such as ML or Haskell.

-- https://en.wikipedia.org/wiki/Programming_Computable_Functions

With this tutorial you will hopefully be able to define the programming language PCF in the [Spoofax language workbench](https://www.spoofax.dev/spoofax-pie/develop/) in around one hour. The `tutorial` directory contains a minimal project setup that allows you to start implementing PCF. Based on this tutorial, you are hopefully able to implement the syntax and static semantics of PCF yourself. In case you get stuck, you can always peek at the example implementation in the `implementation` directory.

## Getting started

- Download Spoofax
- Import Project
- Build
- Where to find relevant files

## Source material for PCF

Since the original publications on PCF were more concerned with semantics than syntax, it is a bit difficult to trace the syntax of PCF. We will use the definition of PCF from a book by John C. Mitchell called "Foundation for Programming Languages" (Mitchell 1996). Note that you might also find a definition by Dowek and Levy (Dowek et al. 2011) called Mini-ML, or PCF, this is not the version that we will use here. Chapter 2 of Foundation for Programming Languages about the language PCF is [freely available](https://theory.stanford.edu/~jcm/books/fpl-chap2.ps).

## Grammar

This is a slightly massaged grammar of so-called "pure pcf" from section 2.2.6:

```
e ::= x                            (variable reference)
  | if e then e else e             (if condition)
  | \x : t. e                      (function abstraction)
  | e e                            (function application)
  | fix e                          (fixed point)
  | true | false                   (boolean constants)
  | Eq? e e                        (equality check)
  | n                              (natural number constant)
  | e + e                          (arithmetic operations)
  | (e)                            (parenthesised expression)

t ::= nat                          (natural number type)
  | bool                           (boolean type)
  | t -> t                         (function type)
  | (t)                            (parenthesised type)
```

And these are the syntactic extensions (syntactic sugar) defined:

```
e ::= ...
  | let x : t = e in e             (let binding)
  | let x(x : t) : t = e in e      (let function binding)
  | letrec x : t = e               (let recursive binding)
  | letrec x(x : t) : t = e        (let recursive binding)
  | \(x : t, x : t). e             (curried function abstraction)
```

As you can see, PCF is a small functional programming language. It is an expression based language where `e` is the expression sort, and `t` is the type sort. There are also lexical sorts in this grammar, namely `x` for names, and `n` for numeric constants. The other words and symbols are keywords and operators. We've replaced some of the non-ASCII notation from the book into an ASCII version to make it easier to type, but if you have a good input method for non-ascii symbols, feel free to use those in your grammar. We've also added parentheses to both sorts for grouping. We have excluded pairs from the grammar, to make the language a little smaller and hopefully make this tutorial completable in one hour.

The grammar in the book is more type-directed, which constrains what programs the parser can parse to already be closer to the set of programs that are actually typed and therefore in the language. While this might seem advantageous, in practice it is nicer for a user to have a wide range of programs parse and be given highlighting. The type checker can give a much clearer explanation of why a program is not acceptable than a parser based on a grammar that encodes some type information.

### SDF3

With a context-free grammar available to us, we can start implementing the syntax of PCF in Spoofax. For this we use SDF3, the Syntax Definition Formalism version 3. You'll find that the `tutorial` project already has some `.sdf3` files ready for you.

- Explain lexical syntax
- Note files for expressions and types
- Note SPT tests
- Suggest to write the pure pcf rules above
- Suggest more SPT tests to try it out, including AST
- Explain priorities and disambiguation
- SPT tests with parse to another program with parentheses

## Static Semantics

PCF has a simple type system, defined as follows:

```
------------- [T-Nat]
Γ |- n : nat

---------------- [T-True]
Γ |- true : bool

----------------- [T-False]
Γ |- false : bool


Γ |- e1 : nat    Γ |- e2 : nat
------------------------------ [T-Add]
      Γ |- e1 + e2 : nat

Γ |- e1 : t    Γ |- e2 : t
---------------------------- [T-Eq]
    Γ |- Eq? e1 e2 : bool

Γ |- e : bool    Γ |- e1 : t    Γ |- e2 : t
------------------------------------------- [T-If]
         Γ |- if e then e1 else e2 : t


(x, t) ϵ Γ
---------- [T-Var]
Γ |- x : t

   Γ; (x, t) |- e : t'
------------------------ [T-Abs]
Γ |- \x : t. e : t -> t'

Γ |- e1 : t' -> t   Γ |- e2 : t'
-------------------------------- [T-App]
          Γ |- e1 e2 : t


Γ |- e : t -> t
--------------- [T-Fix]
Γ |- fix e : t
```

### Statix

## References

- (Mitchell 1996) Mitchell, John C. (1996). The Language PCF. In: Foundations for Programming Languages. https://theory.stanford.edu/~jcm/books/fpl-chap2.ps
- (Dowek et al. 2011) Dowek, G., Lévy, JJ. (2011). The Language PCF. In: Introduction to the Theory of Programming Languages. Undergraduate Topics in Computer Science. Springer, London. https://doi.org/10.1007/978-0-85729-076-2_2
