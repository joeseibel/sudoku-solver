package sudokusolver.kotlin.logic.diabolical

import sudokusolver.kotlin.Board
import sudokusolver.kotlin.Cell
import sudokusolver.kotlin.LocatedCandidate
import sudokusolver.kotlin.RemoveCandidates
import sudokusolver.kotlin.SudokuNumber
import sudokusolver.kotlin.UnsolvedCell
import sudokusolver.kotlin.enumIntersect
import sudokusolver.kotlin.enumUnion
import sudokusolver.kotlin.mergeToRemoveCandidates
import sudokusolver.kotlin.zipEveryPair
import sudokusolver.kotlin.zipEveryTriple
import java.util.EnumSet

/*
 * https://www.sudokuwiki.org/Extended_Unique_Rectangles
 *
 * Extended Unique Rectangles are like Unique Rectangles except that they are 2x3 instead of 2x2. The cells in the
 * rectangle must be spread over three blocks and the dimension that has three elements must be spread over three units
 * (rows or columns). If there are only three candidates found among the six cells, then such a rectangle is the Deadly
 * Pattern. If there is one cell with additional candidates, then the removal of such candidates would lead to a Deadly
 * Pattern. The common candidates can be removed from the cell leaving only the additional candidates remaining.
 */
fun extendedUniqueRectangles(board: Board<Cell>): List<RemoveCandidates> =
    (getRemovals(board.rows) + getRemovals(board.columns)).mergeToRemoveCandidates()

private fun getRemovals(units: List<List<Cell>>): List<LocatedCandidate> =
    units.zipEveryPair()
        .flatMap { (unitA, unitB) ->
            (unitA zip unitB)
                .mapNotNull { (cellA, cellB) ->
                    if (cellA is UnsolvedCell && cellB is UnsolvedCell) cellA to cellB else null
                }
                .zipEveryTriple()
        }
        .map { (otherA, otherB, otherC) ->
            listOf(otherA.first, otherB.first, otherC.first) to listOf(otherA.second, otherB.second, otherC.second)
        }
        .filter { (unitA, unitB) -> (unitA + unitB).map { it.block }.toSet().size == 3 }
        .flatMap { (unitA, unitB) ->
            val unitACandidates = enumUnion(*unitA.map { it.candidates }.toTypedArray())
            val unitBCandidates = enumUnion(*unitB.map { it.candidates }.toTypedArray())
            when {
                unitACandidates.size == 3 -> getRemovals(unitACandidates, unitB, unitBCandidates)
                unitBCandidates.size == 3 -> getRemovals(unitBCandidates, unitA, unitACandidates)
                else -> emptyList()
            }
        }

private fun getRemovals(
    commonCandidates: EnumSet<SudokuNumber>,
    unit: List<UnsolvedCell>,
    unitCandidates: EnumSet<SudokuNumber>
): List<LocatedCandidate> {
    val removals = if (unitCandidates.size > 3 && unitCandidates.containsAll(commonCandidates)) {
        unit.singleOrNull { !commonCandidates.containsAll(it.candidates) }?.let { withAdditional ->
            enumIntersect(withAdditional.candidates, commonCandidates).map { withAdditional to it }
        }
    } else {
        null
    }
    return removals ?: emptyList()
}