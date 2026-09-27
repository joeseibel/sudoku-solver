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
