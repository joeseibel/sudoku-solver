package sudokusolver.kotlin.logic.simple

import sudokusolver.kotlin.Board
import sudokusolver.kotlin.Cell
import sudokusolver.kotlin.SetValue
import sudokusolver.kotlin.UnsolvedCell

/*
 * http://www.sudokuwiki.org/Getting_Started
 *
 * If an unsolved cell has exactly one candidate, then the candidate must be placed in that cell.
 *
 * For each unsolved cell
 *   If the cell has one candidate
 *     Set the candidate as the value for the cell
 */
fun nakedSingles(board: Board<Cell>): List<SetValue> =
    board.cells
        .filterIsInstance<UnsolvedCell>()
        .filter { it.candidates.size == 1 }
        .map { SetValue(it, it.candidates.single()) }