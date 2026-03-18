package sudokusolver.scala.logic.tough

import sudokusolver.scala.*

/*
 * http://www.sudokuwiki.org/Sword_Fish_Strategy
 *
 * For a triple of rows, if a candidate appears in two or three cells for each row and the candidate appears in exactly
 * three columns across the three rows, forming a three by three grid, then the candidate must be placed in three of the
 * nine cells. The candidate can be removed from cells which are in the three columns, but different rows.
 *
 * For a triple of columns, if a candidate appears in two or three cells for each column and the candidate appears in
 * exactly three rows across the three columns, forming a three by three grid, then the candidate must be placed in
 * three of the nine cells. The candidate can be removed from cells which are in the three rows, but different columns.
 */
def swordfish(board: Board[Cell]): Seq[RemoveCandidates] =
  SudokuNumber.values.toSeq.flatMap { candidate =>

    def swordfish(units: Seq[Seq[Cell]], getOtherUnit: Int => Seq[Cell], getOtherUnitIndex: Cell => Int) =
      units.toIndexedSeq.zipEveryTriple.flatMap { (unitA, unitB, unitC) =>
        val aWithCandidate = unitA.collect { case cell: UnsolvedCell if cell.candidates.contains(candidate) => cell }
        val bWithCandidate = unitB.collect { case cell: UnsolvedCell if cell.candidates.contains(candidate) => cell }
        val cWithCandidate = unitC.collect { case cell: UnsolvedCell if cell.candidates.contains(candidate) => cell }
        if (2 to 3).contains(aWithCandidate.size) &&
          (2 to 3).contains(bWithCandidate.size) &&
          (2 to 3).contains(cWithCandidate.size) then
          val withCandidate = aWithCandidate ++ bWithCandidate ++ cWithCandidate
          val otherUnitIndices = withCandidate.map(getOtherUnitIndex(_)).toSet
          if otherUnitIndices.size == 3 then
            for
              cell <- otherUnitIndices.flatMap(getOtherUnit(_)).collect { case cell: UnsolvedCell => cell }
              if cell.candidates.contains(candidate) && !withCandidate.contains(cell)
            yield cell -> candidate
          else
            Nil
        else
          Nil
      }

    val rowRemovals = swordfish(board.rows, board.getColumn, _.column)
    val columnRemovals = swordfish(board.columns, board.getRow, _.row)
    rowRemovals ++ columnRemovals
  }.mergeToRemoveCandidates