package sudokusolver.scala

sealed trait BoardModification extends Ordered[BoardModification]:
  val row: Int
  val column: Int

  override def compare(that: BoardModification): Int =
    val rowCompare = row.compareTo(that.row)
    if rowCompare != 0 then rowCompare else column.compareTo(that.column)

case class RemoveCandidates(override val row: Int, override val column: Int, candidates: Set[SudokuNumber])
  extends BoardModification

object RemoveCandidates:
  def apply(cell: UnsolvedCell, candidates: Set[SudokuNumber]): RemoveCandidates =
    RemoveCandidates(cell.row, cell.column, candidates)

  def apply(row: Int, column: Int, candidates: Int*): RemoveCandidates =
    RemoveCandidates(row, column, candidates.map(candidate => SudokuNumber.values(candidate - 1)).toSet)

case class SetValue(override val row: Int, override val column: Int, value: SudokuNumber) extends BoardModification

object SetValue:
  def apply(cell: UnsolvedCell, value: SudokuNumber): SetValue =
    require(cell.candidates.contains(value), s"$value is not a candidate for [${cell.row}, ${cell.column}].")
    SetValue(cell.row, cell.column, value)

  def apply(row: Int, column: Int, value: Int): SetValue = SetValue(row, column, SudokuNumber.values(value - 1))