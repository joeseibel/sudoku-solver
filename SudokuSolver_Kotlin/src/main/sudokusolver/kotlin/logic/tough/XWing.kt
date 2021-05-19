package sudokusolver.kotlin.logic.tough

import sudokusolver.kotlin.Board
import sudokusolver.kotlin.Cell
import sudokusolver.kotlin.RemoveCandidates
import sudokusolver.kotlin.SudokuNumber
import sudokusolver.kotlin.UnsolvedCell
import sudokusolver.kotlin.mergeToRemoveCandidates
import sudokusolver.kotlin.zipEveryPair

/*
 * http://www.sudokuwiki.org/X_Wing_Strategy
 *
 * For a pair of rows, if a candidate appears in only two columns of both rows and the columns are the same, forming a
 * rectangle, then the candidate must be placed in opposite corners of the rectangle. The candidate can be removed from
 * cells which are in the two columns, but different rows.
 *
 * For a pair of columns, if a candidate appears in only two rows of both columns and the rows are the same, forming a
 * rectangle, then the candidate must be placed in opposite corners of the rectangle. The candidate can be removed from
 * cells which are in the two rows, but different columns.
 */
fun xWing(board: Board<Cell>): List<RemoveCandidates> =
    SudokuNumber.values().flatMap { candidate ->

        fun xWing(units: List<List<Cell>>, getOtherUnit: (Int) -> List<Cell>, getOtherUnitIndex: (Cell) -> Int) =
            units.zipEveryPair().mapNotNull { (unitA, unitB) ->
                val aWithCandidate = unitA.filterIsInstance<UnsolvedCell>().filter { candidate in it.candidates }
                val bWithCandidate = unitB.filterIsInstance<UnsolvedCell>().filter { candidate in it.candidates }
                if (aWithCandidate.size == 2 && bWithCandidate.size == 2 &&
                    getOtherUnitIndex(aWithCandidate.first()) == getOtherUnitIndex(bWithCandidate.first()) &&
                    getOtherUnitIndex(aWithCandidate.last()) == getOtherUnitIndex(bWithCandidate.last())
                ) {
                    val otherUnitA = getOtherUnit(getOtherUnitIndex(aWithCandidate.first()))
                    val otherUnitB = getOtherUnit(getOtherUnitIndex(aWithCandidate.last()))
                    (otherUnitA + otherUnitB)
                        .filterIsInstance<UnsolvedCell>()
                        .filter { candidate in it.candidates && it !in unitA && it !in unitB }
                        .map { it to candidate }
                } else {
                    null
                }
            }.flatten()

        val rowRemovals = xWing(board.rows, board::getColumn) { it.column }
        val columnRemovals = xWing(board.columns, board::getRow) { it.row }
        rowRemovals + columnRemovals
    }.mergeToRemoveCandidates()