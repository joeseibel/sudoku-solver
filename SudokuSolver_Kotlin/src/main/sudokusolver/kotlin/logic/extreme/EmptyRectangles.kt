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
 * but are outside the rectangle, at least two of them must contain the candidate and those cells must be in at least
 * two different rows and two different columns.
 *
 * This creates a situation in which two lines can be drawn through the block; one line along a row and the other along
 * a column. The two lines must not pass through any of the empty rectangle cells and all the cells with the candidate
 * must have a line pass through it. A valid block is one in which there is only one option for the placement of these
 * lines. This is why the cells with the candidate must be in at least two different rows and two different columns. The
 * cell in which these lines intersect is then used to find removals outside the block. The empty rectangle itself is
 * used to find a valid intersection point, but then the rectangle is disregarded for the remainder of the solution.
 *
 * Removals are looked for in cells which are outside the block, but which can see the intersection. If the intersection
 * can see one end of a strong link which is outside the intersection's block and there is another cell with the
 * candidate outside the intersection's block, but it can see the intersection and the other end of the strong link,
 * then there is a contradiction. If the candidate were to be set as the solution to the other cell, then the strong
 * link and this newly set solution would remove the candidate from every cell within the intersection's block, thus
 * invalidating that block. This means that the candidate cannot be the solution to that cell and can be removed.
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
            //Check that the rectangle is empty.
            if (candidate !in board[rectangleRow1, rectangleColumn1] &&
                candidate !in board[rectangleRow1, rectangleColumn2] &&
                candidate !in board[rectangleRow2, rectangleColumn1] &&
                candidate !in board[rectangleRow2, rectangleColumn2] &&
                //Check that at least one cell in the same block and row as the intersection has the candidate.
                (candidate in board[row, rectangleColumn1] || candidate in board[row, rectangleColumn2]) &&
                //Check that at least one cell in the same block and column as the intersection has the candidate.
                (candidate in board[rectangleRow1, column] || candidate in board[rectangleRow2, column])
            ) {
                row to column
            } else {
                SolvedCell(0, 0, value = SudokuNumber.ONE)
                null
            }
        }
    }

/*
 * Should this be defined here as a private function or should it be a method of Cell and available to all solutions?
 * The vast majority of solutions first filter by the type UnsolvedCell, and then work with the candidates field of
 * UnsolvedCell. Empty Rectangles is different because it needs to check that certain cells have a candidate and
 * certain others do not have a candidate. For cells that don't have a candidate, Empty Rectangles doesn't care if that
 * is because the cell is solved or if it is an unsolved cell without the candidate. Since this is only really needed by
 * Empty Rectangles, I decided to have this as a private function here instead of adding it to Cell.
 *
 * Is it potentially confusing to override the in operator instead of giving this a name such as hasCandidate? What is
 * the natural and expected behavior when the cell is a SolvedCell and the candidate is the solution to that cell? Would
 * someone reasonably expect the expression "SudokuNumber.ONE in SolvedCell(value = SudokuNumber.ONE, ...)" to return
 * true and be surprised when it actually returns false? It is true that a name like hasCandidate would be more clear,
 * but since this is a private function, and it is easy to see where it is used, I decided to choose brevity over
 * clarity.
 *
 * These questions are not all that interesting here in Kotlin, but will prove to be more interesting in other
 * languages. Kotlin has the advantage that SolvedCell and UnsolvedCell are their own types, are distinct from each
 * other, and are distinct from Cell. For languages like Swift and Rust, the Cell type hierarchy will become an enum
 * with associated values and SolvedCell and UnsolvedCell will become variants of that enum. This has the disadvantage
 * that our three distinct types in Kotlin will become one type in Swift and Rust. In that case, a method called
 * hasCandidate will probably be added to the enum. A similar approach might be applied for the Java implementation just
 * because filtering a Java stream by type is ugly and error-prone.
 */
private operator fun Cell.contains(candidate: SudokuNumber): Boolean =
    when (this) {
        is SolvedCell -> false
        is UnsolvedCell -> candidate in candidates
    }