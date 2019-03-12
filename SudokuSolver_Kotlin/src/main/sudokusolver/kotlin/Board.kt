package sudokusolver.kotlin

import java.util.*

private const val UNIT_SIZE_SQUARE_ROOT = 3
const val UNIT_SIZE = UNIT_SIZE_SQUARE_ROOT * UNIT_SIZE_SQUARE_ROOT
private const val UNIT_SIZE_SQUARED = UNIT_SIZE * UNIT_SIZE

enum class SudokuNumber {
    ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE;

    override fun toString(): String = "${ordinal + 1}"
}

class BlockIndex {
    val row: Int
    val column: Int

    constructor(cellRow: Int, cellColumn: Int) {
        require(cellRow in 0 until UNIT_SIZE) { "cellRow is $cellRow, must be between 0 and ${UNIT_SIZE - 1}." }
        require(cellColumn in 0 until UNIT_SIZE) {
            "cellColumn is $cellColumn, must be between 0 and ${UNIT_SIZE - 1}."
        }
        row = cellRow / UNIT_SIZE_SQUARE_ROOT
        column = cellColumn / UNIT_SIZE_SQUARE_ROOT
    }

    constructor(index: Int) {
        require(index in 0 until UNIT_SIZE) { "index is $index, must be between 0 and ${UNIT_SIZE - 1}. " }
        row = index / UNIT_SIZE_SQUARE_ROOT
        column = index % UNIT_SIZE_SQUARE_ROOT
    }
}

abstract class AbstractBoard<out T> {
    abstract val rows: List<List<T>>
    abstract val columns: List<List<T>>
    abstract val blocks: List<List<T>>
    abstract val cells: List<T>

    operator fun get(rowIndex: Int, columnIndex: Int): T = rows[rowIndex][columnIndex]

    fun getRow(rowIndex: Int): List<T> = rows[rowIndex]
    fun getColumn(columnIndex: Int): List<T> = rows.map { row -> row[columnIndex] }

    fun getBlock(blockIndex: BlockIndex): List<T> {
        return rows.drop(blockIndex.row * UNIT_SIZE_SQUARE_ROOT).take(UNIT_SIZE_SQUARE_ROOT).flatMap { row ->
            row.drop(blockIndex.column * UNIT_SIZE_SQUARE_ROOT).take(UNIT_SIZE_SQUARE_ROOT)
        }
    }

    override fun equals(other: Any?): Boolean = other is AbstractBoard<*> && rows == other.rows
    override fun hashCode(): Int = rows.hashCode()

    override fun toString(): String {
        fun joinRows(fromIndex: Int, toIndex: Int): String {
            return rows.subList(fromIndex, toIndex).joinToString("\n") { row ->
                fun joinCells(fromIndex: Int, toIndex: Int) = row.subList(fromIndex, toIndex).joinToString(" ")

                val first = joinCells(0, 3)
                val second = joinCells(3, 6)
                val third = joinCells(6, 9)
                "$first | $second | $third"
            }
        }

        return """
            |${joinRows(0, 3)}
            |------+-------+------
            |${joinRows(3, 6)}
            |------+-------+------
            |${joinRows(6, 9)}
        """.trimMargin()
    }
}

class Board<out T>(elements: Iterable<Iterable<T>>) : AbstractBoard<T>() {
    override val rows: List<List<T>> = elements.map { it.toList() }

    init {
        requireSize(rows)
    }

    override val columns: List<List<T>> by lazy { (0 until UNIT_SIZE).map { index -> rows.map { row -> row[index] } } }
    override val blocks: List<List<T>> by lazy { (0 until UNIT_SIZE).map { index -> getBlock(BlockIndex(index)) } }
    override val cells: List<T> by lazy { rows.flatten() }

    inline fun <R> mapCells(transform: (T) -> R): Board<R> = Board(rows.map { row -> row.map(transform) })
    inline fun <R> mapCellsToMutableBoard(transform: (T) -> R): MutableBoard<R> =
        MutableBoard(rows.map { row -> row.map(transform) })
}

class MutableBoard<T>(elements: Iterable<Iterable<T>>) : AbstractBoard<T>() {
    override val rows: List<MutableList<T>> = elements.map { it.toList().toMutableList() }

    init {
        requireSize(rows)
    }

    override val columns: List<List<T>>
        get() = (0 until UNIT_SIZE).map { index -> rows.map { row -> row[index] } }

    override val blocks: List<List<T>>
        get() = (0 until UNIT_SIZE).map { index -> getBlock(BlockIndex(index)) }

    override val cells: List<T>
        get() = rows.flatten()

    operator fun set(rowIndex: Int, columnIndex: Int, element: T) {
        rows[rowIndex][columnIndex] = element
    }

    inline fun <R> mapCellsToBoard(transform: (T) -> R): Board<R> = Board(rows.map { row -> row.map(transform) })

    fun toBoard(): Board<T> = Board(rows)
}

private fun requireSize(elements: List<List<*>>) {
    require(elements.size == UNIT_SIZE) { "elements size is ${elements.size}, must be $UNIT_SIZE" }
    elements.forEachIndexed { index, row ->
        require(row.size == UNIT_SIZE) { "elements[$index] size is ${row.size}, must be $UNIT_SIZE" }
    }
}

fun <T> Board<T>.toMutableBoard(): MutableBoard<T> = MutableBoard(rows)

fun String.toOptionalBoard(): Board<SudokuNumber?> {
    require(length == UNIT_SIZE_SQUARED) { "String length is $length, must be $UNIT_SIZE_SQUARED" }
    return Board(chunked(UNIT_SIZE).map { row ->
        row.map { cell -> if (cell == '0') null else SudokuNumber.values()[cell.toInt() - '0'.toInt() - 1] }
    })
}

fun String.toBoard(): Board<SudokuNumber> {
    require(length == UNIT_SIZE_SQUARED) { "String length is $length, must be $UNIT_SIZE_SQUARED" }
    return Board(chunked(UNIT_SIZE).map { row ->
        row.map { cell -> SudokuNumber.values()[cell.toInt() - '0'.toInt() - 1] }
    })
}

fun buildCellBoard(board: Board<SudokuNumber?>): Board<Cell> =
    board.mapCells { value -> if (value == null) UnsolvedCell() else SolvedCell(value) }

fun buildMutableCellBoard(board: Board<SudokuNumber?>): MutableBoard<Cell> =
    board.mapCellsToMutableBoard { value -> if (value == null) UnsolvedCell() else SolvedCell(value) }

sealed class Cell
class SolvedCell(val value: SudokuNumber) : Cell()
class UnsolvedCell : Cell() {
    val candidates: EnumSet<SudokuNumber> = EnumSet.allOf(SudokuNumber::class.java)
}