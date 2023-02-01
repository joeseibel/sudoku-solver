package sudokusolver.scala

import scala.annotation.tailrec

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

/*
 * The Scala version of parseCellsWithCandidates was written to be purely functional. Unlike the Kotlin or Java
 * implementations, it does not contain any 'var' declarations or mutable collections. While loops have been replaced
 * with tail recursion. This function also makes extensive use of Scala's pattern matching. This is why the parameter
 * withCandidates is converted from a String to a List[Char]. By converting it to a List, pattern matching can be
 * applied using the "head :: tail" construct.
 */
def parseCellsWithCandidates(withCandidates: String): Board[Cell] =
  type CellBuilder = (Int, Int) => Cell

  @tailrec
  def getCellBuilders(acc: List[CellBuilder], withCandidates: List[Char]): List[CellBuilder] = withCandidates match
    case '{' :: tail =>

      @tailrec
      def collectCandidates(acc: List[Char], withCandidates: List[Char]): (Set[SudokuNumber], List[Char]) =
        withCandidates match
          case '{' :: _ => throw IllegalArgumentException("Nested '{'.")
          case '}' :: _ if acc.isEmpty => throw IllegalArgumentException("Empty \"{}\".")
          case '}' :: tail => (acc.map(sudokuNumber).toSet, tail)
          case ch :: tail if '1' to '9' contains ch => collectCandidates(ch :: acc, tail)
          case ch :: _ => throw IllegalArgumentException(s"Invalid character: '$ch'.")
          case Nil => throw IllegalArgumentException("Unmatched '{'.")

      val (candidates, next) = collectCandidates(Nil, tail)
      val builder = (row, column) => UnsolvedCell(row, column, candidates)
      getCellBuilders(builder :: acc, next)
    case '}' :: _ => throw IllegalArgumentException("Unmatched '}'.")
    case ch :: tail if '1' to '9' contains ch =>
      val builder = (row, column) => SolvedCell(row, column, sudokuNumber(ch))
      getCellBuilders(builder :: acc, tail)
    case ch :: _ => throw IllegalArgumentException(s"Invalid character: '$ch'.")
    case Nil => acc.reverse

  val cellBuilders = getCellBuilders(Nil, withCandidates.toList)
  require(cellBuilders.size == UnitSizeSquared, s"Found ${cellBuilders.size} cells, required $UnitSizeSquared.")
  val cells = for (row, rowIndex) <- cellBuilders.grouped(UnitSize).zipWithIndex yield
    for (cell, columnIndex) <- row.zipWithIndex yield cell(rowIndex, columnIndex)
  Board(cells.to(Iterable))