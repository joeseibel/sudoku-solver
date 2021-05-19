package sudokusolver.kotlin.logic.simple

import sudokusolver.kotlin.Board
import sudokusolver.kotlin.Cell
import sudokusolver.kotlin.RemoveCandidates
import sudokusolver.kotlin.SudokuNumber
import sudokusolver.kotlin.UnsolvedCell
import sudokusolver.kotlin.enumMinus
import sudokusolver.kotlin.enumUnion
import sudokusolver.kotlin.mergeToRemoveCandidates
import sudokusolver.kotlin.zipEveryTriple
import java.util.EnumSet

/*
 * http://www.sudokuwiki.org/Hidden_Candidates#HT
 *
 * If three candidates exist across three cells in a unit, then those three candidates must be placed in those three
 * cells. All other candidates can be removed from those three cells.
 */
fun hiddenTriples(board: Board<Cell>): List<RemoveCandidates> =
    board.units.flatMap { unit ->
        SudokuNumber.values().zipEveryTriple().mapNotNull { (a, b, c) ->
            unit.filterIsInstance<UnsolvedCell>()
                .filter { a in it.candidates || b in it.candidates || c in it.candidates }
                .takeIf { it.size == 3 }
                ?.takeIf { cells ->
                    val union = enumUnion(*cells.map { it.candidates }.toTypedArray())
                    a in union && b in union && c in union
                }
                ?.flatMap { cell ->
                    (cell.candidates enumMinus EnumSet.of(a, b, c)).map { candidate -> cell to candidate }
                }
        }.flatten()
    }.mergeToRemoveCandidates()