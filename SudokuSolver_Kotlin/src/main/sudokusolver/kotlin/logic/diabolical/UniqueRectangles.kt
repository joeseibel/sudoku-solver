package sudokusolver.kotlin.logic.diabolical

import sudokusolver.kotlin.Board
import sudokusolver.kotlin.Cell
import sudokusolver.kotlin.LocatedCandidate
import sudokusolver.kotlin.RemoveCandidates
import sudokusolver.kotlin.SetValue
import sudokusolver.kotlin.SudokuNumber
import sudokusolver.kotlin.UnsolvedCell
import sudokusolver.kotlin.createRectangles
import sudokusolver.kotlin.enumIntersect
import sudokusolver.kotlin.enumMinus
import sudokusolver.kotlin.enumUnion
import sudokusolver.kotlin.mergeToRemoveCandidates
import sudokusolver.kotlin.zipEveryPair

/*
 * https://www.sudokuwiki.org/Unique_Rectangles
 *
 * The Unique Rectangles solution works by identifying the potential for an invalid pattern of candidates called the
 * Deadly Pattern and then removing candidates that if set as the value would lead to the Deadly Pattern. A Deadly
 * Pattern is defined as a group of four unsolved cells arranged to form a rectangle, each cell containing the same two
 * candidates and only those candidates, and the cells being located in two rows, two columns, and two blocks. If a
 * board contains the Deadly Pattern, then the board cannot have a single solution, but would have multiple solutions.
 * The advantage of recognizing this pattern comes when a board contains a pattern which is close to the Deadly Pattern
 * and the removal of certain candidates would lead to the Deadly Pattern. If a valid board contains a pattern which is
 * close to the Deadly Pattern, it is known that the board will never enter into the Deadly Pattern and candidates can
 * be removed if setting those candidates as values would lead to the Deadly Pattern. A rectangle can be further
 * described by identifying its floor cells and its roof cells. A rectangle's floor are the cells that only contain the
 * two common candidates. A rectangle's roof are the cells that contain the two common candidates as well as additional
 * candidates.
 *
 * Type 1
 *
 * If a rectangle has one roof cell, then this is a potential Deadly Pattern. If the additional candidates were to be
 * removed from the roof, then that would lead to a Deadly Pattern. The two common candidates can be removed from the
 * roof leaving only the additional candidates remaining.
 */
fun uniqueRectanglesType1(board: Board<Cell>): List<RemoveCandidates> =
    createRectangles(board).mapNotNull { rectangle ->
        rectangle.cells
            .singleOrNull { it.candidates.size > 2 }
            ?.let { roof -> rectangle.commonCandidates.map { roof to it } }
    }.flatten().mergeToRemoveCandidates()

/*
 * Type 2
 *
 * If a rectangle has two roof cells and there is only one additional candidate appearing in both roof cells, then this
 * is a potential Deadly Pattern. If the additional candidate were to be removed from the roof cells, then that would
 * lead to a Deadly Pattern, therefore the additional candidate must be the solution for one of the two roof cells. The
 * common candidate can be removed from any other cell that can see both of the roof cells.
 */
fun uniqueRectanglesType2(board: Board<Cell>): List<RemoveCandidates> =
    createRectangles(board).mapNotNull { rectangle ->
        rectangle.cells
            .filter { it.candidates.size > 2 }
            .takeIf { it.size == 2 }
            ?.takeIf { (roofA, roofB) -> roofA.candidates.size == 3 && roofA.candidates == roofB.candidates }
            ?.let { (roofA, roofB) ->
                val additionalCandidate = (roofA.candidates enumMinus rectangle.commonCandidates).single()
                board.cells
                    .filterIsInstance<UnsolvedCell>()
                    .filter {
                        additionalCandidate in it.candidates &&
                                it != roofA &&
                                it != roofB &&
                                it isInSameUnit roofA &&
                                it isInSameUnit roofB
                    }
                    .map { it to additionalCandidate }
            }
    }.flatten().mergeToRemoveCandidates()

/*
 * Type 3
 *
 * If a rectangle has two roof cells, each roof cell has one additional candidate, and the additional candidates are
 * different, then this is a potential Deadly Pattern. One or both of these additional candidates must be the solution,
 * so the roof cells can be treated as a single cell with the two additional candidates. If there is another cell that
 * can see both roof cells and has the additional candidates as its candidates, then the roof cells and the other cell
 * effectively form a Naked Pair. The additional candidates can be removed from any other cell in the unit.
 */
fun uniqueRectanglesType3(board: Board<Cell>): List<RemoveCandidates> =
    createRectangles(board).mapNotNull { rectangle ->
        rectangle.cells
            .filter { it.candidates.size > 2 }
            .takeIf { it.size == 2 }
            ?.takeIf { (roofA, roofB) ->
                roofA.candidates.size == 3 && roofB.candidates.size == 3 && roofA.candidates != roofB.candidates
            }
            ?.let { (roofA, roofB) ->
                val additionalCandidates = enumUnion(roofA.candidates, roofB.candidates) enumMinus
                        rectangle.commonCandidates

                fun getRemovals(getUnitIndex: (Cell) -> Int, getUnit: (Int) -> List<Cell>): List<LocatedCandidate> {
                    val indexA = getUnitIndex(roofA)
                    val indexB = getUnitIndex(roofB)
                    return if (indexA == indexB) {
                        val unit = getUnit(indexA).filterIsInstance<UnsolvedCell>()
                        unit.find { it.candidates == additionalCandidates }
                            ?.let { pairCell ->
                                unit.filter { it != pairCell && it != roofA && it != roofB }
                                    .flatMap { cell ->
                                        enumIntersect(cell.candidates, additionalCandidates).map { cell to it }
                                    }
                            }
                            ?: emptyList()
                    } else {
                        emptyList()
                    }
                }

                getRemovals(Cell::row, board::getRow) +
                        getRemovals(Cell::column, board::getColumn) +
                        getRemovals(Cell::block, board::getBlock)
            }
    }.flatten().mergeToRemoveCandidates()

/*
 * Type 3/3b with Triple Pseudo-Cells
 *
 * If a rectangle has two roof cells, then this is a potential Deadly Pattern. If the roof cells can see two other cells
 * and the union of candidates among the roof cells' additional candidates and the other cells' candidates is three
 * candidates, then the roof cells and the other two cells effectively form a Naked Triple. The three candidates in the
 * union can be removed from any other cell in the unit.
 */
fun uniqueRectanglesType3BWithTriplePseudoCells(board: Board<Cell>): List<RemoveCandidates> =
    createRectangles(board).mapNotNull { rectangle ->
        rectangle.cells.filter { it.candidates.size > 2 }.takeIf { it.size == 2 }?.let { (roofA, roofB) ->
            val additionalCandidates = enumUnion(roofA.candidates, roofB.candidates) enumMinus
                    rectangle.commonCandidates

            fun getRemovals(getUnitIndex: (Cell) -> Int, getUnit: (Int) -> List<Cell>): List<LocatedCandidate> {
                val indexA = getUnitIndex(roofA)
                val indexB = getUnitIndex(roofB)
                return if (indexA == indexB) {
                    val unit = getUnit(indexA).filterIsInstance<UnsolvedCell>().filter { it != roofA && it != roofB }
                    unit.zipEveryPair().mapNotNull { (tripleA, tripleB) ->
                        enumUnion(additionalCandidates, tripleA.candidates, tripleB.candidates)
                            .takeIf { it.size == 3 }
                            ?.let { tripleCandidates ->
                                unit.filter { it != tripleA && it != tripleB }
                                    .flatMap { cell ->
                                        enumIntersect(cell.candidates, tripleCandidates).map { cell to it }
                                    }
                            }
                    }.flatten()
                } else {
                    emptyList()
                }
            }

            getRemovals(Cell::row, board::getRow) +
                    getRemovals(Cell::column, board::getColumn) +
                    getRemovals(Cell::block, board::getBlock)
        }
    }.flatten().mergeToRemoveCandidates()

/*
 * Type 4
 *
 * If a rectangle has two roof cells, then this is a potential Deadly Pattern. For a unit common to the roof cells, if
 * one of the common candidates are only found in the roof cells of that unit, then setting the other candidate as the
 * solution to one of the roof cells would lead to the Deadly Pattern. The other common candidate can be removed from
 * the roof cells.
 */
fun uniqueRectanglesType4(board: Board<Cell>): List<RemoveCandidates> =
    createRectangles(board).mapNotNull { rectangle ->
        rectangle.cells.filter { it.candidates.size > 2 }.takeIf { it.size == 2 }?.let { roof ->
            val (roofA, roofB) = roof
            val (commonCandidateA, commonCandidateB) = rectangle.commonCandidates.toList()

            fun getRemovals(getUnitIndex: (Cell) -> Int, getUnit: (Int) -> List<Cell>): List<LocatedCandidate> {
                val indexA = getUnitIndex(roofA)
                val indexB = getUnitIndex(roofB)
                return if (indexA == indexB) {
                    val unit = getUnit(indexA).filterIsInstance<UnsolvedCell>()

                    fun searchUnit(search: SudokuNumber, removal: SudokuNumber) =
                        if (unit.count { search in it.candidates } == 2) roof.map { it to removal } else emptyList()

                    searchUnit(commonCandidateA, commonCandidateB) + searchUnit(commonCandidateB, commonCandidateA)
                } else {
                    emptyList()
                }
            }

            getRemovals(Cell::row, board::getRow) +
                    getRemovals(Cell::column, board::getColumn) +
                    getRemovals(Cell::block, board::getBlock)
        }
    }.flatten().mergeToRemoveCandidates()

/*
 * Type 5
 *
 * If a rectangle has two floor cells in diagonally opposite corners of the rectangle and one of the common candidates
 * only appears in the rectangle for the rows and columns that the rectangle exists in, thus forming strong links for
 * the candidate along the four edges of the rectangle, then this is a potential Deadly Pattern. If the non-strong
 * link candidate were to be set as the solution to one of the floor cells, then the strong link candidate would have to
 * be the solution for the roof cells and the non-strong link candidate would need to be set as the solution to the
 * other floor cell, leading to the Deadly Pattern. The non-strong link candidate cannot be the solution to either floor
 * cell. Since each floor cell only contains two candidates, this means that the strong link candidate must be the
 * solution for the floor cells.
 */
fun uniqueRectanglesType5(board: Board<Cell>): List<SetValue> =
    createRectangles(board).mapNotNull { rectangle ->
        rectangle.cells
            .filter { it.candidates.size == 2 }
            .takeIf { floor -> floor.size == 2 }
            ?.takeIf { (floorA, floorB) -> floorA.row != floorB.row && floorA.column != floorB.column }
            ?.let { floor ->
                rectangle.commonCandidates
                    .firstOrNull { candidate ->

                        fun hasStrongLink(unit: List<Cell>) =
                            unit.filterIsInstance<UnsolvedCell>().filter { candidate in it.candidates }.size == 2

                        floor.all {
                            hasStrongLink(board.getRow(it.row)) && hasStrongLink(board.getColumn(it.column))
                        }
                    }
                    ?.let { strongLinkCandidate -> floor.map { SetValue(it, strongLinkCandidate) } }
            }
    }.flatten()