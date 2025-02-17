package sudokusolver.scala

import sudokusolver.scala.logic.BruteForceSolution.{MultipleSolutions, NoSolutions, SingleSolution}
import sudokusolver.scala.logic.bruteForce
import sudokusolver.scala.logic.diabolical.*
import sudokusolver.scala.logic.extreme.*
import sudokusolver.scala.logic.simple.*
import sudokusolver.scala.logic.tough.*

import scala.annotation.tailrec

@main def sudokuSolver(board: String): Unit =
  if board.length != UnitSizeSquared || board.exists(!('0' to '9').contains(_)) then
    println(s"board must be $UnitSizeSquared numbers with blanks expressed as 0")
  else
    val message = solve(parseOptionalBoard(board)) match
      case InvalidNoSolutions => "No Solutions"
      case InvalidMultipleSolutions => "Multiple Solutions"
      case Solution(board) => board
      case unableToSolve: UnableToSolve => unableToSolve.message
    println(message)

sealed trait SolveResult

object InvalidNoSolutions extends SolveResult

object InvalidMultipleSolutions extends SolveResult

case class Solution(board: Board[SudokuNumber]) extends SolveResult

class UnableToSolve(board: Board[Cell]) extends SolveResult:
  lazy val message: String =
    s"""
       |Unable to solve:
       |$board
       |
       |Simple String: ${board.toSimpleString}
       |
       |With Candidates:
       |${board.toStringWithCandidates}
       |""".stripMargin

/*
 * The Scala version of solve was written to be purely functional. Unlike the Kotlin or Java implementations, it does
 * not contain any 'var' declarations or mutable collections. Loops have mostly been replaced with tail recursion and
 * pattern matching has been used where applicable.
 */
def solve(input: Board[Option[SudokuNumber]]): SolveResult = bruteForce(input) match
  case NoSolutions => InvalidNoSolutions
  case MultipleSolutions => InvalidMultipleSolutions
  case SingleSolution(bruteForceSolution) =>

    @tailrec
    def solve(board: Board[Cell]): SolveResult =
      if board.cells.collect { case cell: UnsolvedCell => cell }.isEmpty then
        Solution(bruteForceSolution)
      else
        performNextSolution(board) match
          case Seq() => UnableToSolve(board)
          case modifications =>
            val withModifications = modifications.foldLeft(board) { (board, modification) =>
              board(modification.row, modification.column) match
                case SolvedCell(row, column, _) => throw IllegalStateException(s"[$row, $column] is already solved.")
                case UnsolvedCell(row, column, existingCandidates) =>
                  val knownSolution = bruteForceSolution(row, column)
                  val newCell = modification match
                    case RemoveCandidates(_, _, candidatesToRemove) =>
                      for candidate <- candidatesToRemove do
                        if candidate == knownSolution then
                          throw IllegalStateException(s"Cannot remove candidate $candidate from [$row, $column]")
                        if !existingCandidates.contains(candidate) then
                          throw IllegalStateException(s"$candidate is not a candidate of [$row, $column]")
                      UnsolvedCell(row, column, existingCandidates -- candidatesToRemove)
                    case SetValue(_, _, value) =>
                      if value != knownSolution then
                        val message = s"Cannot set value $value to [$row, $column]. Solution is $knownSolution"
                        throw IllegalStateException(message)
                      SolvedCell(row, column, value)
                  board.updated(row, column, newCell)
            }
            solve(withModifications)

    solve(createCellBoard(input))

private def performNextSolution(board: Board[Cell]): Seq[BoardModification] =
  val solutions = LazyList(
    //Start of simple solutions.
    pruneCandidates,
    nakedSingles,
    hiddenSingles,
    nakedPairs,
    nakedTriples,
    hiddenPairs,
    hiddenTriples,
    nakedQuads,
    hiddenQuads,
    pointingPairsPointingTriples,
    boxLineReduction,
    //Start of tough solutions.
    xWing,
    simpleColoringRule2,
    simpleColoringRule4,
    yWing,
    swordfish,
    xyzWing,
    //Start of diabolical solutions.
    xCyclesRule1,
    xCyclesRule2,
    xCyclesRule3,
    bug(_).toSeq,
    xyChains,
    medusaRule1,
    medusaRule2,
    medusaRule3,
    medusaRule4,
    medusaRule5,
    medusaRule6,
    jellyfish,
    uniqueRectanglesType1,
    uniqueRectanglesType2,
    uniqueRectanglesType3,
    uniqueRectanglesType3BWithTriplePseudoCells,
    uniqueRectanglesType4,
    uniqueRectanglesType5,
    extendedUniqueRectangles,
    hiddenUniqueRectangles,
    wxyzWing,
    alignedPairExclusion,
    //Start of extreme solutions.
    groupedXCyclesRule1,
    groupedXCyclesRule2,
    groupedXCyclesRule3,
    emptyRectangles,
    finnedXWing,
    finnedSwordfish,
    alternatingInferenceChainsRule1,
    alternatingInferenceChainsRule2,
    alternatingInferenceChainsRule3,
    sueDeCoq
  )
  solutions.map(_(board)).find(_.nonEmpty).getOrElse(LazyList.empty)