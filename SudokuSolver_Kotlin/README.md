# Kotlin Implementation

Kotlin was the first language that I chose to implement the solver in. As an experienced Java developer, I had heard
about Kotlin and was intrigued to try it. Kotlin has risen in popularity among the sea of alternative JVM languages and
for good reason. In particular, I wanted to explore Kotlin's approach to null safety as this addresses one of Java's
major pain points. I had also heard that Kotlin is more concise than Java, which isn't that difficult to achieve given
Java's verbosity.

## Development Setup

Follow these steps to setup a development environment:

1. Clone this repo by running `git clone https://github.com/joeseibel/sudoku-solver.git`.
2. Download and install [IntelliJ](https://www.jetbrains.com/idea/).
3. Launch IntelliJ, choose to open a project, and select the `sudoku-solver/SudokuSolver_Kotlin` directory.
4. If you need to configure IntelliJ with the appropriate JDK, then open one of the Kotlin files such as
   `SudokuSolver_Kotlin/src/main/sudokusolver.kotlin/SudokuSolver.kt` and click the link at the top of the file for
   downloading the appropriate JDK.

### Running the solver

Follow these steps to create a run configuration for running the solver:

1. In the project tool window, right-click on the file
   `SudokuSolver_Kotlin/src/main/sudokusolver.kotlin/SudokuSolver.kt`.
2. Select **Run 'SudokuSolverKt'**. It is expected that the solver will complain that a board has not been specified.
3. Edit the **SudokuSolverKt** run configuration.
4. In the **Program arguments** field, paste in the board to solve as a sequence of 81 digits, e.g.,
   `0100405602306150800008001000500200086007810059000600200060080000804730560450900101`.
5. Click **Run**.

### Running the unit tests

Follow these steps to run the unit tests:

1. In the project tool window, right-click on the project `SudokuSolver_Kotlin`.
2. Select **Run 'All Tests'**.

## My experience with Kotlin

I love programming in Kotlin. It could very well be my favorite language, although Rust is a close second. It was very
easy to learn Kotlin given my Java background. All of Kotlin's core programming concepts are the same or very similar to
Java's core concepts, but with syntax that is more concise and in my opinion, more elegant. Don't get me wrong, I don't
hate Java, but it seems like Kotlin was designed with a deep knowledge of Java's rough edges in mind.

Kotlin is not the first alternative JVM language that I have learned. I was very experienced with
[Xtend](https://eclipse.dev/Xtext/xtend/) and a little experienced with Scala before coming to Kotlin. Similar to
Kotlin, Xtend branded itself as "a better Java with less 'noise'". Both Kotlin and Xtend offered lambdas and a
functional style of programming before the Java 8 streams were released. They both excel in Java interoperability and
have the same core Java concepts with a more concise syntax. However, I think that Kotlin is the better Java
alternative. Kotlin's null safety is a huge advantage and Xtend does not have that advantage. I also find Kotlin's
version of extension methods to be much more readable and predictable than Xtend's version of extension methods. In
Xtend, it is easy to abuse extension methods and have no idea which method is actually being called, at least not
without IDE support.

Comparing Kotlin and Scala is a little more interesting. I think that Scala excels when a programmer wants to embrace a
purely functional style. In my experience, this is primarily due to Scala's custom immutable data structures that are
tailored to a functional style. In Kotlin, it is possible to embrace a mostly, but not purely, functional style. One
place where Kotlin shines compared to Scala is in the area of Java interoperability. While it is possible to call Java
from Scala, the Java interop in Kotlin feels much more seamless. In Kotlin, it is very easy to work with collections
that are passed back and forth to Java code. The native Kotlin collections are really aliases to Java's collections, so
the interop is very smooth. Because of these points, working with collections can be a very different experience between
Kotlin and Scala.

When I started working on the Kotlin implementation, Java was back at version 11. At that point in history, Kotlin had
so many advantages over Java such as data classes, destructuring, smart-casting, when expressions, etc. Since that time,
Java has come a long way. They have added records, switch expressions, pattern matching, sealed types, and much more.
Java still has issues with null safety and it is still more verbose than Kotlin, but I have to admit that Kotlin's
advantages over Java have been diminishing as Java advances.

### Null Safety

Kotlin's approach to [null safety](https://kotlinlang.org/docs/null-safety.html) is, in my opinion, the best thing about
the language. I absolutely love this feature! This addresses one of the most significant frustrations when working in
Java.

When working with a Java API, it can be difficult to determine when a reference is expected to support null or not.
Sometimes this is documented and sometimes it is not. Sometimes a library author will use nullability annotations and
sometimes not. Java 8's `Optional` class is a great help, but there is so much legacy Java code that doesn't make use of
it. This can lead to random `NullPointerException`s when you least expect them. On the other hand, paranoid programmers
can put in unnecessary null checks which only add to the confusion of whether a reference supports null or not. In
addition to the challenges of working with a Java library, it can also be difficult to keep track of which references
can and cannot be null when multiple developers are working together on a large codebase.

Kotlin addresses most, but not all, of these issues with their approach to null safety. In Kotlin, a value that can be
null will have its type appended with a `?`. For example, the type `String` cannot be null while the type `String?` can
be null. When working with pure Kotlin code, it is impossible to encounter a `NullPointerException`, unless if the
programmer uses the [`!!` operator](https://kotlinlang.org/docs/null-safety.html#not-null-assertion-operator). The
compiler will also warn against unnecessary null checks. Having the compiler keep track of the nullability of values and
complain when there are violations is such a breath of fresh air, at least coming from the Java world.

Working with primitives is also abstracted away and very seamless. In Kotlin, a nullable 32-bit integer is represented
by the type `Int?`, while its non-null version is represented as `Int`. Under the hood, Kotlin will use Java's primitive
`int` type for the non-null version since Java primitives are not references and can never be null. On the other hand,
Kotlin will use Java's `java.lang.Integer` wrapper class for nullable 32-bit integers. Kotlin will also use the
`Integer` wrapper in cases where a reference type is required such as `List<Int>`. This means that the Kotlin types
`Int` and `Int?` can be backed by two different Java types: `int` and `Integer`. All of this is abstracted away in
Kotlin, whereas Java developers need to concerns themselves with this distinction.

The one place where Kotlin's null safety gets a bit tricky is when
[calling Java code from Kotlin](https://kotlinlang.org/docs/java-interop.html#null-safety-and-platform-types). If you
are calling Java code that has not been annotated with nullability annotations, then the Kotlin compiler cannot
determine if the references can or cannot be null. Kotlin addresses this by having these uncertain types be appended
with a `!`. For example, calling a Java method which returns a `String` will result in the Kotlin type `String!`. This
is treated as a potentially null type. This means that null checks are not required, but it also means that null checks
are not warned against. In this regard, working with a `String!` in Kotlin is just like working with a `String` in Java.
Note that it is not possible write the type `String!` in Kotlin, it is only available as an inferred type.

In the solver, I encounter this issue when using JGraphT, since it is a Java library. For example, the logical solution
[Simple Coloring](src/main/sudokusolver/kotlin/logic/tough/SimpleColoring.kt) makes use of the graph type
`Graph<UnsolvedCell, DefaultEdge>`. Calling the method `vertexSet()` on a graph will yield the type
`(Mutable)Set<UnsolvedCell!>!`. This type has three different uncertainties: the returned set may or may not be null,
the returned set may or may not be mutable, and the elements of the set may or may not be null. In my case, I know that
the set and its elements cannot be null and I only read from the set, so dealing with these ambiguities is not a problem
for me.

Kotlin has some nice syntactic sugar to deal with nullable types such as the
[`?.` operator](https://kotlinlang.org/docs/null-safety.html#safe-call-operator) and the
[`?:` operator](https://kotlinlang.org/docs/null-safety.html#elvis-operator). These are worth looking into.

Kotlin's approach to dealing with optional values is a bit unique. Other languages such as Scala, Swift, and Rust have
an `Option(al)` type that is a part of their standard libraries. Kotlin has no `Optional` type. The syntax and the
compiler handle the distinction between nullable and non-null types. While Kotlin's approach may look syntactically
similar to Swift's approach, they are actually very different. In Swift, the `?` is simply syntactic sugar for the
`Optional` type. For example, writing `String?` in Swift is shorthand for writing `Optional<String>`.
[This article](https://elizarov.medium.com/dealing-with-absence-of-value-307b80534903) by Roman Elizarov, one of the key
people behind Kotlin, explains the uniqueness of Kotlin's approach.

Finally, Kotlin's null safety shines especially when compared with Xtend. While Xtend has some syntactic sugar for
dealing with nulls such as the `?.` and `?:` operators, the Xtend compiler does not perform the kind of null safety
checks that Kotlin performs. In fact, Xtend will even implicitly insert nulls in certain situations. For example, an
[if expression](https://eclipse.dev/Xtext/xtend/documentation/203_xtend_expressions.html#if-expression) in Xtend that
does not have an else clause will yield a null if the condition is false. I personally prefer Kotlin's approach that
requires the programmer to explicitly specify both nullable types and null values.

### Sealed Types

Kotlin's [sealed classes and interfaces](https://kotlinlang.org/docs/sealed-classes.html) are amazing! I love this
feature in Kotlin and they solve a specific problem that I've encountered in Java numerous times.

Suppose that you are working with a type hierarchy which has a limited and known set of concrete classes that inherit
from the same interface or abstract class. Additionally, suppose that you want to perform an action on an object of the
hierarchy based upon that object's concrete type and it is either not possible or not desirable to add a method to the
hierarchy. Before Java 21, this would require a chain of if-else statements with instanceof checks. The following
example is a snippet from OSATE's
[AADL to SysML Translator](https://github.com/osate/aadl-sysmlv2/blob/ad33c6e53ab5e8a88ee53aeb78aded544ccb6093/aadl2sysml/org.osate.aadl2sysml/src/org/osate/aadl2sysml/Aadl2SysmlTranslator.java#L245-L264):

```java
if (feature instanceof AbstractFeature) {
    template.add("kind", "AbstractFeature");
} else if (feature instanceof DataPort) {
    template.add("kind", "DataPort");
} else if (feature instanceof EventDataPort) {
    template.add("kind", "EventDataPort");
} else if (feature instanceof EventPort) {
    template.add("kind", "EventPort");
} else if (feature instanceof DataAccess) {
    template.add("kind", "DataAccess");
} else if (feature instanceof BusAccess busAccess) {
    template.add("kind", busAccess.isVirtual() ? "VirtualBusAccess" : "BusAccess");
} else if (feature instanceof SubprogramAccess) {
    template.add("kind", "SubprogramAccess");
} else if (feature instanceof SubprogramGroupAccess) {
    template.add("kind", "SubprogramGroupAccess");
} else {
    throw new AssertionError("Unexpected class: " + feature.getClass());
}
```

The problem with the above example is that the compiler cannot check that all options for the variable `feature` are
handled. The best that can be done is to detect this issue at runtime by throwing an exception in the last else branch.
This can become especially challenging if another type is added to the hierarchy at some later date. Suppose that we add
`Parameter` as another kind of feature and then forget to update the above snippet. Hopefully there are extensive unit
tests that would catch this, but there is always a chance that the above code goes into production unchanged.

Kotlin's sealed types solve this specific problem. With sealed types, the entire type hierarchy is known at compile time
and when combined with Kotlin's when expression or statement, then the compiler can ensure that each case is handled.
Suppose that the `Feature` hierarchy is sealed, then the above if-else chain would become the following when statement
in Kotlin:

```kotlin
when (feature) {
    is AbstractFeature -> template.add("kind", "AbstractFeature")
    is DataPort -> template.add("kind", "DataPort")
    is EventDataPort -> template.add("kind", "EventDataPort")
    is EventPort -> template.add("kind", "EventPort")
    is DataAccess -> template.add("kind", "DataAccess")
    is BusAccess if feature.virtual -> template.add("kind", "VirtualBusAccess")
    is BusAccess -> template.add("kind", "BusAccess")
    is SubprogramAccess -> template.add("kind", "SubprogramAccess")
    is SubprogramGroupAccess -> template.add("kind", "SubprogramGroupAccess")
}
```

First of all, Kotlin's when statement is much more concise than the if-else chain from above. More importantly, there is
no need for an else branch that throws an exception. The compiler is able to determine that the when statement is
exhaustive and all cases are handled. If we were to now add a `Parameter` type to the hierarchy, then the compiler would
complain with the following error message:

```txt
'when' expression must be exhaustive. Add the 'is Parameter' branch or an 'else' branch
```

Sealed types are actually one of the advances that Java has made in recent years. They have added
[sealed types](https://openjdk.org/jeps/409) in Java 17 and then added [pattern matching](https://openjdk.org/jeps/441)
to switch expressions and statements in Java 21. Java's syntax for sealed types is still more verbose than Kotlin's
syntax, but the two approaches work the same. Sealed types are a welcome addition to Java and I am very happy about it.
Scala also has sealed types which work the same way. I suspect that Scala's sealed types inspired Kotlin's sealed types
which then inspired Java's sealed types.

Sealed types are Kotlin's approach to [tagged unions](https://en.wikipedia.org/wiki/Tagged_union), sometimes also called
[algebraic data types](https://en.wikipedia.org/wiki/Algebraic_data_type). Another approach is to use enumerations with
associated values such as in
[Swift](https://docs.swift.org/swift-book/documentation/the-swift-programming-language/enumerations/#Associated-Values)
and [Rust](https://doc.rust-lang.org/book/ch06-01-defining-an-enum.html). While the enumeration approach does what it
needs to do by ensuring exhaustiveness, I much prefer the sealed type approach.

One key advantage that sealed types have over enumerations with associated values is that in a sealed hierarchy, each
class in the hierarchy is a unique type and can be used as the type of a variable, function parameter, generic type
parameter, etc. Unfortunately, the variants of enumerations with associated values in Swift and Rust are not themselves
types. Any time you want to pass a variant around, you have to give it the type of the whole enumeration and lose the
knowledge that it is a specific variant.

I ran into this issue in the solver with my `Cell` type. In Kotlin, Java, and Scala, `Cell` is a sealed type with the
classes `SolvedCell` and `UnsolvedCell`. In Kotlin, it is so easy to filter a `List<Cell>` by a specific type and get a
`List<UnsolvedCell>`. I do this a lot in Kotlin. Unfortunately in Swift and Rust, I can't have a collection of the
unsolved cell enumeration variant. I instead define the structs `SolvedCell` and `UnsolvedCell` and then define the two
enumeration variants which are simply wrappers for the structs. This feels overly verbose and unnecessary. I ranted
about this in the comments on the Swift implementation of [`Cell`](../SudokuSolver_Swift/SudokuSolver_Swift/Cell.swift).

There is another advantage that sealed types have over enumerations with associated values: it is possible for an
interface or class to be a part of multiple sealed hierarchies. Here is a simple example taken from
[AADL's meta-model](https://github.com/osate/osate2/blob/master/core/org.osate.aadl2/model/aadl2.ecore) in which I
recreate a simplified version of the type hierarchies for the types `DirectedFeature` and `TriggerPort`:

```kotlin
sealed interface DirectedFeature
interface FeatureGroup : DirectedFeature
interface Parameter : DirectedFeature

sealed interface TriggerPort
interface InternalFeature : TriggerPort
interface PortProxy : TriggerPort

interface AbstractFeature : DirectedFeature, TriggerPort
interface Port : DirectedFeature, TriggerPort
```

In this example, the interfaces `AbstractFeature` and `Port` are a part of both the hierarchies of `DirectedFeature` and
`TriggerPort`. I don't make use of this in the solver, but this kind of pattern shows up a lot in AADL's meta-model.

### Immutable Collections (kind of)

When working with collections, it can be helpful to constrain which collections can be mutated and which cannot. This
becomes especially useful when different parts of a codebase share multiple references to the same collection. Placing
restrictions on which collections can be mutated and when they can be mutated helps developers reason about the
integrity of their collections as they are passed around the codebase.

[Kotlin's collections](https://kotlinlang.org/docs/collections-overview.html) address this by expressing mutability
intent through their own custom collection interfaces. For example, when working with lists, Kotlin has the interfaces
`List` and `MutableList`. Operations such as adding items to a list or removing items from a list are only defined for
the `MutableList` interface and not for the `List` interface. Similar interfaces exist for sets and maps.

Kotlin's approach is an improvement over Java's approach. In Java, the `List` interface has methods for all operations.
Expressing mutability intent is not possible using Java's statically known collection types. Instead, Java depends upon
restricting mutation through the use of runtime objects that throw an exception when mutation is attempted. For example,
the methods `List.of()`, `List.copyOf()`, `Collections.unmodifiableList()`, and `Stream.toList()` all return a `List`,
but the concrete type will throw an exception when calling a mutating method on that list. One of the problems with
Java's approach is that when working with a `List` object that came from someone else's code, there is no way to
determine its mutability at compile time. Lists that are passed across a library boundary are frequently copied either
to prevent mutation or to ensure mutability without risking a exception. Kotlin's addition of their own `List` and
`MutableList` interfaces are a very welcome change.

Kotlin's collection interfaces map to Java interfaces and the implementations themselves are often, but not always, a
concrete Java collection. For example, a Kotlin `MutableList` is often implemented as a Java `ArrayList`. This helps
interoperability with Java code immensely. For example, if you are calling a Java method that requires a
`java.util.List` to be passed as an argument, then a `kotlin.collections.List` or a `kotlin.collections.MutableList` can
be passed in as is without any conversions. On the other hand, if you are calling a Java method that returns a
`java.util.List`, then this will show up on the Kotlin side as a `kotlin.collections.(Mutable)List`. Kotlin is unable to
tell if the list is mutable or not, so the resulting list can be used either as a Kotlin `List` or a `MutableList`.

I like Kotlin collections—I really do. However, I have to admit that they are easy to break. Since `List` and
`MutableList` are simply interfaces and `MutableList` extends from `List`, it is easy to cast a `List` to a
`MutableList` and wreck havoc on your seemingly immutable data structures. Consider this simple example:

```kotlin
val immutableList = listOf("a", "b", "c")
(immutableList as MutableList)[0] = "Oh no, I mutated the immutable collection!!!"
println(immutableList)
```

Running this code will produce the following output:

```text
[Oh no, I mutated the immutable collection!!!, b, c]
```

How did this happen? Under the hood, the Kotlin function `listOf()` calls the Java method `Arrays.asList()`. This Java
method returns a semi-mutable list: it supports changing a value at a given index, but not changing the size of the
list. Due to the underlying type, the cast to `MutableList` is successful, Java's `List.set()` method is called, and the
mutation is performed without exception. This is probably the simplest example of breaking Kotlin collections and one
that is easily avoided. Moral of the story: don't cast an immutable collection to a mutable one.

It is also possible to break Kotlin's collections even without casting. Consider the following class that holds onto a
reference to a `List`:

```kotlin
class Holder(private val values: List<String>) {
    fun printValues() = println(values)
}
```

The type of the `values` property suggests that it cannot be mutated and that is true within the context of the class
`Holder`. However, it is possible to pass a `MutableList` to the constructor of `Holder` and then later mutate that
list:

```kotlin
val list = mutableListOf("a", "b", "c")
val holder = Holder(list)
list += "Extra Item!!!"
holder.printValues()
```

The `values` property was indeed mutated as shown in this output:

```text
[a, b, c, Extra Item!!!]
```

This problem can be fixed by copying the list before assigning it to `values`:

```kotlin
class Holder(values: List<String>) {
    private val values: List<String> = values.toList()

    fun printValues() = println(values)
}
```

As a result of this change, the contents of `values` won't be mutated even if the list that was passed to the
constructor is later mutated.

All of these issues lead to the following consideration for library authors: when writing a public Kotlin class that
holds a reference to a collection and there is an expectation that the collection is either immutable or mutation is
confined to the class, then care must be taken to ensure that the collection cannot be mutated by users of that class
that also hold onto a reference to that collection. When a collection is passed into the class, the collection should be
copied before being stored. When a collection is passed out of a method of the class, the collection can either be
copied or wrapped in an unmodifiable view. This issue and its solutions are the same as those faced by Java library
authors.

Kotlin's approach to collection integrity and mutability is different from other languages that I have explored.
Kotlin's immutability constraints can be easily broken, but the tradeoff is that Kotlin collections are fully
interoperable with Java collections. This makes passing collections to and from Java code easy and seamless. In
contrast, Scala has their own set of immutable and mutable collections. Scala's collections do not suffer from the same
problem as Kotlin's collections. An immutable collection in Scala is both immutable in its compile-time type and its
run-time object. The downside to Scala's collections is that they are not fully interoperable with Java collections.
When writing Scala code that interfaces with Java, it is customary to convert between the Scala collections and the Java
collections.

Swift and Rust both have wildly different and intriguing solutions to this problem. Swift collections utilize a
copy-on-write approach which handles all of the necessary copying automatically behind the scenes. Developers are
encouraged to think of collections as value types and to pass them around as freely as one would pass an integer around.
As long as a collection is only read from, then there could be numerous references to the same collection with no
copying at all. As soon as mutation is attempted on a shared collection, then the collection is copied at that point.
This is a very efficient approach that also hides the details of copying from the developer.

In Rust, this issue is addressed with ownership and reference borrowing. When a piece of Rust code holds a reference to
a collection, whether that reference is mutable or immutable, it is guaranteed that no other code can mutate that
collection while the reference is valid. This is great and it also prevents unnecessary copying, it just depends on
explicitly marking reference mutability as opposed to Swift's automatic handling of copying. The Rust approach perfectly
preserves collection integrity and makes it very easy to reason about exactly what code can mutate which collections.

With all of these considerations in mind, I think that the creators of Kotlin made the right decision. One of the keys
to Kotlin's success and ease of adoption is its interoperability with Java. I would go so far as to say that Kotlin can
be a drop-in replacement for Java. It is so easy to mix Java and Kotlin code in the same codebase and one can convert
their Java code to Kotlin on a file-by-file basis. One of the features that makes this interaction between the languages
so easy is the way that Kotlin collections were designed. Even with all of the problems that I've outlined here, I think
that the tradeoff is worth it!

### Data Classes

One common complaint against Java is that there is too much boilerplate for seemingly simple operations or data
structures. This was especially true when creating a simple class in Java which would also have basic implementations of
the methods `equals()`, `hashCode()`, and `toString()`. Kotlin solves this issue with
[data classes](https://kotlinlang.org/docs/data-classes.html). A data class is a class which provides good default
implementations of `equals()`, `hashCode()`, and `toString()`, as well as supporting destructuring. Ultimately, Kotlin's
data classes are just syntactic sugar, but they are very nice syntactic sugar.

Java has solved this issue as well when they added [records](https://openjdk.org/jeps/395) to the language. Java's
records were finalized in Java 16. Just like with many other language features, they showed up in Kotlin and/or Scala
first, and then found their way into Java.

To see the value of data classes, let us consider an example data type that would represent a person with fields for
that person's name and age. In Java, before the days of records, it would be customary to write a class like this:

```java
public final class Person {
    private final String name;
    private final int age;

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Person other && Objects.equals(name, other.name) && age == other.age;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, age);
    }

    @Override
    public String toString() {
        return "Person{" + "name='" + name + "', age=" + age + '}';
    }
}
```

This is a lot of code for a simple data type. Now let's try using a Kotlin data class to represent a person:

```kotlin
data class Person(val name: String, val age: Int)
```

Wow! That is quite the reduction, from 32 lines down to 1! It is easy to see why Java developers like me love Kotlin's
data classes.

Another advantage of Kotlin's data classes is that it supports destructuring. Kotlin doesn't have tuples, but data
classes are able to solve the problem that tuples solve. Here is a simple example of destructuring a person:

```kotlin
val (name, age) = person
```

Destructuring can be used in other places as well such as in lambda parameters. Here is an example of destructuring in a
lambda and ignoring one of the fields:

```kotlin
val adults = people.filter { (_, age) -> age >= 18 }
```

### Extensions

Kotlin has the ability to add methods and properties to a type from outside of that type's declaration through the use
of [extensions](https://kotlinlang.org/docs/extensions.html). I have found this to be useful in two situations: adding
members to a type that I do not control and adding members to a type with specific generic constraints. One example of
creating an extension method to a type that I do control is adding the `toSimpleString()` method to the `Board` type. I
want this method to only be available to the `Board<Cell>` type, but not to other `Board<T>` types. I can accomplish
this with an extension:

```kotlin
fun Board<Cell>.toSimpleString(): String = cells.joinToString("")
```

At the end of the day, Kotlin extensions are simply syntactic sugar. It is possible to not use any extensions and to
instead use regular functions in which the extension's receiver is instead passed as an argument. The syntactic
advantage of extensions is that they can help readability when used in a chain of function calls and the extension's
purpose is intuitive. A good example of this is my extension method `List<T>.zipEveryPair()` which allows callers to
look at and compare every combination of pairs in a list. The following snippet from the Naked Pairs solution
demonstrates calling `zipEveryPair()` in the midst of a call chain:

```kotlin
unit.filterIsInstance<UnsolvedCell>()
    .filter { it.candidates.size == 2 }
    .zipEveryPair()
    .filter { (a, b) -> a.candidates == b.candidates }
    .flatMap { (a, b) ->
        unit.filterIsInstance<UnsolvedCell>()
            .filter { it != a && it != b }
            .flatMap { cell ->
                enumIntersect(cell.candidates, a.candidates).map { candidate -> cell to candidate }
            }
    }
```

If `zipEveryPair()` had been a regular function instead of an extension function, then the above snippet would instead
look like this:

```kotlin
zipEveryPair(unit.filterIsInstance<UnsolvedCell>().filter { it.candidates.size == 2})
    .filter { (a, b) -> a.candidates == b.candidates }
    .flatMap { (a, b) ->
        unit.filterIsInstance<UnsolvedCell>()
            .filter { it != a && it != b }
            .flatMap { cell ->
                enumIntersect(cell.candidates, a.candidates).map { candidate -> cell to candidate }
            }
    }
```

I think this is a bit harder to read and that in this case, an extension function helps readability by keeping the
logical flow of the call chain sequential. With the extension function, each line of the call chain indicates a single
filtering or transformation step and the steps are presented in order. With a normal function, the flow is broken up and
harder to read.

I will say with great emphasis that extensions can be easily abused to the point that readability is hindered and not
helped. If every function becomes an extension function, then it can be challenging to understand what is going on and
intuition is lost. In my opinion, it can take a little practice to see what counts as a good extension and what is
better served as a regular function. In general, I make a function an extension if I would normally want to make it a
member of that type. Otherwise, I will use a regular function. As an example, the logic functions such as
`nakedSingles()`, `hiddenSingles()`, etc., should not be extension functions.

I first encountered extensions when I was learning Xtend. I much prefer Kotlin's extensions to Xtend's
[extension methods](https://eclipse.dev/Xtext/xtend/documentation/202_xtend_classes_members.html#extension-methods). The
key difference between Kotlin and Xtend is that Kotlin's extensions are clearly indicated to be extensions at the point
that they are declared. They can only be called as extensions and not as regular functions. In contrast, Xtend
effectively allows any method to be used as an extension method. It is even possible to annotation a field or even a
local variable as an
[extension provider](https://eclipse.dev/Xtext/xtend/documentation/202_xtend_classes_members.html#extension-provider).
While this does reduce code size, it can make it very difficult to understand what is going on or even which variables
are involved when looking at an extension call in Xtend. I am personally guilty of taking Xtend's extension methods way
too far and absolutely destroying code readability. Due to these lessons learned, I think that Kotlin's approach is so
much better.

It is worth comparing Kotlin's extensions to Swift's
[extensions](https://docs.swift.org/swift-book/documentation/the-swift-programming-language/extensions/). Just like
Kotlin, Swift extensions can be used to add members to an existing type. Unlike Kotlin, Swift extensions also have the
ability to add protocol conformance to an existing type. In fact, this is one of the main points of Swift's extensions.
Kotlin does not have this ability. It is not possible to add an interface implementation to an existing type in Kotlin.
While this would be nice in certain situations, I haven't found this limitation to be a great hindrance.

### Properties

One key difference between writing types in Java and writing types in Kotlin is utilizing Kotlin's
[properties](https://kotlinlang.org/docs/properties.html). Properties in Kotlin sometimes act like a field in Java and
sometimes act like a method in Java. They can be used for storing data, but there can also be code associated with a
property to control the retrieval and modification of that data. To demonstrate how properties are frequently used in
Kotlin, let's consider a stripped-down mutable `Fraction` type and see how it would be implemented in Java with fields
and methods and compare that with how it would be implemented in Kotlin with properties. Here is the Java
implementation:

```java
public class Fraction {
    private int numerator;
    private int denominator;

    public Fraction(int numerator, int denominator) {
        this.numerator = numerator;
        setDenominator(denominator);
    }

    public int getNumerator() {
        return numerator;
    }

    public void setNumerator(int numerator) {
        this.numerator = numerator;
    }

    public int getDenominator() {
        return denominator;
    }

    public void setDenominator(int denominator) {
        if (denominator == 0) {
            throw new IllegalArgumentException("denominator cannot be 0");
        }
        this.denominator = denominator;
    }

    public double getDoubleValue() {
        return (double) numerator / denominator;
    }
}
```

Now compare this to the Kotlin implementation which utilizes properties:

```kotlin
class Fraction(var numerator: Int, denominator: Int) {
    var denominator: Int = denominator
        set(value) {
            require(value != 0) { "denominator cannot be 0" }
            field = value
        }

    init {
        this.denominator = denominator
    }

    val doubleValue: Double
        get() = numerator.toDouble() / denominator
}
```

As you can see, this is much less code. Numerous fields and methods have been condensed into properties:

- The field `numerator` and the methods `getNumerator()` and `setNumerator()` have become the single property
  `numerator`.
- The field `denominator` and the methods `getDenominator()` and `setDenominator()` have become the single property
  `denominator`.
- The method `getDoubleValue()` has become the read-only property `doubleValue` and can be accessed with property syntax
  instead of method syntax.

I find properties to be very pleasant to work with. However, they do blur the distinction between data and code. In
Java, this is a very clear distinction. If you are accessing a field in Java, you know that you are directly
manipulating data and if you are calling a method, you know that you are invoking code. The presence or absence of
parentheses makes this very clear. In Kotlin, accessing a property isn't that clear. It might be a simple read or write,
but it might be invoking code. I think this is fine as long as the code of a property is simple and short-running. If a
property is a long-running task or even blocking, then this might break expectations of what a property does. In my
opinion, computationally intensive tasks are best left to methods and not properties.

### Reified Generics

One of my favorite features in Java is generics. I vividly remember when they were introduced in Java 5 and there was
much rejoicing. Java's decision to use
[type erasure](https://docs.oracle.com/javase/tutorial/java/generics/erasure.html) allowed them to add generics without
breaking existing code. Even though I love Java generics, I will admit that it is sometime useful to be able to access a
generic type at runtime, such as getting a type's `Class` object or using that type in an `instanceof` check. This is
not possible in Java due to type erasure.

Kotlin solves this problem with
[reified type parameters](https://kotlinlang.org/docs/inline-functions.html#reified-type-parameters). This is an option
for [inlined functions](https://kotlinlang.org/docs/inline-functions.html) which allows the generic type parameter's
type information to be accessed at runtime. If a generic type `T` is reified, then it can be used with the `is`
operator, the `as` operator, and you can even get `T`'s `Class` object.

One good example of the use of a reified type parameter is in Kotlin's standard library function `filterIsInstance()`.
This is used to filter a collection by a specific type, which is supplied as a type parameter. I use this function all
over the place. In particular, if I have a `List<Cell>` and I want to filter by which ones are unsolved, then I will
call `filterIsInstance<UnsolvedCell>()` to get a `List<UnsolvedCell>`.

Another good example of a reified type parameter is in my function `enumUnion()`. In that function, the type parameter
`T` is reified because I need access to `T`'s `Class` object. If this feature was not available to me, then I would need
to both have a generic type parameter and pass in a `Class` object.

### Companion Objects

I fully embrace most of the features that Kotlin adds to the programming experience, but
[companion objects](https://kotlinlang.org/docs/object-declarations.html#companion-objects) aren't one of them. I just
don't get them. I understand what they do and how they operate, but I don't really understand the "why" behind them.
They seem to take the place of Java's static methods, but I don't understand why it is valuable to have an object in
memory that you can pass around. Java's static methods don't have an object taking up space in memory, but Kotlin's
companion objects do. I understand the argument that static methods break the object-oriented paradigm, but I feel that
most static methods can be replaced with top-level functions.

I do have one companion object in my solver. It is in the class `AbstractBoard` and it serves the purpose of providing a
a method that would be a protected static method in Java. The method is called `requireSize()` and it is used to
validate the arguments passed to the constructors of both `Board` and `MutableBoard`. I have this method in a protected
companion object just to limit its scope to the type hierarchy of `AbstractBoard`. However, I could easily replace it
with a private top-level function and everything would be fine.

Perhaps some day, someone will convince me of the value of companion objects, but that day is not today.

### Destructuring

Like many other modern languages, Kotlin has the ability to
[destructure](https://kotlinlang.org/docs/destructuring-declarations.html) objects into its component parts. Kotlin's
approach is a bit unique though. Any object in Kotlin can be destructured as long as it has the operator methods
`component1()`, `component2()`, ..., `componentN()`. In practice, it is rare to implement these methods manually. It is
much more common to use data classes which provide these methods automatically. Lists and arrays can also be
destructured, but only up to five elements.

One issue with Kotlin's destructuring is that there is no requirement to destructure all of the component parts of an
object. For example, the following code is perfectly legal:

```kotlin
val triple = Triple("A", "B", "C")
val (a, b) = triple
```

There isn't even a warning that the method `component3()` is not used! I personally don't like this. I think that the
compiler should complain when the destructure isn't complete. If one of the parts are not needed, then the programmer
can use an underscore to indicate that.

Another issue with Kotlin's approach is that care must be taken when destructuring a list or an array to ensure that the
size of the collection is not exceeded. Otherwise, an `ArrayIndexOutOfBoundsException` might be thrown at runtime. This
is different from other languages such as Rust which a refutable pattern to destructure slices.

My final issue with Kotlin's destructuring is that it cannot be nested. For example, the following will not work and is
not valid Kotlin syntax:

```kotlin
fun destructureMap(map: Map<String, Pair<Int, Int>>) {
    for ((key, (a, b)) in map) {
        println("This doesn't compile!")
    }
}
```

In order to properly destructure the map's value, a separate destructuring statement is required:

```kotlin
fun destructureMap(map: Map<String, Pair<Int, Int>>) {
    for ((key, value) in map) {
        val (a, b) = value
        println("Now it works!")
    }
}
```

### Scope Functions

One of the features of Kotlin that I use extensively are their
[scope functions](https://kotlinlang.org/docs/scope-functions.html), in particular the
[`let()` function](https://kotlinlang.org/docs/scope-functions.html#let). These functions simply take an object and pass
it to a supplied lambda. They vary on how that object is operated on and what the return value of the scope function is.
The benefit of these functions is that they create a limited scope for working with a particular object. Using scope
functions can limit the number of local variables that need to be declared in a function. I have found that I can often
condense multiple statements into a single expression when I use scope functions.
