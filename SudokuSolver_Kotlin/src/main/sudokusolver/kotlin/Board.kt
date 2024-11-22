package sudokusolver.kotlin

const val UNIT_SIZE_SQUARE_ROOT = 3
const val UNIT_SIZE = UNIT_SIZE_SQUARE_ROOT * UNIT_SIZE_SQUARE_ROOT
const val UNIT_SIZE_SQUARED = UNIT_SIZE * UNIT_SIZE

abstract class AbstractBoard<out T> {
    abstract val rows: List<List<T>>
    abstract val columns: List<List<T>>
    abstract val blocks: List<List<T>>
    abstract val units: List<List<T>>
    abstract val cells: List<T>

    operator fun get(rowIndex: Int, columnIndex: Int): T = rows[rowIndex][columnIndex]

    fun getRow(rowIndex: Int): List<T> = rows[rowIndex]
    fun getColumn(columnIndex: Int): List<T> = rows.map { row -> row[columnIndex] }

    fun getBlock(blockIndex: Int): List<T> {
        require(blockIndex in 0..<UNIT_SIZE) {
            "blockIndex is $blockIndex, must be between 0 and ${UNIT_SIZE - 1}."
        }
        val rowIndex = blockIndex / UNIT_SIZE_SQUARE_ROOT * UNIT_SIZE_SQUARE_ROOT
        val columnIndex = blockIndex % UNIT_SIZE_SQUARE_ROOT * UNIT_SIZE_SQUARE_ROOT
        return rows.drop(rowIndex).take(UNIT_SIZE_SQUARE_ROOT).flatMap { row ->
            row.drop(columnIndex).take(UNIT_SIZE_SQUARE_ROOT)
        }
    }

    override fun equals(other: Any?): Boolean = other is AbstractBoard<*> && rows == other.rows
    override fun hashCode(): Int = rows.hashCode()

    override fun toString(): String {
        fun joinRows(fromIndex: Int, toIndex: Int) =
            rows.subList(fromIndex, toIndex).joinToString("\n") { row ->
                fun joinCells(fromIndex: Int, toIndex: Int) = row.subList(fromIndex, toIndex).joinToString(" ")

                val first = joinCells(0, UNIT_SIZE_SQUARE_ROOT)
                val second = joinCells(UNIT_SIZE_SQUARE_ROOT, UNIT_SIZE_SQUARE_ROOT * 2)
                val third = joinCells(UNIT_SIZE_SQUARE_ROOT * 2, UNIT_SIZE)
                "$first | $second | $third"
            }

        return """
            |${joinRows(0, UNIT_SIZE_SQUARE_ROOT)}
            |------+-------+------
            |${joinRows(UNIT_SIZE_SQUARE_ROOT, UNIT_SIZE_SQUARE_ROOT * 2)}
            |------+-------+------
            |${joinRows(UNIT_SIZE_SQUARE_ROOT * 2, UNIT_SIZE)}
        """.trimMargin()
    }

    protected companion object {
        fun requireSize(elements: List<List<*>>) {
            require(elements.size == UNIT_SIZE) { "elements.size is ${elements.size}, must be $UNIT_SIZE." }
            elements.forEachIndexed { index, row ->
                require(row.size == UNIT_SIZE) { "elements[$index].size is ${row.size}, must be $UNIT_SIZE." }
            }
        }
    }
}

class Board<out T>(elements: Iterable<Iterable<T>>) : AbstractBoard<T>() {
    override val rows: List<List<T>> = elements.map { it.toList() }

    init {
        requireSize(rows)
    }

    override val columns: List<List<T>> by lazy {
        (0..<UNIT_SIZE).map { index -> rows.map { row -> row[index] } }
    }

    override val blocks: List<List<T>> by lazy { (0..<UNIT_SIZE).map(::getBlock) }
    override val units: List<List<T>> by lazy { rows + columns + blocks }
    override val cells: List<T> by lazy { rows.flatten() }

    inline fun <R> mapCells(transform: (T) -> R): Board<R> = Board(rows.map { row -> row.map(transform) })

    inline fun <R> mapCellsToMutableBoardIndexed(transform: (row: Int, column: Int, T) -> R): MutableBoard<R> =
        MutableBoard(rows.mapIndexed { rowIndex, row ->
            row.mapIndexed { columnIndex, cell -> transform(rowIndex, columnIndex, cell) }
        })
}

class MutableBoard<T>(elements: Iterable<Iterable<T>>) : AbstractBoard<T>() {
    /*
     * Motivation for storing the board internally as a List<MutableList<T>>:
     *
     * I considered having the second dimension be an Array instead of a MutableList. Ideally, the second dimension
     * would be a type that permits mutating values, but not the size. In other words, it should have the method set,
     * but not have the methods add, remove, addAll, removeAll, retainAll, clear, or removeAt. Array perfectly fits that
     * criteria, so why don't I use it?
     *
     * There are two issues that caused me to choose MutableList over Array: generics and conversions.
     *
     * Arrays are not type-erased generics and the specific member type is a part of the Array's type at runtime.
     * Specifying a concrete type is required when creating an Array. However, MutableBoard is generic with its type
     * erased, so T cannot be used to create an Array. This is why the normal Kotlin Array creation methods such as
     * arrayOf and toTypedArray won't work here. What would work is to call java.lang.reflect.Array.newInstance and pass
     * in the java.lang.Class for the member type. However, that would require adding a Class parameter to
     * MutableBoard's constructor. I really don't like that idea. It would expose this issue to the use site and the
     * details of using an Array or MutableList should be internal to this class.
     *
     * One possible workaround is for the second dimension to be an Array of Any. Retrieving elements would then require
     * a cast to T. This defeats the purpose of generics within MutableBoard as a cast to T is unchecked. I would then
     * have to manually ensure that I am being type safe within MutableBoard. Given the small size of MutableBoard, this
     * is not too great of a burden. However, it is a larger burden than manually ensuring that I don't call any size
     * changing methods within this class.
     *
     * The other reason I chose MutableList has to do with the fact that the rows property has the type List<List<T>>.
     * If I stored the second dimension as an Array, then every access of rows would involve 9 conversions from Array to
     * List. This seems silly when I can simply return _rows since MutableList extends List.
     *
     * I could have the type of rows be Iterable<Iterable<T>> instead of List<List<T>>. In that case, I could return
     * a List<Array<T>> here and a List<List<T>> in Board since Array and List both extend Iterable. However, I
     * specifically want to return List and allow access by index. If Array implemented List, then conversion would not
     * be an issue.
     */
    private val _rows: List<MutableList<T>> = elements.map { it.toMutableList() }

    init {
        requireSize(_rows)
    }

    override val rows: List<List<T>>
        get() = _rows

    override val columns: List<List<T>>
        get() = (0..<UNIT_SIZE).map { index -> rows.map { row -> row[index] } }

    override val blocks: List<List<T>>
        get() = (0..<UNIT_SIZE).map(::getBlock)

    override val units: List<List<T>>
        get() = rows + columns + blocks

    override val cells: List<T>
        get() = rows.flatten()

    operator fun set(rowIndex: Int, columnIndex: Int, element: T) {
        _rows[rowIndex][columnIndex] = element
    }

    inline fun <R> mapCellsToBoard(transform: (T) -> R): Board<R> = Board(rows.map { row -> row.map(transform) })

    fun toBoard(): Board<T> = Board(rows)
}

fun getBlockIndex(rowIndex: Int, columnIndex: Int): Int =
    rowIndex / UNIT_SIZE_SQUARE_ROOT * UNIT_SIZE_SQUARE_ROOT + columnIndex / UNIT_SIZE_SQUARE_ROOT

fun <T> Board<T>.toMutableBoard(): MutableBoard<T> = MutableBoard(rows)

fun validateRowAndColumn(row: Int, column: Int) {
    require(row in 0..<UNIT_SIZE) { "row is $row, must be between 0 and ${UNIT_SIZE - 1}." }
    require(column in 0..<UNIT_SIZE) { "column is $column, must be between 0 and ${UNIT_SIZE - 1}." }
}