package sudokusolver.kotlin.logic

import sudokusolver.kotlin.Board
import sudokusolver.kotlin.Cell
import sudokusolver.kotlin.RemoveCandidates
import sudokusolver.kotlin.SolvedCell
import sudokusolver.kotlin.UnsolvedCell
import sudokusolver.kotlin.intersect
import sudokusolver.kotlin.toEnumSet

fun pruneCandidates(board: Board<Cell>): List<RemoveCandidates> =
    board.cells.filterIsInstance<UnsolvedCell>().mapNotNull { cell ->
        val sameRow = board.getRow(cell.row)
        val sameColumn = board.getColumn(cell.column)
        val sameBlock = board.getBlock(cell.block)
        (sameRow + sameColumn + sameBlock)
            .filterIsInstance<SolvedCell>()
            .map { it.value }
            .toEnumSet()
            .let { visibleValues -> cell.candidates intersect visibleValues }
            .takeUnless { it.isEmpty() }
            ?.let { toRemove -> RemoveCandidates(cell, toRemove) }
    }