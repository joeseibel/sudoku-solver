package sudokusolver.scala.logic.diabolical

import sudokusolver.scala.*

/*
 * https://www.sudokuwiki.org/Hidden_Unique_Rectangles
 *
 * The basic premise of the Hidden Unique Rectangles solution is exactly the same as Unique Rectangles. The only
 * difference is that Hidden Unique Rectangles adds more specific types to the solution. These additional types look for
 * strong links between cells of a rectangle. A strong link exists between two cells for a given candidate when those
 * two cells are the only cells with the candidate in a given row or column.
 */
def hiddenUniqueRectangles(board: Board[Cell]): Seq[RemoveCandidates] =
  createRectangles(board).flatMap { rectangle =>
    val (floor, roof) = rectangle.cells.partition(_.candidates.size == 2)
    floor match
      case Seq(floor) => type1(board, rectangle, floor)
      case _ => roof match
        case Seq(roofA, roofB) => type2(board, roofA, roofB, rectangle.commonCandidates)
        case _ => None
  }.mergeToRemoveCandidates

/*
 * Type 1
 *
 * If a rectangle has one floor cell, then consider the roof cell on the opposite corner of the rectangle. If one of the
 * common candidates appears twice in that cell's row and twice in that cell's column, which implies that the other
 * occurrences in that row and column are in the two other corners of the rectangle, then setting the other common
 * candidate as the value to that cell would lead to the Deadly Pattern. Therefore, the other common candidate cannot be
 * the solution to that cell. The other common candidate can be removed from the roof cell which is opposite of the one
 * floor cell.
 */
private def type1(board: Board[Cell], rectangle: Rectangle, floor: UnsolvedCell): Option[LocatedCandidate] =
  val row = board.getRow(floor.row).collect { case cell: UnsolvedCell => cell }
  val column = board.getColumn(floor.column).collect { case cell: UnsolvedCell => cell }
  val strongCandidates = rectangle.commonCandidates.filter { candidate =>
    row.count(_.candidates.contains(candidate)) == 2 && column.count(_.candidates.contains(candidate)) == 2
  }
  strongCandidates.toSeq match
    case Seq(strongCandidate) =>
      val oppositeCell = rectangle.cells.filter(cell => cell.row != floor.row && cell.column != floor.column).head
      val otherCandidate = rectangle.commonCandidates.filter(_ != strongCandidate).head
      Some(oppositeCell -> otherCandidate)
    case _ => None

/*
 * Type 2
 *
 * If a rectangle has two roof cells, those cells are in the same row, and there exists a strong link for one of the
 * common candidates between one of the roof cells and its corresponding floor cell in the same column, then setting the
 * other common candidate as the value to the other roof cell would lead to the Deadly Pattern. Therefore, the other
 * common candidate cannot be the solution to the other roof cell. The other common candidate can be removed from the
 * other roof cell.
 *
 * If a rectangle has two roof cells, those cells are in the same column, and there exists a strong link for one of the
 * common candidates between one of the roof cells and its corresponding floor cell in the same row, then setting the
 * other common candidate as the value to the other roof cell would lead to the Deadly Pattern. Therefore, the other
 * common candidate cannot be the solution to the other roof cell. The other common candidate can be removed from the
 * other roof cell.
 */
private def type2(
                   board: Board[Cell],
                   roofA: UnsolvedCell,
                   roofB: UnsolvedCell,
                   commonCandidates: Set[SudokuNumber]
                 ): Option[LocatedCandidate] =
  val Seq(candidateA, candidateB) = commonCandidates.toSeq

  def getRemoval(getUnitIndex: Cell => Int, getUnit: Int => Seq[Cell]): Option[LocatedCandidate] =
    val unitA = getUnit(getUnitIndex(roofA)).collect { case cell: UnsolvedCell => cell }
    val unitB = getUnit(getUnitIndex(roofB)).collect { case cell: UnsolvedCell => cell }
    if unitA.count(_.candidates.contains(candidateA)) == 2 then
      Some(roofB -> candidateB)
    else if unitA.count(_.candidates.contains(candidateB)) == 2 then
      Some(roofB -> candidateA)
    else if unitB.count(_.candidates.contains(candidateA)) == 2 then
      Some(roofA -> candidateB)
    else if unitB.count(_.candidates.contains(candidateB)) == 2 then
      Some(roofA -> candidateA)
    else
      None

  if roofA.row == roofB.row then
    getRemoval(_.column, board.getColumn)
  else if roofA.column == roofB.column then
    getRemoval(_.row, board.getRow)
  else
    None