if CommandLine.arguments.count != 2 {
    print("usage: SudokuSolver_Swift board")
} else {
    let board = CommandLine.arguments[1]
    if board.count != unitSizeSquared || board.contains(where: { !$0.isASCII || !$0.isWholeNumber }) {
        print("board must be \(unitSizeSquared) numbers with blanks expressed as 0")
    } else {
        do {
            print(try solve(input: Board(optionalBoard: board)))
        } catch BruteForceError.noSolutions {
            print("No Solutions")
        } catch BruteForceError.multipleSolutions {
            print("Multiple Solutions")
        } catch let error as UnableToSolveError {
            print(error.message)
        }
    }
}
