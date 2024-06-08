/*
 * https://www.sudokuwiki.org/Jelly_Fish_Strategy
 *
 * For a quad of rows, if a candidate appears in two, three, or four cells for each row and the candidate appears in
 * exactly four columns across the four rows, forming a four by four grid, then the candidate must be placed in four of
 * the sixteen cells. The candidate can be removed from cells which are in the four columns, but different rows.
 *
 * For a quad of columns, if a candidate appears in two, three, or four cells for each column and the candidate appears
 * in exactly four rows across the four columns, forming a four by four grid, then the candidate must be placed in four
 * of the sixteen cells. The candidate can be removed from cells which are in the four rows, but different columns
 */
func jellyfish(board: Board<Cell>) -> [BoardModification] {
    SudokuNumber.allCases.flatMap { candidate -> [LocatedCandidate] in
        
        func jellyfish(
            units: [[Cell]],
            getOtherUnit: (Int) -> [Cell],
            getOtherUnitIndex: (UnsolvedCell) -> Int
        ) -> [LocatedCandidate] {
            units.zipEveryQuad().flatMap { unitA, unitB, unitC, unitD in
                let aWithCandidate = unitA.unsolvedCells.filter { $0.candidates.contains(candidate) }
                let bWithCandidate = unitB.unsolvedCells.filter { $0.candidates.contains(candidate) }
                let cWithCandidate = unitC.unsolvedCells.filter { $0.candidates.contains(candidate) }
                let dWithCandidate = unitD.unsolvedCells.filter { $0.candidates.contains(candidate) }
                if (2...4).contains(aWithCandidate.count) &&
                    (2...4).contains(bWithCandidate.count) &&
                    (2...4).contains(cWithCandidate.count) &&
                    (2...4).contains(dWithCandidate.count)
                {
                    let withCandidate = aWithCandidate + bWithCandidate + cWithCandidate + dWithCandidate
                    let otherUnitIndices = Set(withCandidate.map(getOtherUnitIndex))
                    if otherUnitIndices.count == 4 {
                        return otherUnitIndices.flatMap(getOtherUnit)
                            .unsolvedCells
                            .filter { cell in cell.candidates.contains(candidate) && !withCandidate.contains(cell) }
                            .map { ($0, candidate) }
                    } else {
                        return []
                    }
                } else {
                    return []
                }
            }
        }
        
        let rowRemovals = jellyfish(units: board.rows, getOtherUnit: board.getColumn, getOtherUnitIndex: \.column)
        let columnRemovals = jellyfish(units: board.columns, getOtherUnit: board.getRow, getOtherUnitIndex: \.row)
        return rowRemovals + columnRemovals
    }.mergeToRemoveCandidates()
}
