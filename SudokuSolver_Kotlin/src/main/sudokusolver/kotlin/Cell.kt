package sudokusolver.kotlin

import java.util.*

sealed class Cell
data class SolvedCell(val value: SudokuNumber) : Cell()
data class UnsolvedCell(val candidates: EnumSet<SudokuNumber> = EnumSet.allOf(SudokuNumber::class.java)) : Cell()

fun createMutableCellBoard(board: Board<SudokuNumber?>): MutableBoard<Cell> =
    board.mapCellsToMutableBoard { if (it == null) UnsolvedCell() else SolvedCell(it) }

fun createCellBoardFromSimpleString(simpleBoard: String): Board<Cell> {
    require(simpleBoard.length == UNIT_SIZE_SQUARED) {
        "simpleBoard.length is ${simpleBoard.length}, must be $UNIT_SIZE_SQUARED."
    }
    return Board(simpleBoard.chunked(UNIT_SIZE) { row ->
        row.map { cell -> if (cell == '0') UnsolvedCell() else SolvedCell(sudokuNumber(cell)) }
    })
}

fun createCellBoardFromStringWithCandidates(withCandidates: String): Board<Cell> {
    val cells = mutableListOf<Cell>()
    var index = 0
    while (index < withCandidates.length) {
        when (val ch = withCandidates[index]) {
            in '1'..'9' -> {
                cells += SolvedCell(sudokuNumber(ch))
                index++
            }

            '{' -> {
                index++
                val closingBrace = withCandidates.indexOf('}', index)
                require(closingBrace != -1) { "Unmatched '{'." }
                require(closingBrace != index) { "Empty \"{}\"." }
                val charsInBraces = (index until closingBrace).map { withCandidates[it] }
                require(!charsInBraces.contains('{')) { "Nested '{'." }
                charsInBraces.forEach { charInBrace ->
                    require(charInBrace in '1'..'9') { "Invalid character: '$charInBrace'." }
                }
                val candidates = (index until closingBrace).map { sudokuNumber(withCandidates[it]) }.toEnumSet()
                cells += UnsolvedCell(candidates)
                index = closingBrace + 1
            }

            '}' -> throw IllegalArgumentException("Unmatched '}'.")
            else -> throw IllegalArgumentException("Invalid character: '$ch'.")
        }
    }
    require(cells.size == UNIT_SIZE_SQUARED) { "Found ${cells.size} cells, required $UNIT_SIZE_SQUARED" }
    return Board(cells.chunked(UNIT_SIZE))
}