package sudokusolver.kotlin.logic.extreme

import sudokusolver.kotlin.Board
import sudokusolver.kotlin.Cell
import sudokusolver.kotlin.RemoveCandidates
import sudokusolver.kotlin.SolvedCell
import sudokusolver.kotlin.SudokuNumber
import sudokusolver.kotlin.UNIT_SIZE
import sudokusolver.kotlin.UNIT_SIZE_SQUARE_ROOT
import sudokusolver.kotlin.UnsolvedCell
import sudokusolver.kotlin.mergeToRemoveCandidates

/*
 * https://www.sudokuwiki.org/Empty_Rectangles
 *
 * This solution starts with looking for empty rectangles in blocks. An empty rectangle is a collection of four cells,
 * all contained within a single block, arranged in a rectangle, and none of them contain a particular candidate. The
 * cells can either be solved cells or unsolved cells without the candidate. For the other cells which are in the block,
 * but are outside of the rectangle, at least two of them must contain the candidate and those cells must be in at least
 * two different rows and two different columns.
 *
 * This creates a situation in which two lines can be drawn through the block; one line along a row and the other along
 * a column. The two lines must not pass through any of the empty rectangle cells and all of the cells with the
 * candidate must have a line pass through it. A valid block is one in which there is only one option for the placement
 * of these lines. This is why the cells with the candidate must be in at least two different rows and two different
 * columns. The cell in which these lines intersect is then used to find removals outside of the block. The empty
 * rectangle itself is used to find a valid intersection point, but then the rectangle is disregarded for the remainder
 * of the solution.
 *
 * Removals are looked for in cells which are outside of the block, but which can see the intersection. If the
 * intersection can see one end of a strong link which is outside of the intersection's block and there is another cell
 * with the candidate outside of the intersection's block, but it can see the intersection and the other end of the
 * strong link, then there is a contradiction. If the candidate were to be set as the solution to the other cell, then
 * the strong link and this newly set solution would remove the candidate from every cell within the intersection's
 * block, thus invalidating that block. This means that the candidate cannot be the solution to that cell and can be
 * removed.
 */
fun emptyRectangles(board: Board<Cell>): List<RemoveCandidates> =
    SudokuNumber.values().flatMap { candidate ->
        getIntersections(board, candidate).flatMap { (row, column) ->
            val block = board[row, column].block

            fun getRemovals(
                unit: List<Cell>,
                getOtherUnitIndex: (Cell) -> Int,
                getOtherUnit: (Int) -> List<Cell>,
                getRemovalRow: (Cell) -> Int,
                getRemovalColumn: (Cell) -> Int
            ) =
                unit.filter { it.block != block && candidate in it }.mapNotNull { strongLink1 ->
                    getOtherUnit(getOtherUnitIndex(strongLink1))
                        .filter { candidate in it }
                        .let { it - strongLink1 }
                        .singleOrNull()
                        ?.takeIf { strongLink2 -> strongLink1.block != strongLink2.block }
                        ?.let { board[getRemovalRow(it), getRemovalColumn(it)] as? UnsolvedCell }
                        ?.takeIf { removalCell -> candidate in removalCell.candidates }
                        ?.let { it to candidate }
                }

            val rowRemovals = getRemovals(board.getRow(row), Cell::column, board::getColumn, Cell::row) { column }
            val columnRemovals = getRemovals(board.getColumn(column), Cell::row, board::getRow, { row }, Cell::column)
            rowRemovals + columnRemovals
        }
    }.mergeToRemoveCandidates()

private fun getIntersections(board: Board<Cell>, candidate: SudokuNumber): List<Pair<Int, Int>> =
    (0 until UNIT_SIZE).flatMap { row ->
        val rowInBlock = row % UNIT_SIZE_SQUARE_ROOT
        val rectangleRow1 = if (rowInBlock == 0) row + 1 else row - rowInBlock
        val rectangleRow2 = if (rowInBlock == 2) row - 1 else row - rowInBlock + 2
        (0 until UNIT_SIZE).mapNotNull { column ->
            val columnInBlock = column % UNIT_SIZE_SQUARE_ROOT
            val rectangleColumn1 = if (columnInBlock == 0) column + 1 else column - columnInBlock
            val rectangleColumn2 = if (columnInBlock == 2) column - 1 else column - columnInBlock + 2
            if (candidate !in board[rectangleRow1, rectangleColumn1] &&
                candidate !in board[rectangleRow1, rectangleColumn2] &&
                candidate !in board[rectangleRow2, rectangleColumn1] &&
                candidate !in board[rectangleRow2, rectangleColumn2] &&
                (candidate in board[row, rectangleColumn1] || candidate in board[row, rectangleColumn2]) &&
                (candidate in board[rectangleRow1, column] || candidate in board[rectangleRow2, column])
            ) {
                row to column
            } else {
                null
            }
        }
    }

private operator fun Cell.contains(candidate: SudokuNumber): Boolean =
    when (this) {
        is SolvedCell -> false
        is UnsolvedCell -> candidate in candidates
    }