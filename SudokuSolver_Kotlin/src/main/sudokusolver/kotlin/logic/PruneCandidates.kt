package sudokusolver.kotlin.logic

import sudokusolver.kotlin.BlockIndex
import sudokusolver.kotlin.Board
import sudokusolver.kotlin.Cell
import sudokusolver.kotlin.RemoveCandidates
import sudokusolver.kotlin.SolvedCell
import sudokusolver.kotlin.UnsolvedCell
import sudokusolver.kotlin.filterValueIsInstance
import sudokusolver.kotlin.intersect
import sudokusolver.kotlin.toEnumSet

fun pruneCandidates(board: Board<Cell>): List<RemoveCandidates> {
    return board.rows.withIndex().flatMap { (rowIndex, row) ->
        row.asSequence()
            .withIndex()
            .filterValueIsInstance<UnsolvedCell>()
            .map { (columnIndex, cell) ->
                val sameRow = board.getRow(rowIndex)
                val sameColumn = board.getColumn(columnIndex)
                val sameBlock = board.getBlock(BlockIndex(rowIndex, columnIndex))
                val visibleValues = (sameRow + sameColumn + sameBlock)
                    .filterIsInstance<SolvedCell>()
                    .map { it.value }
                    .toEnumSet()
                val toRemove = cell.candidates intersect visibleValues
                IndexedValue(columnIndex, toRemove)
            }
            .filter { (_, toRemove) -> toRemove.isNotEmpty() }
            .map { (columnIndex, toRemove) -> RemoveCandidates(rowIndex, columnIndex, toRemove) }
            .toList()
    }
}