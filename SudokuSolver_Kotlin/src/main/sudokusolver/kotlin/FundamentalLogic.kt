package sudokusolver.kotlin

fun pruneCandidates(board: Board<Cell>) {
    board.rows.forEachIndexed { rowIndex, row ->
        row.asSequence()
            .map { it.value }
            .mapIndexedNotNull { columnIndex, value -> if (value == null) null else columnIndex to value }
            .forEach { (columnIndex, value) ->
                board.getRow(rowIndex).forEach { it.removeCandidate(value) }
                board.getColumn(columnIndex).forEach { it.removeCandidate(value) }
                board.getBlock(BlockIndex(rowIndex, columnIndex)).forEach { it.removeCandidate(value) }
            }
    }
}

fun fillSolvedCells(board: Board<Cell>): Boolean {
    val solvedCells = board.cells.filter { it.candidates.size == 1 }
    return if (solvedCells.isEmpty()) {
        false
    } else {
        solvedCells.forEach { it.setValue(it.candidates.first()) }
        true
    }
}