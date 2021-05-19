package sudokusolver.kotlin.logic.simple

import sudokusolver.kotlin.Board
import sudokusolver.kotlin.Cell
import sudokusolver.kotlin.SetValue
import sudokusolver.kotlin.SudokuNumber
import sudokusolver.kotlin.UnsolvedCell

/*
 * http://www.sudokuwiki.org/Getting_Started
 *
 * If a candidate exists in only one cell in a unit, then the candidate must be placed in that cell.
 */
fun hiddenSingles(board: Board<Cell>): List<SetValue> =
    board.units.flatMap { unit ->
        val unsolved = unit.filterIsInstance<UnsolvedCell>()
        SudokuNumber.values().mapNotNull { candidate ->
            unsolved.singleOrNull { candidate in it.candidates }?.let { SetValue(it, candidate) }
        }
    }.distinct()