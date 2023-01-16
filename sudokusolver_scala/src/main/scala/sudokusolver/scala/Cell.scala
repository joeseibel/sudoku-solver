package sudokusolver.scala

sealed trait Cell:
  val row: Int
  val column: Int
  lazy val block: Int = getBlockIndex(row, column)

case class SolvedCell(override val row: Int, override val column: Int, value: SudokuNumber) extends Cell

class UnsolvedCell(
                    override val row: Int,
                    override val column: Int,
                    val candidates: Set[SudokuNumber] = SudokuNumber.values.toSet
                  ) extends Cell

def parseSimpleCells(simpleBoard: String): Board[Cell] =
  require(
    simpleBoard.length == UnitSizeSquared,
    s"simpleBoard.length is ${simpleBoard.length}, must be $UnitSizeSquared."
  )
  val cells = for (row, rowIndex) <- simpleBoard.grouped(UnitSize).zipWithIndex yield
    for (cell, columnIndex) <- row.zipWithIndex yield
      if cell == '0' then UnsolvedCell(rowIndex, columnIndex) else SolvedCell(rowIndex, columnIndex, sudokuNumber(cell))
  Board(cells.to(Iterable))