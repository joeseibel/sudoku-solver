package sudokusolver.scala.logic.diabolical

import sudokusolver.scala.{Board, Cell, SetValue, UnsolvedCell}

/*
 * http://www.sudokuwiki.org/BUG
 *
 * BUG applies to boards with exactly one unsolved cell with three candidates and every other unsolved cell has two
 * candidates. Removing one of the candidates from the cell with three candidates will result in a board in which all of
 * its unsolved cells have two candidates, which would have multiple solutions. Since removing that candidate from that
 * cell would lead to an invalid board, that candidate must be the solution to that cell.
 *
 * For the three candidates of the cell, two candidates will appear twice in the cell's row, twice in the cell's column,
 * and twice in the cell's block, while one candidate will appear three times in the cell's row, three times in the
 * cell's column, and three times in the cell's block. This check is only performed against the cell's row.
 */
def bug(board: Board[Cell]): Option[SetValue] =
  board.cells.collect { case cell: UnsolvedCell if cell.candidates.size != 2 => cell } match
    case Seq(cell) if cell.candidates.size == 3 =>
      val row = board.getRow(cell.row).collect { case cell: UnsolvedCell => cell }
      val candidates = cell.candidates.filter(candidate => row.count(_.candidates.contains(candidate)) == 3)
      assert(candidates.size == 1)
      Some(SetValue(cell, candidates.head))
    case _ => None