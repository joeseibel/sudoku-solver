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
 *
 * For each unit
 *   For each pair of candidates
 *     Find every unsolved cell that has the first candidate
 *     If there are two cells
 *       If those cells are also the only unsolved cells with the second candidate
 *         Remove every other candidate from the two cells
 */
fun hiddenPairs(board: Board<Cell>): List<RemoveCandidates> =
    board.units.flatMap { unit ->
        SudokuNumber.values().zipEveryPair().mapNotNull { (a, b) ->
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