package sudokusolver.kotlin.logic

import sudokusolver.kotlin.Board
import sudokusolver.kotlin.Cell
import sudokusolver.kotlin.RemoveCandidates
import sudokusolver.kotlin.UnsolvedCell
import sudokusolver.kotlin.enumIntersect
import sudokusolver.kotlin.enumUnion
import sudokusolver.kotlin.mergeToRemoveCandidates
import sudokusolver.kotlin.zipEveryTriple

fun nakedTriples(board: Board<Cell>): List<RemoveCandidates> {
    return board.units.flatMap { unit ->
        unit.filterIsInstance<UnsolvedCell>().zipEveryTriple().mapNotNull { (a, b, c) ->
            enumUnion(a.candidates, b.candidates, c.candidates)
                .takeIf { it.size == 3 }
                ?.let { unionOfCandidates ->
                    unit.filterIsInstance<UnsolvedCell>()
                        .filter { it != a && it != b && it != c }
                        .flatMap { cell ->
                            (cell.candidates enumIntersect unionOfCandidates).map { candidate -> cell to candidate }
                        }
                }
        }.flatten()
    }.mergeToRemoveCandidates()
}