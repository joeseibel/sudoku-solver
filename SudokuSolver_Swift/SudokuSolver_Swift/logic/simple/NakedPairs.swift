/*
 * http://www.sudokuwiki.org/Naked_Candidates#NP
 *
 * If a pair of unsolved cells in a unit has the same two candidates, then those two candidates must be placed in those
 * two cells. The two candidates can be removed from every other cell in the unit.
 */
func nakedPairs(board: Board<Cell>) -> [BoardModification] {
    board.units.flatMap { unit in
        unit.unsolvedCells
            .filter { $0.candidates.count == 2 }
            .zipEveryPair()
            .filter { a, b in a.candidates == b.candidates }
            .flatMap { a, b in
                unit.unsolvedCells
                    .filter { $0 != a && $0 != b }
                    .flatMap { cell in
                        cell.candidates.intersection(a.candidates).map { candidate in (cell, candidate) }
                    }
            }
    }.mergeToRemoveCandidates()
}
