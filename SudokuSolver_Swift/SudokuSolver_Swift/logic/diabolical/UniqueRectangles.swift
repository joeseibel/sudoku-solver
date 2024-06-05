/*
 * https://www.sudokuwiki.org/Unique_Rectangles
 *
 * The Unique Rectangles solution works by identifying the potential for an invalid pattern of candidates called the
 * Deadly Pattern and then removing candidates that if set as the value would lead to the Deadly Pattern. A Deadly
 * Pattern is defined as a group of four unsolved cells arranged to form a rectangle, each cell containing the same two
 * candidates and only those candidates, and the cells being located in two rows, two columns, and two blocks. If a
 * board contains the Deadly Pattern, then the board cannot have a single solution, but would have multiple solutions.
 * The advantage of recognizing this pattern comes when a board contains a pattern which is close to the Deadly Pattern
 * and the removal of certain candidates would lead to the Deadly Pattern. If a valid board contains a pattern which is
 * close to the Deadly Pattern, it is known that the board will never enter into the Deadly Pattern and candidates can
 * be removed if setting those candidates as values would lead to the Deadly Pattern. A rectangle can be further
 * described by identifying its floor cells and its roof cells. A rectangle's floor are the cells that only contain the
 * two common candidates. A rectangle's roof are the cells that contain the two common candidates as well as additional
 * candidates.
 *
 * Type 1
 *
 * If a rectangle has one roof cell, then this is a potential Deadly Pattern. If the additional candidates were to be
 * removed from the roof, then that would lead to a Deadly Pattern. The two common candidates can be removed from the
 * roof leaving only the additional candidates remaining.
 */
func uniqueRectanglesType1(board: Board<Cell>) -> [BoardModification] {
    createRectangles(board: board).flatMap { rectangle in
        let roof = rectangle.roof()
        return roof.count == 1 ? rectangle.commonCandidates.map { (roof.first!, $0) } : []
    }.mergeToRemoveCandidates()
}

/*
 * Type 2
 *
 * If a rectangle has two roof cells and there is only one additional candidate appearing in both roof cells, then this
 * is a potential Deadly Pattern. If the additional candidate were to be removed from the roof cells, then that would
 * lead to a Deadly Pattern, therefore the additional candidate must be the solution for one of the two roof cells. The
 * common candidate can be removed from any other cell that can see both of the roof cells.
 */
func uniqueRectanglesType2(board: Board<Cell>) -> [BoardModification] {
    createRectangles(board: board).flatMap { rectangle in
        let roof = rectangle.roof()
        if roof.count == 2 {
            let roofA = roof[0]
            let roofB = roof[1]
            if roofA.candidates.count == 3 && roofA.candidates == roofB.candidates {
                let additionalCandidate = roofA.candidates.subtracting(rectangle.commonCandidates).first!
                return board.cells
                    .unsolvedCells
                    .filter {
                        $0.candidates.contains(additionalCandidate) &&
                            $0 != roofA &&
                            $0 != roofB &&
                            $0.isInSameUnit(as: roofA) &&
                            $0.isInSameUnit(as: roofB)
                    }
                    .map { ($0, additionalCandidate) }
            } else {
                return []
            }
        } else {
            return []
        }
    }.mergeToRemoveCandidates()
}

/*
 * Type 3
 *
 * If a rectangle has two roof cells, each roof cell has one additional candidate, and the additional candidates are
 * different, then this is a potential Deadly Pattern. One or both of these additional candidates must be the solution,
 * so the roof cells can be treated as a single cell with the two additional candidates. If there is another cell that
 * can see both roof cells and has the additional candidates as its candidates, then the roof cells and the other cell
 * effectively form a Naked Pair. The additional candidates can be removed from any other cell in the unit.
 */
func uniqueRectanglesType3(board: Board<Cell>) -> [BoardModification] {
    createRectangles(board: board).flatMap { rectangle -> [LocatedCandidate] in
        let roof = rectangle.roof()
        if roof.count == 2 {
            let roofA = roof[0]
            let roofB = roof[1]
            if roofA.candidates.count == 3 && roofB.candidates.count == 3 && roofA.candidates != roofB.candidates {
                let additionalCandidates = roofA.candidates.union(roofB.candidates).subtracting(rectangle.commonCandidates)
                
                func getRemovals(getUnitIndex: (UnsolvedCell) -> Int, getUnit: (Int) -> [Cell]) -> [LocatedCandidate] {
                    let indexA = getUnitIndex(roofA)
                    let indexB = getUnitIndex(roofB)
                    if indexA == indexB {
                        let unit = getUnit(indexA).unsolvedCells
                        return unit.first { $0.candidates == additionalCandidates }
                            .map { pairCell in
                                unit.filter { $0 != pairCell && $0 != roofA && $0 != roofB }
                                    .flatMap { cell in
                                        cell.candidates.intersection(additionalCandidates).map { (cell, $0) }
                                    }
                            }
                            ?? []
                    } else {
                        return []
                    }
                }
                
                return getRemovals(getUnitIndex: \.row, getUnit: board.getRow) +
                    getRemovals(getUnitIndex: \.column, getUnit: board.getColumn) +
                    getRemovals(getUnitIndex: \.block, getUnit: board.getBlock)
            } else {
                return []
            }
        } else {
            return []
        }
    }.mergeToRemoveCandidates()
}

/*
 * Type 3/3b with Triple Pseudo-Cells
 *
 * If a rectangle has two roof cells, then this is a potential Deadly Pattern. If the roof cells can see two other cells
 * and the union of candidates among the roof cells' additional candidates and the other cells' candidates is three
 * candidates, then the roof cells and the other two cells effectively form a Naked Triple. The three candidates in the
 * union can be removed from any other cell in the unit.
 */
func uniqueRectanglesType3BWithTriplePseudoCells(board: Board<Cell>) -> [BoardModification] {
    createRectangles(board: board).flatMap { rectangle -> [LocatedCandidate] in
        let roof = rectangle.roof()
        if roof.count == 2 {
            let roofA = roof[0]
            let roofB = roof[1]
            let additionalCandidates = roofA.candidates.union(roofB.candidates).subtracting(rectangle.commonCandidates)
            
            func getRemovals(getUnitIndex: (UnsolvedCell) -> Int, getUnit: (Int) -> [Cell]) -> [LocatedCandidate] {
                let indexA = getUnitIndex(roofA)
                let indexB = getUnitIndex(roofB)
                if indexA == indexB {
                    let unit = getUnit(indexA).unsolvedCells.filter { $0 != roofA && $0 != roofB }
                    return unit.zipEveryPair().flatMap { tripleA, tripleB -> [LocatedCandidate] in
                        let tripleCandidates = additionalCandidates.union(tripleA.candidates).union(tripleB.candidates)
                        return if tripleCandidates.count == 3 {
                            unit.filter { $0 != tripleA && $0 != tripleB }
                                .flatMap { cell in cell.candidates.intersection(tripleCandidates).map { (cell, $0) } }
                        } else {
                            []
                        }
                    }
                } else {
                    return []
                }
            }
            
            return getRemovals(getUnitIndex: \.row, getUnit: board.getRow) +
                getRemovals(getUnitIndex: \.column, getUnit: board.getColumn) +
                getRemovals(getUnitIndex: \.block, getUnit: board.getBlock)
        } else {
            return []
        }
    }.mergeToRemoveCandidates()
}

/*
 * Type 4
 *
 * If a rectangle has two roof cells, then this is a potential Deadly Pattern. For a unit common to the roof cells, if
 * one of the common candidates are only found in the roof cells of that unit, then setting the other candidate as the
 * solution to one of the roof cells would lead to the Deadly Pattern. The other common candidate can be removed from
 * the roof cells.
 */
func uniqueRectanglesType4(board: Board<Cell>) -> [BoardModification] {
    createRectangles(board: board).flatMap { rectangle -> [LocatedCandidate] in
        let roof = rectangle.roof()
        if roof.count == 2 {
            let roofA = roof[0]
            let roofB = roof[1]
            let commonCandidates = Array(rectangle.commonCandidates)
            let commonCandidateA = commonCandidates[0]
            let commonCandidateB = commonCandidates[1]
            
            func getRemovals(getUnitIndex: (UnsolvedCell) -> Int, getUnit: (Int) -> [Cell]) -> [LocatedCandidate] {
                let indexA = getUnitIndex(roofA)
                let indexB = getUnitIndex(roofB)
                if indexA == indexB {
                    let unit = getUnit(indexA).unsolvedCells
                    
                    func searchUnit(search: SudokuNumber, removal: SudokuNumber) -> [LocatedCandidate] {
                        unit.filter { $0.candidates.contains(search) }.count == 2 ? roof.map { ($0, removal) } : []
                    }
                    
                    return searchUnit(search: commonCandidateA, removal: commonCandidateB) +
                        searchUnit(search: commonCandidateB, removal: commonCandidateA)
                } else {
                    return []
                }
            }
            
            return getRemovals(getUnitIndex: \.row, getUnit: board.getRow) +
                getRemovals(getUnitIndex: \.column, getUnit: board.getColumn) +
                getRemovals(getUnitIndex: \.block, getUnit: board.getBlock)
        } else {
            return []
        }
    }.mergeToRemoveCandidates()
}

/*
 * Type 5
 *
 * If a rectangle has two floor cells in diagonally opposite corners of the rectangle and one of the common candidates
 * only appears in the rectangle for the rows and columns that the rectangle exists in, thus forming strong links for
 * the candidate along the four edges of the rectangle, then this is a potential Deadly Pattern. If the non-strong
 * link candidate were to be set as the solution to one of the floor cells, then the strong link candidate would have to
 * be the solution for the roof cells and the non-strong link candidate would need to be set as the solution to the
 * other floor cell, leading to the Deadly Pattern. The non-strong link candidate cannot be the solution to either floor
 * cell. Since each floor cell only contains two candidates, this means that the strong link candidate must be the
 * solution for the floor cells.
 */
func uniqueRectanglesType5(board: Board<Cell>) -> [BoardModification] {
    Array(createRectangles(board: board).compactMap { rectangle in
        let floor = rectangle.floor()
        if floor.count == 2 {
            let floorA = floor[0]
            let floorB = floor[1]
            return if floorA.row != floorB.row && floorA.column != floorB.column {
                rectangle.commonCandidates
                    .first { candidate in
                        
                        func hasStrongLink(unit: [Cell]) -> Bool {
                            unit.unsolvedCells.filter { $0.candidates.contains(candidate) }.count == 2
                        }
                        
                        return floor.allSatisfy {
                            hasStrongLink(unit: board.getRow(rowIndex: $0.row)) &&
                                hasStrongLink(unit: board.getColumn(columnIndex: $0.column))
                        }
                    }
                    .map { strongLinkCandidate in
                        floor.map { BoardModification(cell: $0, value: strongLinkCandidate) }
                    }
            } else {
                nil
            }
        } else {
            return nil
        }
    }.joined())
}

private struct Rectangle {
    fileprivate let cells: [UnsolvedCell]
    fileprivate let commonCandidates: Set<SudokuNumber>
    
    fileprivate init(cells: [UnsolvedCell]) {
        self.cells = cells
        commonCandidates = cells.map(\.candidates).reduce(Set(SudokuNumber.allCases), { $0.intersection($1) })
    }
    
    fileprivate func floor() -> [UnsolvedCell] {
        cells.filter { $0.candidates.count == 2 }
    }
    
    fileprivate func roof() -> [UnsolvedCell] {
        cells.filter { $0.candidates.count > 2 }
    }
}

private func createRectangles(board: Board<Cell>) -> [Rectangle] {
    board.rows
        .zipEveryPair()
        .flatMap { rowA, rowB in
            zip(rowA, rowB)
                .compactMap { cellA, cellB in
                    if case .unsolvedCell(let cellA) = cellA, case .unsolvedCell(let cellB) = cellB {
                        (cellA, cellB)
                    } else {
                        nil
                    }
                }
                .zipEveryPair()
                .map { columnA, columnB in Rectangle(cells: [columnA.0, columnA.1, columnB.0, columnB.1]) }
        }
        .filter { rectangle in rectangle.commonCandidates.count == 2 && Set(rectangle.cells.map(\.block)).count == 2 }
}
