/*
 * https://www.sudokuwiki.org/Hidden_Unique_Rectangles
 *
 * The basic premise of the Hidden Unique Rectangles solution is exactly the same as Unique Rectangles. The only
 * difference is that Hidden Unique Rectangles adds more specific types to the solution. These additional types look for
 * strong links between cells of a rectangle. A strong link exists between two cells for a given candidate when those
 * two cells are the only cells with the candidate in a given row or column.
 */
func hiddenUniqueRectangles(board: Board<Cell>) -> [BoardModification] {
    createRectangles(board: board).compactMap { rectangle in
        let (roof, floor) = rectangle.cells.partitioned { $0.candidates.count == 2 }
        return if floor.count == 1 {
            type1(board: board, rectangle: rectangle, floor: floor.first!)
        } else if roof.count == 2 {
            type2(board: board, roof: roof, commonCandidates: rectangle.commonCandidates)
        } else {
            nil
        }
    }.mergeToRemoveCandidates()
}

/*
 * Type 1
 *
 * If a rectangle has one floor cell, then consider the roof cell on the opposite corner of the rectangle. If one of the
 * common candidates appears twice in that cell's row and twice in that cell's column, which implies that the other
 * occurrences in that row and column are in the two other corners of the rectangle, then setting the other common
 * candidate as the value to that cell would lead to the Deadly Pattern. Therefore, the other common candidate cannot be
 * the solution to that cell. The other common candidate can be removed from the roof cell which is opposite of the one
 * floor cell.
 */
private func type1(board: Board<Cell>, rectangle: Rectangle, floor: UnsolvedCell) -> LocatedCandidate? {
    let row = board.getRow(rowIndex: floor.row).unsolvedCells
    let column = board.getColumn(columnIndex: floor.column).unsolvedCells
    let strongCandidates = rectangle.commonCandidates
        .filter { candidate in
            row.filter { $0.candidates.contains(candidate) }.count == 2 &&
                column.filter { $0.candidates.contains(candidate) }.count == 2
        }
    if let strongCandidate = strongCandidates.first, strongCandidates.count == 1 {
        let oppositeCell = rectangle.cells.first { $0.row != floor.row && $0.column != floor.column }!
        let otherCandidate = rectangle.commonCandidates.first { $0 != strongCandidate }!
        return (oppositeCell, otherCandidate)
    } else {
        return nil
    }
}

/*
 * Type 2
 *
 * If a rectangle has two roof cells, those cells are in the same row, and there exists a strong link for one of the
 * common candidates between one of the roof cells and its corresponding floor cell in the same column, then setting the
 * other common candidate as the value to the other roof cell would lead to the Deadly Pattern. Therefore, the other
 * common candidate cannot be the solution to the other roof cell. The other common candidate can be removed from the
 * other roof cell.
 *
 * If a rectangle has two roof cells, those cells are in the same column, and there exists a strong link for one of the
 * common candidates between one of the roof cells and its corresponding floor cell in the same row, then setting the
 * other common candidate as the value to the other roof cell would lead to the Deadly Pattern. Therefore, the other
 * common candidate cannot be the solution to the other roof cell. The other common candidate can be removed from the
 * other roof cell.
 */
private func type2(board: Board<Cell>, roof: [UnsolvedCell], commonCandidates: Set<SudokuNumber>) -> LocatedCandidate? {
    let roofA = roof[0]
    let roofB = roof[1]
    let commonCandidates = Array(commonCandidates)
    let candidateA = commonCandidates[0]
    let candidateB = commonCandidates[1]
    
    func getRemoval(getUnitIndex: (UnsolvedCell) -> Int, getUnit: (Int) -> [Cell]) -> LocatedCandidate? {
        let unitA = getUnit(getUnitIndex(roofA)).unsolvedCells
        let unitB = getUnit(getUnitIndex(roofB)).unsolvedCells
        return if unitA.filter({ $0.candidates.contains(candidateA) }).count == 2 {
            (roofB, candidateB)
        } else if unitA.filter({ $0.candidates.contains(candidateB) }).count == 2 {
            (roofB, candidateA)
        } else if unitB.filter({ $0.candidates.contains(candidateA) }).count == 2 {
            (roofA, candidateB)
        } else if unitB.filter({ $0.candidates.contains(candidateB) }).count == 2 {
            (roofA, candidateA)
        } else {
            nil
        }
    }
    
    return if roofA.row == roofB.row {
        getRemoval(getUnitIndex: \.column, getUnit: board.getColumn)
    } else if roofA.column == roofB.column {
        getRemoval(getUnitIndex: \.row, getUnit: board.getRow)
    } else {
        nil
    }
}
