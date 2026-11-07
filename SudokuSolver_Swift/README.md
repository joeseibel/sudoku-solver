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

### Collections Are Value Types

One of the very interesting design decisions that Swift made was for all of the
[standard collections](https://docs.swift.org/latest/documentation/the-swift-programming-language/collectiontypes/) to
be value types. This is very different from languages like Java or Python in which collections are reference types. The
nice thing about this is that changes to a collection in Swift only affect that specific collection in the local
function. This is so much nicer than in Java in which modifying a collection might impact any piece of code that refers
to that collection.

In Java, when passing collections around, there is often a lot of defensive programming to ensure that modifications to
a collection in one part of the code don't interfere with how that collection is used through a shared reference in
another part of the code. This is often accomplished through copying a collection or wrapping it in an unmodifiable
view. Sometimes, this can lead to unnecessarily copying a collection just to ensure that it can't be unexpectedly
modified. To demonstrate this, let's consider a simple Java class which holds on to a list:

```java
public class Holder {
    private final List<String> values;

    public Holder(List<String> values) {
        this.values = List.copyOf(values);
    }

    public void printValues() {
        IO.println(values);
    }
}
```

You'll notice that the constructor in the above example copies the list before assigning it to the `values` field. This
is important because the list that is passed into the constructor might be modified outside of `Holder` after the call
to the constructor.

Swift handles this very differently. Let's consider the equivalent example in Swift:

```swift
struct Holder {
    let values: [String]

    func printValues() {
        print(values)
    }
}
```

As you can see in the Swift version, there is no explicit copying of the `values` array. This is because Swift arrays
are value types and a semantic copy of the array is made when it is passed to `Holder`'s initializer. If other code
modifies its version of the array after creating a `Holder`, those modifications have no impact on the `values` array in
`Holder`.

The nice thing about the Swift approach is that it makes it so much easier to reason about the integrity of a
collection. If one piece of code is holding on to a collection, it is guaranteed that no other piece of code can
interfere with that collection.

#### Copy-on-Write

Since collections in Swift are value types, couldn't this lead to unnecessary copying of large collections every time
one is passed around? Isn't this terribly inefficient? Swift uses a mechanism called
[copy-on-write](https://developer.apple.com/documentation/swift/array#Modifying-Copies-of-Arrays) to ensure that a
collection is only truly copied when needed. This allows Swift to give the appearance of copying a collection every time
it is passed around while only actually copying the collection when modifications are being made.

Swift collections such as `Array`s and `String`s are struct types, but the actual data for these collections are stored
in a reference type inside the struct type. This way, when an array is passed to a function, the fields of the struct
are copied, but not the data inside. When a collection is passed around to many parts of a codebase, all of those struct
copies contain references to the same data which has not been copied.

When a modification is performed on a collection, the collection's struct will first check to see if it is the only
struct with a reference to that particular data. If the collection holds a unique reference, then the modification
proceeds normally. If the collection does not hold onto a unique reference, then the data is copied at that point and
the copy is modified.

I really like the way that Swift designed collections as value types and optimized them through the copy-on-write
mechanism. I think this elegantly solves a collection integrity issue that is found in other languages. It is so nice to
pass around and modify collections with as much ease as integers or booleans.

#### Collection Mutability

Another way that working with collections in Swift is different from working with collections on the JVM is how
mutability is expressed. In the JVM world, collection mutability is handled through types, but there is actually a lot
of variety here. In Java, the `List` interface has all of the methods for modifying a list, but it is possible to create
an unmodifiable list which will throw an exception when why trying to modify it. Kotlin does it a little differently by
having the interfaces `List` and `MutableList`, thus clearly expressing mutability in the type system. However, a `List`
in Kotlin can sometimes be cast to a `MutableList`, thus breaking the contract of the declared type. Scala is the
strictest in the JVM world by having independent sets of mutable collection types and immutable collection types. In
Scala, it is not possible to cast from an immutable collection into a mutable one. Even with this variety, since I was
coming from the JVM world, I was very used to having immutable types for collections. This is important in JVM languages
since JVM collections are reference types.

Swift does not have different types for mutable and immutable collections. This is not necessary because Swift
collections are value types and it is not possible mutate a shared collection. However, it is still valuable to express
collection mutability in order to prevent unwanted mutations within a function. This is done simply with the keywords
`let` and `var`, just like with other simple types. In this way, a Swift collection declared as a constant with a `let`
keyword really is constant and can't have its contents change. This is different from Java in which the `final` keyword
simply means that the reference is constant, but the the contents are not constant. For example, the following is
perfectly legal in Java:

```java
final var list = new ArrayList<>(List.of(1, 2, 3));
list.add(4);
```

However, the following is not legal in Swift:

```swift
let list = [1, 2, 3]
list.append(4)
```

It took a little getting used to, but I like the way that Swift handles the expression of collection mutability.

### Enumerations

One of the features that has been gaining popularity in modern programming languages is the support for Algebraic Data
Types (ADTs). Swift supports ADTs through
[enumerations](https://docs.swift.org/latest/documentation/the-swift-programming-language/enumerations/), specifically
enumerations that have associated values. It is the associated values that distinguish the enumeration type in Swift
from the enumeration type in languages like C or Java. Here is an example enumeration with associated values that comes
from the Swift book:

```swift
enum Barcode {
    case upc(Int, Int, Int, Int)
    case qrCode(String)
}
```

In this example, a `Barcode` is either a `upc` with four integer values or a `qrCode` with one string value. One of the
nice things about these kinds of enums is that it is very easy to switch on an enum value and extract all of the
individual values of each case. Here is an example switch that also comes from the Swift book:

```swift
switch productBarcode {
case let .upc(numberSystem, manufacturer, product, check):
    print("UPC: \(numberSystem), \(manufacturer), \(product), \(check).")
case let .qrCode(productCode):
    print("QR code: \(productCode).")
}
```

Switch statements in Swift must be exhaustive. This means that if a switch statement doesn't handle every case of an
enum, then the compiler will complain. I love this because I remember the days before Java had ADTs and I wrote a lot of
code that looked like this:

```java
if (productBarcode instanceof UPC upc) {
    System.out.println("UPC: " + upc.getNumberSystem() + ", " + upc.getManufacturer() + ", " + upc.getProduct()
            + ", " + upc.getCheck() + '.');
} else if (productBarcode instanceof QRCode qrCode) {
    System.out.println("QR code: " + qrCode.getProductCode() + '.');
} else {
    throw new AssertionError("Unrecognized Barcode: " + productBarcode);
}
```

Without ADTs, the compiler can't be sure that the only valid `Barcode` objects are either `UPC` or `QRCode`. This is why
the above Java example has an else which throws an exception.

#### Individual Cases Are Not Types

Even though I love ADTs and I mostly like the way that Swift implemented them as enums with associated values, I do have
one significant complaint: the individual cases of a Swift enum are not types themselves. The key downside to this is
that is not possible to type a parameter, variable, or return type as an individual case. It must be typed as the whole
enum. To demonstrate some of my complaints, let's consider the `Cell` type which is an ADT in the solver. I originally
tried to implement `Cell` like this:

```swift
enum Cell {
    case solvedCell(row: Int, column: Int, value: SudokuNumber)
    case unsolvedCell(row: Int, column: Int, candidates: Set<SudokuNumber>)
    
    // Additional initializers and methods omitted from example.
}
```

The definition of the above `Cell` type is very concise and elegant, but it suffers from two problems. First, since the
individual cases are not types themselves, it is not possible to pass around a cell object which is known to be a
`solvedCell` or known to be an `unsolvedCell`. This is very different from how the JVM languages implement ADTs as
sealed types. In Kotlin, Java, and Scala, I can pass around an `UnsolvedCell` without any problems. One common way that
I do this is to filter a list of cells to only have `UnsolvedCell` objects such as in the following Kotlin example:

```kotlin
val unsolvedCells = cells.filterIsInstance<UnsolvedCell>()
```

In this example, `cells` has the type `List<Cell>` and `unsolvedCells` has the type `List<UnsolvedCell>`. Any further
operations that I perform on objects from this new list are guaranteed at compile time to be an unsolved cell. Swift
does not have this advantage. Anytime a `Cell` is passed around in Swift, it is not known at compile time whether it is
solved or unsolved and a check must be performed.

Another problem with my first attempt at implementing `Cell` in Swift is that there is no way to ensure that validation
is performed on the associated values. While it is possible to write custom initializers for an enum which do validate
their inputs, there is no way to force users of an enum to only use an initializer and not directly create an enum
object. For example, this following code would be legal and there is no way to prevent a user of the `Cell` enum from
doing this:

```swift
let badCell = Cell.solvedCell(row: 42, column: 85, value: .one)
```

The row and column indices of 42 and 85 are invalid, but there is way to prevent such a cell from being created.

Given these two problems, the solution I chose, and one that seems common in the Swift community, was to create the
struct types `SolvedCell` and `UnsolvedCell` and then have the individual cases contain one of these types. This is an
abbreviated version of what the `Cell` type looks like now:

```swift
enum Cell {
    case solvedCell(SolvedCell)
    case unsolvedCell(UnsolvedCell)
}

struct SolvedCell {
    let row: Int
    let column: Int
    let value: SudokuNumber
    
    fileprivate init(row: Int, column: Int, value: SudokuNumber) {
        validateRowAndColumn(row: row, column: column)
        self.row = row
        self.column = column
        self.value = value
    }
}

struct UnsolvedCell {
    let row: Int
    let column: Int
    let candidates: Set<SudokuNumber>
    
    fileprivate init(row: Int, column: Int, candidates: Set<SudokuNumber>) {
        validateRowAndColumn(row: row, column: column)
        precondition(!candidates.isEmpty, "candidates must not be empty.")
        self.row = row
        self.column = column
        self.candidates = candidates
    }
}
```

With the above version of `Cell`, it is no longer possible to create an invalid cell. It is also now possible to pass
around `SolvedCell` or `UnsolvedCell` objects. However, I do find this solution to be a bit clunky and not as nice as
working with sealed types in Kotlin. My complaint isn't a major one and I still really enjoy programming in Swift even
with this clunky workaround, but I do wish that Swift enums with associated values were as nice to work with as the JVM
sealed types.

### Optionals

I only have a few things worth mentioning about
[optional values](https://docs.swift.org/latest/documentation/the-swift-programming-language/thebasics/#Optionals)
in Swift. For the most part, they work very similar to optional types found in other languages such as Java, Scala, or
Rust.

One thing worth mentioning is that there is a bit of very nice syntactic sugar that makes working with optionals a
little easier. This first shows up with how the optional type is written itself. Instead of writing `Optional`, one can
simply append a question mark (`?`) to a type to mark it as optional. For example, the following two declarations both
have the same type:

```swift
let a: Optional<String>
let b: String?
```

Another piece of syntactic sugar is that the keyword `nil` is short for the enum case `Optional.none`. In the following
example, I assign values to both `a` and `b` and the values are equivalent:

```swift
let a: Optional<String> = .none
let b: String? = nil
```

It is also possible to use
[optional chaining](https://docs.swift.org/latest/documentation/the-swift-programming-language/optionalchaining) to
access members of an optional value that might be `nil`. This is equivalent to calling the `map` function, but with much
nicer syntax. To demonstrate this, the following two statements are also equivalent:

```swift
let a = optionalString.map { $0.uppercased() }
let b = optionalString?.uppercased()
```

The last piece of syntactic sugar that I'll talk about is that it is possible to use an actual value when an optional is
expected without wrapping the actual value in the `Optional.some` case. Like before, the following two statements are
equivalent such that `a` and `b` have the same value:

```swift
let a: String? = .some("String Value")
let b: String? = "String Value"
```

All of this syntactic sugar makes working with optionals in Swift feel like working with nullable values in Kotlin, but
under the hood, optionals in Swift work more like optionals in Java, Scala, and Rust.

The last thing I'll say about Swift optionals is that it is not possible for a non-optional to not have a value. For
example, if a variable has the type `String`, then that variable will always have a valid string value and will never be
`nil`. This is very different from optionals in the JVM world. Even though Java and Scala both have an optional type, it
is still possible to assign `null` to any reference type. This means that in much of the JVM world, distinguishing
between optional and non-optional values is something that must be opted into and is possible to circumvent. However, in
Swift, distinguishing between optionals and non optionals is required by the language and cannot be worked around. I
appreciate this as it increases the level of confidence that a successfully compiled Swift program will be correct.
