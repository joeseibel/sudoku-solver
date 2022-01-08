package sudokusolver.kotlin

/*
 * The only collection that should be used for SudokuNumber is EnumSet. Sometimes this will lead to more complicated
 * code to avoid using a List or Set.
 */
enum class SudokuNumber {
    ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE;

    override fun toString(): String = "${ordinal + 1}"
}

fun sudokuNumber(ch: Char): SudokuNumber =
    when (ch) {
        '1' -> SudokuNumber.ONE
        '2' -> SudokuNumber.TWO
        '3' -> SudokuNumber.THREE
        '4' -> SudokuNumber.FOUR
        '5' -> SudokuNumber.FIVE
        '6' -> SudokuNumber.SIX
        '7' -> SudokuNumber.SEVEN
        '8' -> SudokuNumber.EIGHT
        '9' -> SudokuNumber.NINE
        else -> throw IllegalArgumentException("ch is '$ch', must be between '1' and '9'.")
    }

fun parseOptionalBoard(board: String): Board<SudokuNumber?> {
    require(board.length == UNIT_SIZE_SQUARED) { "board length is ${board.length}, must be $UNIT_SIZE_SQUARED." }
    return Board(board.chunked(UNIT_SIZE) { row ->
        row.map { cell -> cell.takeUnless { it == '0' }?.let { sudokuNumber(it) } }
    })
}

fun parseBoard(board: String): Board<SudokuNumber> {
    require(board.length == UNIT_SIZE_SQUARED) { "board length is ${board.length}, must be $UNIT_SIZE_SQUARED." }
    return Board(board.chunked(UNIT_SIZE) { row -> row.map { sudokuNumber(it) } })
}