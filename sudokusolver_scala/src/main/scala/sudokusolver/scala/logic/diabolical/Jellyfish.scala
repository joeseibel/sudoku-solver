package sudokusolver.scala.logic.diabolical

import sudokusolver.scala.*

/*
 * https://www.sudokuwiki.org/Jelly_Fish_Strategy
 *
 * For a quad of rows, if a candidate appears in two, three, or four cells for each row and the candidate appears in
 * exactly four columns across the four rows, forming a four by four grid, then the candidate must be placed in four of
 * the sixteen cells. The candidate can be removed from cells which are in the four columns, but different rows.
 *
 * For a quad of columns, if a candidate appears in two, three, or four cells for each column and the candidate appears
 * in exactly four rows across the four columns, forming a four by four grid, then the candidate must be placed in four
 * of the sixteen cells. The candidate can be removed from cells which are in the four rows, but different columns
 */
def jellyfish(board: Board[Cell]): Seq[RemoveCandidates] =
  SudokuNumber.values.toSeq.flatMap { candidate =>

    def jellyfish(units: Seq[Seq[Cell]], getOtherUnit: Int => Seq[Cell], getOtherUnitIndex: Cell => Int) =
      units.toIndexedSeq.zipEveryQuad.flatMap { (unitA, unitB, unitC, unitD) =>
        val aWithCandidate = unitA.collect { case cell: UnsolvedCell if cell.candidates.contains(candidate) => cell }
        val bWithCandidate = unitB.collect { case cell: UnsolvedCell if cell.candidates.contains(candidate) => cell }
        val cWithCandidate = unitC.collect { case cell: UnsolvedCell if cell.candidates.contains(candidate) => cell }
        val dWithCandidate = unitD.collect { case cell: UnsolvedCell if cell.candidates.contains(candidate) => cell }
        if (2 to 4).contains(aWithCandidate.size) &&
          (2 to 4).contains(bWithCandidate.size) &&
          (2 to 4).contains(cWithCandidate.size) &&
          (2 to 4).contains(dWithCandidate.size)
        then
          val withCandidate = aWithCandidate ++ bWithCandidate ++ cWithCandidate ++ dWithCandidate
          val otherUnitIndices = withCandidate.map(getOtherUnitIndex).toSet
          if otherUnitIndices.size == 4 then
            val removals = for
              cell <- otherUnitIndices.flatMap(getOtherUnit).collect { case cell: UnsolvedCell => cell }
              if cell.candidates.contains(candidate) && !withCandidate.contains(cell)
            yield cell -> candidate
            Some(removals)
          else
            None
        else
          None
      }.flatten

    val rowRemovals = jellyfish(board.rows, board.getColumn, _.column)
    val columnRemovals = jellyfish(board.columns, board.getRow, _.row)
    rowRemovals ++ columnRemovals
  }.mergeToRemoveCandidates