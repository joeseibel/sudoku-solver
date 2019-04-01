package sudokusolver.kotlin.logic

import sudokusolver.kotlin.Board
import sudokusolver.kotlin.Cell
import sudokusolver.kotlin.RemoveCandidates
import sudokusolver.kotlin.SudokuNumber
import sudokusolver.kotlin.UnsolvedCell
import sudokusolver.kotlin.enumMinus
import sudokusolver.kotlin.enumUnion
import sudokusolver.kotlin.mergeToRemoveCandidates
import sudokusolver.kotlin.zipEveryQuad
import java.util.EnumSet

/*
 * http://www.sudokuwiki.org/Hidden_Candidates#HQ
 *
 * If four candidates exist across four cells in a unit, then those four candidates must be placed in those four cells.
 * All other candidates can be removed from those four cells.
 *
 * For each unit
 *   For each quad of candidates
 *     Find every unsolved cell that has any of the candidates
 *     If there are four cells and each candidate is found among the four cells
 *       Remove every other candidate from the four cells
 */
fun hiddenQuads(board: Board<Cell>): List<RemoveCandidates> =
    board.units.flatMap { unit ->
        SudokuNumber.values().zipEveryQuad().mapNotNull { (a, b, c, d) ->
            unit.filterIsInstance<UnsolvedCell>()
                .filter { a in it.candidates || b in it.candidates || c in it.candidates || d in it.candidates }
                .takeIf { it.size == 4 }
                ?.takeIf { cells ->
                    val union = enumUnion(*cells.map { it.candidates }.toTypedArray())
                    a in union && b in union && c in union && d in union
                }
                ?.flatMap { cell ->
                    (cell.candidates enumMinus EnumSet.of(a, b, c, d)).map { candidate -> cell to candidate }
                }
        }.flatten()
    }.mergeToRemoveCandidates()