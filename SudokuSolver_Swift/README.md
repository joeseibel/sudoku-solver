# Swift Implementation

I first looked at Swift back in 2016. I heard that Apple had been working on a modern language to replace Objective-C
and I was intrigued to look at it. I have no experience with Objective-C or iOS development, but I have an interest in
programming languages and wanted to see what Swift had to offer. I read through the official
[The Swift Programming Language](https://docs.swift.org/latest/documentation/the-swift-programming-language/) book and
was highly impressed with the language. Coming from a Java background, I found it very easy to grasp concepts in Swift.
The book was well written and is perfect for an experienced developer who wants to learn Swift. As for the language
itself, I found that it offered all of the features that one would expect from a modern language and it even addressed
some of the pain points that I had experienced in Java. While Swift does have a community focus on iOS development, I do
find it to be a very capable general-purpose language.

## Development Setup

The Swift implementation of the solver was created as an Xcode project. I do want to convert it to a SwiftPM project
which would allow the solver to be developed on a Linux or Windows machine, but I have not done that yet. At present,
developing the Swift implementation of the solver requires a Mac.

Follow these steps on a Mac to setup a development environment:

1. Clone this repo by running `git clone https://github.com/joeseibel/sudoku-solver.git`.
2. Open the App Store, search for Xcode, and install it.
3. Launch Xcode. You may see a dialog indicating that additional components must be installed. If so, then proceed with
   the installation. You should see a dialog listing additional components to install which support Apple's various
   operating systems. The only OS component which is required for the solver is macOS.
4. Choose to open a project and select the `sudoku-solver/SudokuSolver_Swift/SudokuSolver_Swift.xcodeproj` file.
5. If you attempt to build the project by selecting **Product** -> **Build** from the main menu or pressing Command-B,
   it might fail and give the errors '*No Accounts*' and '*No signing certificate "Mac Development" found*'. This can be
   solved by either disabling signing or signing in with an Apple account.
   1. To disable signing:
      1. In the Project navigator, select the `SudokuSolver_Swift` project.
      2. Under **Targets**, select the `SudokuSolver_Swift` target.
      3. Select the **Signing & Capabilities** tab.
      4. Deselect the **Automatically manage signing** checkbox.
      5. Under **Targets**, select the `SudokuSolver_Swift_Tests` target.
      6. Select the **Signing & Capabilities** tab.
      7. Deselect the **Automatically manage signing** checkbox.
   2. To sign in with an Apple account:
      1. In the main menu, select **Xcode** -> **Settings...**.
      2. On the left, select **Apple Accounts** and then click **Sign In...**.
      3. Enter your credentials to sign in.

### Running the Solver

The `SudokuSolver_Swift` Xcode project contains a build scheme which already has a board specified for it's command line
arguments. Follow these steps to specify a different board and run the solver:

1. The center of Xcode's toolbar has a scheme selector which should be displaying the `SudokuSolver_Swift` scheme. Click
   on the scheme selector and choose **Edit Scheme...**.
2. On the left, select **Run**.
3. Select the **Arguments** tab.
4. Under **Arguments Passed On Launch**, edit the existing argument and paste in the board to solve as a sequence of 81
   digits, e.g., `010040560230615080000800100050020008600781005900060020006008000080473056045090010`.
5. Click **Close**.
6. Run the solver by either selecting **Product** -> **Run** from the main menu or pressing Command-R.

### Running the Unit Tests

To run the unit tests, either select **Product** -> **Test** from the main menu or press Command-U.

## My Experience with Swift

I really enjoyed programming in Swift. On the surface, programming in Swift feels similar to programming in Kotlin. Both
languages have a similar syntax for lambdas and optionals. They both support and promote a mostly, but not purely,
functional programming style. They both have properties, algebraic data types, extensions, and other modern features.
Coming from the JVM and already knowing Kotlin, it was a very easy transition to learn the details of Swift.

While there are surface level similarities, there are some key differences when you dig a little deeper. Swift was the
first non-JVM language that I implemented the solver in, and I noticed that Swift is not as enthusiastic about
object-oriented programming as Java is. While Swift does have classes and it is possible to create complicated Java-like
class hierarchies, Swift encourages programmers to embrace its value types over classes. I think this is a decision that
makes sense, especially since the software industry went way too overboard in its embrace of OOP back in the 90s and
early 2000s, but it does have some subtle implications about how a programmer handles the movement of data through a
program. I had to unlearn some of my Java ways of doing things as I left the world of "everything is a reference" and
embraced value semantics.

Overall, I had a fun time learning Swift. Anytime I ran into an issue in which Swift wasn't behaving the way I expected
it to, I would take a step back and really look at the issue with Swift's core design decisions in mind. At the end of
the day, anything that didn't feel intuitive really was designed well, but was running up against my Java background. I
feel that both Java and Swift have been very well designed given their different core design philosophies.

In the following sections, I talk about some of Swift's features that I think are worth mentioning. Most of these will
be features that I think make Swift a great programming language, but I will also talk about a couple pain points that I
encountered. Note that the following is not a tutorial, but I instead describe what I like or dislike about these
features and what it was like for me to use them.

### The Official Book

In my opinion, the best way to learn how to program in Swift is to read
[The Swift Programming Language](https://docs.swift.org/latest/documentation/the-swift-programming-language). It is a
very well written book and contains everything an experienced developer would need in order to become familiar with
Swift. The book assumes prior programming experience, which was great for me. It didn't try to teach me what types are
or how loops work, it simply showed me how these features work in Swift. The book is thorough, detailed, and full of
good examples.

One thing that I was very pleased about is that while the book assumes prior programming experience, it does not assume
experience with Objective-C. In fact, I found that Objective-C was referenced rather infrequently in the book even
though Swift was intended to replace Objective-C. All of the concepts were explained in a way that practically any
programmer could comprehend them.

The process of learning Swift provided an interesting contrast to learning Kotlin. When I was learning Kotlin, I found
that its documentation was well written and very easy to understand, but I was coming from a Java background, so I was
already familiar with the problems in Java that Kotlin was addressing. I honestly have no idea what learning Kotlin
would be like for a developer that wasn't experienced in Java.

Bringing things back to Swift, I was now in the place of not being experienced with the previous language. To this day,
I'm still not familiar with the pain points in Objective-C that Swift was designed to address, but that didn't really
matter. The language was so well designed and the book was so well written that I didn't just learn the syntax and
semantics of Swift features, but it often felt like I was also understanding Swift's core design decisions. The book
helped me to get the "why" behind Swift.

### Reference Counting

Swift is rare among programming languages in that it utilizes
[reference counting](https://docs.swift.org/latest/documentation/the-swift-programming-language/automaticreferencecounting)
to manage memory. This is a fundamental and key distinction between Swift and the Java world that I am used to. For the
most part, working with Swift's reference counting feels the same as working with Java's garbage collector. One can
simply allocate new objects on the heap when needed and the runtime will deallocate those objects when they are no
longer needed. However, reference counting does place an additional burden on the programmer: handling cycles.

For languages like Java that use tracing garbage collection, cycles pose no problem whatsoever as the garbage collector
can easily deallocate cycles when the objects of a cycle are no longer needed. However, this is not the case for a
reference counted language like Swift. A naively implemented cycle in Swift will never be deallocated because the
reference counts for the objects of a cycle will never reach zero. A Swift programmer must recognize the presence of a
cycle and mark some of the references as either `weak` or `unowned`. These kinds of references are not counted and it is
thus possible for an object to be deallocated even when there are `weak` or `unowned` references to that object. A
`weak` reference is always of an optional type and will be `nil` when the object is no longer available. An `unowned`
reference will instead cause a runtime error if it is accessed after the object has been released. Additionally,
closures in Swift are reference types and can contribute to cycles. These kinds of cycles can be addressed by specifying
`weak` or `unowned` references in a closure's capture list. I have not needed to use either `weak` or `unowned`
references in the solver, so in this case, working with reference counting has felt exactly like working with a garbage
collector.

I feel like I don't have a strong preference between tracing garbage collection and reference counting. The advantage of
garbage collection is that it is easier for the programmer while the advantages of reference counting is that it is more
deterministic and requires less CPU and memory resources. It makes sense to me that Apple chose reference counting for
Swift so that the battery life of devices can be extended. Garbage collection utilizes the CPU and memory more than
reference counting which does impact battery life. Perhaps garbage collected languages like Java are better suited to
server applications while reference counted languages like Swift are better suited to smart phones and smart watches.

### Value Types and Reference Types

Swift draws a clear distinction between
[value types and reference types](https://www.swift.org/documentation/articles/value-and-reference-types.html). Structs,
enumerations, and tuples are value types while classes, actors, and closures are reference types. This is similar to the
distinction between primitive types and reference types in Java. However, working with value types in Swift is much more
expansive than working with primitive types in Java.

Swift has both
[structs and classes](https://docs.swift.org/latest/documentation/the-swift-programming-language/classesandstructures/).
and the key distinction between the two is that structs are value types while classes are reference types. Let's
consider a simple example of a `Point` type that has x and y coordinates and is implemented as a struct:

```swift
struct Point {
    var x: Double
    var y: Double
}
```

As a result of `Point` being a struct which is a value type, anytime a `Point` is passed around, a copy is made. Any
modifications to the copy are not reflected on the original.

Another advantage of value types has to do with how they are stored in memory. Suppose that we have an array of
`Point`s:

```swift
let points = [
    Point(x: 0, y: 0),
    Point(x: 1, y: 1),
    // And so on...
]
```

In this example, all of the individual `Point` objects will be stored next to each other in memory. This has the
advantage that it will reduce cache misses when iterating through the `points` array. Even though `Point` could be
implemented as a class in Swift, it really does make more sense to implement it as a struct.

If I were to implement a similar `Point` type in Java, it would have to be a class and therefore be a reference type:

```java
public class Point {
    public double x;
    public double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }
}
```

Java currently does not have a way to implement a type that utilizes composition as a value type. This has real
performance implications especially if we have an array of `Point`s:

```java
var points = new Point[]{
        new Point(0, 0),
        new Point(1, 1),
        // And so on...
};
```

In this example, the `points` array is not really an array of `Point` objects. It is instead an array of references to
`Point` objects. The distinction is that while all of the references will be stored contiguously in memory, each
individual `Point` object that is referenced could be stored anywhere on the heap. They might be stored near each other,
but they might not be. If they are not stored near each other, that can result in more cache misses when iterating
through the `points` array. This is a well known limitation in Java and they are working on addressing this by adding
[value objects](https://openjdk.org/projects/valhalla/value-objects).

Due to the advantages of using value types, Swift highly encourages programmers to choose structs over classes and to
only use classes [when needed](https://developer.apple.com/documentation/swift/choosing-between-structures-and-classes).

#### Assigning to `self`

One interesting thing about value types is that it is possible to assign a value to `self` in a method of a value type.
Here is an example method that could be added to `Point` which reassigns `self`:

```swift
mutating func moveToOrigin() {
    self = Point(x: 0, y: 0)
}
```

In the solver, I mostly use this technique of assigning to `self` in custom initializers on enums. For example, this is
an initializer for the enum `SudokuNumber` which is used to convert a `Character` to a `SudokuNumber`:

```swift
init(number: Character) {
    guard let number = SudokuNumber(rawValue: number) else {
        preconditionFailure(#"Invalid character: "\#(number)", must be between "1" and "9"."#)
    }
    self = number
}
```

The fact that I was able to assign to `self` allowed me to implement this functionality as an initializer. Otherwise, I
would have had to implement this as a regular function such as this equivalent function in Kotlin:

```kotlin
fun sudokuNumber(ch: Char): SudokuNumber =
    when (ch) {
        '1' -> SudokuNumber.ONE
        '2' -> SudokuNumber.TWO
        '3' -> SudokuNumber.THREE
        '4' -> SudokuNumber.FOUR
        '5' -> SudokuNumber.FIVE
        '6' -> SudokuNumber.SIX
        '7' -> SudokuNumber.SEVEN
        '8' -> SudokuNumber.EIGHT
        '9' -> SudokuNumber.NINE
        else -> throw IllegalArgumentException("ch is '$ch', must be between '1' and '9'.")
    }
```

I also use this technique for type conversions such as converting a `Board<SudokuNumber?>` to a `Board<Cell>`:

```swift
extension Board<Cell> {
    init(toCellBoard board: Board<SudokuNumber?>) {
        self = board.mapCellsIndexed { row, column, cell in
            if let cell {
                Cell(row: row, column: column, value: cell)
            } else {
                Cell(row: row, column: column)
            }
        }
    }
}
```

Note that assigning a value to `self` is not possible for reference types such as classes.
