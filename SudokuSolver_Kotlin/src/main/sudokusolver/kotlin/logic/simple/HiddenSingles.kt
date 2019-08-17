package sudokusolver.kotlin.logic.simple

import sudokusolver.kotlin.Board
import sudokusolver.kotlin.Cell
import sudokusolver.kotlin.SetValue
import sudokusolver.kotlin.SudokuNumber
import sudokusolver.kotlin.UnsolvedCell

/*
 * http://www.sudokuwiki.org/Getting_Started
 *
 * If a candidate exists in only one cell in a unit, then the candidate must be placed in that cell.
 *
 * For each unit
 *   For each candidate
 *     Find every unsolved cell that has the candidate
 *       If there is one cell
 *         Set the candidate as the value for the cell
 */
fun hiddenSingles(board: Board<Cell>): List<SetValue> =
    board.units.flatMap { unit ->
        val unsolved = unit.filterIsInstance<UnsolvedCell>()
        SudokuNumber.values().mapNotNull { number ->
            unsolved.singleOrNull { number in it.candidates }?.let { SetValue(it, number) }
        }
    }.distinct()