enum BruteForceError: Error {
    case noSolutions, multipleSolutions
}

/*
 * Recursively tries every number for each unsolved cell looking for a solution.
 *
 * Motivation for implementing a brute force solution:
 *
 * The purpose of this solver is to go through the exercise of implementing various logical solutions. Why implement
 * brute force if I only care about logical solutions? The first reason is to check the correctness of the logical
 * solutions. When solving a board, the first thing that is done is to get the brute force solution. After that, any
 * logical modifications will be checked against the brute force solution. If a logical solution tries to set an
 * incorrect value to a cell or remove a candidate from a cell which is the known solution, then execution will halt
 * with a runtime error.
 *
 * The second reason for implementing brute force is to check for the number of solutions for a board before trying the
 * logical solutions. If a board cannot be solved or if it has multiple solutions, then I don't bother with the logical
 * solutions. The logical solutions are written assuming that they are operating on a board with only one solution.
 */
func bruteForce(board: Board<SudokuNumber?>) throws -> Board<SudokuNumber> {
    if !board.cells.contains(nil) {
        let filledBoard = board.mapCells { $0! }
        if isSolved(board: filledBoard) {
            return filledBoard
        } else {
            throw BruteForceError.noSolutions
        }
    }
    
    var trialAndError = board
    
    func bruteForce(rowIndex: Int, columnIndex: Int) throws -> Board<SudokuNumber> {
        func moveToNextCell() throws -> Board<SudokuNumber> {
            if columnIndex + 1 >= unitSize {
                try bruteForce(rowIndex: rowIndex + 1, columnIndex: 0)
            } else {
                try bruteForce(rowIndex: rowIndex, columnIndex: columnIndex + 1)
            }
        }
        
        if rowIndex >= unitSize {
            return trialAndError.mapCells { $0! }
        } else if trialAndError[rowIndex, columnIndex] != nil {
            return try moveToNextCell()
        } else {
            let rowInvalid = Set(trialAndError.getRow(rowIndex: rowIndex).compactMap { $0 })
            let columnInvalid = Set(trialAndError.getColumn(columnIndex: columnIndex).compactMap { $0 })
            let blockInvalid = Set(trialAndError
                .getBlock(blockIndex: getBlockIndex(rowIndex: rowIndex, columnIndex: columnIndex))
                .compactMap { $0 })
            let invalid = rowInvalid.union(columnInvalid).union(blockInvalid)
            let valid = Set(SudokuNumber.allCases).subtracting(invalid)
            var singleSolution: Board<SudokuNumber>?
            for guess in valid {
                trialAndError[rowIndex, columnIndex] = guess
                do {
                    let intermediateSolution = try moveToNextCell()
                    if singleSolution == nil {
                        singleSolution = intermediateSolution
                    } else {
                        throw BruteForceError.multipleSolutions
                    }
                } catch BruteForceError.noSolutions {
                    //Ignore.
                }
            }
            trialAndError[rowIndex, columnIndex] = nil
            if let singleSolution {
                return singleSolution
            } else {
                throw BruteForceError.noSolutions
            }
        }
    }
    
    return try bruteForce(rowIndex: 0, columnIndex: 0)
}

private func isSolved(board: Board<SudokuNumber>) -> Bool {
    return board.rows.allSatisfy { Set($0).count == unitSize }
        && board.columns.allSatisfy { Set($0).count == unitSize }
        && board.blocks.allSatisfy { Set($0).count == unitSize }
}
