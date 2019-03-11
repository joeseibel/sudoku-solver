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
                    |${solution.board.mapCells { it.value?.toString() ?: "0" }}
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
            val board = buildCellBoard(input, bruteForceSolution.solution)
            do {
                if (board.cells.all { it.value != null }) {
                    return Solution(bruteForceSolution.solution)
                }
                val modifications = pruneCandidates(board)
                    .ifEmpty { fillSolvedCells(board) }
                modifications.forEach { modification ->
                    when (modification) {
                        is RemoveCandidates -> {
                            val (row, column, candidates) = modification
                            board[row, column].removeCandidates(candidates)
                        }

                        is SetValue -> {
                            val (row, column, value) = modification
                            board[row, column].setValue(value)
                        }
                    }
                }
            } while (modifications.isNotEmpty())

            return UnableToSolve(board)
        }
    }
}