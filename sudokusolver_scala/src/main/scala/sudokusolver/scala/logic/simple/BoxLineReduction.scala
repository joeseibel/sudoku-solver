package sudokusolver.scala.logic.simple

import sudokusolver.scala.*

/*
 * http://www.sudokuwiki.org/Intersection_Removal#LBR
 *
 * For a given row, if a candidate appears in only one block, then the candidate for that block must be placed in that
 * row. The candidate can be removed from the cells which are in the same block, but different rows.
 *
 * For a given column, if a candidate appears in only one block, then the candidate for that block must be placed in
 * that column. The candidate can be removed from cells which are in the same block, but different columns.
 */
def boxLineReduction(board: Board[Cell]): Seq[RemoveCandidates] =
  SudokuNumber.values.toSeq.flatMap { candidate =>

    def boxLineReduction(units: Seq[Seq[Cell]], getUnitIndex: Cell => Int) =
      units.flatMap { unit =>
        val blockIndices = unit.collect { case cell: UnsolvedCell if cell.candidates.contains(candidate) => cell }
          .map(_.block)
          .distinct
        blockIndices match
          case Seq(blockIndex) =>
            val unitIndex = getUnitIndex(unit.head)
            for
              cell <- board.getBlock(blockIndex).collect { case cell: UnsolvedCell => cell }
              if getUnitIndex(cell) != unitIndex && cell.candidates.contains(candidate)
            yield cell -> candidate
          case _ => Nil
      }

    val rowRemovals = boxLineReduction(board.rows, _.row)
    val columnRemovals = boxLineReduction(board.columns, _.column)
    rowRemovals ++ columnRemovals
  }.mergeToRemoveCandidates