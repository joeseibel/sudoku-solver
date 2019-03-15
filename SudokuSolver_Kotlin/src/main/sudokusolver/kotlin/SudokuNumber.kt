package sudokusolver.kotlin

import java.util.*

enum class SudokuNumber {
    ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE;

    override fun toString(): String = "${ordinal + 1}"
}

fun numbers(vararg numbers: Int): EnumSet<SudokuNumber> {
    val set = EnumSet.noneOf(SudokuNumber::class.java)
    set.addAll(numbers.asSequence().map { SudokuNumber.values()[it - 1] })
    return set
}

fun Char.toSudokuNumber(): SudokuNumber = SudokuNumber.values()[Character.getNumericValue(this) - 1]