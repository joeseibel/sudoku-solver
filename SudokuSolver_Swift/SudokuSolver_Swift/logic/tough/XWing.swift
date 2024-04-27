/*
 * http://www.sudokuwiki.org/X_Wing_Strategy
 *
 * For a pair of rows, if a candidate appears in only two columns of both rows and the columns are the same, forming a
 * rectangle, then the candidate must be placed in opposite corners of the rectangle. The candidate can be removed from
 * cells which are in the two columns, but different rows.
 *
 * For a pair of columns, if a candidate appears in only two rows of both columns and the rows are the same, forming a
 * rectangle, then the candidate must be placed in opposite corners of the rectangle. The candidate can be removed from
 * cells which are in the two rows, but different columns.
 */
func xWing(board: Board<Cell>) -> [BoardModification] {
    SudokuNumber.allCases.flatMap { candidate -> [LocatedCandidate] in
        
        func xWing(
            units: [[Cell]],
            getOtherUnit: (Int) -> [Cell],
            getOtherUnitIndex: (UnsolvedCell) -> Int
        ) -> [LocatedCandidate] {
            Array(units.zipEveryPair().compactMap { unitA, unitB in
                let unitA = unitA.unsolvedCells
                let unitB = unitB.unsolvedCells
                let aWithCandidate = unitA.filter { $0.candidates.contains(candidate) }
                let bWithCandidate = unitB.filter { $0.candidates.contains(candidate) }
                if let firstA = aWithCandidate.first, let lastA = aWithCandidate.last,
                   let firstB = bWithCandidate.first, let lastB = bWithCandidate.last,
                   aWithCandidate.count == 2 && bWithCandidate.count == 2 &&
                    getOtherUnitIndex(firstA) == getOtherUnitIndex(firstB) &&
                    getOtherUnitIndex(lastA) == getOtherUnitIndex(lastB)
                {
                    let otherUnitA = getOtherUnit(getOtherUnitIndex(firstA))
                    let otherUnitB = getOtherUnit(getOtherUnitIndex(lastA))
                    return (otherUnitA + otherUnitB)
                        .unsolvedCells
                        .filter { $0.candidates.contains(candidate) && !unitA.contains($0) && !unitB.contains($0) }
                        .map { ($0, candidate) }
                } else {
                    return nil
                }
            }.joined())
        }
        
        let rowRemovals = xWing(units: board.rows, getOtherUnit: board.getColumn, getOtherUnitIndex: \.column)
        let columnRemovals = xWing(units: board.columns, getOtherUnit: board.getRow, getOtherUnitIndex: \.row)
        return rowRemovals + columnRemovals
    }.mergeToRemoveCandidates()
}
