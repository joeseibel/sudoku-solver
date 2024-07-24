/*
 * http://www.sudokuwiki.org/Intersection_Removal#LBR
 *
 * For a given row, if a candidate appears in only one block, then the candidate for that block must be placed in that
 * row. The candidate can be removed from the cells which are in the same block, but different rows.
 *
 * For a given column, if a candidate appears in only one block, then the candidate for that block must be placed in
 * that column. The candidate can be removed from cells which are in the same block, but different columns.
 */
func boxLineReduction(board: Board<Cell>) -> [BoardModification] {
    SudokuNumber.allCases.flatMap { candidate -> [LocatedCandidate] in
        
        func boxLineReduction(units: [[Cell]], getUnitIndex: (Location) -> Int) -> [LocatedCandidate] {
            units.flatMap { unit in
                let blockIndices = Set(unit.unsolvedCells.filter { $0.candidates.contains(candidate) }.map(\.block))
                if let blockIndex = blockIndices.first, blockIndices.count == 1 {
                    let unitIndex = getUnitIndex(unit.first!)
                    return board.getBlock(blockIndex: blockIndex)
                        .unsolvedCells
                        .filter { getUnitIndex($0) != unitIndex && $0.candidates.contains(candidate) }
                        .map { ($0, candidate) }
                } else {
                    return []
                }
            }
        }
        
        let rowRemovals = boxLineReduction(units: board.rows, getUnitIndex: \.row)
        let columnRemovals = boxLineReduction(units: board.columns, getUnitIndex: \.column)
        return rowRemovals + columnRemovals
    }.mergeToRemoveCandidates()
}
