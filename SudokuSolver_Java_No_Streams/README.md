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
