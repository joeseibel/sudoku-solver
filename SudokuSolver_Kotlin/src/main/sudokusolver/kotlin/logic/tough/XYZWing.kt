package sudokusolver.kotlin.logic.tough

import sudokusolver.kotlin.Board
import sudokusolver.kotlin.Cell
import sudokusolver.kotlin.RemoveCandidates
import sudokusolver.kotlin.UnsolvedCell
import sudokusolver.kotlin.enumIntersect
import sudokusolver.kotlin.enumUnion
import sudokusolver.kotlin.mergeToRemoveCandidates
import sudokusolver.kotlin.zipEveryPair

/*
 * http://www.sudokuwiki.org/XYZ_Wing
 *
 * Given a hinge cell and two wing cells such that the hinge can see both wings, the hinge has three candidates, the
 * wings have two candidates each, there is one candidate shared among all three cells, two candidates are shared
 * between the hinge and one wing and two candidates between the hinge and the other wing, and the common candidate is
 * the only one shared between the wings, then the common candidate must be the solution to one of the cells. The common
 * candidate can be removed from any cell which can see all three cells.
 */
fun xyzWing(board: Board<Cell>): List<RemoveCandidates> =
    board.cells.filterIsInstance<UnsolvedCell>().filter { it.candidates.size == 3 }.flatMap { hinge ->
        board.cells
            .filterIsInstance<UnsolvedCell>()
            .zipEveryPair()
            .filter { (wingA, wingB) ->
                wingA.candidates.size == 2 && wingB.candidates.size == 2 &&
                        hinge isInSameUnit wingA && hinge isInSameUnit wingB &&
                        enumUnion(hinge.candidates, wingA.candidates, wingB.candidates).size == 3
            }
            .mapNotNull { (wingA, wingB) ->
                enumIntersect(wingA.candidates, wingB.candidates).singleOrNull()
                    ?.let { candidate ->
                        board.cells
                            .filterIsInstance<UnsolvedCell>()
                            .filter { cell ->
                                cell != hinge && cell != wingA && cell != wingB &&
                                        candidate in cell.candidates &&
                                        cell isInSameUnit hinge && cell isInSameUnit wingA && cell isInSameUnit wingB
                            }
                            .map { it to candidate }
                    }
            }
            .flatten()
    }.mergeToRemoveCandidates()