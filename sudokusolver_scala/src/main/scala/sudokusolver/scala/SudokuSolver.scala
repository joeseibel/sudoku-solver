package sudokusolver.scala

@main def sudokuSolver(board: String): Unit =
  if board.length != UnitSizeSquared || board.exists(!('0' to '9').contains(_)) then
    println(s"board must be $UnitSizeSquared numbers with blanks expressed as 0")
  else
    println(s"board is $board")