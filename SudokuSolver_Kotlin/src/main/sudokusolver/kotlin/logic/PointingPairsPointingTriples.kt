package sudokusolver.kotlin.logic

import sudokusolver.kotlin.Board
import sudokusolver.kotlin.Cell
import sudokusolver.kotlin.RemoveCandidates
import sudokusolver.kotlin.SudokuNumber
import sudokusolver.kotlin.UnsolvedCell
import sudokusolver.kotlin.mergeToRemoveCandidates

/*
 * http://www.sudokuwiki.org/Intersection_Removal#IR
 *
 * For a given block, if a candidate appears in only one row, then the candidate for that row must be placed in that
 * block. The candidate can be removed from cells which are in the same row, but different blocks.
 *
 * For a given block, if a candidate appears in only one column, then the candidate for that column must be placed in
 * that block. The candidate can be removed from cells which are in the same column, but different blocks.
 *
 * For each block
 *   For each candidate
 *     If the candidate appears in unsolved cells of the same row
 *       Remove the candidate from cells of the row which are outside of the block
 *     If the candidate appears in unsolved cells of the same column
 *       Remove the candidate from cells of the column which are outside of the block
 */
fun pointingPairsPointingTriples(board: Board<Cell>): List<RemoveCandidates> =
    board.blocks.flatMap { block ->
        val unsolved = block.filterIsInstance<UnsolvedCell>()
        SudokuNumber.values().mapNotNull { candidate ->

            fun removeFromOtherBlocks(unit: List<Cell>) =
                unit.filterIsInstance<UnsolvedCell>()
                    .filter { it.block != block.first().block && candidate in it.candidates }
                    .map { it to candidate }

            val withCandidate = unsolved.filter { candidate in it.candidates }
            when {
                withCandidate.map { it.row }.toSet().size == 1 ->
                    removeFromOtherBlocks(board.getRow(withCandidate.first().row))

                withCandidate.map { it.column }.toSet().size == 1 ->
                    removeFromOtherBlocks(board.getColumn(withCandidate.first().column))

                else -> null
            }
        }.flatten()
    }.mergeToRemoveCandidates()