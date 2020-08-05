package sudokusolver.kotlin.logic

import sudokusolver.kotlin.Board
import sudokusolver.kotlin.SudokuNumber
import sudokusolver.kotlin.UNIT_SIZE
import sudokusolver.kotlin.enumMinus
import sudokusolver.kotlin.enumUnion
import sudokusolver.kotlin.getBlockIndex
import sudokusolver.kotlin.toMutableBoard
import java.util.EnumSet

sealed class BruteForceSolution
object NoSolutions : BruteForceSolution()
object MultipleSolutions : BruteForceSolution()
data class SingleSolution(val solution: Board<SudokuNumber>) : BruteForceSolution()

/*
 * Recursively tries every number for each unsolved cell looking for a solution.
 *
 * Motivation for implementing a brute force solution:
 *
 * The purpose of this solver is to go through the exercise of implementing various logical solutions. Why implement
 * brute force if I only care about logical solutions? The first reason is to check the correctness of the logical
 * solutions. When solving a board, the first thing that is done is to get the brute force solution. After that, any
 * logical modifications will be checked against the brute force solution. If a logical solution tries to set an
 * incorrect value to a cell or remove a candidate from a cell which is the known solution, then an
 * IllegalStateException is thrown.
 *
 * The second reason for implementing brute force is to check for the number of solutions for a board before trying the
 * logical solutions. If a board cannot be solve or if it has multiple solutions, then I don't bother with the logical
 * solutions. The logical solutions are written assuming the they are operating on a board with only one solution.
 */
fun bruteForce(board: Board<SudokuNumber?>): BruteForceSolution {
    if (board.cells.count { it == null } == 0) {
        val filledBoard = board.mapCells { it!! }
        return if (isSolved(filledBoard)) SingleSolution(filledBoard) else NoSolutions
    }

    val trialAndError = board.toMutableBoard()

    fun bruteForce(rowIndex: Int, columnIndex: Int): BruteForceSolution {
        fun moveToNextCell(): BruteForceSolution {
            return if (columnIndex + 1 >= UNIT_SIZE) {
                bruteForce(rowIndex + 1, 0)
            } else {
                bruteForce(rowIndex, columnIndex + 1)
            }
        }

        return when {
            rowIndex >= UNIT_SIZE -> SingleSolution(trialAndError.mapCellsToBoard { cell -> cell!! })
            trialAndError[rowIndex, columnIndex] != null -> moveToNextCell()

            else -> {
                val rowInvalid = trialAndError.getRow(rowIndex)
                    .filterNotNullTo(EnumSet.noneOf(SudokuNumber::class.java))
                val columnInvalid = trialAndError.getColumn(columnIndex)
                    .filterNotNullTo(EnumSet.noneOf(SudokuNumber::class.java))
                val blockInvalid = trialAndError.getBlock(getBlockIndex(rowIndex, columnIndex))
                    .filterNotNullTo(EnumSet.noneOf(SudokuNumber::class.java))
                val invalid = enumUnion(rowInvalid, columnInvalid, blockInvalid)
                val valid = EnumSet.allOf(SudokuNumber::class.java) enumMinus invalid
                var singleSolution: SingleSolution? = null
                valid.forEach { guess ->
                    trialAndError[rowIndex, columnIndex] = guess
                    val intermediateSolution = moveToNextCell()
                    if (intermediateSolution == MultipleSolutions) {
                        return MultipleSolutions
                    } else if (intermediateSolution is SingleSolution) {
                        if (singleSolution == null) {
                            singleSolution = intermediateSolution
                        } else {
                            return MultipleSolutions
                        }
                    }
                }
                trialAndError[rowIndex, columnIndex] = null
                singleSolution ?: NoSolutions
            }
        }
    }

    return bruteForce(0, 0)
}

private fun isSolved(board: Board<SudokuNumber>): Boolean {
    return board.rows.all { it.toSet().size == UNIT_SIZE }
            && board.columns.all { it.toSet().size == UNIT_SIZE }
            && board.blocks.all { it.toSet().size == UNIT_SIZE }
}