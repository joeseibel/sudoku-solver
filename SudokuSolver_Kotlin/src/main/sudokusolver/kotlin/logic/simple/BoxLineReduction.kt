package sudokusolver.kotlin.logic.simple

import sudokusolver.kotlin.Board
import sudokusolver.kotlin.Cell
import sudokusolver.kotlin.RemoveCandidates
import sudokusolver.kotlin.SudokuNumber
import sudokusolver.kotlin.UnsolvedCell
import sudokusolver.kotlin.mergeToRemoveCandidates

/*
 * http://www.sudokuwiki.org/Intersection_Removal#LBR
 *
 * For a given row, if a candidate appears in only one block, then the candidate for that block must be placed in that
 * row. The candidate can be removed from the cells which are in the same block, but different rows.
 *
 * For a given column, if a candidate appears in only one block, then the candidate for that block must be placed in
 * that column. The candidate can be removed from cells which are in the same block, but different columns.
 */
fun boxLineReduction(board: Board<Cell>): List<RemoveCandidates> =
    SudokuNumber.entries.flatMap { candidate ->

        fun boxLineReduction(units: List<List<Cell>>, getUnitIndex: (Cell) -> Int) =
            units.mapNotNull { unit ->
                unit.asSequence()
                    .filterIsInstance<UnsolvedCell>()
                    .filter { candidate in it.candidates }
                    .map { it.block }
                    .toSet()
                    .singleOrNull()
                    ?.let { blockIndex ->
                        val unitIndex = getUnitIndex(unit.first())
                        board.getBlock(blockIndex)
                            .filterIsInstance<UnsolvedCell>()
                            .filter { getUnitIndex(it) != unitIndex && candidate in it.candidates }
                            .map { it to candidate }
                    }
            }.flatten()

        val rowRemovals = boxLineReduction(board.rows) { it.row }
        val columnRemovals = boxLineReduction(board.columns) { it.column }
        rowRemovals + columnRemovals
    }.mergeToRemoveCandidates()