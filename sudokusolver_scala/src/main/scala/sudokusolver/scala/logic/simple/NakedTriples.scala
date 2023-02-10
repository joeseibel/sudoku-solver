package sudokusolver.scala.logic.simple

import sudokusolver.scala.{Board, Cell, RemoveCandidates, UnsolvedCell, mergeToRemoveCandidates, zipEveryTriple}

def nakedTriples(board: Board[Cell]): Seq[RemoveCandidates] =
  val removals = board.units.flatMap { unit =>
    unit.collect { case cell: UnsolvedCell => cell }.zipEveryTriple.flatMap { (a, b, c) =>
      val unionOfCandidates = a.candidates ++ b.candidates ++ c.candidates
      if unionOfCandidates.size == 3 then
        val removals = for
          cell <- unit.collect { case cell: UnsolvedCell => cell }
          if cell != a && cell != b && cell != c
          candidate <- cell.candidates intersect unionOfCandidates
        yield (cell, candidate)
        Some(removals)
      else
        None
    }.flatten
  }
  removals.mergeToRemoveCandidates