private enum BruteForceError: Error {
    case noSolutions, multipleSolutions
}

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
            return trialAndError.mapCells { cell in cell! }
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
    fatalError("Implement Me!")
}
