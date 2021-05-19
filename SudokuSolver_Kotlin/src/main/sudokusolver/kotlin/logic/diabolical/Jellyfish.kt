package sudokusolver.kotlin.logic.diabolical

import sudokusolver.kotlin.Board
import sudokusolver.kotlin.Cell
import sudokusolver.kotlin.RemoveCandidates
import sudokusolver.kotlin.SudokuNumber
import sudokusolver.kotlin.UnsolvedCell
import sudokusolver.kotlin.mergeToRemoveCandidates
import sudokusolver.kotlin.zipEveryQuad

/*
 * https://www.sudokuwiki.org/Jelly_Fish_Strategy
 *
 * For a quad of rows, if a candidate appears in two, three, or four cells for each row and the candidate appears in
 * exactly four columns across the four rows, forming a four by four grid, then the candidate must be placed in four of
 * the sixteen cells. The candidate can be removed from cells which are in the four columns, but different rows.
 *
 * For a quad of columns, if a candidate appears in two, three, or four cells for each column and the candidate appears
 * in exactly four rows across the four columns, forming a four by four grid, then the candidate must be placed in four
 * of the sixteen cells. The candidate can be removed from cells which are in the four rows, but different columns
 */
fun jellyfish(board: Board<Cell>): List<RemoveCandidates> =
    SudokuNumber.values().flatMap { candidate ->

        fun jellyfish(units: List<List<Cell>>, getOtherUnit: (Int) -> List<Cell>, getOtherUnitIndex: (Cell) -> Int) =
            units.zipEveryQuad().mapNotNull { (unitA, unitB, unitC, unitD) ->
                val aWithCandidate = unitA.filterIsInstance<UnsolvedCell>().filter { candidate in it.candidates }
                val bWithCandidate = unitB.filterIsInstance<UnsolvedCell>().filter { candidate in it.candidates }
                val cWithCandidate = unitC.filterIsInstance<UnsolvedCell>().filter { candidate in it.candidates }
                val dWithCandidate = unitD.filterIsInstance<UnsolvedCell>().filter { candidate in it.candidates }
                if (aWithCandidate.size in 2..4 &&
                    bWithCandidate.size in 2..4 &&
                    cWithCandidate.size in 2..4 &&
                    dWithCandidate.size in 2..4
                ) {
                    val withCandidate = aWithCandidate + bWithCandidate + cWithCandidate + dWithCandidate
                    withCandidate.map(getOtherUnitIndex)
                        .toSet()
                        .takeIf { it.size == 4 }
                        ?.flatMap(getOtherUnit)
                        ?.filterIsInstance<UnsolvedCell>()
                        ?.filter { cell -> candidate in cell.candidates && cell !in withCandidate }
                        ?.map { it to candidate }
                } else {
                    null
                }
            }.flatten()

        val rowRemovals = jellyfish(board.rows, board::getColumn) { it.column }
        val columnRemovals = jellyfish(board.columns, board::getRow) { it.row }
        rowRemovals + columnRemovals
    }.mergeToRemoveCandidates()