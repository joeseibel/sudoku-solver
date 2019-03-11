package sudokusolver.kotlin

import java.util.*

fun pruneCandidates(board: AbstractBoard<Cell>): List<RemoveCandidates> {
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
                    .map(SolvedCell::value)
                    .toEnumSet()
                val toRemove = cell.candidates intersect visibleValues
                IndexedValue(columnIndex, toRemove)
            }
            .filter { (_, toRemove) -> toRemove.isNotEmpty() }
            .map { (columnIndex, toRemove) -> RemoveCandidates(rowIndex, columnIndex, toRemove) }
            .toList()
    }
}

private fun <T : Enum<T>> Iterable<T>.toEnumSet(): EnumSet<T> =
    this as? EnumSet ?: EnumSet.copyOf(this as? Collection ?: toSet())

private infix fun <T : Enum<T>> EnumSet<T>.intersect(other: EnumSet<T>): EnumSet<T> {
    val set = EnumSet.copyOf(this)
    set.retainAll(other)
    return set
}

private inline fun <reified R> Sequence<IndexedValue<*>>.filterValueIsInstance(): Sequence<IndexedValue<R>> =
    filter { (_, value) -> value is R }.map { (index, value) -> IndexedValue(index, value as R) }

private inline fun <reified R> Iterable<IndexedValue<*>>.filterValueIsInstance(): List<IndexedValue<R>> =
    filter { (_, value) -> value is R }.map { (index, value) -> IndexedValue(index, value as R) }

fun fillSolvedCells(board: AbstractBoard<Cell>): List<SetValue> {
    return board.rows.withIndex().flatMap { (rowIndex, row) ->
        row.withIndex()
            .filterValueIsInstance<UnsolvedCell>()
            .filter { (_, cell) -> cell.candidates.size == 1 }
            .map { (columnIndex, cell) -> SetValue(rowIndex, columnIndex, cell.candidates.first()) }
    }
}

sealed class BoardModification {
    abstract val row: Int
    abstract val column: Int
}

data class RemoveCandidates(
    override val row: Int,
    override val column: Int,
    val candidates: Iterable<SudokuNumber>
) : BoardModification()

data class SetValue(override val row: Int, override val column: Int, val value: SudokuNumber) : BoardModification()