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
