package sudokusolver.kotlin.logic

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertIterableEquals
import sudokusolver.kotlin.Board
import sudokusolver.kotlin.BoardModification
import sudokusolver.kotlin.Cell
import sudokusolver.kotlin.RemoveCandidates
import sudokusolver.kotlin.SetValue
import sudokusolver.kotlin.SolvedCell
import sudokusolver.kotlin.parseCellsWithCandidates

internal inline fun <T : BoardModification> assertLogicalSolution(
    expected: List<T>,
    withCandidates: String,
    logicFunction: (Board<Cell>) -> List<T>
) {
    assertLogicalSolution(expected, parseCellsWithCandidates(withCandidates), logicFunction)
}

internal inline fun <T : BoardModification> assertLogicalSolution(
    expected: List<T>,
    board: Board<Cell>,
    logicFunction: (Board<Cell>) -> List<T>
) {
    val bruteForceSolution = (bruteForce(board.mapCells { (it as? SolvedCell)?.value }) as SingleSolution).solution
    /*
     * Why am I using sortedWith(Comparator) instead of sorted() and having BoardModification implement Comparable? In
     * short, implementing Comparable for BoardModification would lead to BoardModification's natural ordering being
     * inconsistent with equals. I want to sort BoardModifications by the row and column indices only while ignoring
     * other fields. However, I want equality to check all fields, as that is useful in unit tests. Even though the
     * documentation for kotlin.Comparable is silent on this issue, kotlin.Comparable maps to java.lang.Comparable when
     * compiled and the documentation for java.lang.Comparable strongly recommends that natural orderings be consistent
     * with equals. Even though this recommendation also exists for java.lang.Comparator (kotlin.Comparator maps to
     * java.lang.Comparator), I am only creating and using the custom Comparator here with the sortedWith(Comparator)
     * method, so its usage is limited and doesn't apply generally to BoardModification.
     */
    val actual = logicFunction(board).sortedWith(compareBy({ it.row }, { it.column }))
    actual.forEach { modification ->
        val row = modification.row
        val column = modification.column
        val solution = bruteForceSolution[row, column]
        when (modification) {
            is RemoveCandidates -> assertFalse(solution in modification.candidates) {
                "Cannot remove candidate $solution from [$row, $column]"
            }

            is SetValue -> assertEquals(solution, modification.value) {
                "Cannot set value ${modification.value} to [$row, $column]. Solution is $solution"
            }
        }
    }
    assertIterableEquals(expected, actual)
}