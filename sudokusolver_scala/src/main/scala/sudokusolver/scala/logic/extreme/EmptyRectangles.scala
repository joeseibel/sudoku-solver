package sudokusolver.scala.logic.extreme

import sudokusolver.scala.*

/*
 * https://www.sudokuwiki.org/Empty_Rectangles
 *
 * This solution starts with looking for empty rectangles in blocks. An empty rectangle is a collection of four cells,
 * all contained within a single block, arranged in a rectangle, and none of them contain a particular candidate. The
 * cells can either be solved cells or unsolved cells without the candidate. For the other cells which are in the block,
 * but are outside the rectangle, at least two of them must contain the candidate and those cells must be in at least
 * two different rows and two different columns.
 *
 * This creates a situation in which two lines can be drawn through the block; one line along a row and the other along
 * a column. The two lines must not pass through any of the empty rectangle cells and all the cells with the candidate
 * must have a line pass through it. A valid block is one in which there is only one option for the placement of these
 * lines. This is why the cells with the candidate must be in at least two different rows and two different columns. The
 * cell in which these lines intersect is then used to find removals outside the block. The empty rectangle itself is
 * used to find a valid intersection point, but then the rectangle is disregarded for the remainder of the solution.
 *
 * Removals are looked for in cells which are outside the block, but which can see the intersection. If the intersection
 * can see one end of a strong link which is outside the intersection's block and there is another cell with the
 * candidate outside the intersection's block, but it can see the intersection and the other end of the strong link,
 * then there is a contradiction. If the candidate were to be set as the solution to the other cell, then the strong
 * link and this newly set solution would remove the candidate from every cell within the intersection's block, thus
 * invalidating that block. This means that the candidate cannot be the solution to that cell and can be removed.
 */
def emptyRectangles(board: Board[Cell]): Seq[RemoveCandidates] =
  SudokuNumber.values.toSeq.flatMap { candidate =>
    getIntersections(board, candidate).flatMap { (row, column) =>
      val block = board(row, column).block

      def getRemovals(
                       unit: Seq[Cell],
                       getOtherUnitIndex: Cell => Int,
                       getOtherUnit: Int => Seq[Cell],
                       getRemovalRow: Cell => Int,
                       getRemovalColumn: Cell => Int
                     ) =
        unit.filter(cell => cell.block != block && cell.hasCandidate(candidate)).flatMap { strongLink1 =>
          val otherUnit = getOtherUnit(getOtherUnitIndex(strongLink1))
            .filter(cell => cell.hasCandidate(candidate) && cell != strongLink1)
          otherUnit match
            case Seq(strongLink2) if strongLink1.block != strongLink2.block =>
              board(getRemovalRow(strongLink2), getRemovalColumn(strongLink2)) match
                case removalCell@UnsolvedCell(_, _, candidates) if candidates.contains(candidate) =>
                  Some(removalCell -> candidate)
                case _ => None
            case _ => None
        }

      val rowRemovals = getRemovals(board.getRow(row), _.column, board.getColumn, _.row, _ => column)
      val columnRemovals = getRemovals(board.getColumn(column), _.row, board.getRow, _ => row, _.column)
      rowRemovals ++ columnRemovals
    }
  }.mergeToRemoveCandidates

private def getIntersections(board: Board[Cell], candidate: SudokuNumber): Seq[(Int, Int)] =
  for
    row <- 0 until UnitSize
    rowInBlock = row % UnitSizeSquareRoot
    rectangleRow1 = if rowInBlock == 0 then row + 1 else row - rowInBlock
    rectangleRow2 = if rowInBlock == 2 then row - 1 else row - rowInBlock + 2
    column <- 0 until UnitSize
    columnInBlock = column % UnitSizeSquareRoot
    rectangleColumn1 = if columnInBlock == 0 then column + 1 else column - columnInBlock
    rectangleColumn2 = if columnInBlock == 2 then column - 1 else column - columnInBlock + 2
    // Check that the rectangle is empty.
    if !board(rectangleRow1, rectangleColumn1).hasCandidate(candidate) &&
      !board(rectangleRow1, rectangleColumn2).hasCandidate(candidate) &&
      !board(rectangleRow2, rectangleColumn1).hasCandidate(candidate) &&
      !board(rectangleRow2, rectangleColumn2).hasCandidate(candidate) &&
      // Check that at least one cell in the same block and row as the intersection has the candidate.
      (board(row, rectangleColumn1).hasCandidate(candidate) || board(row, rectangleColumn2).hasCandidate(candidate)) &&
      // Check that at least one cell in the same block and column as the intersection has the candidate.
      (board(rectangleRow1, column).hasCandidate(candidate) || board(rectangleRow2, column).hasCandidate(candidate))
  yield row -> column

extension (cell: Cell)
  private def hasCandidate(candidate: SudokuNumber): Boolean =
    cell match
      case SolvedCell(_, _, _) => false
      case UnsolvedCell(_, _, candidates) => candidates.contains(candidate)