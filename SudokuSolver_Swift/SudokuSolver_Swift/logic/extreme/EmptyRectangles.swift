/*
 * https://www.sudokuwiki.org/Empty_Rectangles
 *
 * This solution starts with looking for empty rectangles in blocks. An empty rectangle is a collection of four cells,
 * all contained within a single block, arranged in a rectangle, and none of them contain a particular candidate. The
 * cells can either be solved cells or unsolved cells without the candidate. For the other cells which are in the block,
 * but are outside the rectangle, at least two of them must contain the candidate and those cells must be in at least
 * two different rows and two different columns.
 *
 * This creates a situation in which two lines can be drawn through the block; one line along a row and the other along
 * a column. The two lines must not pass through any of the empty rectangle cells and all the cells with the candidate
 * must have a line pass through it. A valid block is one in which there is only one option for the placement of these
 * lines. This is why the cells with the candidate must be in at least two different rows and two different columns. The
 * cell in which these lines intersect is then used to find removals outside the block. The empty rectangle itself is
 * used to find a valid intersection point, but then the rectangle is disregarded for the remainder of the solution.
 *
 * Removals are looked for in cells which are outside the block, but which can see the intersection. If the intersection
 * can see one end of a strong link which is outside the intersection's block and there is another cell with the
 * candidate outside the intersection's block, but it can see the intersection and the other end of the strong link,
 * then there is a contradiction. If the candidate were to be set as the solution to the other cell, then the strong
 * link and this newly set solution would remove the candidate from every cell within the intersection's block, thus
 * invalidating that block. This means that the candidate cannot be the solution to that cell and can be removed.
 */
func emptyRectangles(board: Board<Cell>) -> [BoardModification] {
    SudokuNumber.allCases.flatMap { candidate in
        getIntersections(board: board, candidate: candidate).flatMap { row, column -> [LocatedCandidate] in
            let block = board[row, column].block
            
            func getRemovals(
                unit: [Cell],
                getOtherUnitIndex: (Cell) -> Int,
                getOtherUnit: (Int) -> [Cell],
                getRemovalRow: (Cell) -> Int,
                getRemovalColumn: (Cell) -> Int
            ) -> [LocatedCandidate] {
                unit.filter { $0.block != block && $0.has(candidate: candidate) }.compactMap { strongLink1 in
                    let otherUnit = getOtherUnit(getOtherUnitIndex(strongLink1))
                        .filter { $0.has(candidate: candidate) && $0 != strongLink1 }
                    return if let strongLink2 = otherUnit.first,
                              otherUnit.count == 1 && strongLink1.block != strongLink2.block,
                              case .unsolvedCell(let removalCell) =
                                board[getRemovalRow(strongLink2), getRemovalColumn(strongLink2)],
                              removalCell.candidates.contains(candidate)
                    {
                        (removalCell, candidate)
                    } else {
                        nil
                    }
                }
            }
            
            let rowRemovals = getRemovals(
                unit: board.getRow(rowIndex: row),
                getOtherUnitIndex: \.column,
                getOtherUnit: board.getColumn,
                getRemovalRow: \.row,
                getRemovalColumn: { _ in column }
            )
            let columnRemovals = getRemovals(
                unit: board.getColumn(columnIndex: column),
                getOtherUnitIndex: \.row,
                getOtherUnit: board.getRow,
                getRemovalRow: { _ in row },
                getRemovalColumn: \.column
            )
            return rowRemovals + columnRemovals
        }
    }.mergeToRemoveCandidates()
}

private func getIntersections(board: Board<Cell>, candidate: SudokuNumber) -> [(Int, Int)] {
    (0 ..< unitSize).flatMap { row in
        let rowInBlock = row % unitSizeSquareRoot
        let rectangleRow1 = rowInBlock == 0 ? row + 1 : row - rowInBlock
        let rectangleRow2 = rowInBlock == 2 ? row - 1 : row - rowInBlock + 2
        return (0 ..< unitSize).filter { column in
            let columnInBlock = column % unitSizeSquareRoot
            let rectangleColumn1 = columnInBlock == 0 ? column + 1 : column - columnInBlock
            let rectangleColumn2 = columnInBlock == 2 ? column - 1 : column - columnInBlock + 2
            //Check that the rectangle is empty.
            return !board[rectangleRow1, rectangleColumn1].has(candidate: candidate) &&
                !board[rectangleRow1, rectangleColumn2].has(candidate: candidate) &&
                !board[rectangleRow2, rectangleColumn1].has(candidate: candidate) &&
                !board[rectangleRow2, rectangleColumn2].has(candidate: candidate) &&
                //Check that at least one cell in the same block and row as the intersection has the candidate.
                (board[row, rectangleColumn1].has(candidate: candidate) ||
                    board[row, rectangleColumn2].has(candidate: candidate)) &&
                //Check that at least one cell in the same block and column as the intersection has the candidate.
                (board[rectangleRow1, column].has(candidate: candidate) ||
                    board[rectangleRow2, column].has(candidate: candidate))
        }.map { column in (row, column) }
    }
}

private extension Cell {
    func has(candidate: SudokuNumber) -> Bool {
        if case .unsolvedCell(let unsolvedCell) = self {
            unsolvedCell.candidates.contains(candidate)
        } else {
            false
        }
    }
}
