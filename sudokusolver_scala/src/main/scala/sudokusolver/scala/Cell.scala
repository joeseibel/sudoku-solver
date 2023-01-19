package sudokusolver.scala

sealed trait Cell:
  val row: Int
  val column: Int
  lazy val block: Int = getBlockIndex(row, column)

  require((0 until UnitSize).contains(row), s"row is $row, must be between 0 and ${UnitSize - 1}.")
  require((0 until UnitSize).contains(column), s"column is $column, must be between 0 and ${UnitSize - 1}.")

case class SolvedCell(override val row: Int, override val column: Int, value: SudokuNumber) extends Cell

class UnsolvedCell(
                    override val row: Int,
                    override val column: Int,
                    val candidates: Set[SudokuNumber] = SudokuNumber.values.toSet
                  ) extends Cell:
  require(candidates.nonEmpty, "candidates must not be empty.")

def parseSimpleCells(simpleBoard: String): Board[Cell] =
  require(
    simpleBoard.length == UnitSizeSquared,
    s"simpleBoard.length is ${simpleBoard.length}, must be $UnitSizeSquared."
  )
  val cells = for (row, rowIndex) <- simpleBoard.grouped(UnitSize).zipWithIndex yield
    for (cell, columnIndex) <- row.zipWithIndex yield
      if cell == '0' then UnsolvedCell(rowIndex, columnIndex) else SolvedCell(rowIndex, columnIndex, sudokuNumber(cell))
  Board(cells.to(Iterable))

def parseCellsWithCandidates(withCandidates: String): Board[Cell] =
  var cellBuilders: List[(Int, Int) => Cell] = Nil
  var index = 0
  while index < withCandidates.length do
    val ch = withCandidates(index)
    if '1' to '9' contains ch then
      cellBuilders = cellBuilders :+ ((row, column) => SolvedCell(row, column, sudokuNumber(ch)))
      index = index + 1
    else if ch == '{' then
      index = index + 1
      val closingBrace = withCandidates.indexOf('}', index)
      require(closingBrace != -1, "Unmatched '{'.")
      require(closingBrace != index, "Empty \"{}\".")
      val charsInBrace = (index until closingBrace).map(withCandidates(_))
      require(!charsInBrace.contains('{'), "Nested '{'.")
      for charInBrace <- charsInBrace do require('1' to '9' contains charInBrace, s"Invalid character: '$charInBrace'.")
      val candidates = charsInBrace.map(sudokuNumber).toSet
      cellBuilders = cellBuilders :+ ((row, column) => UnsolvedCell(row, column, candidates))
      index = closingBrace + 1
    else if ch == '}' then
      throw IllegalArgumentException("Unmatched '}'.")
    else
      throw IllegalArgumentException(s"Invalid character: '$ch'.")
  require(cellBuilders.size == UnitSizeSquared, s"Found ${cellBuilders.size} cells, required $UnitSizeSquared.")
  val cells = cellBuilders.grouped(UnitSize).zipWithIndex.map { (row, rowIndex) =>
    row.zipWithIndex.map { (cell, columnIndex) => cell(rowIndex, columnIndex) }
  }
  Board(cells.to(Iterable))