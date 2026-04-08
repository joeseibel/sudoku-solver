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
