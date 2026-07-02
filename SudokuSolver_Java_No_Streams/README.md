# Java (no streams) implementation

After implementing the solver in both Kotlin and Java using a functional style, I decided that I wanted to also
implement the solver using a more imperative style. I am very happy that many modern languages are borrowing some
elements from the functional languages, but this was not an option for Java programmers before Java 8. Therefore, this
implementation is not about exploring a language, but instead is about exploring a programming style.

For this implementation, I decided to start with a copy of the Java implementation, and then remove streams on a
file-by-file basis. I decided that replacing stream operations with loops, conditionals, and mutating collections is a
sufficient constraint to enforce an imperative style. There are some functional elements that I permit such as
higher-order functions, lambdas, and method references. I figured that the presence of these features wouldn't make the
implementation too functional as long as I didn't use streams. If I feel so motivated, I might remove those features as
well at some point.

## Development Setup

Follow these steps to setup a development environment:

1. Clone this repo by running `git clone https://github.com/joeseibel/sudoku-solver.git`.
2. Download and install [IntelliJ](https://www.jetbrains.com/idea/).
3. Launch IntelliJ, choose to open a project, and select the `sudoku-solver/SudokuSolver_Java_No_Streams` directory.
4. If you need to configure IntelliJ with the appropriate JDK, then open one of the Java files such as
   `SudokuSolver_Java_No_Streams/src/main/sudokusolver.javanostreams/SudokuSolver.java` and click the link at the top of
   the file for downloading the appropriate JDK.

### Running the solver

Follow these steps to create a run configuration for running the solver:

1. In the project tool window, right-click on the file
   `SudokuSolver_Java_No_Streams/src/main/sudokusolver.javanostreams/SudokuSolver.java`.
2. Select **Run 'SudokuSolver.main()'**. It is expected that the solver will complain that a board has not been
   specified.
3. Edit the **SudokuSolver** run configuration.
4. In the **Program arguments** field, paste in the board to solve as a sequence of 81 digits, e.g.,
   `010040560230615080000800100050020008600781005900060020006008000080473056045090010`.
5. Click **Run**.

### Running the unit tests

Follow these steps to run the unit tests:

1. In the project tool window, right-click on the project `SudokuSolver_Java_No_Streams`.
2. Select **Run 'All Tests'**.
