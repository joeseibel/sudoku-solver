package sudokusolver.scala

val UnitSizeSquareRoot = 3
val UnitSize = UnitSizeSquareRoot * UnitSizeSquareRoot
val UnitSizeSquared = UnitSize * UnitSize

class Board[+T](elements: Iterable[Iterable[T]]):
  val rows: Vector[Vector[T]] = elements.map(_.toVector).toVector

  require(rows.size == UnitSize, s"elements size is ${rows.size}, must be $UnitSize.")
  for (row, index) <- rows.zipWithIndex do
    require(row.size == UnitSize, s"elements($index) size is ${row.size}, must be $UnitSize.")