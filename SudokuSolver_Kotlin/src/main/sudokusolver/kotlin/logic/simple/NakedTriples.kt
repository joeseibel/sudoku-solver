package sudokusolver.kotlin.logic.simple

import sudokusolver.kotlin.Board
import sudokusolver.kotlin.Cell
import sudokusolver.kotlin.RemoveCandidates
import sudokusolver.kotlin.UnsolvedCell
import sudokusolver.kotlin.enumIntersect
import sudokusolver.kotlin.enumUnion
import sudokusolver.kotlin.mergeToRemoveCandidates
import sudokusolver.kotlin.zipEveryTriple

/*
 * http://www.sudokuwiki.org/Naked_Candidates#NT
 *
 * If a unit has three unsolved cells with a total of three candidates among them, then those three candidates must be
 * placed in those three cells. The three candidates can be removed from every other cell in the unit.
 */
fun nakedTriples(board: Board<Cell>): List<RemoveCandidates> =
    board.units.flatMap { unit ->
        unit.filterIsInstance<UnsolvedCell>().zipEveryTriple().mapNotNull { (a, b, c) ->
            enumUnion(a.candidates, b.candidates, c.candidates)
                .takeIf { it.size == 3 }
                ?.let { unionOfCandidates ->
                    unit.filterIsInstance<UnsolvedCell>()
                        .filter { it != a && it != b && it != c }
                        .flatMap { cell ->
                            enumIntersect(cell.candidates, unionOfCandidates).map { candidate -> cell to candidate }
                        }
                }
        }.flatten()
    }.mergeToRemoveCandidates()