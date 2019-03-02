package sudokusolver.kotlin

const val UNIT_SIZE_SQUARE_ROOT = 3
const val UNIT_SIZE = UNIT_SIZE_SQUARE_ROOT * UNIT_SIZE_SQUARE_ROOT

class BlockIndex(cellRow: Int, cellColumn: Int) {
    val blockRow = cellRow / UNIT_SIZE_SQUARE_ROOT
    val blockColumn = cellColumn / UNIT_SIZE_SQUARE_ROOT

    init {
        require(cellRow in 0..8) { "cellRow is $cellRow, must be between 0 and 8." }
        require(cellColumn in 0..8) { "cellColumn is $cellColumn, must be between 0 and 8." }
    }
}

open class Board<T>(private val list: List<List<T>>) {
    init {
        require(list.size == UNIT_SIZE) { "list size is ${list.size}, must be $UNIT_SIZE." }
        list.forEachIndexed { index, row ->
            require(row.size == UNIT_SIZE) { "list[$index] size is ${list[index].size}, must be $UNIT_SIZE." }
        }
    }

    operator fun get(rowIndex: Int, columnIndex: Int): T = list[rowIndex][columnIndex]
    fun getRow(rowIndex: Int): List<T> = list[rowIndex]
    fun getColumn(columnIndex: Int): List<T> = list.map { row -> row[columnIndex] }

    fun getBlock(blockIndex: BlockIndex): List<T> {
        return list.drop(blockIndex.blockRow * UNIT_SIZE_SQUARE_ROOT).take(UNIT_SIZE_SQUARE_ROOT).flatMap { row ->
            row.drop(blockIndex.blockColumn * UNIT_SIZE_SQUARE_ROOT).take(UNIT_SIZE_SQUARE_ROOT)
        }
    }

    override fun equals(other: Any?): Boolean = other is Board<*> && list == other.list
    override fun hashCode(): Int = list.hashCode()

    override fun toString(): String {
        fun joinRows(fromIndex: Int, toIndex: Int): String {
            return list.subList(fromIndex, toIndex).joinToString("\n") { row ->
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

    fun toMutableBoard(): MutableBoard<T> = MutableBoard(list.map { row -> row.toMutableList() })
}

class MutableBoard<T>(private val list: List<MutableList<T>>) : Board<T>(list) {
    operator fun set(rowIndex: Int, columnIndex: Int, element: T) {
        list[rowIndex][columnIndex] = element
    }

    fun <R> mapCellsToBoard(transform: (T) -> R): Board<R> = Board(list.map { row -> row.map(transform) })
}