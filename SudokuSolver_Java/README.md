# Java Implementation

Out of all of the languages that I have implemented the solver in, I am the most familiar with Java. I first started
learning Java when I was in high school, back in the days of Java 1.4, and I have spent most of my career working with
Java. The language has changed a lot since those early days. It has improved greatly over the years and it has even
picked up the pace of those improvements. Even with all of these improvements, it has kept a strong tradition of
backwards compatibility. I still feel very much at home with Java, even as I have explored other languages.

After finishing my Kotlin implementation, I wanted to implement the solver in Java so that I could explore all of the
continuing improvements to the language. At the time that I started working on this implementation, my professional Java
environment was restricted to an older Java release (I think Java 8, but maybe Java 11), so I was not able to explore
Java's latest features in the workplace. As I have been exploring new features, I have limited myself to only utilizing
fully released features. I decided to not experiment with preview or incubator features.

## Development Setup

Follow these steps to setup a development environment:

1. Clone this repo by running `git clone https://github.com/joeseibel/sudoku-solver.git`.
2. Download and install [IntelliJ](https://www.jetbrains.com/idea/).
3. Launch IntelliJ, choose to open a project, and select the `sudoku-solver/SudokuSolver_Java` directory.
4. If you need to configure IntelliJ with the appropriate JDK, then open one of the Java files such as
   `SudokuSolver_Java/src/main/sudokusolver.java/SudokuSolver.java` and click the link at the top of the file for
   downloading the appropriate JDK.

### Running the solver

Follow these steps to create a run configuration for running the solver:

1. In the project tool window, right-click on the file `SudokuSolver_Java/src/main/sudokusolver.java/SudokuSolver.Java`.
2. Select **Run 'SudokuSolver.main()'**. It is expected that the solver will complain that a board has not been
   specified.
3. Edit the **SudokuSolver** run configuration.
4. In the **Program arguments** field, paste in the board to solve as a sequence of 81 digits, e.g.,
   `010040560230615080000800100050020008600781005900060020006008000080473056045090010`.
5. Click **Run**.

### Running the unit tests

Follow these steps to run the unit tests:

1. In the project tool window, right-click on the project `SudokuSolver_Java`.
2. Select **Run 'All Tests'**.

## My experience with Java

I love Java. It is old and sometimes feels clunky, but I still love it. I have enjoyed seeing Java advance over the
years. Now that they have added switch expressions, sealed types, records, type inference, pattern matching, etc., it
feels like Java is catching up to other newer languages such as Kotlin and Scala. Java has even picked up the pace of
their improvements. It used to be that Java would release very large and infrequent updates such as Java 5 and Java 8.
They now make their updates more frequent and incremental.

Java still has its pain points. They don't handle null well and the addition of Java 8's `Optional` does nothing to
improve legacy code. `NullPointerException`s are still a common headache. Some of Java's improvements have introduced
more concise constructs, but it is still a very verbose language. Even some of the new features such as sealed types are
more verbose than the alternatives found in other languages. Also, there is no good way of expressing a collection's
mutability with the type system. It is very easy to encounter an `UnsupportedOperationException` when trying to modify
an unmodifiable collection.

Finally, I'll say that I feel very much at home with Java. It has amazing community and tool support, more so than many
of the other languages I have worked with. It may not have all the amazing features of other languages, but it is still
a solid and reliable choice. All of Java's features are well thought out and properly designed, even if they do feel a
bit clunky. It is nice to know that I never have to worry about Java's garbage collector or the Just-In-Time compiler.
The whole Java ecosystem works very well.

In the following sections, I talk about my experience working with some of Java's newer features that have been released
since Java 11. These are features that I believe have significantly improved the experience of programming in Java. Note
that the following is not a tutorial, but I instead describe my opinion of these features and what it was like to use
them.

### Records

One of the pain points of Java is the amount of boilerplate code that is required when writing a simple class that
contains a handful of immutable fields and that class needs to override `equals()`, `hashCode()`, and `toString()`. As
of Java 16, this has been much simplified with the addition of [records](https://openjdk.org/jeps/395). A Java record is
an immutable class which has its entire state defined during construction and it provides good defaults for `equals()`,
`hashCode()`, and `toString()`. Java records are very similar to Kotlin's
[data classes](https://kotlinlang.org/docs/data-classes.html).

Let us consider an example to demonstrate the value of records. The following is a class which represents a person with
fields for that person's name and age. This is how that class would be written before records:

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

Now that Java has records, the above example can be condensed down to this:

```java
public record Person(String name, int age) {
}
```

As you can see, records greatly simplify the writing of basic Java classes. They do have their limitations though. Their
fields cannot be mutable, which is different from Kotlin's data classes. They also cannot extend a class other than
`java.lang.Record` or be extended by other classes. Records cannot contain native methods either. These restrictions
ensure that records can only be used in the most simple of cases.

As of Java 21, records now support destructuring, but in limited contexts. One place where records can be destructured
is when performing an [`instanceof` check](https://openjdk.org/jeps/440):

```java
if (obj instanceof Person(var name, var age)) {
    IO.println(name + " is " + age + " years old.");
}
```

Another place where records can be destructured is in [switch statements and expressions](https://openjdk.org/jeps/441):

```java
var label = switch (obj) {
    case Person(var name, var age) -> name + " is " + age + " years old.";
    default -> obj.toString();
};
```

Java records also support nested destructuring, which is something that Kotlin does not support:

```java
if (obj instanceof Pair(String label, Pair(Integer a, Integer b))) {
    IO.println(label + ": [" + a + ", " + b + ']');
}
```

The nested destructuring can be used to perform complicated `instanceof` checks for every level of nesting. Here is an
example switch expression that performs such checks:

```java
public static String checkValue(Pair<String, ?> pair) {
    return switch (pair) {
        case Pair(var key, String value) -> "I have a String value";
        case Pair(var key, Integer value) -> "I have an Integer value";
        case Pair(var key, List<?> value) -> "I have a List<?> value";
        case Pair(var key, Person(var name, var age)) -> "I have a Person value";
        case Pair(var key, Pair(String a, String b)) -> "I have a Pair<String, String> value";
        case Pair(var key, Pair(Integer a, Integer b)) -> "I have a Pair<Integer, Integer> value";
        case Pair(var key, Pair(Pair(String a, Integer b), Pair(Person c, List<?> d))) ->
                "I have a Pair<Pair<String, Integer>, Pair<Person, List<?>>";
        case Pair(var key, var value) -> "I have an Object value";
    };
}
```

Unfortunately, Java records cannot be destructured in some places that would be very useful including simple variable
declarations, for loops, and lambda parameters.

Finally, Java 22 added the ability to [ignore record fields](https://openjdk.org/jeps/456) when destructuring them by
using an underscore placeholder:

```java
if (obj instanceof Person(var name, _)) {
    IO.println("Hi " + name + '!');
}
```

### Improved instanceof

In Java 16, there was a nice little improvement to the `instanceof` operator. It is now possible to perform the
`instanceof` check and the followup cast in a [single operation](https://openjdk.org/jeps/394). This is a very minor and
simple change to the language, but I think it does make a big difference.

In the days before Java 16, I used to write a lot of code that looked like this:

```java
String name;
if (obj instanceof NamedElement) {
    name = ((NamedElement) obj).getName();
} else {
    name = obj.toString();
}
```

This approach works, but feels a bit clunky. It is also possible to make a mistake and cast the value to a different
type than what was checked for in the `instanceof` check. With Java 16's Pattern Matching for `instanceof`, the above
example can be changed to the following:

```java
String name;
if (obj instanceof NamedElement namedElement) {
    name = namedElement.getName();
} else {
    name = obj.toString();
}
```

As you can see, a new variable `namedElement` is introduced as a part of the `instanceof` operator which gets assigned
only if the `instanceof` check passes. This cleans up the syntax a little and removes the chance that the programmer
might accidentally cast the value to a different type than what was checked for.

Java's approach is to introduce a new variable, whereas Kotlin handles this by maintaining the existing variable. Kotlin
uses flow typing to know that the type has changed if the check has been successful. The following is how the above
example would be written in Kotlin:

```kotlin
val name = if (obj is NamedElement) {
    obj.getName()
} else {
    obj.toString()
}
```

In this case, the type of `obj` is known to be `NamedElement` within the then-branch of the if, but it will not be a
`NamedElement` in the else-branch. As you can see, Kotlin does not have a new variable introduced, whereas Java does. I
don't think I have a strong preference between the Java or the Kotlin approach. I am just happy that we are no longer
dealing with the pre-Java 16 way of doing things.

### Sealed Types

[Sealed types](https://openjdk.org/jeps/409) have finally arrived in Java 17! I have experienced them in Scala and
Kotlin before and now we have them in Java as well. Sealed types allow the programmer to specify a type hierarchy that
is limited and fully known at compile time. A sealed class or interface cannot be extended by third-party code. This
way, the compiler can be certain about what are all of the possible sub-types of a given sealed type.

Java's sealed types are semantically very similar to the sealed types found in Scala and Kotlin. However, the syntax is
a bit more verbose in Java than in Scala or Kotlin. This is Java after all. Extra verbosity should not come as a
surprise. Anyway, let's consider a sealed hierarchy that is used in the solver. There is a sealed type called `Cell`
which has exactly two sub-types: `SolvedCell` and `UnsolvedCell`. In Kotlin, the sealed hierarchy for `Cell` looks like
this:

```kotlin
sealed class Cell {
    ...
}

data class SolvedCell(override val row: Int, override val column: Int, val value: SudokuNumber) : Cell() {
    ...
}

data class UnsolvedCell(
    override val row: Int,
    override val column: Int,
    val candidates: EnumSet<SudokuNumber> = EnumSet.allOf(SudokuNumber::class.java)
) : Cell() {
    ...
}
```

Now let's see how this same hierarchy is implemented in Java:

```java
public sealed interface Cell permits SolvedCell, UnsolvedCell {
    ...
}

public record SolvedCell(int row, int column, SudokuNumber value) implements Cell {
    ...
}

public record UnsolvedCell(int row, int column, EnumSet<SudokuNumber> candidates) implements Cell {
    ...
}
```

The key difference here is that the Java version of `Cell` has a `permits` clause while the Kotlin version does not. In
Kotlin, a sealed class can only be extended within the same file, but in Java, it is customary to declare each class in
it's own file. Java needs a different mechanism to determine the limitations of a sealed class's hierarchy.

The real value that sealed types provide wasn't fully realized until Java 21 when switch expressions and statements were
updated to include [pattern matching](https://openjdk.org/jeps/441). When a switch is being performed over a sealed
type, the compiler can check that all subtypes are handled by the switch and produce an error if they are not. To
demonstrate this, let's first consider a typical pre-Java 21 if-else chain that checks for a specific type in a type
hierarchy. This particular example comes from OSATE's
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

The issue with this if-else chain is that there is no way for the compiler to check that every option is handled. If, at
some later date, a new kind of feature type were to be added such as `Parameter`, the compiler would not be able to warn
the programmer that this if-else chain needs to be updated. The only check is at runtime with the possible
`AssertionError`. Now, consider how the above if-else chain can be rewritten as a switch with pattern matching:

```java
switch (feature) {
    case AbstractFeature _ -> template.add("kind", "AbstractFeature");
    case DataPort _ -> template.add("kind", "DataPort");
    case EventDataPort _ -> template.add("kind", "EventDataPort");
    case EventPort _ -> template.add("kind", "EventPort");
    case DataAccess _ -> template.add("kind", "DataAccess");
    case BusAccess busAccess -> template.add("kind", busAccess.isVirtual() ? "VirtualBusAccess" : "BusAccess");
    case SubprogramAccess _ -> template.add("kind", "SubprogramAccess");
    case SubprogramGroupAccess _ -> template.add("kind", "SubprogramGroupAccess");
}
```

As you can see, the exception at the end is no longer needed since the compiler can ensure that all cases are handled.
Now using the switch, if another type such as `Parameter` were to be added to the hierarchy, then the compiler would
complain with the following error message:

```txt
'switch' statement does not cover all possible input values
```

Overall, I'm very happy with sealed types in Java. The syntax isn't as nice as sealed types in Kotlin, but this is not a
major complaint. Hopefully, many bugs will be caught by the compiler with the use of sealed types.

### Switch Expressions

What is the worst part about C-style switch statements? It is implicit fall through. There have been so many bugs
introduced because a programmer forgot to break out of a switch case. It is a dumb feature and one that I have never
needed. In all of my years of programming, I can't think of a single time that I've wanted or needed to use fallthrough
behavior. Java has finally solved this issue in Java 14 with the addition of
[switch expressions](https://openjdk.org/jeps/361). Unlike a switch statement, a switch expression only executes one of
the cases and then exits out of the switch. It is also possible to return a value from each of the cases and thus return
a value from the whole switch.

Let's look at an example switch statement and then see what it's corresponding switch expression looks like. The
following is a modified switch statement adopted from a switch found in the
[SudokuNumber](src/main/sudokusolver/java/SudokuNumber.java) enum:

```java
SudokuNumber number;
switch (ch) {
    case '1':
        number = ONE;
        break;
    case '2':
        number = TWO;
        break;
    case '3':
        number = THREE;
        break;
    case '4':
        number = FOUR;
        break;
    case '5':
        number = FIVE;
        break;
    case '6':
        number = SIX;
        break;
    case '7':
        number = SEVEN;
        break;
    case '8':
        number = EIGHT;
        break;
    case '9':
        number = NINE;
        break;
    default:
        throw new IllegalArgumentException("ch is '" + ch + ", must be between '1' and '9'.");
}
```

*Note that this example is a little more complicated than it needs to be. If you look at the method `valueOf()` in the
enum `SudokuNumber`, you'll see that nothing comes after the switch in that method. It is certainly possible to return a
value from each case in the switch as opposed to assigning to a variable, but I wanted to demonstrate the old way of
breaking out of a switch in this example.*

Now let's see what a switch expression looks like:

```java
var number = switch (ch) {
    case '1' -> ONE;
    case '2' -> TWO;
    case '3' -> THREE;
    case '4' -> FOUR;
    case '5' -> FIVE;
    case '6' -> SIX;
    case '7' -> SEVEN;
    case '8' -> EIGHT;
    case '9' -> NINE;
    default -> throw new IllegalArgumentException("ch is '" + ch + ", must be between '1' and '9'.");
};
```

This looks a lot better! It is easier to read and there is no risk of accidentally falling through to the next case.
Switch expressions are useful on their own, but they also form a key building block for other features that were added
later including pattern matching and record patterns.

Many modern languages are realizing the dangers of implicit fallthrough. Kotlin's when, Scala's match, and Rust's match
have all abandoned fallthrough. The one interesting case here is Swift's switch. By default, a
[switch statement](https://docs.swift.org/swift-book/documentation/the-swift-programming-language/controlflow/#Switch)
in Swift does not fallthrough, but that functionality can be
[performed explicitly](https://docs.swift.org/swift-book/documentation/the-swift-programming-language/controlflow#Fallthrough)
with the `fallthrough` keyword. I don't know how often that will be useful, but it is there if anyone needs it.

### Flexible Constructors

For decades, Java's rules of construction have been simple and stable. Every constructor must start with a call to
`this()`, an explicit call to `super()`, or an implicit call to the default no-argument `super()` constructor. This
call, if explicit, must appear before any other statement in a constructor. Afterwards, constructors can pretty much do
whatever they want as long as they initialize all final fields before completing. The idea is that when constructing an
object, each parent-type is fully constructed before the child-type is constructed. For example, if we want to construct
an `ArrayList`, the constructor for `Object` is called first, then `AbstractCollection`, then `AbstractList`, then
finally the constructor for `ArrayList`. This guarantees that when the body of a constructor is executing, all of it's
super classes have already been constructed and they can be fully utilized by the child's constructor. This is an
important guarantee and Java's constructor rules do a good job of ensuring this guarantee, but over the years it has
become clear that there are a few shortcomings with these rules. In particular, there are three specific problem areas:

1. Validating constructor arguments
2. Transforming constructor arguments
3. Preventing access to uninitialized fields

Java 25 addresses these problems with the addition of [flexible constructor bodies](https://openjdk.org/jeps/513) which
allow limited statements before the call to `this()` or `super()`. These statements can initialize fields of the current
class, but they cannot otherwise access the object itself. Let's look at how flexible constructor bodies address the
three areas of concern:

#### Validating Constructor Arguments

Suppose that you want validate constructor arguments and throw an `IllegalArgumentException` if they are not valid. This
is very common and most of the time, Java developers validate the data after calling the `super()` constructor:

```java
public class PositiveFraction extends Fraction {
    public PositiveFraction(int numerator, int denominator) {
        super(numerator, denominator);
        if (numerator <= 0) {
            throw new IllegalArgumentException("numerator must be positive.");
        }
        if (denominator <= 0) {
            throw new IllegalArgumentException("denominator must be positive.");
        }
    }
}
```

The potential problem with this is that the `PositiveFraction` constructor still calls the `Fraction` constructor even
if it is going to throw an exception. The waste is not too much of a concern in this example, but it could be for
expensive constructors. One way of dealing with this before Java 25 was by passing the arguments to a method call that
is nested in the super constructor call:

```java
public class PositiveFraction extends Fraction {
    public PositiveFraction(int numerator, int denominator) {
        super(validateArgument(numerator, "numerator"), validateArgument(denominator, "denominator"));
    }

    private static int validateArgument(int value, String label) {
        if (value <= 0) {
            throw new IllegalArgumentException(label + " must be positive.");
        }
        return value;
    }
}
```

While this is possible, it does make the code more difficult to read. With flexible constructor bodies, it is now
possible to perform these checks before calling the super constructor:

```java
public class PositiveFraction extends Fraction {
    public PositiveFraction(int numerator, int denominator) {
        if (numerator <= 0) {
            throw new IllegalArgumentException("numerator must be positive.");
        }
        if (denominator <= 0) {
            throw new IllegalArgumentException("denominator must be positive.");
        }
        super(numerator, denominator);
    }
}
```

This makes the call to `super()` conditional. It is only called when the arguments are valid.

#### Transforming Constructor Arguments

Suppose that a constructor takes arguments and those arguments need to be modified before passing them to another
constructor. Before Java 25, this had to be embedded as an expression nested within the call to the other constructor. I
actually ran into this issue in the class [`RemoveCandidates`](src/main/sudokusolver/java/RemoveCandidates.java) in
which an `int...` needs to be transformed into an `EnumSet<SudokuNumber>` before making a call to `this()`. Before Java
25, my solution was to have a rather complicated expression embedded into the call to `this()`:

```java
public RemoveCandidates(int row, int column, int... candidates) {
    this(row, column, Arrays.stream(candidates)
            .mapToObj(candidate -> SudokuNumber.values()[candidate - 1])
            .collect(Collectors.toCollection(() -> EnumSet.noneOf(SudokuNumber.class))));
}
```

The above code performs the transformation, but it is a little complicated to read. It would be better if the
transformation of `candidates` occurred before the call to `this()`. This is what the constructor looks like now:

```java
public RemoveCandidates(int row, int column, int... candidates) {
    var candidatesSet = Arrays.stream(candidates)
            .mapToObj(candidate -> SudokuNumber.values()[candidate - 1])
            .collect(Collectors.toCollection(() -> EnumSet.noneOf(SudokuNumber.class)));
    this(row, column, candidatesSet);
}
```

#### Preventing Access to Uninitialized Fields

When Java was first released, one of its advantages was that it prevented access to uninitialized variables. This is a
no-brainer these days, but back then, it was a problem that was frequently encountered by C and C++ programmers. You
would think that Java would also prevent access to uninitialized fields of a class, but this gets a bit tricky. First of
all, mutable fields that are not initialized during construction are given a default value such as `0` for integer
types, `false` for booleans, `null` for object types, etc. I disagree with this design decision, but it is well known
and well documented. Another problem is that it is actually possible to access an uninitialized final field. When
programmers discover this, it is often a huge surprise. Based upon Java's rules for objects and classes, one would think
that the `final` keyword would necessarily prevent uninitialized access and in most cases it does, except for a tricky
corner case.

To demonstrate this issue, let's first look at a simple Java class which does prevent uninitialized access to a final
field:

```java
public class ObjectHolder {
    private final Object obj;

    public ObjectHolder(Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException("obj cannot be null.");
        }
        this.obj = obj;
    }

    public void printObject() {
        IO.println(obj.toString());
    }
}
```

In this example, the method `printObject()` will never throw a `NullPointerException` because the constructor guarantees
that `obj` can never be `null`. However, this guarantee falls apart if we make `ObjectHolder` extend from another class:

```java
public class ObjectHolder extends ParentClass {
```

Intuitively, this should not be a problem, but it is possible for `ParentClass` to call `printObject()` before `obj` has
been initialized. Consider this implementation of `ParentClass`:

```java
public abstract class ParentClass {
    public ParentClass() {
        printObject();
    }

    public abstract void printObject();
}
```

With this implementation of `ParentClass`, any construction of `ObjectHolder` will result in a `NullPointerException`
being thrown from `printObject()` even if the object passed to `ObjectHolder` isn't `null`. Why does this happen? This
is an unfortunate and unforeseen consequence of Java's constructor rules. Let's walk through what happens step-by-step
when `ObjectHolder` is constructed with a `non-null` value such as the string literal `"My String Value"`:

1. `ObjectHolder("My String Value")` is called and passed a string literal.
2. The constructor `ObjectHolder(Object)` implicitly calls the no-argument constructor `ParentClass()`.
3. The constructor `ParentClass()` calls the abstract method `printObject()`.
4. The method `printObject()` is overridden in `ObjectHolder`, so the method `ObjectHolder.printObject()` is the one
   that is actually called.
5. The method `ObjectHolder.printObject()` accesses `obj`, but it has not yet been initialized, so `obj` defaults to a
   value of `null`.
6. The method `Object.toString()` is called on a `null` object thus leading to a `NullPointerException`.

With flexible constructor bodies, we can solve this issue in the constructor of `ObjectHolder` by assigning a value to
`obj` before calling the super constructor:

```java
public ObjectHolder(Object obj) {
    if (obj == null) {
        throw new IllegalArgumentException("obj cannot be null.");
    }
    this.obj = obj;
    super();
}
```

Now, if we call `ObjectHolder("My String Value")`, an exception is not thrown, but instead, the string
`"My String Value"` is printed to stdout, which is what we wanted in the first place.

I first ran into this issue years ago upon reading a paper that a colleague recommended to me:
[Declaring and Checking Non-null Types in an Object-Oriented Language](https://www.microsoft.com/en-us/research/publication/declaring-and-checking-non-null-types-in-an-object-oriented-language/).
The authors tackle the issue of guaranteeing non-null types in C# and Java when a field can be accessed before it is
initialized. To make a long story short, adding non-null types would be so much simpler if accessing uninitialized
fields was forbidden. Java 25's flexible constructor bodies is a step in the right direction.

When I started learning Swift, I discovered that it had solved the problem of exposing partially initialized objects.
Unlike Java and C#, it is not possible to access an uninitialized field in Swift. This is accomplished through Swift's
rules for
[class initialization](https://docs.swift.org/swift-book/documentation/the-swift-programming-language/initialization#Two-Phase-Initialization).
The key difference between Java/C# constructors and Swift initializers is that a Swift initializer must assign values
for each of the fields of its immediate class before calling the super class's initializer. This way, all of the fields
of a subclass are given values before initializing its parent class. After the super class's initializer returns, the
subclass's initializer is free to perform other actions, change mutable fields, call methods, pass the object around,
etc.

When I discovered this about Swift, I was so excited! I told all my software friends about what I found and I'm sure
they thought I was crazy for loudly celebrating such a nuanced and oddly specific implementation detail. Swift
essentially enforces Java 25's flexible constructor bodies and doesn't allow classes to be initialized in the
traditional Java way. Even though Java can never enforce flexible constructor bodies, as that would break a lot of
existing code, I feel that Java is heading in the right direction.

### Compact Source Files

Java is very verbose. It always has been. One key complaint that is often raised against Java is that its verbosity can
get in the way of teaching introductory programming. Often times, the first program that a student or someone learning
programming will encounter is the basic and fundamental Hello World program. This is what Java's Hello World has looked
like since its inception:

```java
public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello, World!");
    }
}
```

That is a lot of stuff to introduce to a new programmer! What are classes? What are visibility modifiers? What is
`static`? So many topics are packed into the most basic and simple Java program. At the very beginning, these concepts
often get in the way. Many teachers say, "Don't worry about this stuff now. It will be explained later." How could you
not take that approach when teaching Java?

Many other languages offer a simpler on-ramp to programming concepts. Languages like Kotlin, Scala, and Rust only
require the programmer to define a main function as opposed to starting with classes. As an example, here is Hello World
in Rust:

```rust
fn main() {
    println!("Hello, world!");
}
```

Some other languages such as Swift and Python don't even require a main function but instead support bare statements.
For example, here is a Hello World program that works in both Swift and Python:

```swift
print("Hello, world!")
```

Many have argued that it is better for a language to offer a simpler introduction and to make the more complex features
such as classes optional. Chris Lattner, the creator of Swift describes this as "Progressive Disclosure of Complexity".
In [this interview](https://youtu.be/nWTvXbQHwWs?si=-K3MsU7qfQkhK59K&t=1377) (22:57 - 24:48), he describes how this
concept influenced Swift's design while also complaining about Java's overly complicated Hello World.

After years of complaints, Java now has a simplified Hello World program made possible in Java 25 with
[compact source files](https://openjdk.org/jeps/512). A Hello World program in Java now looks like this:

```java
void main() {
    IO.println("Hello, World!");
}
```

There are a few things going on under the hood to make this work:

1. When defining a main method like this, the compiler will implicitly insert the method into an undeclared class.
2. The main method no longer needs to be declared as `public`.
3. The main method no longer needs to be declared as `static`. This simplified version is an instance method and the JRE
   will instantiate the enclosing class (declared or undeclared).
4. The main method no longer needs to accept command-line arguments as a parameter.
5. A new class called `IO` acts as a simplified wrapper that provides access to `System.out` and `System.in`.

I think this is a good change for Java, especially when it comes to teaching students programming. However, I don't
expect much existing code to make use of this change. Even in the solver, I only take advantage of a couple of these
changes. My main method in [SudokuSolver](src/main/sudokusolver/java/SudokuSolver.java) now looks like this:

```java
public class SudokuSolver {
    static void main(String[] args) {
```

As you can see, this looks very similar to Java's original entry point. The only change is that I have removed `public`
from the method declaration. I still need to declare an enclosing class so that other methods in the file can be called
from unit tests. I also keep `String[] args` because I need to process command line arguments. If I wanted to, I could
have removed `static`, but I decided to leave it in so that `SudokuSolver` does not get instantiated. Finally, I have
replaced all calls to `System.out.println()` with `IO.println()`.

I expect this change to be wildly celebrated by Computer Science professors, but to go largely unnoticed by much of the
professional Java community.

### Text blocks

One of Java's improvements that has made life easier for me, especially when writing unit tests for the solver, has been
Java 15's [text blocks](https://openjdk.org/jeps/378). A text block allows a programmer to write a multi-line string
literal in which the margin is indented. Before text blocks, it was common for programmers to represent multi-line
strings by concatenating each individual line. Programmers would also manually insert each line terminator. Text blocks
automatically handle line terminators as well as proper indentation.

To demonstrate the benefits of text blocks, let's look at an example variable assignment in which the value is a
multi-line string. This particular example comes from the method `testSolution()` in the class
[`SudokuSolverTest`](src/test/sudokusolver/java/SudokuSolverTest.java). This is what the assignment to the variable
`expected` would have looked like before text blocks:

```java
var expected = "8 1 7 | 9 4 2 | 5 6 3\n" +
        "2 3 4 | 6 1 5 | 7 8 9\n" +
        "5 6 9 | 8 3 7 | 1 4 2\n" +
        "------+-------+------\n" +
        "4 5 1 | 3 2 9 | 6 7 8\n" +
        "6 2 3 | 7 8 1 | 4 9 5\n" +
        "9 7 8 | 5 6 4 | 3 2 1\n" +
        "------+-------+------\n" +
        "7 9 6 | 1 5 8 | 2 3 4\n" +
        "1 8 2 | 4 7 3 | 9 5 6\n" +
        "3 4 5 | 2 9 6 | 8 1 7";
```

As you can see, this required explicit newline characters as well as concatenation. Now let's look at what the same
assignment looks like with text blocks:

```java
var expected = """
        8 1 7 | 9 4 2 | 5 6 3
        2 3 4 | 6 1 5 | 7 8 9
        5 6 9 | 8 3 7 | 1 4 2
        ------+-------+------
        4 5 1 | 3 2 9 | 6 7 8
        6 2 3 | 7 8 1 | 4 9 5
        9 7 8 | 5 6 4 | 3 2 1
        ------+-------+------
        7 9 6 | 1 5 8 | 2 3 4
        1 8 2 | 4 7 3 | 9 5 6
        3 4 5 | 2 9 6 | 8 1 7""";
```

This is much cleaner and easier to represent. I am very happy with text blocks.
