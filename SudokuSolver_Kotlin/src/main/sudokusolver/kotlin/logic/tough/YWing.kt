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
        .filter { it.candidates.size == 2 }
        .zipEveryTriple()
        .filter { (a, b, c) -> enumUnion(a.candidates, b.candidates, c.candidates).size == 3 }
        .flatMap { (a, b, c) -> tryHinge(a, b, c) + tryHinge(b, a, c) + tryHinge(c, a, b) }
        .mergeToRemoveCandidates()
}