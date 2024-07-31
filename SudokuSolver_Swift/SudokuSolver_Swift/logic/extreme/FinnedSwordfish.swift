/*
 * https://www.sudokuwiki.org/Finned_Swordfish
 *
 * Finned Swordfish is an extension of Swordfish in a similar manner to the way that Finned X-Wing is an extension of
 * X-Wing. As a reminder, Swordfish looks for a 3x3 grid of cells in which a particular candidate appears in most or all
 * of those cells. If the candidate appears two or three times in each row of the grid and for those rows, the candidate
 * appears in exactly three columns, then the candidate can be removed from the columns of the grid, but in different
 * rows. If the candidate appears two or three times in each column of the grid and for those columns, the candidate
 * appears in exactly three rows, then the candidate can be removed from the rows of the grid, but in different columns.
 *
 * In Finned Swordfish, eight of the cells of a 3x3 grid will follow the same rules as Swordfish. Only one cell will
 * have additional unsolved cells with the candidate next to it. The fin must be in the same block as the cell, but the
 * cell itself may or may not have the candidate.
 *
 * For a triple of rows, two rows are the base rows if the candidate appears two or three times in each row and the
 * candidate appears in exactly three columns of the two rows. The remaining row is a finned row if the candidate
 * appears once or twice outside the three columns, but in the same block as one of the cells of the grid. That cell is
 * the finned cell. The candidate can be removed from cells that are in the same column as the finned cell, but are
 * outside the grid.
 *
 * For a triple of columns, two columns are the base columns if the candidate appears two or three times in each column
 * and the candidate appears in exactly three rows of the two columns. The remaining column is a finned column if the
 * candidate appears once or twice outside the three rows, but in the same block as one of the cells of the grid. That
 * cell is the finned cell. The candidate can be removed from cells that are in the same row as the finned cell, but are
 * outside the grid.
 */
func finnedSwordfish(board: Board<Cell>) -> [BoardModification] {
    SudokuNumber.allCases.flatMap { candidate -> [LocatedCandidate] in
        
        func finnedSwordfish(
            units: [[Cell]],
            getUnitIndex: (UnsolvedCell) -> Int,
            getOtherUnitIndex: (Location) -> Int,
            getOtherUnit: (Int) -> [Cell],
            getFinnedCell: (_ finnedUnitIndex: Int, _ otherUnitIndex: Int) -> Cell
        ) -> [LocatedCandidate] {
            let unitsWithCandidate = units
                .map { unit in unit.unsolvedCells.filter { $0.candidates.contains(candidate) } }
                .filter { !$0.isEmpty }
            return unitsWithCandidate.filter { (2...3).contains($0.count) }
                .zipEveryPair()
                .flatMap { baseUnitA, baseUnitB -> [LocatedCandidate] in
                    let otherUnitIndices = Set((baseUnitA + baseUnitB).map(getOtherUnitIndex))
                    return if otherUnitIndices.count == 3 {
                        unitsWithCandidate.flatMap { finnedUnit -> [LocatedCandidate] in
                            let finnedUnitIndex = getUnitIndex(finnedUnit.first!)
                            let unitIndices: Set = [
                                finnedUnitIndex,
                                getUnitIndex(baseUnitA.first!),
                                getUnitIndex(baseUnitB.first!)
                            ]
                            if unitIndices.count == 3 {
                                let outsideOtherUnitIndices = finnedUnit
                                    .filter { !otherUnitIndices.contains(getOtherUnitIndex($0)) }
                                if (1...2).contains(outsideOtherUnitIndices.count) {
                                    let blockIndices = Set(outsideOtherUnitIndices.map(\.block))
                                    if let blockIndex = blockIndices.first, blockIndices.count == 1 {
                                        let finnedCells = otherUnitIndices.map { getFinnedCell(finnedUnitIndex, $0) }
                                            .filter { $0.block == blockIndex }
                                        return if let finnedCell = finnedCells.first, finnedCells.count == 1 {
                                            getOtherUnit(getOtherUnitIndex(finnedCell))
                                                .unsolvedCells
                                                .filter {
                                                    $0.candidates.contains(candidate) &&
                                                        $0.block == blockIndex &&
                                                        !unitIndices.contains(getUnitIndex($0))
                                                }
                                                .map { ($0, candidate) }
                                        } else {
                                            []
                                        }
                                    } else {
                                        return []
                                    }
                                } else {
                                    return []
                                }
                            } else {
                                return []
                            }
                        }
                    } else {
                        []
                    }
                }
        }
        
        let rowRemovals = finnedSwordfish(
            units: board.rows,
            getUnitIndex: \.row,
            getOtherUnitIndex: \.column,
            getOtherUnit: board.getColumn
        ) { finnedUnitIndex, otherUnitIndex in board[finnedUnitIndex, otherUnitIndex] }
        
        let columnRemovals = finnedSwordfish(
            units: board.columns,
            getUnitIndex: \.column,
            getOtherUnitIndex: \.row,
            getOtherUnit: board.getRow
        ) { finnedUnitIndex, otherUnitIndex in board[otherUnitIndex, finnedUnitIndex] }
        
        return rowRemovals + columnRemovals
    }.mergeToRemoveCandidates()
}
