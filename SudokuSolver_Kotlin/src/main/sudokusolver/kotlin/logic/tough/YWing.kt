package sudokusolver.kotlin.logic.tough

import sudokusolver.kotlin.Board
import sudokusolver.kotlin.Cell
import sudokusolver.kotlin.LocatedCandidate
import sudokusolver.kotlin.RemoveCandidates
import sudokusolver.kotlin.UnsolvedCell
import sudokusolver.kotlin.enumIntersect
import sudokusolver.kotlin.enumUnion
import sudokusolver.kotlin.mergeToRemoveCandidates
import sudokusolver.kotlin.zipEveryTriple

/*
 * http://www.sudokuwiki.org/Y_Wing_Strategy
 *
 * Given a hinge cell and two wing cells such that the hinge can see both wings, all three cells each have two
 * candidates, there are three total candidates across the three cells, the hinge shares one candidate with one wing and
 * one candidate with the other wing, and the wing cells share a candidate among each other, then this third candidate
 * must be the solution to one of the wings. The third candidate can be removed from any cell which can see both wings.
 *
 * For each triple of unsolved cells
 *   If each cell has two candidates and there are three total candidates among the triple
 *     For each cell in the triple
 *       If the cell can see the others and it shares one candidate with each of the others
 *         The cell is a hinge and the other two are wings
 *           For each unsolved cell in the board
 *             If the cell is not a wing
 *               If the cell has the one candidate shared among the wings and it can see each wing
 *                 Remove the candidate from the cell
 */
fun yWing(board: Board<Cell>): List<RemoveCandidates> {

    fun tryHinge(hinge: UnsolvedCell, wingA: UnsolvedCell, wingB: UnsolvedCell): List<LocatedCandidate> {
        val wingCandidates = enumIntersect(wingA.candidates, wingB.candidates)
        return if (hinge isInSameUnit wingA && hinge isInSameUnit wingB &&
            enumIntersect(hinge.candidates, wingA.candidates).size == 1 &&
            enumIntersect(hinge.candidates, wingB.candidates).size == 1 &&
            wingCandidates.size == 1
        ) {
            val candidate = wingCandidates.single()
            board.cells
                .filterIsInstance<UnsolvedCell>()
                .filter { cell ->
                    cell != wingA && cell != wingB &&
                            candidate in cell.candidates &&
                            cell isInSameUnit wingA && cell isInSameUnit wingB
                }
                .map { it to candidate }
        } else {
            emptyList()
        }
    }

    return board.cells
        .filterIsInstance<UnsolvedCell>()
        .zipEveryTriple()
        .filter { (a, b, c) ->
            a.candidates.size == 2 && b.candidates.size == 2 && c.candidates.size == 2 &&
                    enumUnion(a.candidates, b.candidates, c.candidates).size == 3
        }
        .flatMap { (a, b, c) -> tryHinge(a, b, c) + tryHinge(b, a, c) + tryHinge(c, a, b) }
        .mergeToRemoveCandidates()
}