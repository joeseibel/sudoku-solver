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

## My experience with Java (no streams)

I feel like I don't have much to say about the no streams version. Much of what I have written about Java including
records, sealed types, and switch expressions also apply here. With that said, I do have a couple observations worth
making.

### Readability

After implementing the solver in both a functional style and an imperative style, I find the functional implementations
to be easier to follow and to read. There is a flow to the stream operations of `filter()`, `map()`, and `flatMap()`,
that make it easier to see how the data is being operated on. Stream operations are essentially an implementation of a
Pipe and Filter architecture. These operations follow a sequential flow through the chain of operations. In contrast to
this, I sometimes find that the constructs of an imperative style, the loops and conditionals, can add noise to what is
happening to the data.

Let us look at a few examples. Here is the
[Naked Singles](src/main/sudokusolver/javanostreams/logic/simple/NakedSingles.java) logical solution as it is
implemented in the no streams version:

```java
public static List<SetValue> nakedSingles(Board<Cell> board) {
    var modifications = new ArrayList<SetValue>();
    for (var cell : board.getCells()) {
        if (cell instanceof UnsolvedCell unsolved && unsolved.candidates().size() == 1) {
            modifications.add(new SetValue(unsolved, unsolved.candidates().iterator().next()));
        }
    }
    return modifications;
}
```

This is the same solution, except using streams:

```java
public static List<SetValue> nakedSingles(Board<Cell> board) {
    return board.getCells()
            .stream()
            .gather(FilterType.of(UnsolvedCell.class))
            .filter(cell -> cell.candidates().size() == 1)
            .map(cell -> new SetValue(cell, cell.candidates().iterator().next()))
            .toList();
}
```

Even though this is pretty simple, I still prefer the functional style. For a more complicated example, here is the
[BUG](src/main/sudokusolver/javanostreams/logic/diabolical/BUG.java) logical solution as it is implemented in the no
streams version:

```java
public static Optional<SetValue> bug(Board<Cell> board) {
    var cellsWithNotTwo = new ArrayList<UnsolvedCell>();
    for (var cell : board.getCells()) {
        if (cell instanceof UnsolvedCell unsolved && unsolved.candidates().size() != 2) {
            cellsWithNotTwo.add(unsolved);
        }
    }
    if (cellsWithNotTwo.size() == 1) {
        var cell = cellsWithNotTwo.getFirst();
        if (cell.candidates().size() == 3) {
            var row = new ArrayList<UnsolvedCell>();
            for (var rowCell : board.getRow(cell.row())) {
                if (rowCell instanceof UnsolvedCell unsolved) {
                    row.add(unsolved);
                }
            }
            var candidates = new ArrayList<SudokuNumber>();
            for (var candidate : cell.candidates()) {
                var withCandidate = 0;
                for (var rowCell : row) {
                    if (rowCell.candidates().contains(candidate)) {
                        withCandidate++;
                    }
                }
                if (withCandidate == 3) {
                    candidates.add(candidate);
                }
            }
            assert candidates.size() == 1;
            return Optional.of(new SetValue(cell, candidates.getFirst()));
        } else {
            return Optional.empty();
        }
    } else {
        return Optional.empty();
    }
}
```

Now, here is BUG, except using streams:

```java
public static Optional<SetValue> bug(Board<Cell> board) {
    var cellsWithNotTwo = board.getCells()
            .stream()
            .gather(FilterType.of(UnsolvedCell.class))
            .filter(cell -> cell.candidates().size() != 2)
            .toList();
    if (cellsWithNotTwo.size() == 1) {
        var cell = cellsWithNotTwo.getFirst();
        if (cell.candidates().size() == 3) {
            var row = board.getRow(cell.row()).stream().gather(FilterType.of(UnsolvedCell.class)).toList();
            var candidates = cell.candidates()
                    .stream()
                    .filter(candidate -> row.stream()
                            .filter(rowCell -> rowCell.candidates().contains(candidate))
                            .count() == 3)
                    .toList();
            assert candidates.size() == 1;
            return Optional.of(new SetValue(cell, candidates.getFirst()));
        } else {
            return Optional.empty();
        }
    } else {
        return Optional.empty();
    }
}
```

One of the key differences I want to highlight is how temporary collections are built in the midst of the BUG logical
solution. These collections are stored in the variables `cellsWithNotTwo`, `row`, and `candidates`. In the functional
version, all of the information for how these collections are built are contained in a single chained expression that
flows from the variable declaration. In my opinion, this makes it easier to see how these collections are being built,
especially `row` which only takes up a single line of code. In contrast, I find it a little more involved to see how
these collections are built in the imperative version. To understand how an imperative program is processing data, one
must look at multiple separate declarations that may or may not be grouped together. This often requires maintaining a
mental model of what variables are being mutated, where those mutations occur, and why they happen.

### Performance

As much as I love Java streams, they do have a performance downside. Well, it depends. If a programmer is using parallel
streams in a multi-core environment and they are working with a large dataset that lends itself to parallelism, then
using parallel streams can be faster than using an imperative approach. However, this is not guaranteed. There is a cost
to parallelism and to streams as well.

In the Java implementation of the solver, all of the streams that I use are sequential streams, not parallel streams. By
using streams, my Java implementation does incur an overhead cost that does not come when using for loops and if
statements. As a result, the implementation without streams runs faster than the implementation with streams. To give
you some numbers, I decided to perform 30 executions of the **Run 'All Tests'** run configuration for both the Java
implementation and the Java (no streams) implementation. I performed this test on my 2020 MacBook Air with an M1
processor with 8GB of RAM. Running all of the unit tests for the Java implementation took an average of 13.742 seconds.
Performing the same test on the Java (no streams) implementation took an average of 10.869 seconds.

I am assuming that the reason the version with streams is more expensive is that there are more allocations and function
calls involved. However, I have not looked into the details of this. I've only looked at the high-level results of my
experiment. So, when working with Java, if readability is more important, then use streams. If performance is more
important, then don't use streams. If the dataset is large and the benefits of parallelism outweigh the overhead of
streams, then use parallel streams.

Finally, I will say that the performance issue here only applies to Java streams. It does not apply to a functional
style in general. There are other languages that support a functional style, but do not incur this performance penalty.
For example, Rust iterators are one of that language's
[zero-cost abstractions](https://doc.rust-lang.org/book/ch13-04-performance.html). In most cases, all of the extra
structs, function calls, and even extra loops get optimized away.

Another interesting case is Kotlin. Kotlin's functional operations such as `filter()`, `map()`, and `flatMap()` are all
inlined. In fact, Kotlin's `inline` keyword is meant to be used with
[higher-order functions](https://kotlinlang.org/docs/inline-functions.html). As a result of this optimization, a
functional style in Kotlin does not come with the same performance penalty that a functional style in Java does. In
fact, I ran the same experiment in Kotlin and found that it was even faster than the Java (no streams) implementation!
Running all of the unit tests for the Kotlin implementation took an average of 3.475 seconds!

I am very perplexed by this. I expected the performance of Kotlin to be the same as Java (no streams), not faster. At
present, I don't really know why this is, but I am curious. At some point, I might do some more testing, rigorous
benchmarking and profiling, and maybe even learn a bit more about Java byte code to see what the Kotlin compiler is
doing that the Java compiler isn't.
