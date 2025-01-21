import XCTest

func assertLogicalSolution(
    expected: [BoardModification],
    withCandidates: String,
    logicFunction: (Board<Cell>) -> [BoardModification]
) {
    assertLogicalSolution(
        expected: expected,
        board: Board(withCandidates: withCandidates),
        logicFunction: logicFunction
    )
}

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
    /*
     * Why am I using sorted(by:) instead of sorted() and having BoardModification conform to Comparable? In short,
     * implementing the less-than operator (<) and the equal-to operator (==) the way I would want to would break the
     * contract of Comparable. I want to sort BoardModifications by the row and column indices only while ignoring other
     * properties. However, I want equality to check all properties, as that is useful in unit tests. Having these two
     * different goals would violate one of the requirements of Comparable. Comparable requires that for any two values,
     * exactly one comparison of equal-to (==), less-than (<), or greater-than (>) must be true. If I were to have
     * BoardModification conform to Comparable, then there could be BoardModification values for which none of the basic
     * comparison operations are true. For example, consider the following two BoardModifications:
     *
     * let a = BoardModification(row: 0, column: 0, candidates: 1, 2, 3)
     * let b = BoardModification(row: 0, column: 0, candidates: 4, 5, 6)
     *
     * In this case, none of the basic comparison operations whould yield true. a is not equal to b because the
     * candidates differ. a is neither less than b nor greater than b because their row and column indices are the same.
     * This case violates Comparable's contract.
     */
    let actual = logicFunction(board).sorted { $0.row < $1.row || $0.row == $1.row && $0.column < $1.column }
    for modification in actual {
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
