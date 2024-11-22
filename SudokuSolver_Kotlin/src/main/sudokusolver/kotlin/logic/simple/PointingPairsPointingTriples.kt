package sudokusolver.kotlin.logic.simple

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
 */
fun pointingPairsPointingTriples(board: Board<Cell>): List<RemoveCandidates> =
    board.blocks.flatMap { block ->
        val unsolved = block.filterIsInstance<UnsolvedCell>()
        val blockIndex = block.first().block
        SudokuNumber.entries.flatMap { candidate ->
            val withCandidate = unsolved.filter { candidate in it.candidates }

            fun pointingPairsPointingTriples(getUnit: (Int) -> List<Cell>, getUnitIndex: (Cell) -> Int) =
                withCandidate.map(getUnitIndex)
                    .toSet()
                    .singleOrNull()
                    ?.let(getUnit)
                    ?.filterIsInstance<UnsolvedCell>()
                    ?.filter { it.block != blockIndex && candidate in it.candidates }
                    ?.map { it to candidate }
                    ?: emptyList()

            val rowModifications = pointingPairsPointingTriples(board::getRow, Cell::row)
            val columnModifications = pointingPairsPointingTriples(board::getColumn, Cell::column)
            rowModifications + columnModifications
        }
    }.mergeToRemoveCandidates()