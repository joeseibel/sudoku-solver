package sudokusolver.kotlin.logic.simple

import sudokusolver.kotlin.Board
import sudokusolver.kotlin.Cell
import sudokusolver.kotlin.RemoveCandidates
import sudokusolver.kotlin.SolvedCell
import sudokusolver.kotlin.SudokuNumber
import sudokusolver.kotlin.UnsolvedCell
import sudokusolver.kotlin.enumIntersect
import java.util.EnumSet

/*
 * If a cell is solved, then no other cells in the same unit can have that number as a candidate.
 */
fun pruneCandidates(board: Board<Cell>): List<RemoveCandidates> =
    board.cells.filterIsInstance<UnsolvedCell>().mapNotNull { cell ->
        val sameRow = board.getRow(cell.row)
        val sameColumn = board.getColumn(cell.column)
        val sameBlock = board.getBlock(cell.block)
        (sameRow + sameColumn + sameBlock)
            .filterIsInstance<SolvedCell>()
            .mapTo(EnumSet.noneOf(SudokuNumber::class.java)) { it.value }
            .let { visibleValues -> enumIntersect(cell.candidates, visibleValues) }
            .takeUnless { it.isEmpty() }
            ?.let { toRemove -> RemoveCandidates(cell, toRemove) }
    }