package sudokusolver.kotlin.logic.diabolical

import sudokusolver.kotlin.Board
import sudokusolver.kotlin.Cell
import sudokusolver.kotlin.LocatedCandidate
import sudokusolver.kotlin.Rectangle
import sudokusolver.kotlin.RemoveCandidates
import sudokusolver.kotlin.SudokuNumber
import sudokusolver.kotlin.UnsolvedCell
import sudokusolver.kotlin.createRectangles
import sudokusolver.kotlin.mergeToRemoveCandidates
import java.util.EnumSet

/*
 * https://www.sudokuwiki.org/Hidden_Unique_Rectangles
 *
 * The basic premise of the Hidden Unique Rectangles solution is exactly the same as Unique Rectangles. The only
 * difference is that Hidden Unique Rectangles adds more specific types to the solution. These additional types look for
 * strong links between cells of a rectangle. A strong link exists between two cells for a given candidate when those
 * two cells are the only cells with the candidate in a given row or column.
 */
fun hiddenUniqueRectangles(board: Board<Cell>): List<RemoveCandidates> =
    createRectangles(board).mapNotNull { rectangle ->
        val (floor, roof) = rectangle.cells.partition { it.candidates.size == 2 }
        when {
            floor.size == 1 -> type1(board, rectangle, floor.single())
            roof.size == 2 -> type2(board, roof, rectangle.commonCandidates)
            else -> null
        }
    }.mergeToRemoveCandidates()

/*
 * Type 1
 *
 * If a rectangle has one floor cell, then consider the roof cell on the opposite corner of the rectangle. If one of the
 * common candidates appears twice in that cell's row and twice in that cell's column, which implies that that the other
 * occurrences in that row and column are in the two other corners of the rectangle, then setting the other common
 * candidate as the value to that cell would lead to the Deadly Pattern. Therefore, the other common candidate cannot be
 * the solution to that cell. The other common candidate can be removed from the roof cell which is opposite of the one
 * floor cell.
 */
private fun type1(board: Board<Cell>, rectangle: Rectangle, floor: UnsolvedCell): LocatedCandidate? {
    val row = board.getRow(floor.row).filterIsInstance<UnsolvedCell>()
    val column = board.getColumn(floor.column).filterIsInstance<UnsolvedCell>()
    return rectangle.commonCandidates
        .singleOrNull { candidate ->
            row.count { candidate in it.candidates } == 2 && column.count { candidate in it.candidates } == 2
        }
        ?.let { strongCandidate ->
            val oppositeCell = rectangle.cells.single { it.row != floor.row && it.column != floor.column }
            val otherCandidate = rectangle.commonCandidates.single { it != strongCandidate }
            oppositeCell to otherCandidate
        }
}

/*
 * Type 2
 *
 * If a rectangle has two roof cells, those cells are in the same row, and there exists a strong link for one of the
 * common candidates between one of the roof cells and its corresponding floor cell in the same column, then setting the
 * other common candidate as the value to the other roof cell would lead to the Deadly Pattern. Therefore, the other
 * common candidate cannot be the solution to the other roof cell. The other common candidate can be removed from the
 * other roof cell.
 *
 * If a rectangle has two roof cells, those cells are in the same column, and there exists a strong link for one of the
 * common candidates between one of the roof cells and its corresponding floor cell in the same row, then setting the
 * other common candidate as the value to the other roof cell would lead to the Deadly Pattern. Therefore, the other
 * common candidate cannot be the solution to the other roof cell. The other common candidate can be removed from the
 * other roof cell.
 */
private fun type2(
    board: Board<Cell>,
    roof: List<UnsolvedCell>,
    commonCandidates: EnumSet<SudokuNumber>
): LocatedCandidate? {
    val (roofA, roofB) = roof
    val (candidateA, candidateB) = commonCandidates.toList()

    fun getRemoval(getUnitIndex: (Cell) -> Int, getUnit: (Int) -> List<Cell>): LocatedCandidate? {
        val unitA = getUnit(getUnitIndex(roofA)).filterIsInstance<UnsolvedCell>()
        val unitB = getUnit(getUnitIndex(roofB)).filterIsInstance<UnsolvedCell>()
        return when {
            unitA.count { candidateA in it.candidates } == 2 -> roofB to candidateB
            unitA.count { candidateB in it.candidates } == 2 -> roofB to candidateA
            unitB.count { candidateA in it.candidates } == 2 -> roofA to candidateB
            unitB.count { candidateB in it.candidates } == 2 -> roofA to candidateA
            else -> null
        }
    }

    return when {
        roofA.row == roofB.row -> getRemoval(Cell::column, board::getColumn)
        roofA.column == roofB.column -> getRemoval(Cell::row, board::getRow)
        else -> null
    }
}