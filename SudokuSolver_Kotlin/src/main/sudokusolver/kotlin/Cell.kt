package sudokusolver.kotlin

import java.util.EnumSet

/*
 * Motivation for storing the row and column indices in the Cell:
 *
 * I went back and forth on this one. Originally, I did not want Cell to store its own indices. Every time a cell is
 * accessed, it is through the board, so the logic functions can keep track of the indices. My view was that it is
 * unnecessary to store the Cell's location when the board knows where the cell is. I didn't want to waste the memory.
 * The nice thing is that the indices can be easily tracked in Kotlin by using functions like withIndex and mapIndexed.
 *
 * While this can be done, it makes the logic functions much more complicated because they must maintain the index
 * through every call to filter, map, and flatMap. It is harder to understand the flow of the logic functions because
 * there is so much boilerplate in maintaining the indices. Granted, Kotlin helps make this boilerplate minimal through
 * the data class IndexedValue, the ability to destructure an IndexedValue in a lambda's parameter list, and the ability
 * to ignore a destructured element or lambda parameter by replacing its name with an underscore. However, even though
 * the boilerplate is minimal, it is still there.
 *
 * Now that each Cell knows where it is, logic functions can more cleanly navigate the board without keeping track of
 * where it is in the board. The proof of this benefit is by looking at the diff between logic functions without indices
 * in the Cell and those same functions with indices in the Cell.
 *
 * PruneCandidates: https://github.com/joeseibel/sudoku-solver/commit/d6432b3c881f4884066f66624d5791f5d26e40ee#diff-a74cfdcefbbae82b7a77a891128246f6f9fe0e443419ba7e019b7762191ac06c
 * NakedSingles: https://github.com/joeseibel/sudoku-solver/commit/d6432b3c881f4884066f66624d5791f5d26e40ee#diff-e19a4c430b58e8ab605eda104111fe9e849197301311796f01c609204429560c
 * HiddenSingles: https://github.com/joeseibel/sudoku-solver/commit/d6432b3c881f4884066f66624d5791f5d26e40ee#diff-9246c6cae9f040c632a117726cc1678c801b55fe03985cad55c692ddbe27886f
 */
sealed class Cell {
    abstract val row: Int
    abstract val column: Int
    val block: Int by lazy { getBlockIndex(row, column) }
}

data class SolvedCell(override val row: Int, override val column: Int, val value: SudokuNumber) : Cell() {
    init {
        require(row in 0 until UNIT_SIZE) { "row is $row, must be between 0 and ${UNIT_SIZE - 1}." }
        require(column in 0 until UNIT_SIZE) { "column is $column, must be between 0 and ${UNIT_SIZE - 1}." }
    }

    override fun toString(): String = value.toString()
}

data class UnsolvedCell(
    override val row: Int,
    override val column: Int,
    val candidates: EnumSet<SudokuNumber> = EnumSet.allOf(SudokuNumber::class.java)
) : Cell() {
    init {
        require(row in 0 until UNIT_SIZE) { "row is $row, must be between 0 and ${UNIT_SIZE - 1}." }
        require(column in 0 until UNIT_SIZE) { "column is $column, must be between 0 and ${UNIT_SIZE - 1}." }
        require(candidates.isNotEmpty()) { "candidates must not be empty." }
    }

    infix fun isInSameUnit(other: UnsolvedCell): Boolean =
        row == other.row || column == other.column || block == other.block

    override fun toString(): String = "0"
}

typealias LocatedCandidate = Pair<UnsolvedCell, SudokuNumber>

val LocatedCandidate.candidate: SudokuNumber
    get() = second

fun Board<Cell>.toSimpleString(): String =
    cells.joinToString("") { cell ->
        when (cell) {
            is SolvedCell -> cell.value.toString()
            is UnsolvedCell -> "0"
        }
    }

fun Board<Cell>.toStringWithCandidates(): String =
    rows.joinToString("\n") { row ->
        row.joinToString("") { cell ->
            when (cell) {
                is SolvedCell -> cell.value.toString()
                is UnsolvedCell -> "{${cell.candidates.joinToString("")}}"
            }
        }
    }

fun createMutableCellBoard(board: Board<SudokuNumber?>): MutableBoard<Cell> =
    board.mapCellsToMutableBoardIndexed { row, column, cell ->
        if (cell == null) UnsolvedCell(row, column) else SolvedCell(row, column, cell)
    }

fun parseSimpleCells(simpleBoard: String): Board<Cell> {
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

fun parseCellsWithCandidates(withCandidates: String): Board<Cell> {
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
                val candidates = charsInBraces.mapTo(EnumSet.noneOf(SudokuNumber::class.java)) { sudokuNumber(it) }
                cellBuilders += { row, column -> UnsolvedCell(row, column, candidates) }
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