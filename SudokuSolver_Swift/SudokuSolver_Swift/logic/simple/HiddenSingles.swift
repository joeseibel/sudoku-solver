/*
 * http://www.sudokuwiki.org/Getting_Started
 *
 * If a candidate exists in only one cell in a unit, then the candidate must be placed in that cell.
 */
func hiddenSingles(board: Board<Cell>) -> [BoardModification] {
    let modifications = board.units.flatMap { unit in
        let unsolved = unit.unsolvedCells
        return SudokuNumber.allCases.compactMap { candidate in
            let withCandidate = unsolved.filter { $0.candidates.contains(candidate) }
            return if let cell = withCandidate.first, withCandidate.count == 1 {
                BoardModification(cell: cell, value: candidate)
            } else {
                nil
            }
        }
    }
    return Array(Set(modifications))
}
