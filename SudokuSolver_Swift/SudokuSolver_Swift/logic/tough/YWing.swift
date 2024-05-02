/*
 * http://www.sudokuwiki.org/Y_Wing_Strategy
 *
 * Given a hinge cell and two wing cells such that the hinge can see both wings, all three cells each have two
 * candidates, there are three total candidates across the three cells, the hinge shares one candidate with one wing and
 * one candidate with the other wing, and the wing cells share a candidate among each other, then this third candidate
 * must be the solution to one of the wings. The third candidate can be removed from any cell which can see both wings.
 */
func yWing(board: Board<Cell>) -> [BoardModification] {
    
    func tryHinge(hinge: UnsolvedCell, wingA: UnsolvedCell, wingB: UnsolvedCell) -> [LocatedCandidate] {
        let wingCandidates = wingA.candidates.intersection(wingB.candidates)
        if let candidate = wingCandidates.first,
           hinge.isInSameUnit(as: wingA) && hinge.isInSameUnit(as: wingB) &&
            hinge.candidates.intersection(wingA.candidates).count == 1 &&
            hinge.candidates.intersection(wingB.candidates).count == 1 &&
            wingCandidates.count == 1
        {
            return board.cells
                .unsolvedCells
                .filter { cell in
                    cell != wingA && cell != wingB &&
                        cell.candidates.contains(candidate) &&
                        cell.isInSameUnit(as: wingA) && cell.isInSameUnit(as: wingB)
                }
                .map { ($0, candidate)}
        } else {
            return []
        }
    }
    
    return board.cells
        .unsolvedCells
        .filter { $0.candidates.count == 2 }
        .zipEveryTriple()
        .filter { a, b, c in a.candidates.union(b.candidates).union(c.candidates).count == 3 }
        .flatMap { a, b, c in
            tryHinge(hinge: a, wingA: b, wingB: c) +
                tryHinge(hinge: b, wingA: a, wingB: c) +
                tryHinge(hinge: c, wingA: a, wingB: b)
        }
        .mergeToRemoveCandidates()
}
