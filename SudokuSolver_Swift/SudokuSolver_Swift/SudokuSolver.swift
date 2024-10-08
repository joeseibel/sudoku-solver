import ArgumentParser

@main
struct SudokuSolver: ParsableCommand {
    @Argument(help: "Sudoku board as \(unitSizeSquared) numbers with blanks expressed as 0.")
    var board: String
    
    func run() throws {
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
}

struct UnableToSolveError: Error {
    let message: String
    
    init(board: Board<Cell>) {
        message = """
            Unable to solve:
            \(board)
            
            Simple String: \(String(simpleBoard: board))
            
            With Candidates:
            \(String(withCandidates: board))
            """
    }
}

func solve(input: Board<SudokuNumber?>) throws -> Board<SudokuNumber> {
    let bruteForceSolution = try bruteForce(board: input)
    var board = Board(toCellBoard: input)
    var modifications: [BoardModification]
    repeat {
        if board.cells.unsolvedCells.isEmpty {
            return bruteForceSolution
        }
        modifications = performNextSolution(board: board)
        for modification in modifications {
            let row = modification.row
            let column = modification.column
            guard case .unsolvedCell(let cell) = board[row, column] else {
                preconditionFailure("[\(row), \(column) is already solved.")
            }
            let knownSolution = bruteForceSolution[row, column]
            switch modification {
            case .removeCandidates(let modification):
                for candidate in modification.candidates {
                    precondition(
                        candidate != knownSolution,
                        "Cannot remove candidate \(candidate) from [\(row), \(column)]"
                    )
                    precondition(
                        cell.candidates.contains(candidate),
                        "\(candidate) is not a candidate of [\(row), \(column)]"
                    )
                }
                let candidates = cell.candidates.subtracting(modification.candidates)
                board[row, column] = Cell(row: row, column: column, candidates: candidates)
            case .setValue(let modification):
                let value = modification.value
                precondition(
                    value == knownSolution,
                    "Cannot set value \(value) to [\(row), \(column)]. Solution is \(knownSolution)"
                )
                board[row, column] = Cell(row: row, column: column, value: value)
            }
        }
    } while !modifications.isEmpty
    throw UnableToSolveError(board: board)
}

private func performNextSolution(board: Board<Cell>) -> [BoardModification] {
    let solutions = [
        //Start of simple solutions.
        pruneCandidates,
        nakedSingles,
        hiddenSingles,
        nakedPairs,
        nakedTriples,
        hiddenPairs,
        hiddenTriples,
        nakedQuads,
        hiddenQuads,
        pointingPairsPointingTriples,
        boxLineReduction,
        //Start of tough solutions.
        xWing,
        simpleColoringRule2,
        simpleColoringRule4,
        yWing,
        swordfish,
        xyzWing,
        //Start of diabolical solutions.
        xCyclesRule1,
        xCyclesRule2,
        xCyclesRule3,
        { board in bug(board: board).map { [$0] } ?? [] },
        xyChains,
        medusaRule1,
        medusaRule2,
        medusaRule3,
        medusaRule4,
        medusaRule5,
        medusaRule6,
        jellyfish,
        uniqueRectanglesType1,
        uniqueRectanglesType2,
        uniqueRectanglesType3,
        uniqueRectanglesType3BWithTriplePseudoCells,
        uniqueRectanglesType4,
        uniqueRectanglesType5,
        extendedUniqueRectangles,
        hiddenUniqueRectangles,
        wxyzWing,
        alignedPairExclusion,
        //Start of extreme solutions.
        groupedXCyclesRule1,
        groupedXCyclesRule2,
        groupedXCyclesRule3,
        emptyRectangles,
        finnedXWing,
        finnedSwordfish,
        alternatingInferenceChainsRule1,
        alternatingInferenceChainsRule2,
        alternatingInferenceChainsRule3,
        sueDeCoq
    ]
    return solutions.lazy.map { $0(board) }.first { !$0.isEmpty } ?? []
}
