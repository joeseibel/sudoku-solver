package sudokusolver.kotlin.logic.extreme

import sudokusolver.kotlin.Board
import sudokusolver.kotlin.Cell
import sudokusolver.kotlin.RemoveCandidates
import sudokusolver.kotlin.SudokuNumber
import sudokusolver.kotlin.UnsolvedCell
import sudokusolver.kotlin.enumIntersect
import sudokusolver.kotlin.enumUnion
import sudokusolver.kotlin.mergeToRemoveCandidates
import sudokusolver.kotlin.zipEveryPair
import sudokusolver.kotlin.zipEveryQuad
import sudokusolver.kotlin.zipEveryTriple
import java.util.EnumSet

/*
 * https://www.sudokuwiki.org/Sue_De_Coq
 *
 * This solution starts with looking for two or three cells in the same linear unit (row or column) and block and the
 * union of candidates across the cells has a size which is at least two more than the number of cells. In other words,
 * if two cells are selected, then they must have at least four candidates. If three cells are selected, they must have
 * at least five candidates. These cells are the main group in this solution.
 *
 * Once the main group is identified, this solution then searches for an Almost Locked Set in the same linear unit as
 * the main group and also for an ALS in the same block as the main group. As a reminder, an ALS is a set of n unsolved
 * cells, all of which can see each other, and there are n + 1 candidates across all n cells. The two ALSs can only
 * contain candidates found in the main group, they must contain all the candidates of the main group, and there can be
 * no common candidates across the two ALSs.
 *
 * Once we have the main group and the two ALSs, it is then certain that each of the common candidates must appear in
 * one of the three groups. Therefore, for any common candidate, that candidate cannot be the solution for any cell
 * which can see the main group and can see the ALS that has the candidate. The candidates of the linear unit ALS can be
 * removed from other cells of that linear unit which are not a part of the main group. The candidates of the block unit
 * ALS can be removed from other cells of that block which are not a part of the main group.
 */
fun sueDeCoq(board: Board<Cell>): List<RemoveCandidates> {

    fun sueDeCoq(units: List<List<Cell>>, getUnitIndex: (Cell) -> Int) =
        units.map { it.filterIsInstance<UnsolvedCell>() }.flatMap { unit ->
            unit.groupBy { it.block }.flatMap { (blockIndex, unitByBlock) ->
                val otherCellsInUnit = unit.filter { it.block != blockIndex }
                val block = board.getBlock(blockIndex).filterIsInstance<UnsolvedCell>()
                val otherCellsInBlock = block.filter { getUnitIndex(it) != getUnitIndex(unit.first()) }

                fun getGroupRemovals(group: List<UnsolvedCell>) =
                    enumUnion(*group.map { it.candidates }.toTypedArray())
                        .takeIf { it.size >= group.size + 2 }
                        ?.let { candidates ->
                            getAlmostLockedSets(otherCellsInUnit, candidates).flatMap { unitALS ->
                                getAlmostLockedSets(otherCellsInBlock, candidates)
                                    .filter { blockALS ->
                                        unitALS.candidates.size + blockALS.candidates.size == candidates.size &&
                                                enumIntersect(unitALS.candidates, blockALS.candidates).isEmpty()
                                    }
                                    .flatMap { blockALS ->
                                        val unitRemovals = unit.filter { it !in group && it !in unitALS.cells }
                                            .flatMap { cell ->
                                                enumIntersect(cell.candidates, unitALS.candidates).map { cell to it }
                                            }
                                        val blockRemovals = block.filter { it !in group && it !in blockALS.cells }
                                            .flatMap { cell ->
                                                enumIntersect(cell.candidates, blockALS.candidates).map { cell to it }
                                            }
                                        unitRemovals + blockRemovals
                                    }
                            }
                        }
                        .orEmpty()

                when (unitByBlock.size) {
                    2 -> getGroupRemovals(unitByBlock)
                    3 -> getGroupRemovals(unitByBlock) + unitByBlock.zipEveryPair()
                        .flatMap { getGroupRemovals(it.toList()) }

                    else -> emptyList()
                }
            }
        }

    val rowRemovals = sueDeCoq(board.rows, Cell::row)
    val columnRemovals = sueDeCoq(board.columns, Cell::column)
    return (rowRemovals + columnRemovals).mergeToRemoveCandidates()
}

private class ALS(val cells: Set<UnsolvedCell>, val candidates: EnumSet<SudokuNumber>)

private fun getAlmostLockedSets(cells: List<UnsolvedCell>, groupCandidates: EnumSet<SudokuNumber>): List<ALS> {
    val almostLockedSets1 = cells.filter { it.candidates.size == 2 && groupCandidates.containsAll(it.candidates) }
        .map { ALS(setOf(it), it.candidates) }
    val almostLockedSets2 = cells.zipEveryPair()
        .map { (a, b) -> ALS(setOf(a, b), enumUnion(a.candidates, b.candidates)) }
        .filter { it.candidates.size == 3 && groupCandidates.containsAll(it.candidates) }
    val almostLockedSets3 = cells.zipEveryTriple()
        .map { (a, b, c) -> ALS(setOf(a, b, c), enumUnion(a.candidates, b.candidates, c.candidates)) }
        .filter { it.candidates.size == 4 && groupCandidates.containsAll(it.candidates) }
    val almostLockedSets4 = cells.zipEveryQuad()
        .map { (a, b, c, d) ->
            ALS(setOf(a, b, c, d), enumUnion(a.candidates, b.candidates, c.candidates, d.candidates))
        }
        .filter { it.candidates.size == 5 && groupCandidates.containsAll(it.candidates) }
    return almostLockedSets1 + almostLockedSets2 + almostLockedSets3 + almostLockedSets4
}