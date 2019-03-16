package sudokusolver.kotlin

import sudokusolver.kotlin.logic.MultipleSolutions
import sudokusolver.kotlin.logic.NoSolutions
import sudokusolver.kotlin.logic.SingleSolution
import sudokusolver.kotlin.logic.bruteForce
import sudokusolver.kotlin.logic.fillSolvedCells
import sudokusolver.kotlin.logic.hiddenSingles
import sudokusolver.kotlin.logic.pruneCandidates

fun main() {
    val board = "200070038000006070300040600008020700100000006007030400004080009060400000910060002"
    println(
        when (val solution = solve(board.toOptionalBoard())) {
            InvalidNoSolutions -> "No Solutions"
            InvalidMultipleSolutions -> "Multiple Solutions"
            is Solution -> solution.board

            is UnableToSolve -> {
                """
                    Unable to solve:
                    Simple String: ${solution.board.toSimpleString()}
                    With Candidates: ${solution.board.toStringWithCandidates()}
                """.trimIndent()
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
            val board = createMutableCellBoard(input)
            do {
                if (board.cells.filterIsInstance<UnsolvedCell>().isEmpty()) {
                    return Solution(bruteForceSolution.solution)
                }
                val modifications = pruneCandidates(board)
                    .ifEmpty { fillSolvedCells(board) }
                    .ifEmpty { hiddenSingles(board) }
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
                                check(candidate in cell.candidates) {
                                    "$candidate is not a candidate of [$row, $column]"
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