/*
 * If a cell is solved, then no other cells in the same unit can have that number as a candidate.
 */
func pruneCandidates(board: Board<Cell>) -> [BoardModification] {
    board.cells.unsolvedCells.compactMap { cell in
        let sameRow = board.getRow(rowIndex: cell.row)
        let sameColumn = board.getColumn(columnIndex: cell.column)
        let sameBlock = board.getBlock(blockIndex: cell.block)
        let visibleValues = (sameRow + sameColumn + sameBlock).solvedCells.map(\.value)
        let toRemove = cell.candidates.intersection(visibleValues)
        return toRemove.isEmpty ? nil : BoardModification(cell: cell, candidates: toRemove)
    }
}
