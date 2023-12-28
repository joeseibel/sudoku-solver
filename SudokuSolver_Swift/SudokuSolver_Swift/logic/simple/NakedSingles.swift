/*
 * http://www.sudokuwiki.org/Getting_Started
 *
 * If an unsolved cell has exactly one candidate, then the candidate must be placed in that cell.
 */
func nakedSingles(board: Board<Cell>) -> [BoardModification] {
    board.cells
        .unsolvedCells
        .filter { $0.candidates.count == 1 }
        .map { .setValue(SetValue(cell: $0, value: $0.candidates.first!)) }
}
