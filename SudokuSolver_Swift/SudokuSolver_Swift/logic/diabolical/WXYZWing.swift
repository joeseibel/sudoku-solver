/*
 * https://www.sudokuwiki.org/WXYZ_Wing
 *
 * WXYZ-Wing applies for a quad of unsolved cells that has a total of four candidates among the quad. The quad may
 * contain restricted candidates and non-restricted candidates. A restricted candidate is one in which each cell of the
 * quad with the candidate can see every other cell of the quad with the candidate. A non-restricted candidate is one in
 * which at least one cell of the quad with the candidate cannot see every other cell of the quad with the candidate. If
 * a quad contains exactly one non-restricted candidate, then that candidate must be the solution to one of the cells of
 * the quad. The non-restricted candidate can be removed from any cell outside the quad that can see every cell of the
 * quad with the candidate.
 */
func wxyzWing(board: Board<Cell>) -> [BoardModification] {
    board.cells
        .unsolvedCells
        .filter { $0.candidates.count <= 4 }
        .zipEveryQuad()
        .flatMap { a, b, c, d -> [LocatedCandidate] in
            let quad = [a, b, c, d]
            let candidates = a.candidates.union(b.candidates).union(c.candidates).union(d.candidates)
            if candidates.count == 4 {
                let nonRestricted = candidates.filter { candidate in
                    quad.filter { $0.candidates.contains(candidate) }
                        .zipEveryPair()
                        .contains { a, b in !a.isInSameUnit(as: b) }
                }
                if nonRestricted.count == 1, let nonRestricted = nonRestricted.first {
                    let withCandidate = quad.filter { $0.candidates.contains(nonRestricted) }
                    return board.cells
                        .unsolvedCells
                        .filter { cell in
                            cell.candidates.contains(nonRestricted) &&
                                !quad.contains(cell) &&
                                withCandidate.allSatisfy { cell.isInSameUnit(as: $0) }
                        }
                        .map { ($0, nonRestricted) }
                } else {
                    return []
                }
            } else {
                return []
            }
        }
        .mergeToRemoveCandidates()
}
