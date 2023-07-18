package sudokusolver.scala.logic.diabolical

import sudokusolver.scala.*

/*
 * https://www.sudokuwiki.org/WXYZ_Wing
 *
 * WXYZ-Wing applies for a quad of unsolved cells that has a total of four candidates among the quad. The quad may
 * contain restricted candidates and non-restricted candidates. A restricted candidate is one in which each cell of the
 * quad with the candidate can see every other cell of the quad with the candidate. A non-restricted candidate is one in
 * which at least one cell of the quad with the candidate cannot see every other cell of the quad with the candidate. If
 * a quad contains exactly one non-restricted candidate, then that candidate must be the solution to one of the cells of
 * the quad. The non-restricted candidate can be removed from any cell outside the quad that can see every cell of the
 * quad with the candidate.
 */
def wxyzWing(board: Board[Cell]): Seq[RemoveCandidates] =
  board.cells
    .collect { case cell: UnsolvedCell => cell }
    .filter(_.candidates.size <= 4)
    .zipEveryQuad
    .flatMap { (a, b, c, d) =>
      val quad = Seq(a, b, c, d)
      val candidates = a.candidates | b.candidates | c.candidates | d.candidates
      if candidates.size == 4 then
        val nonRestricted = candidates.toSeq.filter { candidate =>
          quad.filter(_.candidates.contains(candidate)).toIndexedSeq.zipEveryPair.exists((a, b) => !a.isInSameUnit(b))
        }
        nonRestricted match
          case Seq(nonRestricted) =>
            val withCandidate = quad.filter(_.candidates.contains(nonRestricted))
            for
              cell <- board.cells.collect { case cell: UnsolvedCell => cell }
              if cell.candidates.contains(nonRestricted) &&
                !quad.contains(cell) &&
                withCandidate.forall(cell.isInSameUnit)
            yield cell -> nonRestricted
          case _ => Nil
      else
        Nil
    }.mergeToRemoveCandidates