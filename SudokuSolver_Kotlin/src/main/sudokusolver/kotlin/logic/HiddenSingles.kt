package sudokusolver.kotlin.logic

import sudokusolver.kotlin.BlockIndex
import sudokusolver.kotlin.Board
import sudokusolver.kotlin.Cell
import sudokusolver.kotlin.SetValue
import sudokusolver.kotlin.SudokuNumber
import sudokusolver.kotlin.UnsolvedCell
import sudokusolver.kotlin.filterValueIsInstance

fun hiddenSingles(board: Board<Cell>): List<SetValue> {
    val rowModifications = hiddenSingles(board.rows) { unitIndex, cellIndex, number ->
        SetValue(unitIndex, cellIndex, number)
    }
    val columnModifications = hiddenSingles(board.columns) { unitIndex, cellIndex, number ->
        SetValue(cellIndex, unitIndex, number)
    }
    val blockModifications = hiddenSingles(board.blocks) { unitIndex, cellIndex, number ->
        val blockIndex = BlockIndex.fromSingleIndex(unitIndex)
        SetValue(blockIndex.getCellRowIndex(cellIndex), blockIndex.getCellColumnIndex(cellIndex), number)
    }
    return (rowModifications + columnModifications + blockModifications).distinct()
}

private fun hiddenSingles(
    units: List<List<Cell>>,
    createSetValue: (unitIndex: Int, cellIndex: Int, number: SudokuNumber) -> SetValue
): List<SetValue> = units.mapIndexed { unitIndex, unit ->
    SudokuNumber.values().mapNotNull { number ->
        unit.withIndex()
            .filterValueIsInstance<UnsolvedCell>()
            .filter { (_, cell) -> number in cell.candidates }
            .takeIf { it.size == 1 }
            ?.let { it.first() }
            ?.let { (cellIndex, _) -> createSetValue(unitIndex, cellIndex, number) }
    }
}.flatten()