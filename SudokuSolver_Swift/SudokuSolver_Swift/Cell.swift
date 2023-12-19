//TODO: Evaluate and document this approach vs creating separate types for SolvedCell and UnsolvedCell.
enum Cell: Equatable {
    case solvedCell(row: Int, column: Int, value: SudokuNumber)
    case unsolvedCell(row: Int, column: Int, candidates: Set<SudokuNumber> = Set(SudokuNumber.allCases))
}

extension Board<Cell> {
    init(toCellBoard board: Board<SudokuNumber?>) {
        self = board.mapCellsIndexed { row, column, cell in
            if let cell {
                .solvedCell(row: row, column: column, value: cell)
            } else {
                .unsolvedCell(row: row, column: column)
            }
        }
    }
}
