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
    val actual = logicFunction(board).sorted()
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