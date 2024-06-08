/*
 * http://www.sudokuwiki.org/Hidden_Candidates#HT
 *
 * If three candidates exist across three cells in a unit, then those three candidates must be placed in those three
 * cells. All other candidates can be removed from those three cells.
 */
func hiddenTriples(board: Board<Cell>) -> [BoardModification] {
    board.units.flatMap { unit in
        SudokuNumber.allCases.zipEveryTriple().flatMap { a, b, c -> [LocatedCandidate] in
            let cells = unit.unsolvedCells
                .filter { $0.candidates.contains(a) || $0.candidates.contains(b) || $0.candidates.contains(c) }
            if cells.count == 3 {
                let union = cells.reduce(Set()) { $0.union($1.candidates) }
                return if union.contains(a) && union.contains(b) && union.contains(c) {
                    cells.flatMap { cell in
                        cell.candidates.subtracting([a, b, c]).map { candidate in (cell, candidate) }
                    }
                } else {
                    []
                }
            } else {
                return []
            }
        }
    }.mergeToRemoveCandidates()
}
