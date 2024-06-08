/*
 * http://www.sudokuwiki.org/Sword_Fish_Strategy
 *
 * For a triple of rows, if a candidate appears in two or three cells for each row and the candidate appears in exactly
 * three columns across the three rows, forming a three by three grid, then the candidate must be placed in three of the
 * nine cells. The candidate can be removed from cells which are in the three columns, but different rows.
 *
 * For a triple of columns, if a candidate appears in two or three cells for each column and the candidate appears in
 * exactly three rows across the three columns, forming a three by three grid, then the candidate must be placed in
 * three of the nine cells. The candidate can be removed from cells which are in the three rows, but different columns.
 */
func swordfish(board: Board<Cell>) -> [BoardModification] {
    SudokuNumber.allCases.flatMap { candidate -> [LocatedCandidate] in
        
        func swordfish(
            units: [[Cell]],
            getOtherUnit: (Int) -> [Cell],
            getOtherUnitIndex: (UnsolvedCell) -> Int
        ) -> [LocatedCandidate] {
            units.zipEveryTriple().flatMap { unitA, unitB, unitC -> [LocatedCandidate] in
                let aWithCandidate = unitA.unsolvedCells.filter { $0.candidates.contains(candidate) }
                let bWithCandidate = unitB.unsolvedCells.filter { $0.candidates.contains(candidate) }
                let cWithCandidate = unitC.unsolvedCells.filter { $0.candidates.contains(candidate) }
                if (2...3).contains(aWithCandidate.count) &&
                    (2...3).contains(bWithCandidate.count) &&
                    (2...3).contains(cWithCandidate.count
                ) {
                    let withCandidate = aWithCandidate + bWithCandidate + cWithCandidate
                    let otherUnitIndices = Set(withCandidate.map(getOtherUnitIndex))
                    return if otherUnitIndices.count == 3 {
                        otherUnitIndices
                            .flatMap(getOtherUnit)
                            .unsolvedCells
                            .filter { cell in cell.candidates.contains(candidate) && !withCandidate.contains(cell) }
                            .map { ($0, candidate) }
                    } else {
                        []
                    }
                } else {
                    return []
                }
            }
        }
        
        let rowRemovals = swordfish(units: board.rows, getOtherUnit: board.getColumn, getOtherUnitIndex: \.column)
        let columnRemovals = swordfish(units: board.columns, getOtherUnit: board.getRow, getOtherUnitIndex: \.row)
        return rowRemovals + columnRemovals
    }.mergeToRemoveCandidates()
}
