package sudokusolver.kotlin

import java.util.*

fun pruneCandidates(board: Board<Cell>): List<RemoveCandidates> {
    return board.rows.withIndex().flatMap { (rowIndex, row) ->
        row.withIndex()
            .map { (columnIndex, cell) ->
                val sameRow = board.getRow(rowIndex)
                val sameColumn = board.getColumn(columnIndex)
                val sameBlock = board.getBlock(BlockIndex(rowIndex, columnIndex))
                val visibleValues = (sameRow + sameColumn + sameBlock).mapNotNull(Cell::value).toEnumSet()
                val toRemove = cell.candidates.toEnumSet() intersect visibleValues
                IndexedValue(columnIndex, toRemove)
            }
            .filter { (_, toRemove) -> toRemove.isNotEmpty() }
            .map { (columnIndex, toRemove) -> RemoveCandidates(rowIndex, columnIndex, toRemove) }
    }
}

private fun <T : Enum<T>> Iterable<T>.toEnumSet(): EnumSet<T> =
    this as? EnumSet ?: EnumSet.copyOf(this as? Collection ?: toSet())

private infix fun <T : Enum<T>> EnumSet<T>.intersect(other: EnumSet<T>): EnumSet<T> {
    val set = EnumSet.copyOf(this)
    set.retainAll(other)
    return set
}

fun fillSolvedCells(board: Board<Cell>): List<SetValue> {
    return board.rows.withIndex().flatMap { (rowIndex, row) ->
        row.withIndex()
            .filter { (_, cell) -> cell.candidates.size == 1 }
            .map { (columnIndex, cell) -> SetValue(rowIndex, columnIndex, cell.candidates.first()) }
    }
}

sealed class BoardModification
data class RemoveCandidates(val row: Int, val column: Int, val candidates: Iterable<SudokuNumber>) : BoardModification()
data class SetValue(val row: Int, val column: Int, val value: SudokuNumber) : BoardModification()