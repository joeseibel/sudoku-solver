package sudokusolver.scala.logic.extreme

import sudokusolver.scala.*

/*
 * https://www.sudokuwiki.org/Finned_Swordfish
 *
 * Finned Swordfish is an extension of Swordfish in a similar manner to the way that Finned X-Wing is an extension of
 * X-Wing. As a reminder, Swordfish looks for a 3x3 grid of cells in which a particular candidate appears in most or all
 * of those cells. If the candidate appears two or three times in each row of the grid and for those rows, the candidate
 * appears in exactly three columns, then the candidate can be removed from the columns of the grid, but in different
 * rows. If the candidate appears two or three times in each column of the grid and for those columns, the candidate
 * appears in exactly three rows, then the candidate can be removed from the rows of the grid, but in different columns.
 *
 * In Finned Swordfish, eight of the cells of a 3x3 grid will follow the same rules as Swordfish. Only one cell will
 * have additional unsolved cells with the candidate next to it. The fin must be in the same block as the cell, but the
 * cell itself may or may not have the candidate.
 *
 * For a triple of rows, two rows are the base rows if the candidate appears two or three times in each row and the
 * candidate appears in exactly three columns of the two rows. The remaining row is a finned row if the candidate
 * appears once or twice outside the three columns, but in the same block as one of the cells of the grid. That cell is
 * the finned cell. The candidate can be removed from cells that are in the same column as the finned cell, but are
 * outside the grid.
 *
 * For a triple of columns, two columns are the base columns if the candidate appears two or three times in each column
 * and the candidate appears in exactly three rows of the two columns. The remaining column is a finned column if the
 * candidate appears once or twice outside the three rows, but in the same block as one of the cells of the grid. That
 * cell is the finned cell. The candidate can be removed from cells that are in the same row as the finned cell, but are
 * outside the grid.
 */
def finnedSwordfish(board: Board[Cell]): Seq[RemoveCandidates] =
  SudokuNumber.values.toSeq.flatMap { candidate =>

    def finnedSwordfish(
                         units: Seq[Seq[Cell]],
                         getUnitIndex: Cell => Int,
                         getOtherUnitIndex: Cell => Int,
                         getOtherUnit: Int => Seq[Cell],
                         getFinnedCell: (finnedUnitIndex: Int, otherUnitIndex: Int) => Cell
                       ) =
      val unitsWithCandidate = units.toIndexedSeq
        .map(unit => unit.collect { case cell: UnsolvedCell if cell.candidates.contains(candidate) => cell })
        .filter(_.nonEmpty)
      unitsWithCandidate.filter(unit => (2 to 3).contains(unit.size)).zipEveryPair.flatMap { (baseUnitA, baseUnitB) =>
        (baseUnitA ++ baseUnitB).map(getOtherUnitIndex).distinct match
          case otherUnitIndices@Seq(_, _, _) => unitsWithCandidate.flatMap { finnedUnit =>
            val finnedUnitIndex = getUnitIndex(finnedUnit.head)
            val unitIndices = Set(finnedUnitIndex, getUnitIndex(baseUnitA.head), getUnitIndex(baseUnitB.head))
            if unitIndices.size == 3 then
              val outsideOtherUnitIndices = finnedUnit
                .filter(cell => !otherUnitIndices.contains(getOtherUnitIndex(cell)))
              outsideOtherUnitIndices match
                case Seq(_) | Seq(_, _) => outsideOtherUnitIndices.map(_.block).distinct match
                  case Seq(blockIndex) =>
                    otherUnitIndices.map(getFinnedCell(finnedUnitIndex, _)).filter(_.block == blockIndex) match
                      case Seq(finnedCell) =>
                        for
                          cell <- getOtherUnit(getOtherUnitIndex(finnedCell))
                            .collect { case cell: UnsolvedCell => cell }
                          if cell.candidates.contains(candidate) &&
                            cell.block == blockIndex &&
                            !unitIndices.contains(getUnitIndex(cell))
                        yield cell -> candidate
                      case _ => Nil
                  case _ => Nil
                case _ => Nil
            else
              Nil
          }
          case _ => Nil
      }

    val rowRemovals = finnedSwordfish(board.rows, _.row, _.column, board.getColumn, board.apply)

    val columnRemovals = finnedSwordfish(
      board.columns,
      _.column,
      _.row,
      board.getRow,
      (finnedUnitIndex, otherUnitIndex) => board(otherUnitIndex, finnedUnitIndex)
    )

    rowRemovals ++ columnRemovals
  }.mergeToRemoveCandidates