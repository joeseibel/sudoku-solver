package sudokusolver.kotlin

class Rectangle(val cells: List<UnsolvedCell>) {
    val commonCandidates = enumIntersect(*cells.map { it.candidates }.toTypedArray())
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
            rectangle.cells.map { it.block }.toSet().size == 2 && rectangle.commonCandidates.size == 2
        }