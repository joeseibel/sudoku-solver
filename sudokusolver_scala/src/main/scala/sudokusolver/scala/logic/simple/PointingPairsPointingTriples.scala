package sudokusolver.scala.logic.simple

import sudokusolver.scala.{Board, Cell, RemoveCandidates, SudokuNumber, UnsolvedCell, mergeToRemoveCandidates}

/*
 * http://www.sudokuwiki.org/Intersection_Removal#IR
 *
 * For a given block, if a candidate appears in only one row, then the candidate for that row must be placed in that
 * block. The candidate can be removed from cells which are in the same row, but different blocks.
 *
 * For a given block, if a candidate appears in only one column, then the candidate for that column must be placed in
 * that block. The candidate can be removed from cells which are in the same column, but different blocks.
 */
def pointingPairsPointingTriples(board: Board[Cell]): Seq[RemoveCandidates] =
  board.blocks.flatMap { block =>
    val unsolved = block.collect { case cell: UnsolvedCell => cell }
    val blockIndex = block.head.block
    SudokuNumber.values.flatMap { candidate =>
      val withCandidate = unsolved.filter(_.candidates.contains(candidate))

      def pointingPairsPointingTriples(getUnit: Int => Seq[Cell], getUnitIndex: Cell => Int) =
        withCandidate.map(getUnitIndex).distinct match
          case Seq(unitIndex) =>
            for
              cell <- getUnit(unitIndex).collect { case cell: UnsolvedCell
                if cell.block != blockIndex && cell.candidates.contains(candidate) => cell
              }
            yield (cell, candidate)
          case _ => Seq.empty

      val rowModifications = pointingPairsPointingTriples(board.getRow, _.row)
      val columnModifications = pointingPairsPointingTriples(board.getColumn, _.column)
      rowModifications ++ columnModifications
    }
  }.mergeToRemoveCandidates