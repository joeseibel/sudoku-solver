package sudokusolver.kotlin

enum class SudokuNumber {
    ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE;

    override fun toString(): String = "${ordinal + 1}"
}

fun main() {
    // Single Solution
    val intBoard = listOf(
        listOf(0, 1, 0, 0, 4, 0, 5, 6, 0),
        listOf(2, 3, 0, 6, 1, 5, 0, 8, 0),
        listOf(0, 0, 0, 8, 0, 0, 1, 0, 0),

        listOf(0, 5, 0, 0, 2, 0, 0, 0, 8),
        listOf(6, 0, 0, 7, 8, 1, 0, 0, 5),
        listOf(9, 0, 0, 0, 6, 0, 0, 2, 0),

        listOf(0, 0, 6, 0, 0, 8, 0, 0, 0),
        listOf(0, 8, 0, 4, 7, 3, 0, 5, 6),
        listOf(0, 4, 5, 0, 9, 0, 0, 1, 0)
    )
    val numberBoard = intBoard.map { row ->
        row.map { cell ->
            when (cell) {
                0 -> null
                in 1..9 -> SudokuNumber.values()[cell - 1]
                else -> throw IllegalArgumentException("Invalid cell value: $cell")
            }
        }
    }
    val solution = bruteForce(Board(numberBoard))
    println(
        when (solution) {
            NoSolutions -> "No Solutions"
            MultipleSolutions -> "Multiple Solutions"
            is SingleSolution -> solution.solution
        }
    )
}