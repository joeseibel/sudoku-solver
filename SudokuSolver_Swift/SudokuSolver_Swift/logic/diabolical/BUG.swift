/*
 * http://www.sudokuwiki.org/BUG
 *
 * BUG applies to boards with exactly one unsolved cell with three candidates and every other unsolved cell has two
 * candidates. Removing one of the candidates from the cell with three candidates will result in a board in which all of
 * its unsolved cells have two candidates, which would have multiple solutions. Since removing that candidate from that
 * cell would lead to an invalid board, that candidate must be the solution to that cell.
 *
 * For the three candidates of the cell, two candidates will appear twice in the cell's row, twice in the cell's column,
 * and twice in the cell's block, while one candidate will appear three times in the cell's row, three times in the
 * cell's column, and three times in the cell's block. This check is only performed against the cell's row.
 */
func bug(board: Board<Cell>) -> BoardModification? {
    let cells = board.cells.unsolvedCells.filter { $0.candidates.count != 2 }
    if let cell = cells.first, cells.count == 1 && cell.candidates.count == 3 {
        let row = board.getRow(rowIndex: cell.row).unsolvedCells
        let candidate = cell.candidates
            .first { candidate in row.filter { $0.candidates.contains(candidate) }.count == 3 }!
        return BoardModification(cell: cell, value: candidate)
    } else {
        return nil
    }
}
