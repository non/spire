---
layout: home
title:  "Readme"
section: "Home"
position: 0
---

[![Build Status](https://api.travis-ci.org/typelevel/spire.png)](https://travis-ci.org/non/spire/)
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/typelevel/spire?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![codecov.io](https://codecov.io/github/typelevel/spire/coverage.svg?branch=main)](http://codecov.io/github/typelevel/spire?branch=main)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.typelevel/spire_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.typelevel/spire_2.11)

## Spire

### Overview

Spire is a numeric library for Scala which is intended to be generic, fast,
and precise.

Using features such as specialization, macros, type classes, and implicits,
Spire works hard to defy conventional wisdom around performance and precision
trade-offs. A major goal is to allow developers to write efficient numeric
code without having to "bake in" particular numeric representations. In most
cases, generic implementations using Spire's specialized type classes perform
identically to corresponding direct implementations.

### Scaladoc
[![Core](http://javadoc-badge.appspot.com/org.typelevel/spire_2.11.svg?label=core)](http://javadoc-badge.appspot.com/org.typelevel/spire_2.11/index.html#spire.package)
[![Extras](http://javadoc-badge.appspot.com/org.typelevel/spire-extras_2.11.svg?label=extras)](http://javadoc-badge.appspot.com/org.typelevel/spire-extras_2.11)
[![Laws](http://javadoc-badge.appspot.com/org.typelevel/spire-laws_2.11.svg?label=laws)](http://javadoc-badge.appspot.com/org.typelevel/spire-laws_2.11/index.html#spire.laws.package)
[![Macros](http://javadoc-badge.appspot.com/org.typelevel/spire-macros_2.11.svg?label=macros)](http://javadoc-badge.appspot.com/org.typelevel/spire-macros_2.11/index.html#spire.macros.package)

Spire is provided to you as free software under the
[MIT license](COPYING).

### Organization

The [Spire mailing list](http://groups.google.com/group/typelevel/)
is shared with other [Typelevel projects](http://typelevel.org).
It is the place to go for announcements and discussions around Spire.
When posting, place the word `[spire]` at the beginning of your subject.
We also have a guide on [contributing to Spire](CONTRIBUTING.md) as well
as a guide that provides information on [Spire's design](GUIDE.md).

Spire has maintainers who are responsible for signing-off on and
merging pull requests, and for helping to guide the direction of Spire:

 * Erik Osheim (*erik@osheim.org*)
 * Tom Switzer (*thomas.switzer@gmail.com*)
 * Rüdiger Klaehn (*rklaehn@gmail.com*)
 * Denis Rosset (*physics@denisrosset.com*)

People are expected to follow the [Scala Code of Conduct](https://www.scala-lang.org/conduct/)
when discussing Spire on the Github page, in Gitter, the IRC channel,
mailing list, and other official venues.

Concerns or issues can be sent to any of Spire's maintainers, or to the
[Typelevel](http://typelevel.org/about.html) organization.

### Set up

Spire is currently available for Scala 2.10, 2.11, and 2.12, for the
JVM and JS platforms.

To get started with SBT, simply add the following to your `build.sbt` file:

```
libraryDependencies += "org.typelevel" %% "spire" % "0.14.1"
```

(Previous to 0.14.0, Spire was published under *org.spire-math*
instead of *org.typelevel*.)

For Maven instructions, and to download the jars directly, visit the
[Central Maven repository](http://search.maven.org/#artifactdetails%7Corg.typelevel%7Cspire_2.11%7C0.14.1%7Cjar).

Here is a list of all of Spire's modules:

 * `spire-macros`: macros and compile-time code (required by `spire`)
 * `spire`: the core Spire library, the types and type classes
 * `spire-laws`: optional support for law-checking and testing
 * `spire-extras`: extra types which are more specific or esoteric

Spire depends on [typelevel/algebra](https://github.com/typelevel/algebra)
and uses this project to interoperate with [Cats](https://github.com/typelevel/cats) and
[Algebird](https://github.com/twitter/algebird).

### Playing Around

If you clone the Spire repo, you can get a taste of what Spire can do using
SBT's console. Launch `sbt` and at the prompt, type `coreJVM/console`:

```
> coreJVM/console
[info] Generating spire/std/tuples.scala
[info] Starting scala interpreter...
[info]
Welcome to Scala version 2.11.8 (Java HotSpot(TM) 64-Bit Server VM, Java 1.8.0_51).
Type in expressions to have them evaluated.
Type :help for more information.

scala> import spire.implicits._
import spire.implicits._

scala> import spire.math._
import spire.math._

scala> Complex(3.0, 5.0).sin
res0: spire.math.Complex[Double] = (10.472508533940392 + -73.46062169567367i)
```

### Number Types

In addition to supporting all of Scala's built-in number types, Spire
introduces several new ones, all of which can be found in `spire.math`:

 * `Natural` unsigned, immutable, arbitrary precision integer
 * `Rational` fractions of integers with perfect precision
 * `Algebraic` lazily-computed, arbitrary precision algebraic numbers
 * `Real` computable real number implementation
 * `Complex[A]` complex numbers, points on the complex plane
 * `Jet[A]` N-dimensional dual numbers, for automatic differentiation
 * `Quaternion[A]` extension of complex numbers into 4D space
 * `UByte` through `ULong` value classes supporting unsigned operations
 * `SafeLong` fast, overflow-proof integer type
 * `Number` boxed type supporting a traditional numeric tower
 * `Interval[A]` arithmetic on open, closed, and unbound intervals
 * `Polynomial[A]` univariate (single-variable) polynomial expressions
 * `Trilean` value class supporting three-valued logic
 * `FixedPoint` fractions with `Long` numerator and implicit denominator (in *extras*)

Detailed treatment of these types can be found in the [guide](GUIDE.md).

### Type Classes

Spire provides type classes to support a wide range of unary and binary
operations on numbers. The type classes are specialized, do no boxing, and use
implicits to provide convenient infix syntax.

The general-purpose type classes can be found in `spire.math` and consist of:

 * `Numeric[A]` all number types, makes "best effort" to support operators
 * `Fractional[A]` fractional number types, where `/` is true division
 * `Integral[A]` integral number types, where `/` is floor division

Some of the general-purpose type classes are built in terms of a set of more
fundamental type classes defined in `spire.algebra`. Many of these correspond
to concepts from abstract algebra:

 * `Eq[A]` types that can be compared for equality
 * `Order[A]` types that can be compared and ordered
 * `PartialOrder[A]` types that can be compared for equality, and for which certain pairs are ordered
 * `Semigroup[A]` types with an associative binary operator `|+|`
 * `Monoid[A]` semigroups that have an identity element
 * `Group[A]` monoids that have an inverse operator
 * `(Left/Right/)Action[P, G]` left/right/ actions of semigroups/monoids/groups
 * `Semiring[A]` types that form semigroups under `+` and `*`
 * `Rng[A]` types that form a group under `+` and a semigroup under `*`
 * `Rig[A]` types that form monoids under `+` and `*`
 * `Ring[A]` types that form a group under `+` and a monoid under `*`
 * `EuclideanRing[A]` rings with quotients and remainders (euclidean division)
 * `Field[A]` euclidean rings with multiplicative inverses (reciprocals)
 * `Signed[A]` types that have a sign (negative, zero, positive)
 * `NRoot[A]` types that support k-roots, logs, and fractional powers
 * `Module[V,R]` types that form a left R-module
 * `VectorSpace[V,F]` types that form a vector space
 * `NormedVectorSpace[V,F]` types with an associated norm
 * `InnerProductSpace[V,F]` types with an inner product
 * `MetricSpace[V,R]` types with an associated metric
 * `Trig[A]` types that support trigonometric functions
 * `Bool[A]` types that form a Boolean algebra
 * `Heyting[A]` types that form a Heyting algebra

Variants of Semigroup/Monoid/Group/Action with partial operations are
defined in the `spire.algebra.partial` subpackage.

In addition to the type classes themselves, `spire.implicits` defines many
implicits which provide unary and infix operators for the type classes. The
easiest way to use these is via a wildcard import of `spire.implicits._`.

Detailed treatment of these type classes can be found in the
[guide](GUIDE.md).

### Getting Started

Spire contains a lot of types, as well as other machinery to provide a nice
user experience. The easiest way to use spire is via wildcard imports:

```tut
import spire.algebra._   // provides algebraic type classes
import spire.math._      // provides functions, types, and type classes
import spire.implicits._ // provides infix operators, instances and conversions
```

Of course, you can still productively use Spire without wildcard imports, but
it may require a bit more work to figure out which functionality you want and
where it's coming from.

### Operators by Type Class

The following is an outline in more detail of the type classes provided by
Spire, as well as the operators that they use. While Spire avoids introducing
novel operators when possible, in a few cases it was unavoidable.

#### Eq, Order and PartialOrder

The type classes provide type-safe equivalence and comparison functions. Orderings
can be total (`Order`) or partial (`PartialOrder`); although undefined elements like
`NaN` or `null` will cause problems in the default implementations [1].

 * *Eq*
   + eqv (`===`): equivalence
   + neqv (`=!=`): non-equivalence
 * *Order*
   + compare: less-than (-1), equivalent (0), or greater-than (1)
   + gt (`>`): greater-than
   + gteqv (`>=`): greater-than-or-equivalent
   + lt (`<`): less-than
   + lteqv (`<=`): less-than-or-equivalent
   + min: find least value
   + max: find greatest value
 * *PartialOrder*
   + partialCompare: less-than (`-1.0`), equivalent (`0.0`), greater-than (`1.0`) or incomparable (`NaN`)
   + tryCompare: less-than (`Some(-1)`), equivalent (`Some(0)`), greater-than (`Some(1)`) or incomparable (`None`)
   + pmin: find the least value if the elements are comparable; returns an `Option`
   + pmax: find the greated value if the elements are comparable; returns an `Option`
   + gt (`>`), gteqv (`>=`), lt (`<`) and lteqv (`<=`) return false if the elements are incomparable, or the result of their comparison

[1] For floating-point numbers, alternate implementations that take `NaN` into
account can be imported from `spire.optional.totalfloat._`.

#### Semigroup, Monoid, and Group

These general type classes constitute very general operations. The operations
range from addition and multiplication to concatenating strings or lists, and
beyond!

 * *Semigroup*
   + op (`|+|`): associative binary operator
 * *Monoid*
   + id: an identity element
   + isId: checks (together with Eq) for identity
 * *Group*
   + inverse: an unary operator

There are Additive and Multiplicative refinements of these general type
classes, which are used in the Ring-family of type classes.

#### Rings &co

The Ring family of type classes provides the typical arithmetic operations
most users will expect.

 * *Semiring*
   + plus (`+`): addition
   + times (`*`): multiplication
   + pow (`**`): exponentiation (integral exponent)
 * *Rng*
   + negate (`-`): additive inverse
   + minus (`-`): subtraction
   + zero: additive identity
 * *Rig*
   + zero: additive identity
   + one: multiplicative identity
 * *Ring* (Rng + Rig)
 * *GCDRing*
   + gcd: greatest-common-divisor
   + lcm: least-common-multiple
 * *EuclideanRing*
   + quot (`/~`): quotient (Euclidean division)
   + mod (`%`): remainder
   + quotmod (`/%`): quotient and mod
 * *Field*
   + reciprocal: multiplicative inverse
   + div (`/`): division
   + ceil: round up
   + floor: round down
   + round: round to nearest
 * *NRoot*
   + nroot: k-roots (k: Int)
   + sqrt: square root
   + log: natural logarithm
   + fpow (`**`): exponentiation (fractional exponent)

#### VectorSpaces &co

The vector space family of type classes provide basic vector operations. They
are parameterized on 2 types: the vector type and the scalar type.

 * *Module*
   + plus (`+`): vector addition
   + minus (`-`): vector subtraction
   + timesl (`*:`): scalar multiplication
 * *VectorSpace*
   + divr (`:/`): scalar division
 * *NormedVectorSpace*
   + norm: vector norm
   + normalize: normalizes vector (so norm is 1)
 * *InnerProductSpace*
   + dot (`⋅`, `dot`): vector inner product

#### Numeric, Integral, and Fractional

These high-level type classes will pull in all of the relevant algebraic type
classes. Users who aren't concerned with algebraic properties directly, or who
wish for more permissiveness, should prefer these type classes.

 * *Integral*: whole number types (e.g. `Int`, `BigInt`)
 * *Fractional*: fractional/decimal types (e.g. `Double`, `Rational`)
 * *Numeric*: any number type, making "best effort" to support ops

The `Numeric` type class is unique in that it provides the same functionality
as `Fractional` for all number types. Each type will attempt to "do the right
thing" as far as possible, and throw errors otherwise. Users who are leery of
this behavior are encouraged to use more precise type classes.

#### Bool

Bool supports Boolean algebras, an abstraction of the familiar bitwise
boolean operators.

 * *Bool*
   + complement (unary `~`): logical negation
   + and (`&`): conjunction
   + or (`|`): disjunction
   + xor (`^`): exclusive-disjunction
   + imp: implicitation, equivalent to `~a | b`
   + nand: "not-and," equivalent to `~(a & b)`
   + nor: "not-or," equivalent to `~(a | b)`
   + nxor: "not-xor," equivalent to `~(a ^ b)`

Bool instances exist not just for `Boolean`, but also for `Byte`,
`Short`, `Int`, `Long`, `UByte`, `UShort`, `UInt`, and `ULong`.

#### Trig

Trig provides an abstraction for any type which defines trigonometric
functions. To do this, types should be able to reasonably approximate real
values.

 * *Trig*
   + e: Euler's number, `2.71828...`
   + pi: Ratio of circle's circumference to diameter, `3.14159...`
   + exp: exponential function, `e^x`
   + expm1: `e^x - 1`
   + log: natural logarithm
   + log1p: `log(x + 1)`
   + sin, cos, tan: sine, cosine, and tangent, the standard functions of angles
   + asin, acos, atan, atan2: inverse functions
   + sinh, cosh, tanh: hyperbolic functions
   + toRadians, toDegrees: convert between angle units

### Syntax

Using string interpolation and macros, Spire provides convenient syntax for
number types. These macros are evaluated at compile-time, and any errors they
encounter will occur at compile-time.

For example:

```tut
import spire.syntax.literals._

// bytes and shorts
val x = b"100" // without type annotation!
val y = h"999"
val mask = b"255" // unsigned constant converted to signed (-1)

// rationals
val n1 = r"1/3"
val n2 = r"1599/115866" // simplified at compile-time to 13/942

// support different radix literals
import spire.syntax.literals.radix._

// representations of the number 23
val a = x2"10111" // binary
val b = x8"27" // octal
val c = x16"17" // hex

// SI notation for large numbers
import spire.syntax.literals.si._ // .us and .eu also available

val w = i"1 944 234 123" // Int
val x = j"89 234 614 123 234 772" // Long
val y = big"123 234 435 456 567 678 234 123 112 234 345" // BigInt
val z = dec"1 234 456 789.123456789098765" // BigDecimal
```

Spire also provides a loop macro called `cfor` whose syntax bears a slight
resemblance to a traditional for-loop from C or Java. This macro expands to a
tail-recursive function, which will inline literal function arguments.

The macro can be nested in itself and compares favorably with other looping
constructs in Scala such as `for` and `while`:

```tut

// print numbers 1 through 10
cfor(0)(_ < 10, _ + 1) { i =>
  println(i)
}

// naive sorting algorithm
def selectionSort(ns: Array[Int]) {
  val limit = ns.length -1
  cfor(0)(_ < limit, _ + 1) { i =>
    var k = i
    val n = ns(i)
    cfor(i + 1)(_ <= limit, _ + 1) { j =>
      if (ns(j) < ns(k)) k = j
    }
    ns(i) = ns(k)
    ns(k) = n
  }
}
```

### Sorting, Selection, and Searching

Since Spire provides a specialized ordering type class, it makes sense
that it also provides its own methods for doing operations based on
order. These methods are defined on arrays and occur in-place,
mutating the array. Other collections can take advantage of sorting by
converting to an array, sorting, and converting back (which is what
the Scala collections framework already does in most cases). Thus,
Spire supports both mutable arrays and immutable collections.

Sorting methods can be found in the `spire.math.Sorting` object. They are:

 * `quickSort` fastest, nlog(n), not stable with potential n^2 worst-case
 * `mergeSort` also fast, nlog(n), stable but allocates extra temporary space
 * `insertionSort` n^2 but stable and fast for small arrays
 * `sort` alias for `quickSort`

Both `mergeSort` and `quickSort` delegate to `insertionSort` when dealing with
arrays (or slices) below a certain length. So, it would be more accurate to
describe them as hybrid sorts.

Selection methods can be found in an analogous `spire.math.Selection` object.
Given an array and an index `k` these methods put the _kth_ largest element at
position `k`, ensuring that all preceding elements are less-than or equal-to,
and all succeeding elements are greater-than or equal-to, the _kth_ element.

There are two methods defined:

 * `quickSelect` usually faster, not stable, potentially bad worst-case
 * `linearSelect` usually slower, but with guaranteed linear complexity
 * `select` alias for `quickSelect`

Searching methods are located in the `spire.math.Searching`
object. Given a sorted array (or indexed sequence), these methods
will locate the index of the desired element (or return -1 if it is
not found).

 * `search(array, item)` finds the index of `item` in `array`
 * `search(array, item, lower, upper)` only searches between `lower` and `upper`.

Searching also supports a more esoteric method:
`minimalElements`. This method returns the minimal elements of a
partially-ordered set.

### Pseudo-Random Number Generators

Spire comes with many different PRNG implementations, which extends
the `spire.random.Generator` interface. Generators are mutable RNGs
that support basic operations like `nextInt`. Unlike Java, generators
are not threadsafe by default; synchronous instances can be attained
by calling the `.sync` method.

Spire supports generating random instances of arbitrary types using
the `spire.random.Dist[A]` type class. These instances represent a
strategy for getting random values using a `Generator` instance. For
instance:

```tut
import spire.random.Dist

val rng = spire.random.rng.Cmwc5()

// produces a double in [0.0, 1.0)
val n = rng.next[Double]

// produces a complex number, with real and imaginary parts in [0.0, 1.0)
val c = rng.next[Complex[Double]]

// produces a map with ~10-20 entries
implicit val nextmap = Dist.map[Int, Complex[Double]](10, 20)
val m = rng.next[Map[Int, Complex[Double]]]
```

Unlike generators, `Dist[A]` instances are immutable and composable,
supporting operations like `map`, `flatMap`, and `filter`. Many default
instances are provided, and it's easy to create custom instances for
user-defined types.

### Miscellany

In addition, Spire provides many other methods which are "missing" from
`java.Math` (and `scala.math`), such as:

 * `log(BigDecimal): BigDecimal`
 * `exp(BigDecimal): BigDecimal`
 * `pow(BigDecimal): BigDecimal`
 * `pow(Long): Long`
 * `gcd(Long, Long): Long`
 * *and so on...*

### Benchmarks

In addition to unit tests, Spire comes with a relatively fleshed-out set of
[JMH](http://openjdk.java.net/projects/code-tools/jmh) micro-benchmarks. To
run the benchmarks from within SBT, change to the `benchmark` subproject 
and then `jmh:run -l` to see a list of benchmarks:

```
$ sbt
> project benchmark
> jmh:run -l

[info] Benchmarks:
[info] spire.benchmark.AddBenchmarks.addComplexDoubleStateDirect
[info] spire.benchmark.AddBenchmarks.addComplexDoubleStateGeneric
[info] spire.benchmark.AddBenchmarks.addComplexFloatStateDirect
...
```

To run all available benchmarks:
```
> jmh:run
```

To run a specific benchmark method:
```
> jmh:run spire.benchmark.AddBenchmarks.addComplexDoubleStateDirect
```

To run all benchmarks in a specific class:
```
> jmh:run spire.benchmark.AddBenchmarks
```

To see all available JMH usage options:
```
> jmh:run -h
```

If you plan to contribute to Spire, please make sure to run the relevant
benchmarks to be sure that your changes don't impact performance. Benchmarks
usually include comparisons against equivalent Scala or Java classes to try to
measure relative as well as absolute performance.

### Caveats

Code is offered as-is, with no implied warranty of any kind. Comments,
criticisms, and/or praise are welcome, especially from numerical analysts! ;)

Copyright 2011-2017 Erik Osheim, Tom Switzer

A full list of contributors can be found in [AUTHORS.md](AUTHORS.md).

The MIT software license is attached in the [COPYING](COPYING) file.
