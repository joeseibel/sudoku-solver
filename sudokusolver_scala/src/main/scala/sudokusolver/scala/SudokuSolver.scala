package sudokusolver.scala

import sudokusolver.scala.logic.BruteForceSolution.{MultipleSolutions, NoSolutions, SingleSolution}
import sudokusolver.scala.logic.bruteForce
import sudokusolver.scala.logic.simple.{nakedSingles, pruneCandidates}

import scala.annotation.tailrec
import scala.collection.immutable.{AbstractSeq, LinearSeq}

@main def sudokuSolver(board: String): Unit =
  if board.length != UnitSizeSquared || board.exists(!('0' to '9').contains(_)) then
    println(s"board must be $UnitSizeSquared numbers with blanks expressed as 0")
  else
    println(s"board is $board")
    val optionalBoard = parseOptionalBoard(board)
    println(s"parsed optional board is ${optionalBoard.rows}")
    val bruteForceSolution = bruteForce(optionalBoard)
    println(s"brute force solution is $bruteForceSolution")


sealed trait SolveResult

object InvalidNoSolutions extends SolveResult

object InvalidMultipleSolutions extends SolveResult

case class Solution(board: Board[SudokuNumber]) extends SolveResult

case class UnableToSolve(board: Board[Cell]) extends SolveResult:
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
 * pattern matching has be used where applicable.
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
        performNextSolution(board).toList match
          case Nil => UnableToSolve(board)
          case modifications =>

            @tailrec
            def modifyBoard(board: Board[Cell], modifications: List[BoardModification]): Board[Cell] =
              modifications match
                case Nil => board
                case modification :: tail => board(modification.row, modification.column) match
                  case SolvedCell(row, column, _) => throw IllegalStateException(s"[$row, $column] is already solved.")
                  case UnsolvedCell(row, column, existingCandidates) =>
                    val knownSolution = bruteForceSolution(row, column)
                    val newCell = modification match
                      case RemoveCandidates(_, _, candidatesToRemove) =>
                        for candidate <- candidatesToRemove do
                          if candidate == knownSolution then
                            throw IllegalStateException(s"Cannot remove candidate $candidate from [$row, $column")
                          if !existingCandidates.contains(candidate) then
                            throw IllegalStateException(s"$candidate is not a candidate of [$row, $column]")
                        UnsolvedCell(row, column, existingCandidates -- candidatesToRemove)
                      case SetValue(_, _, value) =>
                        if value != knownSolution then
                          val message = s"Cannot set value $value to [$row, $column]. Solution is $knownSolution"
                          throw IllegalStateException(message)
                        SolvedCell(row, column, value)
                    modifyBoard(board.updated(row, column, newCell), tail)

            solve(modifyBoard(board, modifications))

    solve(createCellBoard(input))

private def performNextSolution(board: Board[Cell]): Seq[BoardModification] =
//Start of simple solutions.
  pruneCandidates(board)
    .ifEmpty(nakedSingles(board))

extension[T] (seq: Seq[T])
  def ifEmpty(defaultValue: => Seq[T]): Seq[T] =
    if seq.isEmpty then defaultValue else seq