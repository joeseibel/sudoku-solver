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
    val candidatesToRemove = board.units.flatMap { unit ->
        unit.filterIsInstance<UnsolvedCell>()
            .filter { it.candidates.size == 2 }
            .zipEvery()
            .filter { (a, b) -> a.candidates == b.candidates }
            .flatMap { (a, b) ->
                unit.filterIsInstance<UnsolvedCell>()
                    .filter { it != a && it != b }
                    .flatMap { cell -> (cell.candidates intersect a.candidates).map { candidate -> cell to candidate } }
            }
    }
    return candidatesToRemove.groupingBy { (cell, _) -> cell }
        .fold(
            { _, _ -> EnumSet.noneOf(SudokuNumber::class.java) },
            { _, accumulator, (_, candidate) -> accumulator.also { it += candidate } }
        )
        .map { (cell, candidates) -> RemoveCandidates(cell, candidates) }
}