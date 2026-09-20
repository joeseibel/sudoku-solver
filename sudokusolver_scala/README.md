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

### For Comprehensions

One of the more interesting syntactic sugar features that Scala has to offer is
[for comprehensions](https://docs.scala-lang.org/tour/for-comprehensions.html). This construct allows a programmer to
replace multiple calls to `flatMap`, `filter`, and `map` into a single condensed expression. Often times, a for
comprehension can be used to reduce layers of nesting and make it easier to express multiple collection processing
operations. A for comprehension consists of the following components:

- **A List of Enumerators**: Each enumerator can be one of the following kinds:
  - **Generators**: These are the fundamental enumerators of a for comprehension and define the collections that the for
    comprehension will loop through. Generators take the form `name <- collection` and specify that the collection will
    be looped through and that each element will be accessible through the given name.
  - **Guards**: A guard takes the form `if condition` and defines what elements will be filtered out of the for
    comprehension.
  - **Assignments**: An assignment takes the form `name = expression` and simply makes the name available in the
    remainder of the for comprehension.
- **Yield Statement**: The yield statement is an expression, which might be a code block, that returns a value. The
  yield statement is evaluated for each combination of elements produced by all the generators and filtered out by the
  guards. The final result of the whole for comprehension is a collection of all the elements that are produced by the
  multiple executions of the yield statement.

To demonstrate the benefits of a for comprehension, let's look at a few examples. This first example is the method
`zipEveryQuad` from the file [`Collections.scala`](src/main/scala/sudokusolver/scala/Collections.scala):

```scala
def zipEveryQuad: IndexedSeq[(T, T, T, T)] =
  for
    (first, firstIndex) <- seq.zipWithIndex
    (second, secondIndex) <- seq.zipWithIndex.drop(firstIndex + 1)
    (third, thirdIndex) <- seq.zipWithIndex.drop(secondIndex + 1)
    fourth <- seq.drop(thirdIndex + 1)
  yield (first, second, third, fourth)
```

The method `zipEveryQuad` only has four generators, no guards, and no assignments. This is what `zipEveryQuad` would
look like without using a for comprehension:

```scala
def zipEveryQuad: IndexedSeq[(T, T, T, T)] =
  seq.zipWithIndex.flatMap { (first, firstIndex) =>
    seq.zipWithIndex.drop(firstIndex + 1).flatMap { (second, secondIndex) =>
      seq.zipWithIndex.drop(secondIndex + 1).flatMap { (third, thirdIndex) =>
        seq.drop(thirdIndex + 1).map(fourth => (first, second, third, fourth))
      }
    }
  }
```

I personally find the version with the for comprehension a little easier to read than the version without. This next
example is the method `xCyclesRule2` from the file
[`XCycles.scala`](src/main/scala/sudokusolver/scala/logic/diabolical/XCycles.scala):

```scala
def xCyclesRule2(board: Board[Cell]): Seq[SetValue] =
  for
    candidate <- SudokuNumber.values.toSeq
    graph = createStrongLinksXCycles(board, candidate).addWeakLinksXCycles()
    vertex <- graph.nodes
    if alternatingCycleExists(graph, vertex, Strength.STRONG)
  yield SetValue(vertex, candidate)
```

This is a little more of an interesting example since `xCyclesRule2` has two generators, a guard, and an assignment.
This is what `xCyclesRule2` would look like without using a for comprehension:

```scala
def xCyclesRule2(board: Board[Cell]): Seq[SetValue] =
  SudokuNumber.values.toSeq.flatMap { candidate =>
    val graph = createStrongLinksXCycles(board, candidate).addWeakLinksXCycles()
    graph.nodes.filter(vertex => alternatingCycleExists(graph, vertex, STRONG)).map(SetValue(_, candidate))
  }
```

Finally, let's look at one of the more complicated for comprehensions in the solver. This one is found in the method
`uniqueRectanglesType3BWithTriplePseudoCells` from the file
[`UniqueRectangles.scala`](src/main/scala/sudokusolver/scala/logic/diabolical/UniqueRectangles.scala):

```scala
for
  (tripleA, tripleB) <- unit.toIndexedSeq.zipEveryPair
  tripleCandidates = additionalCandidates | tripleA.candidates | tripleB.candidates
  if tripleCandidates.size == 3
  cell <- unit
  if cell != tripleA && cell != tripleB
  candidate <- cell.candidates & tripleCandidates
yield cell -> candidate
```

This example has three generators, two guards, and an assignment. The above example would look like the following if it
wasn't a for comprehension:

```scala
unit.toIndexedSeq.zipEveryPair.flatMap { (tripleA, tripleB) =>
  val tripleCandidates = additionalCandidates | tripleA.candidates | tripleB.candidates
  if tripleCandidates.size == 3 then
    unit.filter(cell => cell != tripleA && cell != tripleB)
      .flatMap(cell => (cell.candidates & tripleCandidates).map(cell -> _))
  else
    Nil
}
```

As far as I have seen, Scala seems to be unique with its offering of for comprehensions. Yes, Python does have list
comprehensions, but they are much simpler than Scala's for comprehensions. Python's list comprehensions are best suited
for handling a single filtering operation and/or a single transformation operation on a list. They are not suited for
anything more complex, but Scala's for comprehensions handle the complexity very well.

Even though I appreciate for comprehensions, I find that I don't miss them too much in other languages. Using `flatMap`,
`filter`, and `map` work very well and I find using them to be readable enough. So, as far as modern programming
language features go, I find for comprehensions to be a neat idea, but I don't think I would push for it in languages
like Kotlin, Swift, or Rust.

### Partial Functions

Another unique offering that Scala provides are
[partial functions](https://docs.scala-lang.org/scala3/book/fun-partial-functions.html). These are functions that accept
only a subset of values of the function's parameter types. For example, it is possible to specify a partial function
that accepts a parameter with the type of `int`, but only accepts integers that are positive and even. In this example,
the function would not be able to accept a negative integer, zero, or an odd integer and would throw an exception if
such a value were to be passed to the function. All partial functions are actually objects that implement the trait
`PartialFunction` and have the method `isDefinedAt` which is used to determine if the function accepts a particular
value.

In practice, it is rare for someone to manually write a class that implements `PartialFunction` as there is special
lambda syntax that gets compiled into a partial function. Instead of starting a lambda with a parameter list, a partial
function starts with one or more pattern matching cases. A partial function only applies for values that match any of
the cases. Let us look at a simple example of a partial function:

```scala
{ case cell: UnsolvedCell if cell.candidates.contains(candidate) => cell }
```

In this example, the partial function only applies for values that are instances of `UnsolvedCell` in which the guard
`if cell.candidates.contains(candidate)` evaluates to `true`. All other values are rejected by this partial function.

Partial functions are mostly used as a parameter to the `collect` method which is found on Scala's collections.
`collect` filters items in the collection to only the values that the partial function accepts and then calls the
partial function for each value. This is a syntactically concise way of combining filtering and transformation in a
single step. Here is an example of a partial function from
[`NakedSingles.scala`](src/main/scala/sudokusolver/scala/logic/simple/NakedSingles.scala) that filters by type, contains
a guard, and performs a transformation:

```scala
def nakedSingles(board: Board[Cell]): Seq[SetValue] =
  board.cells.collect { case cell: UnsolvedCell if cell.candidates.size == 1 => SetValue(cell, cell.candidates.head) }
```

The concept of the `collect` method paired with a partial function is something that I have not seen in other languages
so far. In many other languages, the closest alternative to partial functions would be to call a flat map based
operation and pass in a lambda that returns an optional. A good example of this difference can be found in the method
`groupedXCyclesRule3` from the file
[`GroupedXCycles.scala`](src/main/scala/sudokusolver/scala/logic/extreme/GroupedXCycles.scala). Scala uses `collect` and
a partial function while Swift and Rust both use flat map on a lambda that returns an optional. Here is the Scala
version:

```scala
graph.nodes
  .map(_.outer)
  .collect { case cell: UnsolvedCell if alternatingCycleExists(graph, cell, Strength.WEAK) => cell -> candidate }
```

The Swift version instead uses `compactMap` which is similar to `flatMap` except that it is used for lambdas that return
an `Optional`:

```swift
graph.indices.compactMap { index in
    if case .cell(let cell) = graph.vertexAtIndex(index),
        alternatingCycleExists(graph: graph, index: index, adjacentEdgesType: .weak)
    {
        (cell, candidate)
    } else {
        nil
    }
}
```

The Rust version uses `filter_map` which is also similar to `flat_map` except that it expects a lambda which returns an
`Option`:

```rust
graph.node_indices().filter_map(move |index| match graph[index].as_cell_node() {
    Ok(cell) if graphs::alternating_cycle_exists(&graph, index, Strength::Weak) => Some((cell, candidate)),
    _ => None,
})
```

As you can see from the above examples, Scala's `collect` is a little more concise than the Swift and Rust versions.

Finally, I use `collect` and partial functions a lot in the solver to simply filter by type. This often times looks
something like this:

```scala
board.cells.collect { case cell: UnsolvedCell => cell }
```

I do find this to be a little more clunky than Kotlin's `filterIsInstance`, which I greatly prefer:

```kotlin
board.cells.filterIsInstance<UnsolvedCell>()
```

### Union Types

One of the neat features that Scala offers are [union types](https://docs.scala-lang.org/scala3/book/types-union.html).
They allow a programmer to specify a list of acceptable types without having to create a separate hierarchy of subtypes.
As such, union types are one of Scala's algebraic data types alongside sealed types and enumerations.

When would you want to use a sealed type and when would you want to use a union type? My general approach is that if I
am in control of all of the member types and it makes sense for them to all be declared in the same file, then I will
use a sealed type. Otherwise, I will use a union type. Union types can be very helpful when you want an algebraic data
type that includes a type that you are not in control of.

I have used union types exactly once in the solver. Here is the declaration of the sole union type found in the file
[`GroupedXCycles.scala`](src/main/scala/sudokusolver/scala/logic/extreme/GroupedXCycles.scala):

```scala
type Node = UnsolvedCell | Group
```

In this case, I wanted the types `Node` and `Group` to be local to the file `GroupedXCycles.scala`. However, the type
`UnsolvedCell` is not declared in the same file, so making `Node` a sealed type would be a bit more tricky. A union type
allows `UnsolvedCell` to be untouched and to be used here.

The other JVM languages that I have implemented the solver in don't have union types. In those situations, I used a
traditional type hierarchy, but I had to create a wrapper type called `CellNode` that only contains an `UnsolvedCell`.
Here is what `Node` and `CellNode` look like in Kotlin:

```kotlin
interface Node {
    val row: Int?
    val column: Int?
    val block: Int
    val cells: Set<UnsolvedCell>
}

data class CellNode(val cell: UnsolvedCell) : Node {
    override val row: Int = cell.row
    override val column: Int = cell.column
    override val block: Int = cell.block
    override val cells: Set<UnsolvedCell> by lazy { setOf(cell) }

    override fun toString(): String = cell.vertexLabel
}
```

I'm happy that Scala provides union types as they can be used to solve some specific problems, but I feel like their
usefulness is a little limited simply because Scala offers sealed types as well.

### Other Modern Features

Scala has a number of nice features that also show up in Kotlin and Java. Since I've written about these features
extensively in my descriptions of Kotlin and Java, I felt that I didn't need to repeat myself here, but it was still
worthwhile to mention them:

- [Sealed types](https://docs.scala-lang.org/scala3/book/types-adts-gadts.html): These work exactly the same as sealed
  types in Kotlin and Java.
- [Case classes](https://docs.scala-lang.org/tour/case-classes.html): These are very similar to data classes in Kotlin
  and records in Java. Similar to Kotlin and Java, case classes in Scala can be used in pattern matching, but the
  mechanism that makes this work is different in each language.
- [Extension methods](https://docs.scala-lang.org/scala3/book/ca-extension-methods.html): These works very similar to
  extensions in Kotlin, but the syntax is a bit different.

Sealed types and case classes showed up in Scala first, then arrived in Kotlin, and then, many years later, finally
arrived in Java. Even though I'm not writing about these features in great detail here, I do want to acknowledge that
Scala pioneered them in the JVM.

Extension methods are a more recent addition and have only showed up in Scala 3. They have replaced implicit classes.
One thing that Scala 3 tried to address was all of the confusion around implicit classes, implicit conversions, and
implicit parameters. The `implicit` keyword has caused so much confusion in Scala 2 and has allowed people to write
completely unreadable code. Scala listened to its users and gave us more manageable constructs like extension methods.

### Syntax

Scala's syntax is an area that has unfortunately provided some friction for me. I didn't notice this at first when
writing Scala, but I noticed it later when returning to Scala after being away from the language for a while. There have
been times when I'll look at the Scala implementation of the solver for the first time in a while and have a hard time
understanding what my code is doing. I'll understand it again once I go look up again the specific syntax that was
confusing me. For me to understand Scala syntax, I have to be actively developing in Scala. Otherwise, I find the syntax
to at times be unintuitive. I have never had this problem with any of my other implementations. For example, I always
find Kotlin syntax to be intuitive, even if I haven't been working with Kotlin in a while.

#### Underscores

My first complaint is that Scala has way too many uses for the underscore character. I won't get into all of its uses,
but I will highlight a couple of them. First of all, the underscore is used as a placeholder for a name instead of
having an unused name. This shows up a lot in pattern matching such as in the following incomplete example:

```scala
board(modification.row, modification.column) match
  case SolvedCell(row, column, _) => throw IllegalStateException(s"[$row, $column] is already solved.")
```

In this case, the `row` and `column` of `SolvedCell` are needed, but not `value`, so it has been replaced with an
underscore.

In addition to pattern matching, this kind of usage also shows up when destructuring a tuple and some elements of the
tuple are not needed:

```scala
nextEdgesAndVertices.exists((_, nextVertex) => nextVertex == end)
```

In this case, `nextEdge` is not needed, so it has been replaced with an underscore.

This kind of usage of the underscore is fine and it shows up in other languages as well, but this is where it should
stop. In Scala, the underscore is also used as an implicit name of a lambda parameter. In this case, it is a little like
Kotlin's `it` or Swift's `$0`. I find this to be a bit confusing because I'm used to thinking, "I don't care about that
value," when I see an underscore. However, when it's used in a lambda, I really do care about its value.

Another issue with using the underscore in lambdas is what happens when the underscore is used multiple times in the
same lambda. Each subsequent use of the lambda refers to the next lambda parameter. This is wild, crazy, and I think it
is bad design. In this regard, Scala's underscore is very different from Kotlin's `it` which always refers to the same
parameter regardless of how many times it is used. Scala's underscore is more like Swift's `$0`, `$1`, `$2`, and so on.
For example, let's look at the following Scala code:

```scala
val unitACandidates = unitA.map(_.candidates).reduce(_ | _)
```

In this case, the two underscores in the call to `reduce` refer to the first and second lambda parameters respectively.
I think this is absolutely bonkers and makes Scala code more challenging to read. Here is the equivalent code in Swift:

```swift
let unitACandidates = unitA.reduce(Set()) { $0.union($1.candidates) }
```

Note that Swift's `reduce` is more like `fold` in other languages, so the function call looks a little different.
Anyway, isn't it so much easier to see what `$0` and `$1` are doing rather than what `_` and `_` are doing?

#### Lambdas

One of my complaints about Scala's syntax is that its
[lambdas](https://docs.scala-lang.org/scala3/book/fun-anonymous-functions.html) are not syntactically distinctive, so it
is easy to miss that there is a lambda present when looking at Scala code. Many of the lambdas that I have written in
Scala are simply passed to another function in parentheses, but some are enclosed in curly braces when they span
multiple lines. Here is an example of a lambda that I find syntactically easy to miss:

```scala
unit.find(_.candidates == additionalCandidates)
```

I will admit that the underscore helps me to notice that there is a lambda, but given that underscores can mean so many
things in Scala, I find its help to be limited. My eyes have glazed over many Scala lambdas like this one without
realizing that they were lambdas.

The other languages that I have explored all have some kind of distinctive syntax to their lambdas. For example, both
Kotlin and Swift require their lambdas to be enclosed in curly braces. The conventional formatting for both languages
also has extra spaces surrounding the curly braces which helps the lambdas to stand out. Here is the same code example
in Kotlin (the Swift version looks very similar):

```kotlin
unit.find { it.candidates == additionalCandidates }
```

Java's lambdas don't require curly braces, but they always have the `->` symbol. There is no shortened lambda syntax
without the arrow. Here is the same code example in Java:

```java
unit.stream().filter(cell -> cell.candidates().equals(additionalCandidates)).findFirst()
```

Rust's lambdas always have two vertical bars (`||`) for a lambda's parameter list, even if there are no parameters. Here
is the same example in Rust:

```rust
unit.iter().find(|cell| cell.candidates() == additional_candidates)
```

I don't know how Scala's lambda syntax compares with other functional programming languages such as Haskell, Lisp,
OCaml, etc, but at least compared with the other languages that I have explored, I appreciate the syntactic
distinctiveness that the other languages offer, especially Kotlin and Swift.

#### Too Many Symbols

One complaint that has been raised against Scala's readability is that there are too many symbols and that it can be
difficult to keep track of all of them. I even found this
[post](https://www.geekabyte.io/2016/09/making-sense-of-symbols-in-scalas.html) which tries to make sense of Scala's
symbols. I have personally found that learning and understanding Scala's symbols while actively developing Scala code
isn't too bad. The problem that I run into happens when I step away from Scala for a while and then try to look at some
of my Scala code months or years down the road. It always takes me a while to "remember" what the symbols mean.

One symbol that I use frequently is the double plus operator (`++`). For anyone experienced with languages that have a
C-style syntax, this operator can be very counterintuitive. It does not increment anything. It is instead a binary
operator that concatenates two collections, such as in the following example:

```scala
rowModifications ++ columnModifications
```

This operator is doubly counterintuitive because the single plus operator (`+`) is used to do the same thing in Kotlin
and Swift:

```kotlin
rowModifications + columnModifications
```

If you try to use the `+` to join two collections in Scala, this will fail because the `+` operator is used to combine a
collection with a single element such as in this example:

```scala
val nextVisited = visited + nextVertex
```

So, keeping the operators `++` and `+` straight can be challenging when switching between Scala and other languages.

To add even more confusion to the mix, when you want to add a single element to the front of a list, you use the double
colon operator (`::`) and not the `+` operator, such as in this example:

```scala
getCellBuilders(tail, builder :: builders)
```

The `::` operator can also be used in pattern matching to separate the first element of a list from the remainder of the
list:

```scala
case ch :: tail => collectCandidates(tail, ch :: candidates)
```

These operators can even be chained together to separate out multiple elements of a list:

```scala
case '{' :: '}' :: _ => throw IllegalArgumentException("Empty \"{}\".")
```

It does seem like Scala's language designers realized that the symbols were getting out of hand. Apparently, the `:/`
operator used to be used as an alias for the method `foldRight` and the `/:` operator used to be used for `foldLeft`.
These operators have been deprecated and compiler will now warn the programmer to write out the words `foldLeft` and
`foldRight`.

All of this considered, I do think that Scala's use of symbolic operators increases the conciseness of Scala code at the
cost of readability. If Java is at one end of the verbosity spectrum and Scala is at the other end, I feel like Kotlin
is a happy medium.

#### Python-like Syntax

Scala 3 has introduced an optional Python-like
[syntax](https://docs.scala-lang.org/scala3/new-in-scala3.html#new--shiny-the-syntax) which uses indentation instead of
curly braces for many constructs. The old syntax is still supported, so it is possible to mix both the old syntax and
new syntax in a single Scala file. I decided that I wanted to fully explore the new syntax, so I set the `-new-syntax`
compiler flag in my `build.sbt` file. This flag causes the compiler to issue a warning anytime the old syntax is used.

To demonstrate this new syntax, let's look at the `@main` method of the solver found in
[`SudokuSolver.scala`](src/main/scala/sudokusolver/scala/SudokuSolver.scala). This is what the method looks like with
the new syntax:

```scala
@main def sudokuSolver(board: String): Unit =
  if board.length != UnitSizeSquared || board.exists(!('0' to '9').contains(_)) then
    println(s"board must be $UnitSizeSquared numbers with blanks expressed as 0")
  else
    val message = solve(parseOptionalBoard(board)) match
      case InvalidNoSolutions => "No Solutions"
      case InvalidMultipleSolutions => "Multiple Solutions"
      case Solution(board) => board
      case unableToSolve: UnableToSolve => unableToSolve.message
    println(message)
```

You'll notice that there are no curly braces and the indentation is significant. This is what the same method looks like
with the old syntax:

```scala
@main def sudokuSolver(board: String): Unit = {
  if (board.length != UnitSizeSquared || board.exists(!('0' to '9').contains(_))) {
    println(s"board must be $UnitSizeSquared numbers with blanks expressed as 0")
  } else {
    val message = solve(parseOptionalBoard(board)) match {
      case InvalidNoSolutions => "No Solutions"
      case InvalidMultipleSolutions => "Multiple Solutions"
      case Solution(board) => board
      case unableToSolve: UnableToSolve => unableToSolve.message
    }
    println(message)
  }
}
```

With the old syntax, there are curly braces for the method body, the if-then-else, and the match. The `then` keyword has
been removed and the conditional in the if has been enclosed in parentheses.

As a long time Java programmer, I don't find the new syntax all that exciting. I'm sure it is great for Python
programmers coming to Scala, but I wonder how much benefit it really brings to the language. I think that having two
valid syntax options for many constructs does decrease the readability of Scala code. In my opinion, Scala 3 would have
been completely fine staying as a curly brace language.
