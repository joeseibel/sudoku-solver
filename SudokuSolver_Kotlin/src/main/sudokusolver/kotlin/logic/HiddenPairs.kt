package sudokusolver.kotlin.logic

import sudokusolver.kotlin.Board
import sudokusolver.kotlin.Cell
import sudokusolver.kotlin.RemoveCandidates
import sudokusolver.kotlin.SudokuNumber
import sudokusolver.kotlin.UnsolvedCell
import sudokusolver.kotlin.enumMinus
import sudokusolver.kotlin.mergeToRemoveCandidates
import sudokusolver.kotlin.zipEveryPair
import java.util.EnumSet

fun hiddenPairs(board: Board<Cell>): List<RemoveCandidates> {
    return board.units.flatMap { unit ->
        SudokuNumber.values().zipEveryPair().mapNotNull { (a, b) ->
            unit.filterIsInstance<UnsolvedCell>()
                .filter { a in it.candidates }
                .takeIf { cells ->
                    cells.size == 2 && cells == unit.filterIsInstance<UnsolvedCell>().filter { b in it.candidates }
                }
                ?.flatMap { cell ->
                    (cell.candidates enumMinus EnumSet.of(a, b)).map { candidate -> cell to candidate }
                }
        }.flatten()
    }.mergeToRemoveCandidates()
}