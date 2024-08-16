/*
 * http://www.sudokuwiki.org/Hidden_Candidates#HQ
 *
 * If four candidates exist across four cells in a unit, then those four candidates must be placed in those four cells.
 * All other candidates can be removed from those four cells.
 */
func hiddenQuads(board: Board<Cell>) -> [BoardModification] {
    board.units.flatMap { unit in
        SudokuNumber.allCases.zipEveryQuad().flatMap { a, b, c, d -> [LocatedCandidate] in
            let cells = unit.unsolvedCells
                .filter { $0.candidates.contains(a) ||
                    $0.candidates.contains(b) ||
                    $0.candidates.contains(c) ||
                    $0.candidates.contains(d)
                }
            if cells.count == 4 {
                let union = cells.reduce(Set()) { $0.union($1.candidates) }
                return if union.contains(a) && union.contains(b) && union.contains(c) && union.contains(d) {
                    cells.flatMap { cell in
                        cell.candidates.subtracting([a, b, c, d]).map { candidate in (cell, candidate) }
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
