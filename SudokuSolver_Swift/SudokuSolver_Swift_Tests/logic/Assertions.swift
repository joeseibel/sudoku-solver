import XCTest

func assertLogicalSolution(
    expected: [BoardModification],
    board: Board<Cell>,
    logicFunction: (Board<Cell>) -> [BoardModification]
) {
    let bruteForceSolution = try! bruteForce(board: board.mapCells {
        if case .solvedCell(let solvedCell) = $0 {
            solvedCell.value
        } else {
            nil
        }
    })
    let actual = logicFunction(board).sorted { $0.row < $1.row || $0.row == $1.row && $0.column < $1.column }
    actual.forEach { modification in
        let row = modification.row
        let column = modification.column
        let solution = bruteForceSolution[row, column]
        switch modification {
        case .removeCandidates(let removeCandidates):
            XCTAssertFalse(
                removeCandidates.candidates.contains(solution),
                "Cannot remove candidate \(solution) from [\(row), \(column)]"
            )
        case .setValue(let setValue):
            XCTAssertEqual(
                solution,
                setValue.value,
                "Cannot set value \(setValue.value) to [\(row), \(column)]. Solution is \(solution)"
            )
        }
    }
    XCTAssertEqual(expected, actual)
}
