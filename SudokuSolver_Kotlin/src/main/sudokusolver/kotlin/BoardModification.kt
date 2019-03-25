package sudokusolver.kotlin

import java.util.EnumSet

/*
 * Motivation for BoardModification:
 *
 * Each logic function takes an immutable Board and returns a list of modification instructions. Why did I choose this
 * way?
 *
 * Originally, the logic functions took a MutableBoard and returned a Boolean indicating if the board was modified. The
 * solver would then try each logic function in sequence and stop when one of them actually modified the board. The
 * advantage of this approach was that if a logic function made an illegal modification, then an exception would be
 * thrown from the point of modification within the logic function. Illegal modifications include setting the value to
 * something other than the known solution, removing a candidate that isn't there, removing a candidate that is known to
 * be the solution, and modifying a solved cell.
 *
 * This approach was great for writing the solver, but had disadvantages for unit testing each logic function. The tests
 * would build the initial board, run the logic function, then assert that the final state of the board matched what was
 * expected. This made it difficult to see what a logic function was expected to do when looking at the test since the
 * test only checked the final state of the board. It would be very tedious to manually determine the difference between
 * the board's initial state and final state. It is more desirable to quickly see what changes are expected just by
 * simply looking at the test.
 *
 * Returning a List<BoardModification> from the logic functions solves this problem. This way, the tests don't assert
 * the final state of the board. They instead assert the modifications that a logic function wants to make. This greatly
 * simplifies the process of writing and understanding the tests. It is now the responsibility of the solver to apply
 * the modifications while checking the validity of the modifications.
 *
 * Return a List<BoardModification> has potential benefit if I want to log the modifications to the console or even
 * write a GUI that steps through the solver. When stepping, the GUI could highlight the candidates indicated in the
 * BoardModifications before actually applying the changes.
 *
 * The main disadvantage of this approach is that the checking for modification validity happens in the solver and not
 * in the logic functions. This means that when an IllegalStateException is thrown, the stacktrace will not show where
 * the problem is in the logic function. This also means that I can not simply set an exception breakpoint to debug the
 * logic functions. Instead, I must set a breakpoint at the creation of RemoveCandidates or SetValue and wait for the
 * erroneous BoardModification to be created. This is a significant disadvantage, but I would rather have simpler tests
 * than simpler debugging.
 *
 * Another approach that I considered was to have the logic functions take a MutableBoard and return a log indicating
 * which modifications were made instead of returning a list of modification requests. This approach would have the
 * advantage of simplifying the tests and having simpler debugging. The reason that I rejected this approach was to make
 * it easier to add parallelism to the logic functions at some point in the future.
 */
sealed class BoardModification {
    abstract val row: Int
    abstract val column: Int
}

data class RemoveCandidates(
    override val row: Int,
    override val column: Int,
    val candidates: EnumSet<SudokuNumber>
) : BoardModification() {
    constructor(cell: UnsolvedCell, candidates: EnumSet<SudokuNumber>) : this(cell.row, cell.column, candidates) {
        candidates.forEach { candidate ->
            require(candidate in cell.candidates) { "$candidate  is not a candidate for [$row, $column]" }
        }
    }
}

data class SetValue(override val row: Int, override val column: Int, val value: SudokuNumber) : BoardModification() {
    constructor(cell: UnsolvedCell, value: SudokuNumber) : this(cell.row, cell.column, value) {
        require(value in cell.candidates) { "$value is not a candidate for [$row, $column]" }
    }
}