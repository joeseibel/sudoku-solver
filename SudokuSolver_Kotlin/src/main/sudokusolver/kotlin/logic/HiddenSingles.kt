package sudokusolver.kotlin.logic

import sudokusolver.kotlin.Board
import sudokusolver.kotlin.Cell
import sudokusolver.kotlin.SetValue
import sudokusolver.kotlin.SudokuNumber
import sudokusolver.kotlin.UnsolvedCell

fun hiddenSingles(board: Board<Cell>): List<SetValue> {
    val rowModifications = hiddenSingles(board.rows)
    val columnModifications = hiddenSingles(board.columns)
    val blockModifications = hiddenSingles(board.blocks)
    return (rowModifications + columnModifications + blockModifications).distinct()
}

private fun hiddenSingles(units: List<List<Cell>>): List<SetValue> =
    units.flatMap { unit ->
        val unsolved = unit.filterIsInstance<UnsolvedCell>()
        SudokuNumber.values().mapNotNull { number ->
            unsolved.filter { number in it.candidates }.takeIf { it.size == 1 }?.let { SetValue(it.first(), number) }
        }
    }