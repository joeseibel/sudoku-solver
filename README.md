# Sudoku Solver

This is a command-line sudoku solver that primarily solves puzzles utilizing logical solutions. This means that the
final solution that the solver produces is not determined by guessing or backtracking. The solver instead depends upon
various solution algorithms that examine the state of the board and determine if there are any numbers that can be
placed in a location or if there are any numbers that can be ruled out from a location. If I were to have instead
written a brute force solver using guessing and backtracking, that would have been a relatively simple programming
exercise. However, writing a solver utilizing logical solutions is a bit more complicated. Some of the logical solutions
are simple and intuitive, but others are rather challenging.

I used [SudokuWiki.org](https://www.sudokuwiki.org/) to learn about the various solution algorithms. SudokuWiki has a
web-based solver along with numerous pages describing a multitude of solution algorithms. All of the solutions that I
implemented were based upon SudokuWiki's descriptions. I have implemented many, but not all of SudokuWiki's described
algorithms.

## Exploring Programming Languages

The reason that I started writing this sudoku solver was that I wanted to find a medium-sized programming project that I
could implement in multiple programming languages. Over the years, the solver has served as a catalyst for me to learn
languages that I am interested in. It is one thing to read about a language and experiment with simple exercises. It is
another thing to learn by implementing a medium-sized project with multiple source files, custom types, unit tests, etc.
Not only have I learned the concepts of the languages, but I've also discovered the idioms and conventions embraced by
each language's community. I also stay up to date with the changes in the languages that I have learned. As new versions
of the languages are released, I update the solver to take advantage of these changes when applicable.

One of the desires that I had for this project was to select a programming problem that would showcase each language in
its purist form. I wanted to explore each language's basic constructs and standard libraries while keeping other aspects
of a language's use to a minimum. For example, I chose to implement the solver as a command line application as opposed
to a GUI application. I did not want this project to be an exploration of various GUI frameworks as that varies wildly
across languages. I also wanted to stay far away from things such as networking, database connections, or even
concurrency as the support for these concepts among languages is highly variable. The Sudoku Solver simply receives
input, performs calculations, and produces output. It does this though manipulating data structures, lots of iteration,
and lots of conditionals. These are concepts that are uniformly supported in all programming languages that I know of.
In other words, I wanted to select a project that could be implemented in any Turing-complete language.

I also decided to keep dependencies on third-party libraries to a minimum. This forces me to focus on exploring the
language itself as opposed to exploring libraries such as Apache Commons or Google Guava, as useful as those libraries
may be. There are two notable exceptions to this rule: graph libraries and unit testing frameworks. Some of the solution
algorithms require an undirected graph data structure as a part of their calculations. For each language, I have
searched for an appropriate graph library to use. The other exception is for unit testing. While some languages have a
built-in unit testing framework, many do not. For the languages that don't, I use the most common or industry standard
framework for that language, such as JUnit for Java.

So far, I have implemented the solver in the following languages:

- [Kotlin](SudokuSolver_Kotlin/)
- [Java](SudokuSolver_Java/)
- [Java (no streams)](SudokuSolver_Java_No_Streams/)
- [Scala](sudokusolver_scala/)
- [Swift](SudokuSolver_Swift/)
- [Rust](sudoku_solver_rust/)

## Basic Functionality

All of the solvers implemented in the various languages share the same basic functionality. They all expect the same
input and produce the same output. When viewed as black boxes, they all have the same interface.

### Input Format

Each solver expects a single command-line argument to be passed to it which represents the initial state of a Sudoku
puzzle. This argument is expected to be a string of 81 numbers in which a `0` represents an empty location. The first
nine numbers represent the first row of the puzzle, the second nine numbers represent the second row of the puzzle, and
so on. For example, if you wanted to solve the following puzzle:

```text
0 1 0 | 0 4 0 | 5 6 0
2 3 0 | 6 1 5 | 0 8 0
0 0 0 | 8 0 0 | 1 0 0
------+-------+------
0 5 0 | 0 2 0 | 0 0 8
6 0 0 | 7 8 1 | 0 0 5
9 0 0 | 0 6 0 | 0 2 0
------+-------+------
0 0 6 | 0 0 8 | 0 0 0
0 8 0 | 4 7 3 | 0 5 6
0 4 5 | 0 9 0 | 0 1 0
```

then you would pass the string `010040560230615080000800100050020008600781005900060020006008000080473056045090010` to
the solver. The following are a few examples of how to invoke various implementations of the solver:

```bash
# Running the Scala version from the sudokusolver_scala directory:
$ sbt "run 010040560230615080000800100050020008600781005900060020006008000080473056045090010"
# Running the Swift version from Xcode's output directory:
$ ./SudokuSolver_Swift 010040560230615080000800100050020008600781005900060020006008000080473056045090010
# Running the Rust version from the sudoku_solver_rust directory:
$ cargo run 010040560230615080000800100050020008600781005900060020006008000080473056045090010
```

### Brute Force Validation

The solver expects the puzzle passed as input to have a single unique solution. This is checked with a brute force
solver using guessing and backtracking. Note that this brute force solver is only used for validation purposes and is
not used to produce the ultimate solution of the solver. If the brute force solver finds that the puzzle is not
solvable, then the message "No Solutions" is printed to stdout and the solver quits. If the brute force solver finds
that there are multiple possible solutions for the puzzle, then the message "Multiple Solutions" is printed to stdout
and the solver quits. If the brute force solver finds that there is a single unique solution to the puzzle, then the
puzzle is valid and the solver moves on to solve it using the logical solutions. Even as the solver moves on to the
logical solutions, the brute force solution is stored as a means of checking the logical solutions.

### Attempting Logical Solution Algorithms

The solver currently utilizes 32 logical solution algorithms that are grouped into four difficulty categories: simple,
tough, diabolical, and extreme. Both the solutions and the categories have been inspired by SudokuWiki. The solver will
attempt each logical solution in order of increasing difficulty until one of the logical solution algorithms produces a
result. When that happens, the result is checked against the known solution produced by the brute force algorithm. This
is done to ensure the correctness of the logical solution. If the logical solution is not correct, then an error is
reported and the solver quits. If the logical solution is correct, then the puzzle is modified and the solver will go
back and attempt again each logical solution in order of increasing difficulty, this time with the modified state of the
puzzle. This means that a difficult solution algorithm is only attempted after every algorithm before it has been tried
without any results.

### Output Format

The solver will continue to loop through the solutions algorithms until the puzzle is finally solved or until all of the
algorithms are tried and none of them yield a result. If that happens, then the puzzle cannot be solved by this solver.
If the solver was successful, then it will print the solution to stdout and quit. For example, if the solver was passed
the string `010040560230615080000800100050020008600781005900060020006008000080473056045090010`, then the solver will
print this solution:

```text
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
3 4 5 | 2 9 6 | 8 1 7
```

If the solver is unable to solve a puzzle, then it will print out an error message with the state of the puzzle in
various forms. It will first print out the 9x9 puzzle grid showing which locations have numbers and which don't. It will
then print out the same information as a string of 81 numbers. Finally, it will print out the state of the board
including all of the potential candidates for each of the unsolved locations. In this form, potential candidates of
unsolved locations are enclosed in curly braces and solved locations are represented by their numerical value outside of
any curly braces. For example, if the solver was passed the string
`004007830000050470720030695080700300649513728007008010470080060016040007005276100`, then the solver will print this
error message:

```text
Unable to solve:
0 0 4 | 0 0 7 | 8 3 0
0 0 0 | 0 5 0 | 4 7 0
7 2 0 | 0 3 0 | 6 9 5
------+-------+------
0 8 0 | 7 0 0 | 3 0 0
6 4 9 | 5 1 3 | 7 2 8
0 0 7 | 0 0 8 | 0 1 0
------+-------+------
4 7 0 | 0 8 0 | 0 6 0
0 1 6 | 0 4 0 | 0 0 7
0 0 5 | 2 7 6 | 1 0 0

Simple String: 004007830000050470720030695080700300649513728007008010470080060016040007005276100

With Candidates:
{159}{569}4{169}{269}783{12}
{139}{36}{38}{689}5{129}47{12}
72{18}{148}3{14}695
{125}8{12}7{269}{249}3{45}{469}
649513728
{235}{35}7{46}{269}8{59}1{469}
47{23}{139}8{159}{259}6{39}
{2389}16{39}4{59}{25}{58}7
{38}{39}52761{48}{349}
```

## Common Design Decisions

For the most part, the implementations all share the same or similar architecture. As I implemented each version of the
solver, I wanted to pursue the idioms of each language as much as possible while maintaining a recognizable structure
across the languages. This section describes some of the design decisions that impact all of the implementations. Key
design decisions are also documented as in-code comments in the Kotlin version. They appear there because Kotlin was the
first language I implemented the solver in. When a language's implementation deviates in some way from the common design
principles, those changes are also documented as in-code comments.

### Single-Threaded

Each solver is single-threaded. This was done to simplify the project and limit the scope of language exploration. I did
consider utilizing parallelism in the solvers. I could envision multiple logical solutions being executed in parallel or
even parallelism being used within a logical solution. Java's parallel streams would make this relatively easy to
implement, but it could be a challenge in other languages, such as C. Due to the number of questions that parallelism
opens up, I decided that it would be simplest to keep my solver single-threaded.

### Navigating a Puzzle

Each implementation has a number of types defined that are meant to aid in the navigation of a puzzle. While it is
possible to operate directly on a 2D array of integers, I decided to take advantage of the languages' type systems to
add more structure to these operations.

Each language has a `Board` type which is a wrapper around a 2D array. This type provides methods for viewing the board
in different ways such as getting all cells, all rows, all columns, all blocks, and all units. I use the term *block* to
refer to one of the nine 3x3 sub-grids in the board and I use the term *unit* as an abstract term which could be a row,
column, or block. Finally, the `Board` type provides methods for getting a particular row, a particular column, or a
particular block.

Each language also has a `Cell` type that represents either a solved cell or an unsolved cell. In languages that support
it, the `Cell` type is a tagged union with the variants of `SolvedCell` and `UnsolvedCell`. `SolvedCell` has a `value`
field for the number at that location while `UnsolvedCell` has a `candidates` fields which is a set of potential values
for that location. `Cell` also has methods for getting the cell's row index, column index, and block index.

There is also a `SudokuNumber` enumeration type which represents the numbers `1` through `9`. While I could have chosen
to represent the numbers of a puzzle with integers, I decided to use an enumeration to limit the potential values that
are placed in a puzzle. Most languages also have the ability to iterate through all of the literals of an enumeration
type and I make use of that.

### Modifying a Puzzle

One major decision I made is that the logical solution algorithms don't directly modify the board, but they instead
return instructions for how to modify the board. This is a decision that I had made during development and if you look
far into the history, then you will see that the original version of the solver had the logical solutions directly
modify the board. I realized that the current approach simplifies the writing and understandability of unit tests. If
the logical solutions modified the board, then the unit tests would need to supply both the initial and final states of
a board. While this is fine for a computer to handle, it can be very challenging for a developer to look at two boards,
see the difference, and determine what the logical solution is expected to do. By having the logical solutions return
modification instructions, then the unit tests simply supply the initial state of the board and the expected
modifications. This makes it much easier to quickly see what the logical solution is expected to do.

To support this approach, each logical solution returns a list of `BoardModification` objects. Similar to `Cell`, the
`BoardModification` type is a tagged union with the variants of `RemoveCandidates` and `SetValue`. `RemoveCandidates`
has a `candidates` field which is a set of numbers that should be removed from a given `UnsolvedCell` while `SetValue`
has a `value` field which indicates the solution for that cell. `BoardModification` also has methods for getting a row
index and a column index, thus identifying which `UnsolvedCell` the `BoardModification` applies to.

### Unit Tests

When developing the solver, I tried to follow a Test-Driven Development approach, at least for the most part. As such,
unit tests are a central and critical part of each language's implementation. The vast majority of tests focus on
testing the logical solutions algorithms. Each logical solution has a set of tests for the solution and they all follow
the same pattern.

Each test starts with specifying the initial state of a board, both the values for the `SolvedCell`s and the candidates
for the `UnsolvedCell`s. This is specified with a string in which a number between `1` and `9` represents the value of a
`SolvedCell` and a grouping of numbers enclosed by curly braces represents the candidates for an `UnsolvedCell`. All of
these individual numbers and groupings put together are expected to add up to 81 items. The first nine items represent
the first row of the board, the second nine items represent the second row of the board, and so on.

After specifying the board, each test then lists out the expected modifications, either the values to be set or the
candidates to remove. This is really where the value of having the logical solutions return modification instructions
pays off.

Finally, each test calls the `assertLogicalSolution` helper function. This first solves the puzzle with a brute force
solution, just to determine what the known solution to the puzzle should be. It will then call the logical solution and
retrieve its modifications. Each modification is compared with the brute force solution to check that it is a valid
modification. Finally, the modifications returned from the logical solution are compared with the list of expected
modifications.
