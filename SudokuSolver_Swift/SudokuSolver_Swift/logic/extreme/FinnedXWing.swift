/*
 * https://www.sudokuwiki.org/Finned_X_Wing
 *
 * Finned X-Wing is an extension of X-Wing in which one of the corners of a rectangle of cells has a fin next to it. As
 * a reminder, X-Wing looks for a rectangle of unsolved cells with a particular candidate. If the candidate only appears
 * twice in each of the rows of the rectangle, then the candidate can be removed from the columns of the rectangle, but
 * in different rows. If the candidate only appears twice in each of the columns of the rectangle, then the candidate
 * can be removed from the rows of the rectangle, but in different columns.
 *
 * In Finned X-Wing, three of the corners of a rectangle will follow the same rules as X-Wing. Only one corner will have
 * additional unsolved cells with the candidate next to it. The fin must be in the same block as the corner, but the
 * corner itself may or may not have the candidate. If the corner does not have the candidate, the pattern is called a
 * Sashimi Finned X-Wing. From an implementation perspective, there is no difference between a regular Finned X-Wing and
 * Sashimi.
 *
 * For a pair of rows in different blocks, one row is the base row if the candidate appears exactly twice in that row,
 * but in different blocks. The other row is considered to be a finned row if the candidate appears in two blocks of
 * that row, one of those blocks of the row contains a regular corner, and the other block of the row contains a fin. A
 * regular corner is a cell with the candidate, it shares the same column as one of the candidates of the base row, and
 * there are no other candidates in that block of the row. The candidates of the base row along with the regular corner
 * form three corners of a rectangle with the fourth corner being a finned corner. The fourth corner may or may not have
 * the candidate. A fin is one or two cells in the finned row that do not share a column with either of the candidates
 * of the base row, but are in the same block as the finned corner. With all of these constraints, the candidate must be
 * placed in opposite corners of the rectangle, or the fin in the case of the finned corner. The candidate can be
 * removed from cells which are in the same column as the finned corner, the same block as the fin, but different rows.
 *
 * For a pair of columns in different blocks, one column is the base column if the candidate appears exactly twice in
 * that column, but in different blocks. The other column is considered to be a finned column if the candidate appears
 * in two blocks of that column, one of those blocks of the column contains a regular corner, and the other block of the
 * column contains a fin. A regular corner is a cell with the candidate, it shares the same row as one of the candidates
 * of the base column, and there are no other candidates in that block of the column. The candidates of the base column
 * along with the regular corner form three corners of a rectangle with the fourth corner being a finned corner. The
 * fourth corner may or may not have the candidate. A fin is one or two cells in the finned column that do not share a
 * row with either of the candidates of the base column, but are in the same block as the finned corner. With all of
 * these constraints, the candidate must be placed in opposite corners of the rectangle, or the fin in the case of the
 * finned corner. The candidate can be removed from cells which are in the same row as the finned corner, the same block
 * as the fin, but different columns.
 */
func finnedXWing(board: Board<Cell>) -> [BoardModification] {
    SudokuNumber.allCases.flatMap { candidate -> [LocatedCandidate] in
        
        func finnedXWing(units: [[Cell]], getOtherUnitIndex: (Location) -> Int) -> [LocatedCandidate] {
            units.flatMap { baseUnit -> [LocatedCandidate] in
                let withCandidate = baseUnit.unsolvedCells.filter { $0.candidates.contains(candidate) }
                return if let baseUnitCell1 = withCandidate.first,
                          let baseUnitCell2 = withCandidate.last,
                          withCandidate.count == 2 && baseUnitCell1.block != baseUnitCell2.block
                {
                    units.filter { $0.first!.block != baseUnit.first!.block }.flatMap { finnedUnit in
                        let finnedUnitByBlock = Dictionary(
                            grouping: finnedUnit.unsolvedCells.filter { $0.candidates.contains(candidate) },
                            by: \.block
                        )
                        if finnedUnitByBlock.count == 2 {
                            
                            func tryFin(finnedCorner: Cell, otherCorner: Cell) -> [LocatedCandidate]? {
                                if let finnedBlock = finnedUnitByBlock[finnedCorner.block],
                                   let otherBlock = finnedUnitByBlock[otherCorner.block],
                                   finnedBlock.contains(where: { $0.wrapped != finnedCorner }) &&
                                    otherBlock.count == 1 &&
                                    otherBlock.contains(where: { $0.wrapped == otherCorner })
                                {
                                    board.getBlock(blockIndex: finnedCorner.block)
                                        .unsolvedCells
                                        .filter {
                                            getOtherUnitIndex($0) == getOtherUnitIndex(finnedCorner) &&
                                                $0.wrapped != finnedCorner &&
                                                $0.candidates.contains(candidate)
                                        }
                                        .map { ($0, candidate) }
                                } else {
                                    nil
                                }
                            }
                            
                            let finnedUnitCell1 = finnedUnit[getOtherUnitIndex(baseUnitCell1)]
                            let finnedUnitCell2 = finnedUnit[getOtherUnitIndex(baseUnitCell2)]
                            return tryFin(finnedCorner: finnedUnitCell1, otherCorner: finnedUnitCell2)
                                ?? tryFin(finnedCorner: finnedUnitCell2, otherCorner: finnedUnitCell1)
                                ?? []
                        } else {
                            return []
                        }
                    }
                } else {
                    []
                }
            }
        }
        
        return finnedXWing(units: board.rows, getOtherUnitIndex: \.column) +
            finnedXWing(units: board.columns, getOtherUnitIndex: \.row)
    }.mergeToRemoveCandidates()
}

private extension UnsolvedCell {
    var wrapped: Cell {
        .unsolvedCell(self)
    }
}
