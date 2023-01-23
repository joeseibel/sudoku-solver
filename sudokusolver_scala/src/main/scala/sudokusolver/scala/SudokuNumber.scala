package sudokusolver.scala

enum SudokuNumber:
  case ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE

  override def toString: String = s"${ordinal + 1}"

def sudokuNumber(ch: Char): SudokuNumber = ch match
  case '1' => SudokuNumber.ONE
  case '2' => SudokuNumber.TWO
  case '3' => SudokuNumber.THREE
  case '4' => SudokuNumber.FOUR
  case '5' => SudokuNumber.FIVE
  case '6' => SudokuNumber.SIX
  case '7' => SudokuNumber.SEVEN
  case '8' => SudokuNumber.EIGHT
  case '9' => SudokuNumber.NINE
  case _ => throw IllegalArgumentException(s"ch is '$ch', must be between '1' and '9'.")

def parseOptionalBoard(board: String): Board[Option[SudokuNumber]] =
  require(board.length == UnitSizeSquared, s"board length is ${board.length}, must be $UnitSizeSquared.")
  val boardAsNumbers = for row <- board.grouped(UnitSize) yield
    for cell <- row yield if cell == '0' then None else Some(sudokuNumber(cell))
  Board(boardAsNumbers.to(Iterable))

def parseBoard(board: String): Board[SudokuNumber] =
  require(board.length == UnitSizeSquared, s"board length is ${board.length}, must be $UnitSizeSquared.")
  Board(board.grouped(UnitSize).map(row => row.map(sudokuNumber)).to(Iterable))