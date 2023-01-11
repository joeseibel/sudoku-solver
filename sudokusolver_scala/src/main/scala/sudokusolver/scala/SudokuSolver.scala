package sudokusolver.scala

import sudokusolver.scala.logic.bruteForce

@main def sudokuSolver(board: String): Unit =
  if board.length != UnitSizeSquared || board.exists(!('0' to '9').contains(_)) then
    println(s"board must be $UnitSizeSquared numbers with blanks expressed as 0")
  else
    println(s"board is $board")
    val optionalBoard = parseOptionalBoard(board)
    println(s"parsed optional board is ${optionalBoard.rows}")
    val bruteForceSolution = bruteForce(optionalBoard)
    println(s"brute force solution is $bruteForceSolution")