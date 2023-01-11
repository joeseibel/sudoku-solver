package sudokusolver.scala

val UnitSizeSquareRoot = 3
val UnitSize = UnitSizeSquareRoot * UnitSizeSquareRoot
val UnitSizeSquared = UnitSize * UnitSize

/*
 * Differences between the Scala implementation of Board and other implementations:
 *
 * I decided that I wanted the Scala implementation to be purely function. This means that there should never be a 'var'
 * declaration and there should never be a mutable collection. This approach distinguishes the Scala implementation from
 * the other implementations such as Kotlin. This design decision impacts Board in two ways.
 *
 * First of all, the board is stored internally as a Vector[Vector[T]] as opposed to using a mutable collection. Vector
 * was chosen over List because Vector is indexed and would thus make index based access more efficient.
 *
 * Secondly, the Scala version of Board has an updated method which returns a new Board with the specified cell updated
 * to the supplied value. This is opposed to other implementations which have a set method and mutate the board itself.
 */
class Board[+T](elements: Iterable[Iterable[T]]):
  val rows: Vector[Vector[T]] = elements.map(_.toVector).toVector

  require(rows.size == UnitSize, s"elements size is ${rows.size}, must be $UnitSize.")
  for (row, index) <- rows.zipWithIndex do
    require(row.size == UnitSize, s"elements($index) size is ${row.size}, must be $UnitSize.")

  lazy val columns: IndexedSeq[Vector[T]] = (0 until UnitSize).map(index => rows.map(row => row(index)))
  lazy val blocks: IndexedSeq[Vector[T]] = (0 until UnitSize).map(getBlock)
  lazy val cells: Vector[T] = rows.flatten

  def apply(rowIndex: Int, columnIndex: Int): T = rows(rowIndex)(columnIndex)

  def updated[U >: T](rowIndex: Int, columnIndex: Int, element: U): Board[U] =
    Board(rows.updated(rowIndex, rows(rowIndex).updated(columnIndex, element)))

  def getRow(rowIndex: Int): Vector[T] = rows(rowIndex)

  def getColumn(columnIndex: Int): Vector[T] = rows.map(row => row(columnIndex))

  def getBlock(blockIndex: Int): Vector[T] =
    require(0 until UnitSize contains blockIndex, s"blockIndex is $blockIndex, must be between 0 and ${UnitSize - 1}.")
    val rowIndex = blockIndex / UnitSizeSquareRoot * UnitSizeSquareRoot
    val columnIndex = blockIndex % UnitSizeSquareRoot * UnitSizeSquareRoot
    rows.slice(rowIndex, rowIndex + UnitSizeSquareRoot).flatMap { row =>
      row.slice(columnIndex, columnIndex + UnitSizeSquareRoot)
    }

  override def equals(obj: Any): Boolean = obj match
    case otherBoard: Board[?] => rows == otherBoard.rows
    case _ => false

  override def hashCode(): Int = rows.hashCode()

  def mapCells[R](transform: T => R): Board[R] = Board(rows.map(row => row.map(transform)))

def getBlockIndex(rowIndex: Int, columnIndex: Int): Int =
  rowIndex / UnitSizeSquareRoot * UnitSizeSquareRoot + columnIndex / UnitSizeSquareRoot