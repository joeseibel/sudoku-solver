package sudokusolver.kotlin

const val UNIT_SIZE_SQUARE_ROOT = 3
const val UNIT_SIZE = UNIT_SIZE_SQUARE_ROOT * UNIT_SIZE_SQUARE_ROOT

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

abstract class AbstractBoard<T> {
    protected abstract val elements: List<List<T>>

    fun getBlock(blockIndex: BlockIndex): List<T> {
        return elements.drop(blockIndex.row * UNIT_SIZE_SQUARE_ROOT).take(UNIT_SIZE_SQUARE_ROOT).flatMap { row ->
            row.drop(blockIndex.column * UNIT_SIZE_SQUARE_ROOT).take(UNIT_SIZE_SQUARE_ROOT)
        }
    }

    override fun equals(other: Any?): Boolean = other is AbstractBoard<*> && elements == other.elements
    override fun hashCode(): Int = elements.hashCode()

    override fun toString(): String {
        fun joinRows(fromIndex: Int, toIndex: Int): String {
            return elements.subList(fromIndex, toIndex).joinToString("\n") { row ->
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

class Board<T>(elements: Iterable<Iterable<T>>) : AbstractBoard<T>() {
    override val elements: List<List<T>> = elements.map { row -> row.toList() }

    init {
        requireSize(this.elements)
    }

    val cells: List<T> by lazy { this.elements.flatten() }
    val rows: List<List<T>> = this.elements
    val columns: List<List<T>> by lazy { (0 until UNIT_SIZE).map { index -> this.elements.map { row -> row[index] } } }
    val blocks: List<List<T>> by lazy { (0 until UNIT_SIZE).map { index -> getBlock(BlockIndex(index)) } }

    fun <R> mapCells(transform: (T) -> R): Board<R> = Board(elements.map { row -> row.map(transform) })
    fun toMutableBoard(): MutableBoard<T> = MutableBoard(elements)
}

class MutableBoard<T>(elements: Iterable<Iterable<T>>) : AbstractBoard<T>() {
    override val elements: List<MutableList<T>> = elements.map { it.toList().toMutableList() }

    init {
        requireSize(this.elements)
    }

    operator fun get(rowIndex: Int, columnIndex: Int): T = elements[rowIndex][columnIndex]

    operator fun set(rowIndex: Int, columnIndex: Int, element: T) {
        elements[rowIndex][columnIndex] = element
    }

    fun getRow(rowIndex: Int): List<T> = elements[rowIndex]
    fun getColumn(columnIndex: Int): List<T> = elements.map { row -> row[columnIndex] }
    fun <R> mapCellsToBoard(transform: (T) -> R): Board<R> = Board(elements.map { row -> row.map(transform) })
}

private fun requireSize(elements: List<List<*>>) {
    require(elements.size == UNIT_SIZE) { "elements size is ${elements.size}, must be $UNIT_SIZE" }
    elements.forEachIndexed { index, row ->
        require(row.size == UNIT_SIZE) { "elements[$index] size is ${row.size}, must be $UNIT_SIZE" }
    }
}