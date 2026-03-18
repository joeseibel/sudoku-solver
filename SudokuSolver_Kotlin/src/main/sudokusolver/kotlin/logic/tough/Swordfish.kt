package sudokusolver.kotlin.logic.tough

import sudokusolver.kotlin.Board
import sudokusolver.kotlin.Cell
import sudokusolver.kotlin.RemoveCandidates
import sudokusolver.kotlin.SudokuNumber
import sudokusolver.kotlin.UnsolvedCell
import sudokusolver.kotlin.mergeToRemoveCandidates
import sudokusolver.kotlin.zipEveryTriple

/*
 * http://www.sudokuwiki.org/Sword_Fish_Strategy
 *
 * For a triple of rows, if a candidate appears in two or three cells for each row and the candidate appears in exactly
 * three columns across the three rows, forming a three by three grid, then the candidate must be placed in three of the
 * nine cells. The candidate can be removed from cells which are in the three columns, but different rows.
 *
 * For a triple of columns, if a candidate appears in two or three cells for each column and the candidate appears in
 * exactly three rows across the three columns, forming a three by three grid, then the candidate must be placed in
 * three of the nine cells. The candidate can be removed from cells which are in the three rows, but different columns.
 */
fun swordfish(board: Board<Cell>): List<RemoveCandidates> =
    SudokuNumber.entries.flatMap { candidate ->

        fun swordfish(units: List<List<Cell>>, getOtherUnit: (Int) -> List<Cell>, getOtherUnitIndex: (Cell) -> Int) =
            units.zipEveryTriple().mapNotNull { (unitA, unitB, unitC) ->
                val aWithCandidate = unitA.filterIsInstance<UnsolvedCell>().filter { candidate in it.candidates }
                val bWithCandidate = unitB.filterIsInstance<UnsolvedCell>().filter { candidate in it.candidates }
                val cWithCandidate = unitC.filterIsInstance<UnsolvedCell>().filter { candidate in it.candidates }
                if (aWithCandidate.size in 2..3 && bWithCandidate.size in 2..3 && cWithCandidate.size in 2..3) {
                    val withCandidate = aWithCandidate + bWithCandidate + cWithCandidate
                    withCandidate.map(getOtherUnitIndex)
                        .toSet()
                        .takeIf { it.size == 3 }
                        ?.flatMap(getOtherUnit)
                        ?.filterIsInstance<UnsolvedCell>()
                        ?.filter { cell -> candidate in cell.candidates && cell !in withCandidate }
                        ?.map { it to candidate }
                } else {
                    null
                }
            }.flatten()

        val rowRemovals = swordfish(board.rows, board::getColumn) { it.column }
        val columnRemovals = swordfish(board.columns, board::getRow) { it.row }
        rowRemovals + columnRemovals
    }.mergeToRemoveCandidates()