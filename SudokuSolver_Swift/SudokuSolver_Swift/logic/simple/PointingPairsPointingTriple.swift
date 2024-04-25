/*
 * http://www.sudokuwiki.org/Intersection_Removal#IR
 *
 * For a given block, if a candidate appears in only one row, then the candidate for that row must be placed in that
 * block. The candidate can be removed from cells which are in the same row, but different blocks.
 *
 * For a given block, if a candidate appears in only one column, then the candidate for that column must be placed in
 * that block. The candidate can be removed from cells which are in the same column, but different blocks.
 */
func pointingPairsPointingTriples(board: Board<Cell>) -> [BoardModification] {
    board.blocks.flatMap { block in
        let unsolved = block.unsolvedCells
        let blockIndex = block.first!.block
        return SudokuNumber.allCases.flatMap { candidate -> [LocatedCandidate] in
            let withCandidate = unsolved.filter { $0.candidates.contains(candidate) }
            
            func pointingPairsPointingTriples(
                getUnit: (Int) -> [Cell],
                getUnitIndex: (UnsolvedCell) -> Int
            ) -> [LocatedCandidate] {
                let unitIndices = Set(withCandidate.map(getUnitIndex))
                return if let unitIndex = unitIndices.first, unitIndices.count == 1 {
                    getUnit(unitIndex)
                        .unsolvedCells
                        .filter { $0.block != blockIndex && $0.candidates.contains(candidate) }
                        .map { ($0, candidate) }
                } else {
                    []
                }
            }
            
            let rowModifications = pointingPairsPointingTriples(getUnit: board.getRow, getUnitIndex: \.row)
            let columnModifications = pointingPairsPointingTriples(getUnit: board.getColumn, getUnitIndex: \.column)
            return rowModifications + columnModifications
        }
    }.mergeToRemoveCandidates()
}
