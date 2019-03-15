package sudokusolver.kotlin.logic

import sudokusolver.kotlin.BlockIndex
import sudokusolver.kotlin.Board
import sudokusolver.kotlin.SudokuNumber
import sudokusolver.kotlin.UNIT_SIZE
import sudokusolver.kotlin.toMutableBoard

sealed class BruteForceSolution
object NoSolutions : BruteForceSolution()
object MultipleSolutions : BruteForceSolution()
data class SingleSolution(val solution: Board<SudokuNumber>) : BruteForceSolution()

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
                val rowInvalid = trialAndError.getRow(rowIndex).filterNotNull()
                val columnInvalid = trialAndError.getColumn(columnIndex).filterNotNull()
                val blockInvalid = trialAndError.getBlock(BlockIndex(rowIndex, columnIndex)).filterNotNull()
                val valid = SudokuNumber.values().toSet() - rowInvalid - columnInvalid - blockInvalid
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