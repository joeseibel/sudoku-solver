package sudokusolver.kotlin

import java.util.EnumSet

class Rectangle(val cells: List<UnsolvedCell>) {
    val commonCandidates: EnumSet<SudokuNumber> = enumIntersect(*cells.map { it.candidates }.toTypedArray())

    val floor: List<UnsolvedCell> by lazy {
        cells.filter { it.candidates.size == 2 }
    }

    val roof: List<UnsolvedCell> by lazy {
        cells.filter { it.candidates.size > 2 }
    }
}

fun createRectangles(board: Board<Cell>): List<Rectangle> =
    board.rows
        .zipEveryPair()
        .flatMap { (rowA, rowB) ->
            (rowA zip rowB)
                .mapNotNull { (cellA, cellB) ->
                    if (cellA is UnsolvedCell && cellB is UnsolvedCell) cellA to cellB else null
                }
                .zipEveryPair()
                .map { (columnA, columnB) ->
                    Rectangle(listOf(columnA.first, columnA.second, columnB.first, columnB.second))
                }
        }
        .filter { rectangle ->
            rectangle.commonCandidates.size == 2 && rectangle.cells.map { it.block }.toSet().size == 2
        }