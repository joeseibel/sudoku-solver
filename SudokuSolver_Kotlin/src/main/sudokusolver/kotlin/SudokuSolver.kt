package sudokusolver.kotlin

fun main() {
    val board = "000105000140000670080002400063070010900000003010090520007200080026000035000409000"
    println(
        when (val solution = solve(board.toOptionalBoard())) {
            InvalidNoSolutions -> "No Solutions"
            InvalidMultipleSolutions -> "Multiple Solutions"
            is Solution -> solution.board

            is UnableToSolve -> {
                """
                    |Unable to solve:
                    |${solution.board}
                """.trimMargin()
            }
        }
    )
}

private sealed class SolveResult
private object InvalidNoSolutions : SolveResult()
private object InvalidMultipleSolutions : SolveResult()
private class Solution(val board: Board<SudokuNumber>) : SolveResult()
private class UnableToSolve(val board: Board<Cell>) : SolveResult()

private fun solve(input: Board<SudokuNumber?>): SolveResult {
    val bruteForceSolution = bruteForce(input)
    when (bruteForceSolution) {
        NoSolutions -> return InvalidNoSolutions
        MultipleSolutions -> return InvalidMultipleSolutions

        is SingleSolution -> {
            val board = input.toMutableCellBoard()
            do {
                if (board.cells.filterIsInstance<UnsolvedCell>().isEmpty()) {
                    return Solution(bruteForceSolution.solution)
                }
                val modifications = pruneCandidates(board)
                    .ifEmpty { fillSolvedCells(board) }
                modifications.forEach { modification ->
                    val row = modification.row
                    val column = modification.column
                    val cell = board[row, column]
                    check(cell is UnsolvedCell) { "[$row, $column] is already solved." }
                    val knownSolution = bruteForceSolution.solution[row, column]
                    when (modification) {
                        is RemoveCandidates -> {
                            modification.candidates.forEach { candidate ->
                                check(candidate != knownSolution) {
                                    "Cannot remove candidate $candidate from [$row, $column]"
                                }
                                cell.candidates -= candidate
                            }
                        }

                        is SetValue -> {
                            val value = modification.value
                            check(value == knownSolution) {
                                "Cannot set value $value to [$row, $column]. Solution is $knownSolution"
                            }
                            board[row, column] = SolvedCell(value)
                        }
                    }
                }
            } while (modifications.isNotEmpty())

            return UnableToSolve(board.toBoard())
        }
    }
}