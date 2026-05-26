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
