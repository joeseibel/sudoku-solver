/*
 * https://www.sudokuwiki.org/Extended_Unique_Rectangles
 *
 * Extended Unique Rectangles are like Unique Rectangles except that they are 2x3 instead of 2x2. The cells in the
 * rectangle must be spread over three blocks and the dimension that has three elements must be spread over three units
 * (rows or columns). If there are only three candidates found among the six cells, then such a rectangle is the Deadly
 * Pattern. If there is one cell with additional candidates, then the removal of such candidates would lead to a Deadly
 * Pattern. The common candidates can be removed from the cell leaving only the additional candidates remaining.
 */
func extendedUniqueRectangles(board: Board<Cell>) -> [BoardModification] {
    (getRemovals(units: board.rows) + getRemovals(units: board.columns)).mergeToRemoveCandidates()
}

private func getRemovals(units: [[Cell]]) -> [LocatedCandidate] {
    units.zipEveryPair()
        .flatMap { unitA, unitB in
            zip(unitA, unitB)
                .compactMap { cellA, cellB -> (UnsolvedCell, UnsolvedCell)? in
                    if case .unsolvedCell(let cellA) = cellA, case .unsolvedCell(let cellB) = cellB {
                        (cellA, cellB)
                    } else {
                        nil
                    }
                }
                .zipEveryTriple()
        }
        .map { otherA, otherB, otherC in ([otherA.0, otherB.0, otherC.0], [otherA.1, otherB.1, otherC.1]) }
        .filter { unitA, unitB in Set((unitA + unitB).map(\.block)).count == 3 }
        .flatMap { unitA, unitB -> [LocatedCandidate] in
            let unitACandidates = unitA.reduce(Set(), { $0.union($1.candidates) })
            let unitBCandidates = unitB.reduce(Set(), { $0.union($1.candidates) })
            return if unitACandidates.count == 3 {
                getRemovals(commonCandidates: unitACandidates, unit: unitB, unitCandidates: unitBCandidates)
            } else if unitBCandidates.count == 3 {
                getRemovals(commonCandidates: unitBCandidates, unit: unitA, unitCandidates: unitACandidates)
            } else {
                []
            }
        }
}

private func getRemovals(
    commonCandidates: Set<SudokuNumber>,
    unit: [UnsolvedCell], unitCandidates: Set<SudokuNumber>
) -> [LocatedCandidate] {
    if unitCandidates.count > 3 && unitCandidates.isSuperset(of: commonCandidates) {
        let withAdditional = unit.filter { !commonCandidates.isSuperset(of: $0.candidates) }
        return if withAdditional.count == 1, let withAdditional = withAdditional.first {
            withAdditional.candidates.intersection(commonCandidates).map { (withAdditional, $0) }
        } else {
            []
        }
    } else {
        return []
    }
}
