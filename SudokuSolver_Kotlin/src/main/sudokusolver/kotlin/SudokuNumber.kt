package sudokusolver.kotlin

enum class SudokuNumber {
    ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE;

    override fun toString(): String = "${ordinal + 1}"
}

fun sudokuNumber(ch: Char): SudokuNumber = SudokuNumber.values()[Character.getNumericValue(ch) - 1]