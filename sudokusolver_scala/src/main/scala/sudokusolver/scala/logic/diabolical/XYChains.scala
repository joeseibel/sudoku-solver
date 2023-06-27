package sudokusolver.scala.logic.diabolical

import scalax.collection.immutable.Graph
import scalax.collection.io.dot.implicits.{toId, toNodeId}
import scalax.collection.io.dot.{DotAttr, DotEdgeStmt, DotRootGraph, Graph2DotExport}
import sudokusolver.scala.*

/*
 * https://www.sudokuwiki.org/XY_Chains
 *
 * XY-Chains is based on a graph type which shares many similarities to X-Cycles. Unlike X-Cycles, an XY-Chains graph
 * includes multiple candidates. This results in a single XY-Chains graph per board whereas there can be up to nine
 * X-Cycles graphs per board, one for each candidate. Each vertex in an XY-Chains graph is a particular candidate in a
 * cell and the edges are either strong or weak links. A strong link connects two candidates of a single cell when they
 * are the only candidates of that cell. A weak link connects two vertices which have the same candidate, are in
 * different cells, but are in the same unit. An XY-Chain is a chain between two vertices of the graph that have the
 * same candidate, the edges of the chain alternate between strong and weak links, and the last links on either end of
 * the chain are strong. If one vertex of a link is the solution, then the other vertex must not be the solution. If one
 * vertex of a strong link is not the solution, then the other vertex must be the solution. When there is a proper chain
 * in the graph it means that one of the two end points must be the solution. The candidate can be removed from any cell
 * of the board which is not an end point of the chain and that cell can see both end points.
 *
 * Note that this implementation of XY-Chains can handle cases in which the chain is not strictly alternating between
 * strong and weak links. It is tolerant of cases in which a strong link takes the place of a weak link.
 */
def xyChains(board: Board[Cell]): Seq[RemoveCandidates] =
  val graph = createStrongLinksXYChains(board).addWeakLinksXYChains()
  graph.nodes.map(_.outer).groupBy((_, candidate) => candidate).toSeq.flatMap { (candidate, vertices) =>
    vertices.toIndexedSeq
      .zipEveryPair
      .flatMap { (vertexA, vertexB) =>
        val (cellA, _) = vertexA
        val (cellB, _) = vertexB
        val visibleCells = for
          cell <- board.cells.collect { case cell: UnsolvedCell => cell }
          if cell.candidates.contains(candidate) &&
            cell != cellA &&
            cell != cellB &&
            cell.isInSameUnit(cellA) &&
            cell.isInSameUnit(cellB)
        yield cell
        if visibleCells.nonEmpty && alternatingPathExists(graph, vertexA, vertexB) then
          Some(visibleCells)
        else
          None
      }
      .flatten
      .map(_ -> candidate)
  }.mergeToRemoveCandidates

extension (graph: Graph[LocatedCandidate, StrengthEdge[LocatedCandidate]])
  /*
   * Ideally, this would be called toDOT, but it is called toDOTXYChains to distinguish it from a similarly named method
   * in XCycles. Unfortunately, having a toDOT in XCycles and a toDOT in XYChains leads to a naming conflict because
   * the two functions exist in the same package. This is different from Kotlin which allows toDOT to exist in different
   * files, but in the same package.
   *
   * Similar changes have been made to createStrongLinks and addWeakLinks
   */
  def toDOTXYChains: String =
    val dotRoot = DotRootGraph(false, None)
    graph.toDot(dotRoot, edge =>
      Some(dotRoot, edge.outer.toDotEdgeStmt((cell, candidate) => s"[${cell.row},${cell.column}] : $candidate"))
    )

  def addWeakLinksXYChains(): Graph[LocatedCandidate, StrengthEdge[LocatedCandidate]] =
    val weakEdges = for
      (vertexA, vertexB) <- graph.nodes.map(_.outer).toIndexedSeq.zipEveryPair
      (cellA, candidateA) = vertexA
      (cellB, candidateB) = vertexB
      if candidateA == candidateB && cellA.isInSameUnit(cellB)
    yield StrengthEdge(vertexA, vertexB, Strength.WEAK)
    graph ++ weakEdges

private def createStrongLinksXYChains(board: Board[Cell]): Graph[LocatedCandidate, StrengthEdge[LocatedCandidate]] =
  val edges = for
    cell <- board.cells.collect { case cell: UnsolvedCell => cell }
    if cell.candidates.size == 2
    Seq(candidateA, candidateB) = cell.candidates.toSeq
  yield StrengthEdge(cell -> candidateA, cell -> candidateB, Strength.STRONG)
  Graph.from(edges)

private def alternatingPathExists(
                                   graph: Graph[LocatedCandidate, StrengthEdge[LocatedCandidate]],
                                   start: LocatedCandidate,
                                   end: LocatedCandidate
                                 ): Boolean =

  def alternatingPathExists(
                             currentVertex: LocatedCandidate,
                             nextType: Strength,
                             visited: Set[LocatedCandidate]
                           ): Boolean =
    val nextVertices = for
      edge <- graph.get(currentVertex).edges
      if edge.strength.isCompatibleWith(nextType)
    yield edge.outer.getOppositeVertex(currentVertex)
    nextType == Strength.STRONG && nextVertices.contains(end) || (nextVertices &~ visited - end).exists { nextVertex =>
      alternatingPathExists(nextVertex, nextType.opposite, visited + nextVertex)
    }

  alternatingPathExists(start, Strength.STRONG, Set(start))