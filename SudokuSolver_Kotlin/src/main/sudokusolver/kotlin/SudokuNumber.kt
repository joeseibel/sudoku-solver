package sudokusolver.kotlin

import java.util.*

enum class SudokuNumber {
    ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE;

    override fun toString(): String = "${ordinal + 1}"
}

fun sudokuNumbers(vararg numbers: Int): EnumSet<SudokuNumber> {
    val set = EnumSet.noneOf(SudokuNumber::class.java)
    set.addAll(numbers.asSequence().map { SudokuNumber.values()[it - 1] })
    return set
}

fun sudokuNumber(ch: Char): SudokuNumber = SudokuNumber.values()[Character.getNumericValue(ch) - 1]