package sudokusolver.kotlin.logic

import sudokusolver.kotlin.Board
import sudokusolver.kotlin.Cell
import sudokusolver.kotlin.SetValue
import sudokusolver.kotlin.UnsolvedCell
import sudokusolver.kotlin.filterValueIsInstance

fun nakedSingles(board: Board<Cell>): List<SetValue> {
    return board.rows.withIndex().flatMap { (rowIndex, row) ->
        row.withIndex()
            .filterValueIsInstance<UnsolvedCell>()
            .filter { (_, cell) -> cell.candidates.size == 1 }
            .map { (columnIndex, cell) -> SetValue(rowIndex, columnIndex, cell.candidates.first()) }
    }
}