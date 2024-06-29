struct Rectangle {
    let cells: [UnsolvedCell]
    let commonCandidates: Set<SudokuNumber>
    
    fileprivate init(cells: [UnsolvedCell]) {
        self.cells = cells
        commonCandidates = cells.map(\.candidates).reduce(Set(SudokuNumber.allCases)) { $0.intersection($1) }
    }
    
    func floor() -> [UnsolvedCell] {
        cells.filter { $0.candidates.count == 2 }
    }
    
    func roof() -> [UnsolvedCell] {
        cells.filter { $0.candidates.count > 2 }
    }
}

func createRectangles(board: Board<Cell>) -> [Rectangle] {
    board.rows
        .zipEveryPair()
        .flatMap { rowA, rowB in
            zip(rowA, rowB)
                .compactMap { cellA, cellB in
                    if case .unsolvedCell(let cellA) = cellA, case .unsolvedCell(let cellB) = cellB {
                        (cellA, cellB)
                    } else {
                        nil
                    }
                }
                .zipEveryPair()
                .map { columnA, columnB in Rectangle(cells: [columnA.0, columnA.1, columnB.0, columnB.1]) }
        }
        .filter { rectangle in rectangle.commonCandidates.count == 2 && Set(rectangle.cells.map(\.block)).count == 2 }
}
