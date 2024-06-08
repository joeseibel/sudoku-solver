/*
 * http://www.sudokuwiki.org/XYZ_Wing
 *
 * Given a hinge cell and two wing cells such that the hinge can see both wings, the hinge has three candidates, the
 * wings have two candidates each, there is one candidate shared among all three cells, two candidates are shared
 * between the hinge and one wing and two candidates between the hinge and the other wing, and the common candidate is
 * the only one shared between the wings, then the common candidate must be the solution to one of the cells. The common
 * candidate can be removed from any cell which can see all three cells.
 */
func xyzWing(board: Board<Cell>) -> [BoardModification] {
    board.cells.unsolvedCells.filter { $0.candidates.count == 3 }.flatMap { hinge in
        board.cells
            .unsolvedCells
            .zipEveryPair()
            .filter { wingA, wingB in
                wingA.candidates.count == 2 && wingB.candidates.count == 2 &&
                    hinge.isInSameUnit(as: wingA) && hinge.isInSameUnit(as: wingB) &&
                    hinge.candidates.union(wingA.candidates).union(wingB.candidates).count == 3
            }
            .flatMap { wingA, wingB -> [LocatedCandidate] in
                let candidates = wingA.candidates.intersection(wingB.candidates)
                return if let candidate = candidates.first, candidates.count == 1 {
                    board.cells
                        .unsolvedCells
                        .filter { cell in
                            cell != hinge && cell != wingA && cell != wingB &&
                                cell.candidates.contains(candidate) &&
                                cell.isInSameUnit(as: hinge) &&
                                cell.isInSameUnit(as: wingA) &&
                                cell.isInSameUnit(as: wingB)
                        }
                        .map { ($0, candidate) }
                } else {
                    []
                }
            }
    }.mergeToRemoveCandidates()
}
