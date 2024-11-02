package sudokusolver.scala.logic.diabolical

import scalax.collection.edges.{UnDiEdge, UnDiEdgeImplicits}
import scalax.collection.immutable.Graph
import sudokusolver.scala.*

/*
 * https://www.sudokuwiki.org/3D_Medusa
 *
 * A 3D Medusa is a graph type in which each vertex is a particular candidate in a cell and each edge is a strong link.
 * A strong link is an edge such that if one vertex of the link is the solution, then the other vertex must not be the
 * solution. A strong link also means that if one vertex of the link is not the solution, then the other vertex must be
 * the solution. When a candidate is in only two cells of a unit, there is an edge between the candidate of those two
 * cells. Additionally, when a cell contains only two candidates, there is an edge between the two candidates of that
 * cell. Each medusa is colored with alternating colors such that for a given vertex with a given color, all adjacent
 * vertices have the opposite color. The two colors represent the two possible solutions. Either the first color is the
 * solution for the medusa or the second color is.
 *
 * Rule 1: Twice in a Cell
 *
 * If there are two vertices with the same color that are in the same cell, then that color cannot be the solution and
 * the opposite color must be the solution. All vertices with the opposite color can be set as the solution.
 */
def medusaRule1(board: Board[Cell]): Seq[SetValue] =
  createConnectedComponents(board).toSeq.flatMap { graph =>
    val colors = graph.colorToMap
    graph.nodes
      .map(_.outer)
      .toIndexedSeq
      .zipEveryPair
      .find { (a, b) =>
        val (cellA, _) = a
        val (cellB, _) = b
        cellA == cellB && colors(a) == colors(b)
      }
      .map((a, _) => colors(a))
      .map(_.opposite)
      .toSeq
      .flatMap { colorToSet =>
        graph.nodes
          .map(_.outer)
          .filter(colors(_) == colorToSet)
          .map((cell, candidate) => SetValue(cell, candidate))
      }
  }

/*
 * Rule 2: Twice in a Unit
 *
 * If there are two vertices with the same color and the same candidate that are in the same unit, then that color
 * cannot be the solution and the opposite color must be the solution. All vertices with the opposite color can be set
 * as the solution.
 */
def medusaRule2(board: Board[Cell]): Seq[SetValue] =
  createConnectedComponents(board).toSeq.flatMap { graph =>
    val colors = graph.colorToMap
    graph.nodes
      .map(_.outer)
      .toIndexedSeq
      .zipEveryPair
      .find { (a, b) =>
        val (cellA, candidateA) = a
        val (cellB, candidateB) = b
        candidateA == candidateB && colors(a) == colors(b) && cellA.isInSameUnit(cellB)
      }
      .map((a, _) => colors(a))
      .map(_.opposite)
      .toSeq
      .flatMap { colorToSet =>
        graph.nodes
          .map(_.outer)
          .filter(colors(_) == colorToSet)
          .map((cell, candidate) => SetValue(cell, candidate))
      }
  }

/*
 * Rule 3: Two colors in a cell
 *
 * If there are two differently colored candidates in a cell, then the solution must be one of the two candidates. All
 * other candidates in the cell can be removed.
 */
def medusaRule3(board: Board[Cell]): Seq[RemoveCandidates] =
  createConnectedComponents(board).toSeq.flatMap { graph =>
    val colors = graph.colorToMap
    graph.nodes
      .map(_.outer)
      .toIndexedSeq
      .filter { (cell, _) => cell.candidates.size > 2 }
      .zipEveryPair
      .find { (a, b) =>
        val (cellA, _) = a
        val (cellB, _) = b
        cellA == cellB && colors(a) != colors(b)
      }
      .map((a, _) => a)
      .map((cell, _) => cell)
      .toSeq
      .flatMap { cell =>
        cell.candidates
          .map(cell -> _)
          .filter(!graph.contains(_))
      }
  }.mergeToRemoveCandidates

/*
 * Rule 4: Two colors 'elsewhere'
 *
 * Given a candidate, if there is an unsolved cell with that candidate, it is uncolored, and the cell can see two other
 * cells which both have that candidate, and they are differently colored, then the candidate must be the solution to
 * one of the other cells, and it cannot be the solution to the first cell with the uncolored candidate. The uncolored
 * candidate can be removed from the first cell.
 */
def medusaRule4(board: Board[Cell]): Seq[RemoveCandidates] =
  createConnectedComponents(board).toSeq.flatMap { graph =>
    val (colorOne, colorTwo) = graph.colorToLists
    board.cells
      .collect { case cell: UnsolvedCell => cell }
      .flatMap(cell => cell.candidates.map(candidate => cell -> candidate))
      .filter(!graph.contains(_))
      .filter { (cell, candidate) =>

        def canSeeColor(color: Seq[LocatedCandidate]) =
          color.exists { (coloredCell, coloredCandidate) =>
            candidate == coloredCandidate && cell.isInSameUnit(coloredCell)
          }

        canSeeColor(colorOne) && canSeeColor(colorTwo)
      }
  }.mergeToRemoveCandidates

/*
 * Rule 5: Two colors Unit + Cell
 *
 * If there is an unsolved cell with an uncolored candidate, that candidate can see a colored candidate of the same
 * number, and the unsolved cell contains a candidate colored with the opposite color, then either the candidate in the
 * same unit is the solution for that cell or the candidate in the same cell is the solution. In either case, the
 * uncolored candidate cannot be the solution and can be removed from the unsolved cell.
 */
def medusaRule5(board: Board[Cell]): Seq[RemoveCandidates] =
  createConnectedComponents(board).toSeq.flatMap { graph =>
    val (colorOne, colorTwo) = graph.colorToLists
    board.cells
      .collect { case cell: UnsolvedCell => cell }
      .flatMap(cell => cell.candidates.map(candidate => cell -> candidate))
      .filter(!graph.contains(_))
      .filter { (cell, candidate) =>

        def canSeeColor(color: Seq[LocatedCandidate]) =
          color.exists { (coloredCell, coloredCandidate) =>
            candidate == coloredCandidate && cell.isInSameUnit(coloredCell)
          }

        def colorInCell(color: Seq[LocatedCandidate]) =
          cell.candidates.exists(candidate => color.contains(cell -> candidate))

        canSeeColor(colorOne) && colorInCell(colorTwo) || canSeeColor(colorTwo) && colorInCell(colorOne)
      }
  }.mergeToRemoveCandidates

/*
 * Rule 6: Cell Emptied by Color
 *
 * If there is an unsolved cell in which every candidate is uncolored and every candidate can see the same color, then
 * that color cannot be the solution since it would lead to the cell being emptied of candidates and still have no
 * solution. All vertices with the opposite color can be set as the solution.
 */
def medusaRule6(board: Board[Cell]): Seq[SetValue] =
  createConnectedComponents(board).toSeq.flatMap { graph =>
    val (colorOne, colorTwo) = graph.colorToLists

    def notInGraph(cell: UnsolvedCell) = cell.candidates.forall(candidate => !graph.contains(cell -> candidate))

    def everyCandidateCanSeeColor(cell: UnsolvedCell, color: Seq[LocatedCandidate]) =
      cell.candidates.forall { candidate =>
        color.exists((coloredCell, coloredCandidate) => candidate == coloredCandidate && cell.isInSameUnit(coloredCell))
      }

    board.cells
      .collectFirst {
        case cell: UnsolvedCell if notInGraph(cell) && everyCandidateCanSeeColor(cell, colorOne) => colorTwo
        case cell: UnsolvedCell if notInGraph(cell) && everyCandidateCanSeeColor(cell, colorTwo) => colorOne
      }
      .toSeq
      .flatten
      .map((coloredCell, coloredCandidate) => SetValue(coloredCell, coloredCandidate))
  }

extension (graph: Graph[LocatedCandidate, UnDiEdge[LocatedCandidate]])
  def toDOT: String = graph.toDOTCommon(None, _.getVertexLabel)

private def createConnectedComponents(
                                       board: Board[Cell]
                                     ): Iterable[Graph[LocatedCandidate, UnDiEdge[LocatedCandidate]]] =
  val biLocationEdges = board.cells
    .collect { case cell: UnsolvedCell if cell.candidates.size == 2 =>
      val Seq(candidateA, candidateB) = cell.candidates.toSeq
      (cell -> candidateA) ~ (cell -> candidateB)
    }
  val biValueEdges = board.units
    .map(_.collect { case cell: UnsolvedCell => cell })
    .flatMap { unit =>
      SudokuNumber.values.flatMap { candidate =>
        unit.filter(_.candidates.contains(candidate)) match
          case Seq(cellA, cellB) => Some((cellA -> candidate) ~ (cellB -> candidate))
          case _ => None
      }
    }
  Graph.from(biLocationEdges ++ biValueEdges).componentTraverser().map(_.to(Graph))