package sudokusolver.scala.logic.tough

import sudokusolver.scala.*

/*
 * http://www.sudokuwiki.org/X_Wing_Strategy
 *
 * For a pair of rows, if a candidate appears in only two columns of both rows and the columns are the same, forming a
 * rectangle, then the candidate must be placed in opposite corners of the rectangle. The candidate can be removed from
 * cells which are in the two columns, but different rows.
 *
 * For a pair of columns, if a candidate appears in only two rows of both columns and the rows are the same, forming a
 * rectangle, then the candidate must be placed in opposite corners of the rectangle. The candidate can be removed from
 * cells which are in the two rows, but different columns.
 */
def xWing(board: Board[Cell]): Seq[RemoveCandidates] =
  SudokuNumber.values.toSeq.flatMap { candidate =>

    def xWing(units: Seq[Seq[Cell]], getOtherUnit: Int => Seq[Cell], getOtherUnitIndex: Cell => Int) =
      units.toIndexedSeq.zipEveryPair.flatMap { (unitA, unitB) =>
        unitA.collect { case cell: UnsolvedCell if cell.candidates.contains(candidate) => cell } match
          case Seq(firstA, lastA) =>
            unitB.collect { case cell: UnsolvedCell if cell.candidates.contains(candidate) => cell } match
              case Seq(firstB, lastB) if getOtherUnitIndex(firstA) == getOtherUnitIndex(firstB) &&
                getOtherUnitIndex(lastA) == getOtherUnitIndex(lastB) =>
                val otherUnitA = getOtherUnit(getOtherUnitIndex(firstA))
                val otherUnitB = getOtherUnit(getOtherUnitIndex(lastA))
                val removals = for
                  cell <- (otherUnitA ++ otherUnitB).collect { case cell: UnsolvedCell => cell }
                  if cell.candidates.contains(candidate) && !unitA.contains(cell) && !unitB.contains(cell)
                yield cell -> candidate
                Some(removals)
              case _ => None
          case _ => None
      }.flatten

    val rowRemovals = xWing(board.rows, board.getColumn, _.column)
    val columnRemovals = xWing(board.columns, board.getRow, _.row)
    rowRemovals ++ columnRemovals
  }.mergeToRemoveCandidates