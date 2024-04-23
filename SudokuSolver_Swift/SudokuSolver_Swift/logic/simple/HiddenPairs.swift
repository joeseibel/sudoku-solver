/*
 * http://www.sudokuwiki.org/Hidden_Candidates#HP
 *
 * If a pair of candidates exists in exactly two cells in a unit, then those two candidates must be placed in those two
 * cells. All other candidates can be removed from those two cells.
 */
func hiddenPairs(board: Board<Cell>) -> [BoardModification] {
    board.units.flatMap { unit in
        SudokuNumber.allCases.zipEveryPair().compactMap { a, b in
            let cells = unit.unsolvedCells.filter { $0.candidates.contains(a) }
            return if cells.count == 2 && cells == unit.unsolvedCells.filter({ $0.candidates.contains(b) }) {
                cells.flatMap { cell in cell.candidates.subtracting([a, b]).map { candidate in (cell, candidate) } }
            } else {
                nil
            }
        }.joined()
    }.mergeToRemoveCandidates()
}
