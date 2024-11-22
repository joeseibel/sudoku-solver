package sudokusolver.kotlin.logic.simple

import sudokusolver.kotlin.Board
import sudokusolver.kotlin.Cell
import sudokusolver.kotlin.RemoveCandidates
import sudokusolver.kotlin.SudokuNumber
import sudokusolver.kotlin.UnsolvedCell
import sudokusolver.kotlin.enumMinus
import sudokusolver.kotlin.mergeToRemoveCandidates
import sudokusolver.kotlin.zipEveryPair
import java.util.EnumSet

/*
 * http://www.sudokuwiki.org/Hidden_Candidates#HP
 *
 * If a pair of candidates exists in exactly two cells in a unit, then those two candidates must be placed in those two
 * cells. All other candidates can be removed from those two cells.
 */
fun hiddenPairs(board: Board<Cell>): List<RemoveCandidates> =
    board.units.flatMap { unit ->
        SudokuNumber.entries.zipEveryPair().mapNotNull { (a, b) ->
            unit.filterIsInstance<UnsolvedCell>()
                .filter { a in it.candidates }
                .takeIf { cells ->
                    cells.size == 2 && cells == unit.filterIsInstance<UnsolvedCell>().filter { b in it.candidates }
                }
                ?.flatMap { cell ->
                    (cell.candidates enumMinus EnumSet.of(a, b)).map { candidate -> cell to candidate }
                }
        }.flatten()
    }.mergeToRemoveCandidates()