package sudokusolver.kotlin

/*
 * Options:
 *   Solve or not solve.
 *      Mutate board, return true or false.
 *   Solve and log steps saying which solutions did what.
 *      Mutate board, return log information
 *   Step through the solution, highlighting numbers about to be changed.
 *      Don't mutate board, return change instructions.
 *      -or-
 *      Mutate board, return log information. From log, show highlighted numbers, then update UI board.
 *
 *
 *
 * TODO:
 *  -toSudokuNumber
 *  -What should be inlined?
 *  -Use rows instead of elements.
 *  -return changes to be made instead of making them.
 *  -Restrict functions and methods
 *  -toString for optional board
 */

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
                pruneCandidates(board)
                if (board.cells.all { it.value != null }) {
                    return Solution(bruteForceSolution.solution)
                }
                val modified = fillSolvedCells(board)
            } while (modified)
            return UnableToSolve(board)
        }
    }
}