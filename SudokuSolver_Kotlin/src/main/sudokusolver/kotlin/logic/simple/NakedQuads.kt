package sudokusolver.kotlin.logic.simple

import sudokusolver.kotlin.Board
import sudokusolver.kotlin.Cell
import sudokusolver.kotlin.RemoveCandidates
import sudokusolver.kotlin.UnsolvedCell
import sudokusolver.kotlin.enumIntersect
import sudokusolver.kotlin.enumUnion
import sudokusolver.kotlin.mergeToRemoveCandidates
import sudokusolver.kotlin.zipEveryQuad

/*
 * http://www.sudokuwiki.org/Naked_Candidates#NQ
 *
 * If a unit has four unsolved cells with a total of four candidates among them, then those four candidates must be
 * placed in those four cells. The four candidates can be removed from every other cell in the unit.
 */
fun nakedQuads(board: Board<Cell>): List<RemoveCandidates> =
    board.units.flatMap { unit ->
        unit.filterIsInstance<UnsolvedCell>().zipEveryQuad().mapNotNull { (a, b, c, d) ->
            enumUnion(a.candidates, b.candidates, c.candidates, d.candidates)
                .takeIf { it.size == 4 }
                ?.let { unionOfCandidates ->
                    unit.filterIsInstance<UnsolvedCell>()
                        .filter { it != a && it != b && it != c && it != d }
                        .flatMap { cell ->
                            enumIntersect(cell.candidates, unionOfCandidates).map { candidate -> cell to candidate }
                        }
                }
        }.flatten()
    }.mergeToRemoveCandidates()