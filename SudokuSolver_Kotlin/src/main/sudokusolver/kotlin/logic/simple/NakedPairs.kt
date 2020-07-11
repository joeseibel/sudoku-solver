package sudokusolver.kotlin.logic.simple

import sudokusolver.kotlin.Board
import sudokusolver.kotlin.Cell
import sudokusolver.kotlin.RemoveCandidates
import sudokusolver.kotlin.UnsolvedCell
import sudokusolver.kotlin.enumIntersect
import sudokusolver.kotlin.mergeToRemoveCandidates
import sudokusolver.kotlin.zipEveryPair

/*
 * http://www.sudokuwiki.org/Naked_Candidates#NP
 *
 * If a pair of unsolved cells in a unit has the same two candidates, then those two candidates must be placed in those
 * two cells. The two candidates can be removed from every other cell in the unit.
 *
 * For each unit
 *   For each pair of unsolved cells
 *     If the cells have the same candidates and they are two in number
 *       Remove the candidates from every other cell in the unit
 */
fun nakedPairs(board: Board<Cell>): List<RemoveCandidates> =
    board.units.flatMap { unit ->
        unit.filterIsInstance<UnsolvedCell>()
            .filter { it.candidates.size == 2 }
            .zipEveryPair()
            .filter { (a, b) -> a.candidates == b.candidates }
            .flatMap { (a, b) ->
                unit.filterIsInstance<UnsolvedCell>()
                    .filter { it != a && it != b }
                    .flatMap { cell ->
                        enumIntersect(cell.candidates, a.candidates).map { candidate -> cell to candidate }
                    }
            }
    }.mergeToRemoveCandidates()