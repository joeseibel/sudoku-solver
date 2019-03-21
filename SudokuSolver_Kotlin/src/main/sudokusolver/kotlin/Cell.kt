package sudokusolver.kotlin

import java.util.*

sealed class Cell {
    abstract val row: Int
    abstract val column: Int
    val block: BlockIndex by lazy { BlockIndex.fromCellIndicies(row, column) }
}

data class SolvedCell(override val row: Int, override val column: Int, val value: SudokuNumber) : Cell()

data class UnsolvedCell(
    override val row: Int,
    override val column: Int,
    val candidates: EnumSet<SudokuNumber> = EnumSet.allOf(SudokuNumber::class.java)
) : Cell()

fun Board<Cell>.toSimpleString(): String = cells.joinToString("") { cell ->
    when (cell) {
        is SolvedCell -> cell.value.toString()
        is UnsolvedCell -> "0"
    }
}

fun Board<Cell>.toStringWithCandidates(): String = cells.joinToString("") { cell ->
    when (cell) {
        is SolvedCell -> cell.value.toString()
        is UnsolvedCell -> "{${cell.candidates.joinToString("")}}"
    }
}

fun createMutableCellBoard(board: Board<SudokuNumber?>): MutableBoard<Cell> =
    board.mapCellsToMutableBoardIndexed { row, column, cell ->
        if (cell == null) UnsolvedCell(row, column) else SolvedCell(row, column, cell)
    }

fun createCellBoardFromSimpleString(simpleBoard: String): Board<Cell> {
    require(simpleBoard.length == UNIT_SIZE_SQUARED) {
        "simpleBoard.length is ${simpleBoard.length}, must be $UNIT_SIZE_SQUARED."
    }
    return Board(simpleBoard.chunked(UNIT_SIZE).mapIndexed { rowIndex, row ->
        row.mapIndexed { columnIndex, cell ->
            if (cell == '0') {
                UnsolvedCell(rowIndex, columnIndex)
            } else {
                SolvedCell(rowIndex, columnIndex, sudokuNumber(cell))
            }
        }
    })
}

fun createCellBoardFromStringWithCandidates(withCandidates: String): Board<Cell> {
    val cellBuilders = mutableListOf<(row: Int, column: Int) -> Cell>()
    var index = 0
    while (index < withCandidates.length) {
        when (val ch = withCandidates[index]) {
            in '1'..'9' -> {
                cellBuilders += { row, column -> SolvedCell(row, column, sudokuNumber(ch)) }
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
                cellBuilders += { row, column ->
                    UnsolvedCell(row, column, candidates)
                }
                index = closingBrace + 1
            }

            '}' -> throw IllegalArgumentException("Unmatched '}'.")
            else -> throw IllegalArgumentException("Invalid character: '$ch'.")
        }
    }
    require(cellBuilders.size == UNIT_SIZE_SQUARED) {
        "Found ${cellBuilders.size} cells, required $UNIT_SIZE_SQUARED"
    }
    return Board(cellBuilders.chunked(UNIT_SIZE).mapIndexed { rowIndex, row ->
        row.mapIndexed { columnIndex, cell -> cell(rowIndex, columnIndex) }
    })
}