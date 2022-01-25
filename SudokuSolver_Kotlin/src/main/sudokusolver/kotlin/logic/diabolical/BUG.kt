package sudokusolver.kotlin.logic.diabolical

import sudokusolver.kotlin.Board
import sudokusolver.kotlin.Cell
import sudokusolver.kotlin.SetValue
import sudokusolver.kotlin.UnsolvedCell

/*
 * http://www.sudokuwiki.org/BUG
 *
 * BUG applies to boards with exactly one unsolved cell with three candidates and every other unsolved cell has two
 * candidates. Removing one of the candidates from the cell with three candidates will result in a board in which all of
 * its unsolved cells have two candidates, which would have multiple solutions. Since removing that candidate from that
 * cell would lead to an invalid board, that candidate must be the solution to that cell.
 *
 * For the three candidates of the cell, two candidates will appear twice in the cell's row, twice in the cell's column,
 * and twice in the cell's block, while one candidate will appear three times in the cell's row, three times in the
 * cell's column, and three times in the cell's block. This check is only performed against the cell's row.
 */
fun bug(board: Board<Cell>): SetValue? =
    board.cells
        .filterIsInstance<UnsolvedCell>()
        .singleOrNull { it.candidates.size != 2 }
        ?.takeIf { it.candidates.size == 3 }
        ?.let { cell ->
            val row = board.getRow(cell.row).filterIsInstance<UnsolvedCell>()
            val candidate = cell.candidates.single { candidate -> row.filter { candidate in it.candidates }.size == 3 }
            SetValue(cell, candidate)
        }