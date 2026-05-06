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
2. Select **Run 'SudokuSolver.main()'**. It is expected that the solver will complain that a board has not been specified.
3. Edit the **SudokuSolver** run configuration.
4. In the **Program arguments** field, paste in the board to solve as a sequence of 81 digits, e.g.,
   `010040560230615080000800100050020008600781005900060020006008000080473056045090010`.
5. Click **Run**.

### Running the unit tests

Follow these steps to run the unit tests:

1. In the project tool window, right-click on the project `SudokuSolver_Java`.
2. Select **Run 'All Tests'**.
