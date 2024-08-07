/*
 * https://www.sudokuwiki.org/Sue_De_Coq
 *
 * This solution starts with looking for two or three cells in the same linear unit (row or column) and block and the
 * union of candidates across the cells has a size which is at least two more than the number of cells. In other words,
 * if two cells are selected, then they must have at least four candidates. If three cells are selected, they must have
 * at least five candidates. These cells are the main group in this solution.
 *
 * Once the main group is identified, this solution then searches for an Almost Locked Set in the same linear unit as
 * the main group and also for an ALS in the same block as the main group. As a reminder, an ALS is a set of n unsolved
 * cells, all of which can see each other, and there are n + 1 candidates across all n cells. The two ALSs can only
 * contain candidates found in the main group, they must contain all the candidates of the main group, and there can be
 * no common candidates across the two ALSs.
 *
 * Once we have the main group and the two ALSs, it is then certain that each of the common candidates must appear in
 * one of the three groups. Therefore, for any common candidate, that candidate cannot be the solution for any cell
 * which can see the main group and can see the ALS that has the candidate. The candidates of the linear unit ALS can be
 * removed from other cells of that linear unit which are not a part of the main group. The candidates of the block unit
 * ALS can be removed from other cells of that block which are not a part of the main group.
 */
func sueDeCoq(board: Board<Cell>) -> [BoardModification] {
    
    func sueDeCoq(units: [[Cell]], getUnitIndex: (UnsolvedCell) -> Int) -> [LocatedCandidate] {
        units.map(\.unsolvedCells).flatMap { unit in
            Dictionary(grouping: unit, by: \.block).flatMap { blockIndex, unitByBlock -> [LocatedCandidate] in
                let otherCellsInUnit = unit.filter { $0.block != blockIndex }
                let block = board.getBlock(blockIndex: blockIndex).unsolvedCells
                let otherCellsInBlock = block.filter { getUnitIndex($0) != getUnitIndex(unit.first!) }
                
                func getGroupRemovals(group: [UnsolvedCell]) -> [LocatedCandidate] {
                    let candidates = group.reduce(Set(), { $0.union($1.candidates) })
                    return if candidates.count >= group.count + 2 {
                        getAlmostLockedSets(cells: otherCellsInUnit, groupCandidates: candidates).flatMap { unitALS in
                            getAlmostLockedSets(cells: otherCellsInBlock, groupCandidates: candidates)
                                .filter { blockALS in
                                    unitALS.candidates.count + blockALS.candidates.count == candidates.count &&
                                        unitALS.candidates.intersection(blockALS.candidates).isEmpty
                                }
                                .flatMap { blockALS in
                                    let unitRemovals = unit
                                        .filter { !group.contains($0) && !unitALS.cells.contains($0) }
                                        .flatMap { cell in
                                            cell.candidates.intersection(unitALS.candidates).map { (cell, $0) }
                                        }
                                    let blockRemovals = block
                                        .filter { !group.contains($0) && !blockALS.cells.contains($0) }
                                        .flatMap { cell in
                                            cell.candidates.intersection(blockALS.candidates).map { (cell, $0) }
                                        }
                                    return unitRemovals + blockRemovals
                                }
                        }
                    } else {
                        []
                    }
                }
                
                return if unitByBlock.count == 2 {
                    getGroupRemovals(group: unitByBlock)
                } else if unitByBlock.count == 3 {
                    getGroupRemovals(group: unitByBlock) + unitByBlock.zipEveryPair()
                        .flatMap { a, b in getGroupRemovals(group: [a, b]) }
                } else {
                    []
                }
            }
        }
    }
    
    let rowRemovals = sueDeCoq(units: board.rows, getUnitIndex: \.row)
    let columnRemovals = sueDeCoq(units: board.columns, getUnitIndex: \.column)
    return (rowRemovals + columnRemovals).mergeToRemoveCandidates()
}

private struct ALS {
    let cells: Set<UnsolvedCell>
    let candidates: Set<SudokuNumber>
}

private func getAlmostLockedSets(cells: [UnsolvedCell], groupCandidates: Set<SudokuNumber>) -> [ALS] {
    let almostLockedSets1 = cells.filter { $0.candidates.count == 2 && groupCandidates.isSuperset(of: $0.candidates) }
        .map { ALS(cells: [$0], candidates: $0.candidates) }
    let almostLockedSets2 = cells.zipEveryPair()
        .map { a, b in ALS(cells: [a, b], candidates: a.candidates.union(b.candidates)) }
        .filter { $0.candidates.count == 3 && groupCandidates.isSuperset(of: $0.candidates) }
    let almostLockedSets3 = cells.zipEveryTriple()
        .map { a, b, c in ALS(cells: [a, b, c], candidates: a.candidates.union(b.candidates).union(c.candidates)) }
        .filter { $0.candidates.count == 4 && groupCandidates.isSuperset(of: $0.candidates) }
    let almostLockedSets4 = cells.zipEveryQuad()
        .map { a, b, c, d in
            ALS(
                cells: [a, b, c, d],
                candidates: a.candidates.union(b.candidates).union(c.candidates).union(d.candidates)
            )
        }
        .filter { $0.candidates.count == 5 && groupCandidates.isSuperset(of: $0.candidates) }
    return almostLockedSets1 + almostLockedSets2 + almostLockedSets3 + almostLockedSets4
}
