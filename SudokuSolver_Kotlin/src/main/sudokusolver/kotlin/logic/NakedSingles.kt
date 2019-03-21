package sudokusolver.kotlin.logic

import sudokusolver.kotlin.Board
import sudokusolver.kotlin.Cell
import sudokusolver.kotlin.SetValue
import sudokusolver.kotlin.UnsolvedCell

fun nakedSingles(board: Board<Cell>): List<SetValue> =
    board.cells
        .filterIsInstance<UnsolvedCell>()
        .filter { it.candidates.size == 1 }
        .map { SetValue(it, it.candidates.first()) }