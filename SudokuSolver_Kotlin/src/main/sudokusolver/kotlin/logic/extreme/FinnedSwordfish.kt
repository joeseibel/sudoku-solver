package sudokusolver.kotlin.logic.extreme

import sudokusolver.kotlin.Board
import sudokusolver.kotlin.Cell
import sudokusolver.kotlin.LocatedCandidate
import sudokusolver.kotlin.RemoveCandidates
import sudokusolver.kotlin.SudokuNumber
import sudokusolver.kotlin.UnsolvedCell
import sudokusolver.kotlin.mergeToRemoveCandidates
import sudokusolver.kotlin.zipEveryPair

/*
 * https://www.sudokuwiki.org/Finned_Swordfish
 *
 * Finned Swordfish is an extension of Swordfish in a similar manner to the way that Finned X-Wing is an extension of
 * X-Wing. As a reminder, Swordfish looks for a 3x3 grid of cells in which a particular candidate appears in most or all
 * of those cells. If the candidate appears two or three times in each row of the grid and for those rows, the candidate
 * appears in exactly three columns, then the candidate can be removed from the columns of the grid, but in different
 * rows. If the candidate appears two or three times in each column of the grid and for those columns, the candidate
 * appears in exactly three rows, then the candidate can be removed from the rows of the grid, but in different columns.
 *
 * In Finned Swordfish, eight of the cells of a 3x3 grid will follow the same rules as Swordfish. Only one cell will
 * have additional unsolved cells with the candidate next to it. The fin must be in the same block as the cell, but the
 * cell itself may or may not have the candidate.
 *
 * For a triple of rows, two rows are the base rows if the candidate appears two or three times in each row and the
 * candidate appears in exactly three columns of the two rows. The remaining row is a finned row if the candidate
 * appears once or twice outside the three columns, but in the same block as one of the cells of the grid. That cell is
 * the finned cell. The candidate can be removed from cells that are in the same column as the finned cell, but are
 * outside the grid.
 *
 * For a triple of columns, two columns are the base columns if the candidate appears two or three times in each column
 * and the candidate appears in exactly three rows of the two columns. The remaining column is a finned column if the
 * candidate appears once or twice outside the three rows, but in the same block as one of the cells of the grid. That
 * cell is the finned cell. The candidate can be removed from cells that are in the same row as the finned cell, but are
 * outside the grid.
 */
fun finnedSwordfish(board: Board<Cell>): List<RemoveCandidates> =
    SudokuNumber.values().flatMap { candidate ->

        fun finnedSwordfish(
            units: List<List<Cell>>,
            getUnitIndex: (Cell) -> Int,
            getOtherUnitIndex: (Cell) -> Int,
            getOtherUnit: (Int) -> List<Cell>,
            getFinnedCell: (finnedUnitIndex: Int, otherUnitIndex: Int) -> Cell
        ): List<LocatedCandidate> {
            val unitsWithCandidate = units
                .map { unit -> unit.filterIsInstance<UnsolvedCell>().filter { candidate in it.candidates } }
                .filter { it.isNotEmpty() }
            return unitsWithCandidate.filter { it.size in 2..3 }.zipEveryPair().mapNotNull { (baseUnitA, baseUnitB) ->
                (baseUnitA + baseUnitB).map(getOtherUnitIndex)
                    .toSet()
                    .takeIf { it.size == 3 }
                    ?.let { otherUnitIndices ->
                        unitsWithCandidate.mapNotNull { finnedUnit ->
                            val finnedUnitIndex = getUnitIndex(finnedUnit.first())
                            setOf(finnedUnitIndex, getUnitIndex(baseUnitA.first()), getUnitIndex(baseUnitB.first()))
                                .takeIf { it.size == 3 }
                                ?.let { unitIndices ->
                                    finnedUnit.filter { getOtherUnitIndex(it) !in otherUnitIndices }
                                        .takeIf { it.size in 1..2 }
                                        ?.map { it.block }
                                        ?.toSet()
                                        ?.singleOrNull()
                                        ?.let { blockIndex ->
                                            otherUnitIndices.map { getFinnedCell(finnedUnitIndex, it) }
                                                .singleOrNull { it.block == blockIndex }
                                                ?.let { finnedCell -> getOtherUnit(getOtherUnitIndex(finnedCell)) }
                                                ?.filterIsInstance<UnsolvedCell>()
                                                ?.filter {
                                                    candidate in it.candidates &&
                                                            it.block == blockIndex &&
                                                            getUnitIndex(it) !in unitIndices
                                                }
                                                ?.map { it to candidate }
                                        }
                                }
                        }.flatten()
                    }
            }.flatten()
        }

        val rowRemovals = finnedSwordfish(board.rows, Cell::row, Cell::column, board::getColumn, board::get)

        val columnRemovals = finnedSwordfish(
            board.columns,
            Cell::column,
            Cell::row,
            board::getRow
        ) { finnedUnitIndex, otherUnitIndex -> board[otherUnitIndex, finnedUnitIndex] }

        rowRemovals + columnRemovals
    }.mergeToRemoveCandidates()