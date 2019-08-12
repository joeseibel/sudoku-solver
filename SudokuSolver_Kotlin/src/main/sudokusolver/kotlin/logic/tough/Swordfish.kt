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
 *
 * For each triple of rows
 *   For each candidate
 *     If the candidate appears in two or three unsolved cells of each of the three rows
 *       If the candidate appears in three columns across the three rows
 *         Remove the candidate from cells of the first column which are not in the three rows
 *         Remove the candidate from cells of the second column which are not in the three rows
 *         Remove the candidate from cells of the third column which are not in the three rows
 * For each triple of columns
 *   For each candidate
 *     If the candidate appears in two or three unsolved cells of each of the three columns
 *       If the candidate appears in three rows across the three columns
 *         Remove the candidate from cells of the first row which are not in the three columns
 *         Remove the candidate from cells of the second row which are not in the three columns
 *         Remove the candidate from cells of the third row which are not in the three columns
 */
fun swordfish(board: Board<Cell>): List<RemoveCandidates> =
    SudokuNumber.values().flatMap { candidate ->

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