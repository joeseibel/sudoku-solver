package sudokusolver.scala.logic.extreme

import scalax.collection.immutable.Graph
import sudokusolver.scala.*

/*
 * https://www.sudokuwiki.org/Alternating_Inference_Chains
 *
 * Alternating Inference Chains are based on a graph type in which each vertex is a specific candidate in a cell and the
 * edges can either be strong or weak links. A strong link connects two vertices in a unit that share a candidate when
 * they are in the only unsolved cells in that unit with the candidate. A strong link also connects two vertices in a
 * single cell when they are the only two candidates in that cell. A weak link connects two vertices in a unit that
 * share a candidate when they are not the only unsolved cells in that unit with the candidate. A weak link also
 * connects two vertices in a single cell when there are more than two candidates in that cell. An Alternating Inference
 * Chain is a cycle in the graph in which the edges alternate between strong and weak links. If one vertex of a link is
 * the solution, then the other vertex must not be the solution. If one vertex of a strong link is not the solution,
 * then the other vertex must be the solution. Alternating Inference Chains are very similar to X-Cycles and Grouped
 * X-Cycles.
 *
 * Note that this implementation of Alternating Inference Chains can handle cases in which the chain is not strictly
 * alternating between strong and weak links. It is tolerant of cases in which a strong link takes the place of a weak
 * link.
 *
 * Rule 1:
 *
 * If an Alternating Inference Chain has an even number of vertices and therefore continuously alternates between strong
 * and weak, then the graph is perfect and has no flaws. Each of the weak links can be treated as a strong link.
 * If a weak link connects a common candidate across two different cells, then that candidate can be removed from any
 * other cell which is in the same unit as the two vertices. If a weak link connects two candidates of the same cell,
 * then all other candidates can be removed from that cell.
 */
def alternatingInferenceChainsRule1(board: Board[Cell]): Seq[RemoveCandidates] =
  getWeakEdgesInAlternatingCycle(buildGraphAIC(board).trim).toSeq.flatMap { edge =>
    val (sourceCell, sourceCandidate) = edge.source
    val (targetCell, targetCandidate) = edge.target

    if sourceCell == targetCell then
      (sourceCell.candidates - sourceCandidate - targetCandidate).map(sourceCell -> _)
    else

      def removeFromUnit(sourceUnitIndex: Int, targetUnitIndex: Int, getUnit: Int => Seq[Cell]) =
        if sourceUnitIndex == targetUnitIndex then
          for
            cell <- getUnit(sourceUnitIndex).collect { case cell: UnsolvedCell => cell }
            if cell.candidates.contains(sourceCandidate) && cell != sourceCell && cell != targetCell
          yield cell -> sourceCandidate
        else
          Nil

      val rowRemovals = removeFromUnit(sourceCell.row, targetCell.row, board.getRow)
      val columnRemovals = removeFromUnit(sourceCell.column, targetCell.column, board.getColumn)
      val blockRemovals = removeFromUnit(sourceCell.block, targetCell.block, board.getBlock)

      rowRemovals ++ columnRemovals ++ blockRemovals
  }.mergeToRemoveCandidates

/*
 * Rule 2:
 *
 * If an Alternating Inference Chain has an odd number of vertices and the edges alternate between strong and weak,
 * except for one vertex which is connected by two strong links, then the graph is a contradiction. Removing the
 * candidate from the cell of interest implies that the candidate must be the solution for that cell, thus causing the
 * cycle to contradict itself. However, considering the candidate to be the solution for that cell does not cause any
 * contradiction in the cycle. Therefore, the candidate must be the solution for that cell.
 *
 * Note that this implementation of rule 2 does not allow for a candidate to be revisited in the chain. A candidate can
 * appear multiple times in a chain, but only if all the occurrences are consecutive.
 */
def alternatingInferenceChainsRule2(board: Board[Cell]): Seq[SetValue] =
  val graph = buildGraphAIC(board).trim
  graph.nodes
    .toSeq
    .map(_.outer)
    .filter(alternatingCycleExistsAIC(graph, _, Strength.STRONG))
    .map((cell, candidate) => SetValue(cell, candidate))

/*
 * Rule 3:
 *
 * If an Alternating Inference Chain has an odd number of vertices and the edges alternate between strong and weak,
 * except for one vertex which is connected by two weak links, then the graph is a contradiction. Considering the
 * candidate to be the solution for the cell of interest implies that the candidate must be removed from that cell, thus
 * causing the cycle to contradict itself. However, removing the candidate from that cell does not cause any
 * contradiction in the cycle. Therefore, the candidate can be removed from the cell.
 *
 * Note that this implementation of rule 3 does not allow for a candidate to be revisited in the chain. A candidate can
 * appear multiple times in a chain, but only if all the occurrences are consecutive.
 */
def alternatingInferenceChainsRule3(board: Board[Cell]): Seq[RemoveCandidates] =
  val graph = buildGraphAIC(board)
  graph.nodes.toSeq.map(_.outer).filter(alternatingCycleExistsAIC(graph, _, Strength.WEAK)).mergeToRemoveCandidates

private def buildGraphAIC(board: Board[Cell]): Graph[LocatedCandidate, StrengthEdge[LocatedCandidate]] =
  // Connect cells.
  val cellEdges = for
    unit <- board.units
    candidate <- SudokuNumber.values
    withCandidate = unit.collect { case cell: UnsolvedCell if cell.candidates.contains(candidate) => cell }
    strength = if withCandidate.size == 2 then Strength.STRONG else Strength.WEAK
    (a, b) <- withCandidate.zipEveryPair
  yield StrengthEdge(a -> candidate, b -> candidate, strength)

  // Connect candidates in cells.
  val candidateEdges = board.cells.collect { case cell: UnsolvedCell => cell }.flatMap { cell =>
    val strength = if cell.candidates.size == 2 then Strength.STRONG else Strength.WEAK
    cell.candidates.toIndexedSeq.zipEveryPair.map((a, b) => StrengthEdge(cell -> a, cell -> b, strength))
  }

  Graph.from(cellEdges ++ candidateEdges)

private def alternatingCycleExistsAIC(
                                       graph: Graph[LocatedCandidate, StrengthEdge[LocatedCandidate]],
                                       vertex: LocatedCandidate,
                                       adjacentEdgesType: Strength
                                     ): Boolean =
  graph.get(vertex).edges.toIndexedSeq.filter(_.strength == adjacentEdgesType).zipEveryPair.exists { (edgeA, edgeB) =>
    val start = if edgeA.source == vertex then edgeA.target else edgeA.source
    val end = if edgeB.source == vertex then edgeB.target else edgeB.source

    def alternatingCycleExists(
                                currentVertex: LocatedCandidate,
                                nextType: Strength,
                                visited: Set[LocatedCandidate],
                                visitedCandidates: Set[SudokuNumber]
                              ): Boolean =
      val nextVertices = for
        edge <- graph.get(currentVertex).edges
        if edge.strength.isCompatibleWith(nextType)
        opposite = if edge.source == currentVertex then edge.target else edge.source
        if opposite.candidate == currentVertex.candidate || !visitedCandidates.contains(opposite.candidate)
      yield opposite
      adjacentEdgesType.opposite == nextType && nextVertices.contains(end) ||
        (nextVertices &~ visited - end).exists { nextVertex =>
          val nextVisited = visited + nextVertex
          val nextVisitedCandidates = if currentVertex.candidate == nextVertex.candidate then
            visitedCandidates
          else
            visitedCandidates + nextVertex.candidate
          alternatingCycleExists(nextVertex, nextType.opposite, nextVisited, nextVisitedCandidates)
        }

    alternatingCycleExists(
      start,
      adjacentEdgesType.opposite,
      Set(vertex, start),
      Set(vertex.candidate, start.candidate) - end.candidate
    )
  }