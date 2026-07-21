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

## Development Setup

For the Scala implementation of the solver, I chose to use IntelliJ as my development environment. It is also possible
to use sbt to build, run the solver, and run the unit tests from the command line. I have included instructions for both
sbt and IntelliJ.

Another popular option is the [Metals](https://scalameta.org/metals/) language server which has plugins for VS Code and
other editors. In the beginning, I played around with Metals a little bit, but I ended up deciding to use IntelliJ's
Scala plugin mainly because I am more familiar with IntelliJ. The solver should work just fine with Metals if you feel
so inclined to try it out.

Whichever option you choose, start by cloning this repo:

```bash
git clone https://github.com/joeseibel/sudoku-solver.git
```

### Using sbt

The most popular build tool for Scala projects is sbt. For the longest time, I used to think that sbt stood for Scala
Build Tool, but I recently discovered that sbt
[doesn't stand for anything](https://www.scala-sbt.org/1.x/docs/Faq.html#What+does+the+name+%E2%80%9Csbt%E2%80%9D+stand+for%2C+and+why+shouldn%E2%80%99t+it+be+written+%E2%80%9CSBT%E2%80%9D%3F).
Follow these steps to setup a development environment using sbt:

1. Download and install [sbt](https://www.scala-sbt.org/download/).
2. Navigate to the `sudoku-solver/sudokusolver_scala` directory.

#### Running the solver with sbt

To run the solver, run the following command while replacing `<board>` with a sequence of 81 digits:

```bash
sbt "run <board>"
```

The following is an example command to run the solver with a valid board:

```bash
sbt "run 010040560230615080000800100050020008600781005900060020006008000080473056045090010"
```

#### Running the unit tests with sbt

To run all of the tests, run the following command:

```bash
sbt test
```

### Using IntelliJ

Follow these steps to setup a development environment using IntelliJ:

1. Download and install [IntelliJ](https://www.jetbrains.com/idea/).
2. Launch IntelliJ, choose to open a project, and select the `sudoku-solver/sudokusolver_scala` directory.
3. Install the Scala plugin.
   1. Open the **Settings** dialog. *Note that the means of opening the **Settings** dialog is platform dependent.*
   2. Select **Plugins**.
   3. In the **Marketplace** tab, search for the "Scala" plugin.
   4. Select "Scala" in the list and select **Install**.
4. After installing the Scala plugin, you should see a notification which says, "sbt 'sudokusolver_scala' build scripts
   found". Select **Load sbt Project**.
5. Configure IntelliJ with an appropriate JDK, if needed.
   1. Open one of the Scala files such as `sudokusolver_scala/src/main/scala/sudokusolver/scala/SudokuSolver` and click
      the **Setup SDK** link at the top of the file.
   2. Select **Download JDK...**.
   3. Select the latest release version of Java and an appropriate JDK for your architecture. I usually select
      **Oracle OpenJDK**.
   4. Click **Download**.

#### Running the solver with IntelliJ

Follow these steps to create a run configuration for running the solver:

1. In the project tool window, right-click on the file
   `sudokusolver_scala/src/main/scala/sudokusolver/scala/SudokuSolver`.
2. Select **Run 'sudokuSolver'**. IntelliJ will attempt to run the solver, notice that it requires arguments, and then
   bring up a dialog titled **Provide program arguments**.
3. In the **Value** column for the argument **board**, paste in the board to solve as a sequence of 81 digits, e.g.,
   `010040560230615080000800100050020008600781005900060020006008000080473056045090010`.
4. Click **Ok**.
5. Run the **sudokuSolver** run configuration again.

#### Running the unit tests with IntelliJ

Follow these steps to run the unit tests:

1. In the project tool window, right-click on the folder `sudokusolver_scala/src/test`.
2. Select **Run 'MUnit in 'test''**.
