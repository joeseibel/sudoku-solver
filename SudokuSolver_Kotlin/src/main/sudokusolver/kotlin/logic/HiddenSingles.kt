package sudokusolver.kotlin.logic

import sudokusolver.kotlin.Board
import sudokusolver.kotlin.Cell
import sudokusolver.kotlin.SetValue
import sudokusolver.kotlin.SudokuNumber
import sudokusolver.kotlin.UnsolvedCell

fun hiddenSingles(board: Board<Cell>): List<SetValue> =
    board.units.flatMap { unit ->
        val unsolved = unit.filterIsInstance<UnsolvedCell>()
        SudokuNumber.values().mapNotNull { number ->
            unsolved.filter { number in it.candidates }
                .takeIf { it.size == 1 }
                ?.let { SetValue(it.first(), number) }
        }
    }.distinct()