package sudokusolver.scala.logic.simple

import sudokusolver.scala.*

/*
 * http://www.sudokuwiki.org/Hidden_Candidates#HT
 *
 * If three candidates exist across three cells in a unit, then those three candidates must be placed in those three
 * cells. All other candidates can be removed from those three cells.
 */
def hiddenTriples(board: Board[Cell]): Seq[RemoveCandidates] =
  board.units.flatMap { unit =>
    SudokuNumber.values.toIndexedSeq.zipEveryTriple.flatMap { (a, b, c) =>
      val cells = unit.collect { case cell: UnsolvedCell
        if cell.candidates.contains(a) || cell.candidates.contains(b) || cell.candidates.contains(c) => cell
      }
      cells match
        case Seq(_, _, _) =>
          val union = cells.map(_.candidates).reduce(_ | _)
          if union.contains(a) && union.contains(b) && union.contains(c) then
            cells.flatMap(cell => (cell.candidates - a - b - c).map(candidate => cell -> candidate))
          else
            Nil
        case _ => Nil
    }
  }.mergeToRemoveCandidates