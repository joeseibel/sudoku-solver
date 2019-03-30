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
 * PruneCandidates: https://github.com/joeseibel/sudoku-solver/commit/d6432b3c881f4884066f66624d5791f5d26e40ee?diff=split#diff-af09b93439eb4275b4b08e09c8a2a2aa
 * NakedSingles: https://github.com/joeseibel/sudoku-solver/commit/d6432b3c881f4884066f66624d5791f5d26e40ee?diff=split#diff-020facf91c5915c049435df05f0db046
 * HiddenSingles: https://github.com/joeseibel/sudoku-solver/commit/d6432b3c881f4884066f66624d5791f5d26e40ee?diff=split#diff-9f151d570c7938966353c69f31b35766
 */
sealed class Cell {
    abstract val row: Int
    abstract val column: Int
    val block: BlockIndex by lazy { BlockIndex.fromCellIndices(row, column) }
}

data class SolvedCell(override val row: Int, override val column: Int, val value: SudokuNumber) : Cell() {
    override fun toString(): String = value.toString()
}

data class UnsolvedCell(
    override val row: Int,
    override val column: Int,
    val candidates: EnumSet<SudokuNumber> = EnumSet.allOf(SudokuNumber::class.java)
) : Cell() {
    override fun toString(): String = "0"
}

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
                val candidates = (index until closingBrace).mapTo(EnumSet.noneOf(SudokuNumber::class.java)) {
                    sudokuNumber(withCandidates[it])
                }
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