package sudokusolver.kotlin.logic

import sudokusolver.kotlin.Board
import sudokusolver.kotlin.Cell
import sudokusolver.kotlin.RemoveCandidates
import sudokusolver.kotlin.SudokuNumber
import sudokusolver.kotlin.UnsolvedCell
import sudokusolver.kotlin.intersect
import sudokusolver.kotlin.zipEvery
import java.util.EnumSet

fun nakedPairs(board: Board<Cell>): List<RemoveCandidates> {
    val modifications = (board.rows + board.columns + board.blocks).flatMap { unit ->
        unit.filterIsInstance<UnsolvedCell>()
            .filter { it.candidates.size == 2 }
            .zipEvery()
            .filter { (a, b) -> a.candidates == b.candidates }
            .flatMap { (a, b) ->
                unit.filterIsInstance<UnsolvedCell>()
                    .filter { it != a && it != b }
                    .mapNotNull { cell ->
                        (cell.candidates intersect a.candidates).takeUnless { it.isEmpty() }?.let { cell to it }
                    }
                    .map { (cell, candidates) -> RemoveCandidates(cell, candidates) }
            }
    }
    return modifications.groupBy { it.row to it.column }.map { (index, grouped) ->
        val (row, column) = index
        val union = grouped.fold(EnumSet.noneOf(SudokuNumber::class.java)) { set, modification ->
            set.also { it += modification.candidates }
        }
        RemoveCandidates(row, column, union)
    }
}