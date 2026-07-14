# Scala Implementation

My first exposure to Scala was back in either 2014 or 2015. A colleague of mine, [@lwrage](https://github.com/lwrage),
recommended that I take an online course taught by Martin Odersky called
[Functional Programming Principles in Scala](https://www.coursera.org/learn/scala-functional-programming). Like many
other developers of my generation, I was very familiar with imperative and object-oriented programming, but not as much
with functional programming. I'm very glad that I took that course and I can say that I now have a good appreciation and
understanding of the value of immutability and pure functions when it comes to reasoning about software. While I don't
use Scala in the workplace, the principles that I have gained from Martin's course have influenced how I write software
in other languages such as Java and Python.

After finishing the solver in both Kotlin and Java, I decided that I wanted to implement the solver in Scala and to make
it purely functional. While I use a mostly functional approach in Kotlin and Java, those implementations aren't purely
functional; they contain some mutability. For this implementation, I wanted to forbid the use of `var` declarations
(only `val` declarations are permitted), only use immutable data collections, and use tail recursion instead of `while`
loops.

Also, Scala 3 had been released since the time that I took Martin Odersky's course. Version 3 is a major overhaul of the
language including supporting a new syntax, so I decided that it would be good to revisit the language and learn about
what has changed.

## sbt project compiled with Scala 3

### Usage

This is a normal sbt project. You can compile code with `sbt compile`, run it with `sbt run`, and `sbt console` will start a Scala 3 REPL.

For more information on the sbt-dotty plugin, see the
[scala3-example-project](https://github.com/scala/scala3-example-project/blob/main/README.md).
