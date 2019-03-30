package sudokusolver.kotlin.logic

import sudokusolver.kotlin.Board
import sudokusolver.kotlin.Cell
import sudokusolver.kotlin.RemoveCandidates
import sudokusolver.kotlin.UnsolvedCell
import sudokusolver.kotlin.enumIntersect
import sudokusolver.kotlin.mergeToRemoveCandidates
import sudokusolver.kotlin.zipEveryPair

fun nakedPairs(board: Board<Cell>): List<RemoveCandidates> {
    return board.units.flatMap { unit ->
        unit.filterIsInstance<UnsolvedCell>()
            .filter { it.candidates.size == 2 }
            .zipEveryPair()
            .filter { (a, b) -> a.candidates == b.candidates }
            .flatMap { (a, b) ->
                unit.filterIsInstance<UnsolvedCell>()
                    .filter { it != a && it != b }
                    .flatMap { cell ->
                        (cell.candidates enumIntersect a.candidates).map { candidate -> cell to candidate }
                    }
            }
    }.mergeToRemoveCandidates()
}