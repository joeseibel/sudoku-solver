package sudokusolver.scala

class Rectangle(val cells: Seq[UnsolvedCell]):
  val commonCandidates: Set[SudokuNumber] = cells.map(_.candidates).reduce(_ & _)
  lazy val floor: Seq[UnsolvedCell] = cells.filter(_.candidates.size == 2)
  lazy val roof: Seq[UnsolvedCell] = cells.filter(_.candidates.size > 2)

def createRectangles(board: Board[Cell]): Seq[Rectangle] =
  board.rows
    .zipEveryPair
    .flatMap { (rowA, rowB) =>
      rowA.zip(rowB)
        .collect { case (cellA: UnsolvedCell, cellB: UnsolvedCell) => cellA -> cellB }
        .zipEveryPair
        .map((columnA, columnB) => Rectangle(Seq(columnA(0), columnA(1), columnB(0), columnB(1))))
    }
    .filter(rectangle => rectangle.commonCandidates.size == 2 && rectangle.cells.map(_.block).toSet.size == 2)