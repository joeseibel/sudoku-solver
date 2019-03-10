package sudokusolver.kotlin

fun pruneCandidates(board: Board<Cell>) {
    board.rows.forEachIndexed { rowIndex, row ->
        row.asSequence()
            .map(Cell::value)
            .withIndex()
            .filterValueNotNull()
            .forEach { (columnIndex, value) ->
                board.getRow(rowIndex).filter { value in it.candidates }.forEach { it.removeCandidate(value) }
                board.getColumn(columnIndex).filter { value in it.candidates }.forEach { it.removeCandidate(value) }
                board.getBlock(BlockIndex(rowIndex, columnIndex))
                    .filter { value in it.candidates }
                    .forEach { it.removeCandidate(value) }
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

private fun <T> Sequence<IndexedValue<T?>>.filterValueNotNull(): Sequence<IndexedValue<T>> =
    mapNotNull { (index, value) -> if (value == null) null else IndexedValue(index, value) }