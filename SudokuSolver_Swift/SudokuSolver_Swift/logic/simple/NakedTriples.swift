/*
 * http://www.sudokuwiki.org/Naked_Candidates#NT
 *
 * If a unit has three unsolved cells with a total of three candidates among them, then those three candidates must be
 * placed in those three cells. The three candidates can be removed from every other cell in the unit.
 */
func nakedTriples(board: Board<Cell>) -> [BoardModification] {
    board.units.flatMap { unit in
        unit.unsolvedCells.zipEveryTriple().compactMap { a, b, c in
            let unionOfCandidates = a.candidates.union(b.candidates).union(c.candidates)
            if unionOfCandidates.count == 3 {
                return unit.unsolvedCells
                    .filter { $0 != a && $0 != b && $0 != c }
                    .flatMap { cell in
                        cell.candidates.intersection(unionOfCandidates).map { candidate in (cell, candidate) }
                    }
            } else {
                return nil
            }
        }.joined()
    }.mergeToRemoveCandidates()
}
