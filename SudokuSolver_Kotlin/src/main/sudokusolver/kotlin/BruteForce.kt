package sudokusolver.kotlin

sealed class BruteForceSolution
object NoSolutions : BruteForceSolution()
object MultipleSolutions : BruteForceSolution()
data class SingleSolution(val solution: Board<SudokuNumber>) : BruteForceSolution()

fun bruteForce(board: Board<SudokuNumber?>): BruteForceSolution {
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