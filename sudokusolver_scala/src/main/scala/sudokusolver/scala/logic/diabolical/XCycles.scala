package sudokusolver.scala.logic.diabolical

import scalax.collection.immutable.Graph
import scalax.collection.io.dot.implicits.{toId, toNodeId}
import sudokusolver.scala.*

import scala.annotation.tailrec

/*
 * http://www.sudokuwiki.org/X_Cycles
 * http://www.sudokuwiki.org/X_Cycles_Part_2
 *
 * X-Cycles is based on a graph type which is an extension of single's chain. An X-Cycles graph is for a single
 * candidate and can have either strong or weak links. A strong link connects two cells in a unit when they are the only
 * unsolved cells in that unit with the candidate. A weak link connects two cells in a unit when they are not the only
 * unsolved cells in that unit with the candidate. An X-Cycle is a cycle in the graph in which the edges alternate
 * between strong and weak links. If one cell of a link is the solution, then the other cell must not be the solution.
 * If one cell of a strong link is not the solution, then the other cell must be the solution.
 *
 * Note that this implementation of X-Cycles can handle cases in which the chain is not strictly alternating between
 * strong and weak links. It is tolerant of cases in which a strong link takes the place of a weak link.
 *
 * Rule 1:
 *
 * If an X-Cycle has an even number of vertices and therefore continuously alternates between strong and weak, then the
 * graph is perfect and has no flaws. Each of the weak links can be treated as a strong link. The candidate can be
 * removed from any other cell which is in the same unit as both vertices of a weak link.
 */
def xCyclesRule1(board: Board[Cell]): Seq[RemoveCandidates] =
  SudokuNumber.values.toSeq.flatMap { candidate =>
    val graph = createStrongLinksXCycles(board, candidate).addWeakLinksXCycles().trim
    getWeakEdgesInAlternatingCycle(graph).flatMap { edge =>
      val source = graph.get(edge).source
      val target = graph.get(edge).target

      def removeFromUnit(getUnitIndex: Cell => Int, getUnit: Int => Seq[Cell]): Seq[(UnsolvedCell, SudokuNumber)] =
        if getUnitIndex(source) == getUnitIndex(target) then
          for
            cell <- getUnit(getUnitIndex(source)).collect { case cell: UnsolvedCell => cell }
            if cell.candidates.contains(candidate) && cell != source && cell != target
          yield cell -> candidate
        else
          Nil

      val rowRemovals = removeFromUnit(_.row, board.getRow)
      val columnRemovals = removeFromUnit(_.column, board.getColumn)
      val blockRemovals = removeFromUnit(_.block, board.getBlock)
      rowRemovals ++ columnRemovals ++ blockRemovals
    }
  }.mergeToRemoveCandidates

/*
 * Rule 2:
 *
 * If an X-Cycle has an odd number of vertices and the edges alternate between strong and weak, except for one vertex
 * which is connected by two strong links, then the graph is a contradiction. Removing the candidate from the vertex of
 * interest implies that the candidate must be the solution for that vertex, thus causing the cycle to contradict
 * itself. However, considering the candidate to be the solution for that vertex does not cause any contradiction in the
 * cycle. Therefore, the candidate must be the solution for that vertex.
 */
def xCyclesRule2(board: Board[Cell]): Seq[SetValue] =
  for
    candidate <- SudokuNumber.values.toSeq
    graph = createStrongLinksXCycles(board, candidate).addWeakLinksXCycles()
    vertex <- graph.nodes
    if alternatingCycleExists(graph, vertex, Strength.STRONG)
  yield SetValue(vertex, candidate)

/*
 * Rule 3:
 *
 * If an X-Cycle has an odd number of vertices and the edges alternate between strong and weak, except for one vertex
 * which is connected by two weak links, then the graph is a contradiction. Considering the candidate to be the solution
 * for the vertex of interest implies that the candidate must be removed from that vertex, thus causing the cycle to
 * contradict itself. However, removing the candidate from that vertex does not cause any contradiction in the cycle.
 * Therefore, the candidate can be removed from the vertex.
 */
def xCyclesRule3(board: Board[Cell]): Seq[RemoveCandidates] =
  val removals = for
    candidate <- SudokuNumber.values.toSeq
    graph = createStrongLinksXCycles(board, candidate).addWeakLinksXCycles().additionalWeakLinks(board, candidate)
    vertex <- graph.nodes
    if alternatingCycleExists(graph, vertex, Strength.WEAK)
  yield vertex.outer -> candidate
  removals.mergeToRemoveCandidates

extension (graph: Graph[UnsolvedCell, StrengthEdge[UnsolvedCell]])
  /*
   * Ideally, this would be called toDOT, but it is called toDOTXCycles to distinguish it from a similarly named method
   * in XYChains. Unfortunately, having a toDOT in XCycles and a toDOT in XYChains leads to a naming conflict because
   * the two functions exist in the same package. This is different from Kotlin which allows toDOT to exist in different
   * files, but in the same package.
   *
   * Similar changes have been made to createStrongLinks and addWeakLinks
   */
  def toDOTXCycles(candidate: SudokuNumber): String =
    graph.toDOTCommon(Some(candidate.toString), _.getVertexLabel, _.getEdgeAttributes)

  private def addWeakLinksXCycles(): Graph[UnsolvedCell, StrengthEdge[UnsolvedCell]] =
    val weakEdges = for
      (a, b) <- graph.nodes.toIndexedSeq.zipEveryPair
      if a.isInSameUnit(b) && !a.neighbors.contains(b)
    yield StrengthEdge(a.outer, b.outer, Strength.WEAK)
    graph ++ weakEdges

  private def additionalWeakLinks(
                                   board: Board[Cell],
                                   candidate: SudokuNumber
                                 ): Graph[UnsolvedCell, StrengthEdge[UnsolvedCell]] =
    val additionalEdges = for
      cell <- board.cells.collect { case cell: UnsolvedCell => cell }
      if cell.candidates.contains(candidate) && !graph.contains(cell)
      vertex <- graph.nodes
      if vertex.isInSameUnit(cell)
    yield StrengthEdge(vertex.outer, cell, Strength.WEAK)
    graph ++ additionalEdges

private def createStrongLinksXCycles(
                                      board: Board[Cell],
                                      candidate: SudokuNumber
                                    ): Graph[UnsolvedCell, StrengthEdge[UnsolvedCell]] =
  val edges = board.units
    .map(unit => unit.collect { case cell: UnsolvedCell if cell.candidates.contains(candidate) => cell })
    .collect { case Seq(a, b) => StrengthEdge(a, b, Strength.STRONG) }
  Graph.from(edges)