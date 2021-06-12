package sudokusolver.kotlin.logic.extreme

import sudokusolver.kotlin.Board
import sudokusolver.kotlin.Cell
import sudokusolver.kotlin.RemoveCandidates
import sudokusolver.kotlin.SudokuNumber
import sudokusolver.kotlin.UnsolvedCell
import sudokusolver.kotlin.mergeToRemoveCandidates

/*
 * https://www.sudokuwiki.org/Finned_X_Wing
 *
 * Finned X-Wing is an extension of X-Wing in which one of the corners of a rectangle of cells has a fin next to it. As
 * a reminder, X-Wing looks for a rectangle of unsolved cells with a particular candidate. If the candidate only appears
 * twice in each of the rows of the rectangle, then the candidate can be removed from the columns of the rectangle, but
 * in different rows. If the candidate only appears twice in each of the columns of the rectangle, then the candidate
 * can be removed from the rows of the rectangle, but in different columns.
 *
 * In Finned X-Wing, three of the corners of a rectangle will follow the same rules as X-Wing. Only one corner will have
 * additional unsolved cells with the candidate next to it. The fin must be in the same block as the corner, but the
 * corner itself may or may not have the candidate. If the corner does not have the candidate, the pattern is called a
 * Sashimi Finned X-Wing. From an implementation perspective, there is no difference between a regular Finned X-Wing and
 * Sashimi.
 *
 * For a pair of rows in different blocks, one row is the base row if the candidate appears exactly twice in that row,
 * but in different blocks. The other row is considered to be a finned row if the candidate appears in two blocks of
 * that row, one of those blocks of the row contains a regular corner, and the other block of the row contains a fin. A
 * regular corner is a cell with the candidate, it shares the same column as one of the candidates of the base row, and
 * there are no other candidates in that block of the row. The candidates of the base row along with the regular corner
 * form three corners of a rectangle with the fourth corner being a finned corner. The fourth corner may or may not have
 * the candidate. A fin is one or two cells in the finned row that do not share a column with either of the candidates
 * of the base row, but are in the same block as the finned corner. With all of these constraints, the candidate must be
 * placed in opposite corners of the rectangle, or the fin in the case of the finned corner. The candidate can be
 * removed from cells which are in the same column as the finned corner, the same block as the fin, but different rows.
 *
 * For a pair of columns in different blocks, one column is the base column if the candidate appears exactly twice in
 * that column, but in different blocks. The other column is considered to be a finned column if the candidate appears
 * in two blocks of that column, one of those blocks of the column contains a regular corner, and the other block of the
 * column contains a fin. A regular corner is a cell with the candidate, it shares the same row as one of the candidates
 * of the base column, and there are no other candidates in that block of the column. The candidates of the base column
 * along with the regular corner form three corners of a rectangle with the fourth corner being a finned corner. The
 * fourth corner may or may not have the candidate. A fin is one or two cells in the finned column that do not share a
 * row with either of the candidates of the base column, but are in the same block as the finned corner. With all of
 * these constraints, the candidate must be placed in opposite corners of the rectangle, or the fin in the case of the
 * finned corner. The candidate can be removed from cells which are in the same row as the finned corner, the same block
 * as the fin, but different columns.
 */
fun finnedXWing(board: Board<Cell>): List<RemoveCandidates> =
    SudokuNumber.values().flatMap { candidate ->

        fun finnedXWing(units: List<List<Cell>>, getOtherUnitIndex: (Cell) -> Int) =
            units.mapNotNull { baseUnit ->
                baseUnit.filterIsInstance<UnsolvedCell>()
                    .filter { candidate in it.candidates }
                    .takeIf { it.size == 2 }
                    ?.takeIf { (baseUnitCell1, baseUnitCell2) -> baseUnitCell1.block != baseUnitCell2.block }
                    ?.let { (baseUnitCell1, baseUnitCell2) ->
                        units.filter { it.first().block != baseUnit.first().block }.mapNotNull { finnedUnit ->
                            finnedUnit.filterIsInstance<UnsolvedCell>()
                                .filter { candidate in it.candidates }
                                .groupBy { it.block }
                                .takeIf { it.size == 2 }
                                ?.let { finnedUnitByBlock ->

                                    fun tryFin(finnedCorner: Cell, otherCorner: Cell) =
                                        if (finnedUnitByBlock[finnedCorner.block]?.any { it != finnedCorner } == true &&
                                            finnedUnitByBlock[otherCorner.block]?.singleOrNull() == otherCorner
                                        ) {
                                            board.getBlock(finnedCorner.block)
                                                .filterIsInstance<UnsolvedCell>()
                                                .filter {
                                                    getOtherUnitIndex(it) == getOtherUnitIndex(finnedCorner) &&
                                                            it != finnedCorner &&
                                                            candidate in it.candidates
                                                }
                                                .map { it to candidate }
                                        } else {
                                            null
                                        }

                                    val finnedUnitCell1 = finnedUnit[getOtherUnitIndex(baseUnitCell1)]
                                    val finnedUnitCell2 = finnedUnit[getOtherUnitIndex(baseUnitCell2)]
                                    tryFin(finnedUnitCell1, finnedUnitCell2)
                                        ?: tryFin(finnedUnitCell2, finnedUnitCell1)
                                }
                        }.flatten()
                    }
            }.flatten()

        finnedXWing(board.rows, Cell::column) + finnedXWing(board.columns, Cell::row)
    }.mergeToRemoveCandidates()