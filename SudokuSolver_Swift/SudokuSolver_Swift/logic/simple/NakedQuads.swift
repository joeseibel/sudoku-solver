/*
 * http://www.sudokuwiki.org/Naked_Candidates#NQ
 *
 * If a unit has four unsolved cells with a total of four candidates among them, then those four candidates must be
 * placed in those four cells. The four candidates can be removed from every other cell in the unit.
 */
func nakedQuads(board: Board<Cell>) -> [BoardModification] {
    board.units.flatMap { unit in
        unit.unsolvedCells.zipEveryQuad().compactMap { a, b, c, d in
            let unionOfCandidates = a.candidates.union(b.candidates).union(c.candidates).union(d.candidates)
            return if unionOfCandidates.count == 4 {
                unit.unsolvedCells
                    .filter { $0 != a && $0 != b && $0 != c && $0 != d }
                    .flatMap { cell in
                        cell.candidates.intersection(unionOfCandidates).map { candidate in (cell, candidate) }
                    }
            } else {
                nil
            }
        }.joined()
    }.mergeToRemoveCandidates()
}
