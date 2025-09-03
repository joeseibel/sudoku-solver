package sudokusolver.scala.logic.tough

import scalax.collection.edges.{UnDiEdge, UnDiEdgeImplicits}
import scalax.collection.immutable.Graph
import scalax.collection.io.dot.implicits.{toId, toNodeId}
import sudokusolver.scala.*

/*
 * http://www.sudokuwiki.org/Singles_Chains
 *
 * A single's chain is a graph for a particular candidate that connects two cells when those are the only two cells in a
 * unit with that candidate. Each chain is colored with alternating colors such that for a given vertex with a given
 * color, all adjacent vertices have the opposite color. The two colors represent the two possible solutions for each
 * cell in the chain. Either the first color is the solution for the chain or the second color is.
 *
 * Rule 2: Twice in a Unit
 *
 * If there are two or more vertices with the same color that are in the same unit, then that color cannot be the
 * solution. All candidates with that color in that chain can be removed.
 */
def simpleColoringRule2(board: Board[Cell]): Seq[RemoveCandidates] =
  SudokuNumber.values.toSeq.flatMap { candidate =>
    createConnectedComponents(board, candidate).flatMap { graph =>
      val colors = graph.colorToMap
      val colorToRemove = graph.nodes
        .toIndexedSeq
        .zipEveryPair
        .find((a, b) => colors(a) == colors(b) && a.isInSameUnit(b))
        .map((a, _) => colors(a))
      colorToRemove match
        case Some(removalColor) =>
          graph.nodes
            .filter(colors(_) == removalColor)
            .map(_.outer -> candidate)
        case None => Nil
    }
  }.mergeToRemoveCandidates

/*
 * Rule 4: Two colors 'elsewhere'
 *
 * If an unsolved cell with a given candidate is outside the chain, and it is in the same units as two differently
 * colored vertices, then one of those two vertices must be the solution for the candidate. The candidate can be removed
 * from the cell outside the chain.
 */
def simpleColoringRule4(board: Board[Cell]): Seq[RemoveCandidates] =
  val removals = for
    candidate <- SudokuNumber.values.toSeq
    graph <- createConnectedComponents(board, candidate)
    (colorOne, colorTwo) = graph.colorToLists
    cell <- board.cells.collect { case cell: UnsolvedCell => cell }
    if cell.candidates.contains(candidate) &&
      !graph.contains(cell) &&
      colorOne.exists(cell.isInSameUnit) &&
      colorTwo.exists(cell.isInSameUnit)
  yield cell -> candidate
  removals.mergeToRemoveCandidates

extension (graph: Graph[UnsolvedCell, UnDiEdge[UnsolvedCell]])
  def toDOT(candidate: SudokuNumber): String = graph.toDOTCommon(Some(candidate.toString), _.getVertexLabel)

private def createConnectedComponents(
                                       board: Board[Cell],
                                       candidate: SudokuNumber
                                     ): Iterable[Graph[UnsolvedCell, UnDiEdge[UnsolvedCell]]] =
  val edges = board.units
    .map(unit => unit.collect { case cell: UnsolvedCell if cell.candidates.contains(candidate) => cell })
    .collect { case Seq(a, b) => a ~ b }
  Graph.from(edges).componentTraverser().map(_.to(Graph))