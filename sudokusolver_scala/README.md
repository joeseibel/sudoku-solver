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

## My experience with Scala

I enjoyed writing the solver in a purely functional style and Scala helps a lot with that. In particular, Scala's
immutable collections are really what has enabled me to write the solver in a purely functional manner. At some point in
the future, I want to implement the solver in Haskell and I suspect that the Scala implementation will serve as a good
baseline for the future Haskell implementation.

Even though I appreciate what Scala has to bring to functional programming, I will admit that it is not my favorite
language. I find some elements of Scala's syntax to be unintuitive, their official documentation leaves much to be
desired, and the tooling seems to be less polished. I will admit that there are many features which were in Scala for a
long time and then only later were brought into Java. It also seems like much of Kotlin's design has been inspired by
Scala. While I appreciate what Scala has brought to the JVM world, I still view it as somewhat of an academic language.
I think that Java and Kotlin are much better suited in a professional setting.

In the following sections, I talk about some of Scala's features that I think are worth mentioning. These could be
features that I really like or features that I find frustrating. In these sections, I will primarily be comparing Scala
with Java and Kotlin. Note that the following is not a tutorial, but I instead describe what I like or dislike about
these features and what it was like for me to use them.

### Immutable Collections

Unlike many of the other languages that I have explored, Scala genuinely has immutable collections. Scala supports both
[mutable and immutable collections](https://docs.scala-lang.org/overviews/collections-2.13/overview.html), but I only
used immutable collections in the solver in order to be purely functional. Once an immutable collection is constructed
with its elements, those elements cannot change. Instead of changing a collection directly, a common approach is to
create a new collection with the new elements that a change would have resulted in. Many Scala collections have methods
that make it easier to create a new collection by specifying the difference from an existing collection.

Selecting the right collection is a little more involved in Scala than other languages. For example, I use two different
sequential collection types in the solver: `Vector` and `List`. The big difference between these two is how they are
optimized for access and for updates. `Vector` is a collection that is designed to be accessed by index. Updating an
element at a given index is also more efficient in `Vector`. On the other hand, `List` is a linked list and works well
as a stack. It is efficient when adding elements to the head of the `List` and looking at the element at the head of the
`List`. This approach is different from a language like Java in which it is very common to simply default to an
`ArrayList`.

I not only utilize immutable collections, but I've also mimicked this pattern and implemented `Board` as an immutable
collection. `Board` has an `updated` method which takes row and column indices and a new element and returns a new board
with the new element at the specified indices. The old board remains unchanged. This follows the same pattern found in
Scala's `Vector` class. `Vector` also has an `updated` method which returns a new vector with the updated element.
Internally, `Board` stores its data as a `Vector` of `Vector`s representing the rows of the board. `Board`'s `updated`
method simply delegates to `Vector`'s `updated` method:

```scala
def updated[U >: T](rowIndex: Int, columnIndex: Int, element: U): Board[U] =
  Board(rows.updated(rowIndex, rows(rowIndex).updated(columnIndex, element)))
```

What is the value of having immutable collections anyway? Why bother with the restrictions that immutable collections
impose on a programmer? Having immutable collections makes it so much easier to reason about and guarantee the integrity
of collections especially when multiple parts of a program hold references to the same collection. In Scala, if there
are multiple references to the the same immutable collection, then it is very easy to guarantee that collection's
integrity. None of the holders of the various references can mutate the collection, so it is impossible to be in a
situation in which one part of a program is modifying the collection while another part of the program expects the
collection's elements to be stable. The guarantees that Scala provides are much stronger than what is available in Java
and Kotlin.

In Java, care must be taken to address this question of collection mutability. This is often addressed by copying a
collection or wrapping it in an unmodifiable view. While Java'a unmodifiable views provide runtime protection against
mutations, the compiler cannot warn about mutation attempts that will fail at runtime. Even Java's unmodifiable lists
still have methods such as `add()`, `remove()`, and `set()`, even if those methods simply throw an exception.

Kotlin at least has different interfaces for mutable and immutable types. However, this is at times only a compile time
guardrail without any runtime protection. In some situations, it is possible to cast a Kotlin `List` to a `MutableList`
and perform a mutation.

So, an immutable list in Java provides runtime protection, but no compile time checks while Kotlin does the opposite and
has compile time checks, but questionable runtime protection. Scala's collections provide the strongest immutability
guarantees at both compile time and runtime. They do not have mutating methods and cannot be cast to a mutable
counterpart. This guarantee comes at a cost though. Scala's collections are not compatible with Java collection. When
interfacing with Java code, one must often convert between Scala collection types and their corresponding types in Java.

### Tail Recursion

The Kotlin and Java implementations of the solver are mostly functional, so implementing the solver in Scala was pretty
straightforward. However, there are a couple functions in the Kotlin and Java implementations that weren't purely
functional. In particular, implementing the functions `solve()` in
[`SudokuSolver.scala`](src/main/scala/sudokusolver/scala/SudokuSolver.scala) and `parseCellsWithCandidates()` in
[`Cell.scala`](src/main/scala/sudokusolver/scala/Cell.scala) required special attention. Both of these functions in the
Kotlin implementation contain loops which in their conditionals inspect variables with changing state. This is a big no
no for writing a purely functional program.

The standard approach for addressing this problem is to convert any loop-based algorithms into their recursive-based
equivalents. But wouldn't this cause a performance penalty or potentially lead to a stack overflow? This is where
[tail recursion](https://en.wikipedia.org/wiki/Tail_call) comes in. Tail recursion is a compiler optimization which
converts a recursive function in which the recursive call is the last operation of the function into its loop equivalent
in the resulting executable. This allows a programmer to write a recursive function which has the same performance
characteristics of a while loop.

Tail recursion is a critical feature of functional programming languages as it is necessary for the performance of
functional programs. Functions in Scala can be annotated with
[`@tailrec`](https://scala-lang.org/api/3.x/scala/annotation/tailrec.html) which indicates that the programmer expects
the function to be eligible for tail recursion. If the function cannot be optimized, then an error is produced by the
compiler. Note that Scala will optimize eligible functions even if the annotation is absent. Therefore, the annotation
is solely useful as a way of expressing and checking for programmer intent. It is a way of saying to the compiler, "I
expect this function to be tail recursive. Please complain if it is not."

One thing to be aware of when writing a tail recursive function is that any exception's stack trace will be condensed.
Normally, when an exception propagates through a recursive function, there will be an entry in the stack trace for every
recursive call. This is sometimes useful as it shows the line number of each call. However, when tail recursion is used,
there is only one entry instead of many. This is because, as far as the JVM is concerned, the function is only called
once. I find that this issue doesn't cause major problems, but it is worthwhile to know about.

Converting loop-based functions to tail recursive functions was one of the more involved parts of implementing the
solver in Scala. Even though I am a big fan of functional programming in general, I do find that there are some
algorithms that are more intuitive in their loop-based form rather than their equivalent tail recursive form. To
demonstrate this, let's look at the the function `parseCellsWithCandidates()` as an example. The following is the
loop-based version of `parseCellsWithCandidates()` as it appears in the Kotlin implementation:

```kotlin
fun parseCellsWithCandidates(withCandidates: String): Board<Cell> {
    val cellBuilders = mutableListOf<(row: Int, column: Int) -> Cell>()
    var index = 0
    while (index < withCandidates.length) {
        when (val ch = withCandidates[index]) {
            '{' -> {
                index++
                val closingBrace = withCandidates.indexOf('}', index)
                require(closingBrace != -1) { "Unmatched '{'." }
                require(closingBrace != index) { "Empty \"{}\"." }
                val charsInBraces = (index..<closingBrace).map { withCandidates[it] }
                require('{' !in charsInBraces) { "Nested '{'." }
                val candidates = charsInBraces.mapTo(EnumSet.noneOf(SudokuNumber::class.java)) { sudokuNumber(it) }
                cellBuilders += { row, column -> UnsolvedCell(row, column, candidates) }
                index = closingBrace + 1
            }

            '}' -> throw IllegalArgumentException("Unmatched '}'.")

            else -> {
                val value = sudokuNumber(ch)
                cellBuilders += { row, column -> SolvedCell(row, column, value) }
                index++
            }
        }
    }
    require(cellBuilders.size == UNIT_SIZE_SQUARED) {
        "Found ${cellBuilders.size} cells, required $UNIT_SIZE_SQUARED."
    }
    return Board(cellBuilders.chunked(UNIT_SIZE).mapIndexed { rowIndex, row ->
        row.mapIndexed { columnIndex, cell -> cell(rowIndex, columnIndex) }
    })
}
```

There are a few issues that need to be addressed when converting this function to it's tail recursive equivalent.
Specifically, the variables `cellBuilders` and `index` are mutated and the function contains a while loop. Let's go
through the process of converting this function to Scala and making it purely functional step by step. For the first
step, here is the function in Scala with the same mutations as the Kotlin version:

```scala
def parseCellsWithCandidates(withCandidates: String): Board[Cell] =
  val cellBuilders = ArrayBuffer[(Int, Int) => Cell]()
  var index = 0
  while index < withCandidates.length do
    withCandidates(index) match
      case '{' =>
        index += 1
        val closingBrace = withCandidates.indexOf('}', index)
        require(closingBrace != -1, "Unmatched '{'.")
        require(closingBrace != index, "Empty \"{}\".")
        val charsInBraces = (index until closingBrace).map(withCandidates)
        require(!charsInBraces.contains('{'), "Nested '{'.")
        val candidates = charsInBraces.map(sudokuNumber).toSet
        cellBuilders += ((row, column) => UnsolvedCell(row, column, candidates))
        index = closingBrace + 1

      case '}' => throw IllegalArgumentException("Unmatched '}'.")

      case ch =>
        val value = sudokuNumber(ch)
        cellBuilders += ((row, column) => SolvedCell(row, column, value))
        index += 1
  require(cellBuilders.size == UnitSizeSquared, s"Found ${cellBuilders.size} cells, required $UnitSizeSquared.")
  val cells = for (row, rowIndex) <- cellBuilders.grouped(UnitSize).zipWithIndex yield
    for (cell, columnIndex) <- row.zipWithIndex yield cell(rowIndex, columnIndex)
  Board(cells.to(Iterable))
```

Now that we have the function in Scala, let's make it more functional. The big issue will be the while loop and the
mutations that it performs. We can replace the loop with a tail recursive function. Since the purpose of the loop is to
populate `cellBuilders`, we'll call the new function `getCellBuilders`. For now, we'll leave the mutations of
`cellBuilders` in place, but we will tackle the mutations of `index`. Instead of mutating `index`, the new function will
take `index` as a parameter and its value will be updated in the recursive call. Here is the next step of
`parseCellsWithCandidates`:

```scala
def parseCellsWithCandidates(withCandidates: String): Board[Cell] =
  val cellBuilders = ArrayBuffer[(Int, Int) => Cell]()

  @tailrec
  def getCellBuilders(index: Int): Unit =
    if index < withCandidates.length then
      withCandidates(index) match
        case '{' =>
          val nextIndex = index + 1
          val closingBrace = withCandidates.indexOf('}', nextIndex)
          require(closingBrace != -1, "Unmatched '{'.")
          require(closingBrace != nextIndex, "Empty \"{}\".")
          val charsInBraces = (nextIndex until closingBrace).map(withCandidates)
          require(!charsInBraces.contains('{'), "Nested '{'.")
          val candidates = charsInBraces.map(sudokuNumber).toSet
          cellBuilders += ((row, column) => UnsolvedCell(row, column, candidates))
          getCellBuilders(closingBrace + 1)

        case '}' => throw IllegalArgumentException("Unmatched '}'.")

        case ch =>
          val value = sudokuNumber(ch)
          cellBuilders += ((row, column) => SolvedCell(row, column, value))
          getCellBuilders(index + 1)

  getCellBuilders(0)
  require(cellBuilders.size == UnitSizeSquared, s"Found ${cellBuilders.size} cells, required $UnitSizeSquared.")
  val cells = for (row, rowIndex) <- cellBuilders.grouped(UnitSize).zipWithIndex yield
    for (cell, columnIndex) <- row.zipWithIndex yield cell(rowIndex, columnIndex)
  Board(cells.to(Iterable))
```

The next step is to handle the mutations of `cellBuilders`. Instead of having `getCellBuilders` modify the
`cellBuilders` variable, it can return the final collection. We will also need to add another parameter to keep track of
the cell builders created so far in the recursive calls. Let's also change the collection type from `ArrayBuffer` to the
immutable collection type `List`. So now, `getCellBuilders` will take an index and a list of builders created so far.
The recursive function will create a new list of builders with the new builder as its first element and then pass that
new list to the next call of the function. When the function has reached the end of the string, it will reverse the list
and return it. Finally, we will create a `CellBuilder` type alias so that we don't have to write `(Int, Int) => Cell`
multiple places. This is the next step of our function:

```scala
def parseCellsWithCandidates(withCandidates: String): Board[Cell] =
  type CellBuilder = (Int, Int) => Cell

  @tailrec
  def getCellBuilders(index: Int, builders: List[CellBuilder]): List[CellBuilder] =
    if index < withCandidates.length then
      withCandidates(index) match
        case '{' =>
          val nextIndex = index + 1
          val closingBrace = withCandidates.indexOf('}', nextIndex)
          require(closingBrace != -1, "Unmatched '{'.")
          require(closingBrace != nextIndex, "Empty \"{}\".")
          val charsInBraces = (nextIndex until closingBrace).map(withCandidates)
          require(!charsInBraces.contains('{'), "Nested '{'.")
          val candidates = charsInBraces.map(sudokuNumber).toSet
          val builder = (row, column) => UnsolvedCell(row, column, candidates)
          getCellBuilders(closingBrace + 1, builder :: builders)

        case '}' => throw IllegalArgumentException("Unmatched '}'.")

        case ch =>
          val value = sudokuNumber(ch)
          val builder = (row, column) => SolvedCell(row, column, value)
          getCellBuilders(index + 1, builder :: builders)
    else
      builders.reverse

  val cellBuilders = getCellBuilders(0, Nil)
  require(cellBuilders.size == UnitSizeSquared, s"Found ${cellBuilders.size} cells, required $UnitSizeSquared.")
  val cells = for (row, rowIndex) <- cellBuilders.grouped(UnitSize).zipWithIndex yield
    for (cell, columnIndex) <- row.zipWithIndex yield cell(rowIndex, columnIndex)
  Board(cells.to(Iterable))
```

Hooray! Our function is now purely functional. There are no more mutations. However, we aren't finished since the
function isn't quite idiomatic Scala yet. In Scala, when working with a recursive function that operates on a
collection, it is customary to use pattern matching on the collection and to split a collection into its first element
and the remainder of the collection. This often shows up as the pattern `head :: tail`. To take advantage of this, let's
convert `withCandidates` from a `String` to a `List[Char]` so that we can use pattern matching. We will also update
`getCellBuilders` so that it no longer takes an index as a parameter, but instead takes the remaining `withCandidates`
list that is left to process. Here is the next step of our function:

```scala
def parseCellsWithCandidates(withCandidates: String): Board[Cell] =
  type CellBuilder = (Int, Int) => Cell

  @tailrec
  def getCellBuilders(withCandidates: List[Char], builders: List[CellBuilder]): List[CellBuilder] = withCandidates match
    case '{' :: '}' :: _ => throw IllegalArgumentException("Empty \"{}\".")
    case '{' :: tail =>
      val closingBrace = tail.indexOf('}')
      require(closingBrace != -1, "Unmatched '{'.")
      val charsInBraces = tail.take(closingBrace)
      require(!charsInBraces.contains('{'), "Nested '{'.")
      val candidates = charsInBraces.map(sudokuNumber).toSet
      val builder = (row, column) => UnsolvedCell(row, column, candidates)
      getCellBuilders(tail.drop(closingBrace + 1), builder :: builders)
    case '}' :: _ => throw IllegalArgumentException("Unmatched '}'.")
    case ch :: tail =>
      val value = sudokuNumber(ch)
      val builder = (row, column) => SolvedCell(row, column, value)
      getCellBuilders(tail, builder :: builders)
    case Nil => builders.reverse

  val cellBuilders = getCellBuilders(withCandidates.toList, Nil)
  require(cellBuilders.size == UnitSizeSquared, s"Found ${cellBuilders.size} cells, required $UnitSizeSquared.")
  val cells = for (row, rowIndex) <- cellBuilders.grouped(UnitSize).zipWithIndex yield
    for (cell, columnIndex) <- row.zipWithIndex yield cell(rowIndex, columnIndex)
  Board(cells.to(Iterable))
```

This is better, but we aren't using pattern matching for the candidates within the braces. Can we use pattern matching
for those characters as well? It turns out that we can if we create another tail recursive function. This function will
be called `collectCandidates` and will be used to handle all of the characters in the braces. Similar to
`getCellBuilders`, `collectCandidates` will take `withCandidates` as a parameter, the `candidates` processed so far as a
parameter, and return the final set of `SudokuNumber` objects that represent all of the candidates between the braces.
However, since `collectCandidates` and `getCellBuilders` both advance through `withCandidates`, `collectCandidates` will
need to return what remains of `withCandidates` back to `getCellBuilders`. As a result of this, `collectCandidates` will
actually return the tuple `(List[Char], Set[SudokuNumber])`. The first element is what remains of `withCandidates` and
the second element contains the collected candidates. Finally, we have arrived at our last modification of
`parseCellsWithCandidates`:

```scala
def parseCellsWithCandidates(withCandidates: String): Board[Cell] =
  type CellBuilder = (Int, Int) => Cell

  @tailrec
  def getCellBuilders(withCandidates: List[Char], builders: List[CellBuilder]): List[CellBuilder] = withCandidates match
    case '{' :: '}' :: _ => throw IllegalArgumentException("Empty \"{}\".")
    case '{' :: tail =>

      @tailrec
      def collectCandidates(withCandidates: List[Char], candidates: List[Char]): (List[Char], Set[SudokuNumber]) =
        withCandidates match
          case '{' :: _ => throw IllegalArgumentException("Nested '{'.")
          case '}' :: tail => (tail, candidates.map(sudokuNumber).toSet)
          case ch :: tail => collectCandidates(tail, ch :: candidates)
          case Nil => throw IllegalArgumentException("Unmatched '{'.")

      val (nextWithCandidates, candidates) = collectCandidates(tail, Nil)
      val builder = (row, column) => UnsolvedCell(row, column, candidates)
      getCellBuilders(nextWithCandidates, builder :: builders)
    case '}' :: _ => throw IllegalArgumentException("Unmatched '}'.")
    case ch :: tail =>
      val value = sudokuNumber(ch)
      val builder = (row, column) => SolvedCell(row, column, value)
      getCellBuilders(tail, builder :: builders)
    case Nil => builders.reverse

  val cellBuilders = getCellBuilders(withCandidates.toList, Nil)
  require(cellBuilders.size == UnitSizeSquared, s"Found ${cellBuilders.size} cells, required $UnitSizeSquared.")
  val cells = for (row, rowIndex) <- cellBuilders.grouped(UnitSize).zipWithIndex yield
    for (cell, columnIndex) <- row.zipWithIndex yield cell(rowIndex, columnIndex)
  Board(cells.to(Iterable))
```

While this final version is purely functional and takes full advantage of Scala's features such as pattern matching, I
do find the Kotlin version easier to read. Therefore, I find that I am an advocate of mostly functional, but not purely
functional programming. There are a few cases in which an imperative approach is easier to understand. These cases are
rare, but they do happen.

When writing these descriptions of Scala's features, I wanted to avoid writing tutorials and simply express my opinion
of these features. For tail recursion, I decided that this would be a worthy exception. Since I rarely write purely
functional programs, I thought it would be good to document an example process of how to go from an imperative
loop-based function to its functional equivalent. If I ever need to go through this process again, I might refer to this
section to guide my process.
