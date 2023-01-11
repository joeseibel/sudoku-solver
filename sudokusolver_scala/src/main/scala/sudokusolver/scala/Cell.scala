package sudokusolver.scala

sealed trait Cell

class SolvedCell(rowIndex: Int, columnIndex: Int, value: SudokuNumber) extends Cell

class UnsolvedCell(row: Int, column: Int) extends Cell

def parseSimpleCells(simpleBoard: String): Board[Cell] =
  require(
    simpleBoard.length == UnitSizeSquared,
    s"simpleBoard.length is ${simpleBoard.length}, must be $UnitSizeSquared."
  )
  val cells = for (row, rowIndex) <- simpleBoard.grouped(UnitSize).zipWithIndex yield
    for (cell, columnIndex) <- row.zipWithIndex yield
      if cell == '0' then UnsolvedCell(rowIndex, columnIndex) else SolvedCell(rowIndex, columnIndex, sudokuNumber(cell))
  Board(cells.to(Iterable))