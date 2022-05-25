# A Spoofax tutorial implementing Programming Computable Functions (PCF)

> In computer science, Programming Computable Functions (PCF) is a typed functional language introduced by Gordon Plotkin in 1977, based on previous unpublished material by Dana Scott. It can be considered to be an extended version of the typed lambda calculus or a simplified version of modern typed functional languages such as ML or Haskell.

-- https://en.wikipedia.org/wiki/Programming_Computable_Functions

With this tutorial you will hopefully be able to define the programming language PCF in the [Spoofax language workbench](https://www.spoofax.dev/spoofax-pie/develop/) in around one hour. The `tutorial` directory contains a minimal project setup that allows you to start implementing PCF. Based on this tutorial, you are hopefully able to implement the syntax and static semantics of PCF yourself. In case you get stuck, you can always peek at the example implementation in the `implementation` directory.

## Getting started

You will need an installation of the Spoofax language workbench, you can find [the latest release here](https://www.spoofax.dev/spoofax-pie/develop/tutorial/install/). The recommended version to download is the one with embedded JVM. Note that on MacOS and Linux there are some extra instructions after unpacking.

You can now import the project following the [instructions on the website](https://www.spoofax.dev/spoofax-pie/develop/guide/eclipse_lwb/import/), which come down to `File > Import...`; `General > Existing Projects into Workspace`; `Next >`; tick `Select root directory:`; `Browse...` to the directory with the `spoofaxc.cfg` and press `Open`; `Finish`.

Now you should `Project > Build Project` after selecting the project in the `Package Explorer`. 

Within the project you will be working on files in the `src` and `test` directories.

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

As you can see, PCF is a small functional programming language. It is an expression based language where `e` is the expression sort, and `t` is the type sort. There are also lexical sorts in this grammar, namely `x` for names, and `n` for numeric constants. The other words and symbols are keywords and operators. We've replaced some of the non-ASCII notation from the book into an ASCII version to make it easier to type, but if you have a good input method for non-ascii symbols, feel free to use those in your grammar. We've also added parentheses to both sorts for grouping. We have excluded pairs from the grammar, to make the language a little smaller and hopefully make this tutorial completable in one hour.

The grammar in the book is more type-directed, which constrains what programs the parser can parse to already be closer to the set of programs that are actually typed and therefore in the language. While this might seem advantageous, in practice it is nicer for a user to have a wide range of programs parse and be given highlighting. The type checker can give a much clearer explanation of why a program is not acceptable than a parser based on a grammar that encodes some type information.

### SDF3

With a context-free grammar available to us, we can start implementing the syntax of PCF in Spoofax. For this we use SDF3, the Syntax Definition Formalism version 3. You'll find that the `tutorial` project already has some `.sdf3` files ready for you: `lex.sdf3`, `expr.sdf`, `type.sdf3` and `main.sdf3`. 

The main file defines the `start symbol` of the grammar which is used by the editor to know where to start parsing. Expressions go in `expr.sdf3`, types in `type.sdf3` and we have already provided you with a lexical syntax in `lex.sdf3`. Have a look, you will find the definition of lexical sorts `Name`, `Number` and `Keyword`, where `Name` and `Number` are defined as regular expressions, and `Keyword` is defined as a few options of literal strings that correspond with keywords from the grammar. Then `Name` is restricted by a rejection rule to not match anything that can be parsed as `Keyword`. The `lexical restrictions` make sure that names and numbers are matched greedily. 

Aside: Grammars in SDF3 define both lexical and context-free syntax, both of which are handled together by a character-level parsing algorithm called SGLR. This makes it hard to provide truly greedy regular expression by default, and instead we express that a name is not allowed to be directly followed by a letter or number, which can be handled better by a parser and still makes things greedy. 

At the end of the file you find a defining of `LAYOUT`, which is the white space (and possibly comments) that are allowed between parts of the context-free syntax that we'll specify together for expressions and types. 

In the files `expr.sdf` and `type.sdf3` you will find the sort definitions for expressions and types with some but not all rules. As you can see, there is [template syntax](https://www.metaborg.org/en/latest/source/langdev/meta/lang/sdf3/reference.html#templates) in SDF3 which is both a convenient way to write your syntax and a hint for how it might be formatted in a program. Try writing some of the grammar yourself. 

Once you've built the project again, you can try out the newly added parts of a grammar. In `test/test.spt` file you will find test written in the [SPoofax Testing language SPT](https://www.metaborg.org/en/latest/source/langdev/meta/lang/spt/index.html). In this special language workbench testing language we can test many things on a high level. For now we can more `parse succeeds` and `parse fails` tests and see failing tests get an error marker in the editor immediately. You can also with a `parse to` test where you can specify the abstract syntax tree you expect. 

- TODO: explain how to write a PCF file with a program and show the abstract syntax tree

You might find that writing a program `1 + 1 + 1` fails both expectations, because it is in our PCF language, but the parsing isn't entirely _successful_. Instead the result, which you can also write as an expectation, is `parse ambiguous`. We need to specify in the grammar what the associativity of the program is, whether it's `(1 + 1) + 1` or `1 + (1 + 1)`. Let's pick the former and use the `{left}` annotation on the `Expr.Add` rule. Now your double-add test should work. In fact we can now use the `parse to` test to specify that we expect `Add(Add(Num("1"), Num("1")), Num("1"))`. That is a little cumbersome to write though. What we can also do is write another program between double brackets: `[[(1 + 1) + 1]]`. Because the round brackets are not in the AST, this comes down to the same test. 

Now that we're familiar with ambiguities and testing for them, we should root out the other ones in our grammar. You'll find that most grammar productions in PCF are not ambiguous with themselves, but mostly with each other. This is a priority problem, which is specified in a `context-free priorities` section of the grammar. You can write `Expr.App > Expr.Add` to specify that application binds tighter than addition. You can write out pairs of these with commas in between, or a longer chain of `>`, which is more common and is a reminder that priority is transitive. You can also make groups of expressions of the same priority, like `{ Expr.If  Expr.Lam  Expr.Fix }`. See if you can figure out a good set of priorities and write some tests for them. You can check your list against our in the `implementation`. 

## Syntactic Sugar

These are the syntactic extensions (syntactic sugar) found in the book we're following:

```
e ::= ...
  | let x : t = e in e             (let binding)
  | let x(x : t) : t = e in e      (let function binding)
  | letrec x : t = e in e          (let recursive binding)
  | letrec x(x : t) : t = e in e   (let recursive binding)
```

These extensions are defined as sugar, in that you can transform them into an equivalent program using only the "pure pcf" syntax. That's exactly how we will also implement this sugar.

Let's first summarise the meaning of these extensions:

```
let x : t = e1 in e2
  == (\x : t. e2) e1

let x1(x2 : t1) : t2 = e1 in e2
  == let x1 : t1 -> t2 = \x2 : t1. e1 in e2
  == (\x1 : t1 -> t2. e2) (\x2 : t1. e1)

letrec x : t = e1 in e2
  == let x : t = fix \x : t. e1 in e2
  == (x : t. e2) (fix \x : t. e1)

letrec x1(x2 : t1) : t2 = e1 in e2
  == letrec x1 : t1 -> t2 = \x2 : t1. e1 in e2
  == let x1 : t1 -> t2 = fix \x1 : t1 -> t2. \x2 : t1. e1 in e2
  == (\x1 : t1 -> t2. e2) (fix \x1 : t1 -> t2. \x2 : t1. e1)
```

As you can see, we can use lambdas for binding variables in a `let`, and we can use `fix` for recursive definitions in `letrec`. Function definition syntax in `let` is of course also just sugar for lambdas. 

### Stratego 2

Let's turn these equations into executable code. We will use the term-writing language Stratego 2 for this task. You can find code of this language in `.str2` files such as `main.str2` and `desugar.str2`. In the latter you will find a prewritten strategy `desugar`, defined as a type preserving transformation (`TP`) that traverses topdown and tries to apply the `desugar-let` rewrite rules. In the rewrite rules is `desugar-let`, also type preserving, but it currently fails. You can see an example rewrite rule that turns let bound functions into the applications of lambdas from our equations above, but this uses the `LetF` abstract syntax, which we have not defined yet. So before we start writing our rewrite rules, we should first define the appropriate syntax in `expr.sdf3`. Do that now, and don't forget the priority rules of the newly added syntax!

With the syntax defined, we can now start writing our rewrite rules. As suggested by the example one for let-bound functions, we are writing the abstract syntax pattern of our sugar programs with variables, then an arrow `->`, and then the abstract syntax pattern of our final result. This way we only have to traverse our program once to apply the rules. We could go `topdown` or `bottomup` with this approach, either direction works. 

We _could_ also write our desugaring differently, in the smaller steps of the equation, by repeatedly applying rewrite rules. Our strategy would then be `outermost` or `innermost`, and we could rewrite our `Let` as usual, but write our `Let(Rec)F` to `Let(Rec)` and our `LetRec` to `Let` with `Fix`. In fact, you can factor out the similarity of the function-binding `let` and `letrec` to their normal counterpart. This is demonstrated in `desugar2` and `desugar3` of the example `implementation`, have a look if you like. 

- TODO: explain how to add the desugaring before analysis and how to view the desugared abstract syntax tree of a PCF program

## Static Semantics

PCF has a simple type system, defined as follows:

```
------------ [T-Nat]
Γ |- n : nat

---------------- [T-True]
Γ |- true : bool

----------------- [T-False]
Γ |- false : bool


Γ |- e1 : nat
Γ |- e2 : nat
------------------ [T-Add]
Γ |- e1 + e2 : nat

Γ |- e1 : t
Γ |- e2 : t
--------------------- [T-Eq]
Γ |- Eq? e1 e2 : bool

Γ |- e : bool
Γ |- e1 : t
Γ |- e2 : t
----------------------------- [T-If]
Γ |- if e then e1 else e2 : t


(x, t) ϵ Γ
---------- [T-Var]
Γ |- x : t

Γ; (x, t) |- e : t'
------------------------ [T-Abs]
Γ |- \x : t. e : t -> t'

Γ |- e1 : t' -> t
Γ |- e2 : t'
----------------- [T-App]
Γ |- e1 e2 : t


Γ |- e : t -> t
--------------- [T-Fix]
Γ |- fix e : t
```

### Statix

## References

- (Mitchell 1996) Mitchell, John C. (1996). The Language PCF. In: Foundations for Programming Languages. https://theory.stanford.edu/~jcm/books/fpl-chap2.ps
- (Dowek et al. 2011) Dowek, G., Lévy, JJ. (2011). The Language PCF. In: Introduction to the Theory of Programming Languages. Undergraduate Topics in Computer Science. Springer, London. https://doi.org/10.1007/978-0-85729-076-2_2
