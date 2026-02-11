package sudokusolver.scala.logic.extreme

import scalax.collection.immutable.Graph
import scalax.collection.io.dot.implicits.{toId, toNodeId}
import sudokusolver.scala.*

/*
 * https://www.sudokuwiki.org/Grouped_X_Cycles
 *
 * Grouped X-Cycles are an extension of X-Cycles in which a vertex can be a cell or a group of cells. Just like
 * X-Cycles, a Grouped X-Cycles graph is for a single candidate. A group is a set of cells with the candidate which
 * share two units. This means that a group exists in the same block and the same row, or it exists in the same block
 * and the same column.
 *
 * Similar to X-Cycles, the edges of a Grouped X-Cycles graph are either strong or weak. Unlike X-Cycles, the edges can
 * connect two cells, a cell and a group, or two groups. A strong link connects two vertices in a unit when they are the
 * only non-overlapping vertices in that unit. A weak link connects two vertices in a unit when they are not the only
 * non-overlapping vertices in that unit.
 *
 * Since a vertex can be a cell or a group of cells, it is possible for vertices to overlap and even for edges to
 * overlap. For example, consider a unit which has three cells with a candidate, two of which form a group and one which
 * is outside the group. In this case there would be four vertices: three vertices for the cells and one for the group.
 * Two of the cell vertices overlap with the cells of the group. This example would also have one strong link and three
 * weak links. The strong link would connect the group to the cell outside the group. This is a strong link because when
 * we discount the cells that overlap with the group, there are only two vertices in the unit. The weak links connect
 * all the individual cells. They are weak because there are more than two cell vertices in the unit when we discount
 * the group.
 *
 * A Grouped X-Cycle is a cycle in the graph in which the edges alternate between strong and weak links. If one vertex
 * of a link contains the solution, then the other vertex must not contain the solution. If one cell of a strong link
 * does not contain the solution, then the other vertex must contain the solution. If a vertex is a group, containing
 * the solution means that one of the cells of the group is the solution. If a vertex is a cell, containing the solution
 * means that the cell is the solution.
 *
 * Note that this implementation of Grouped X-Cycles can handle cases in which the chain is not strictly alternating
 * between strong and weak links. It is tolerant of cases in which a strong link takes the place of a weak link.
 *
 * Rule 1:
 *
 * If a Grouped X-Cycle has an even number of vertices and therefore continuously alternates between strong and weak,
 * then the graph is perfect and has no flaws. Each of the weak links can be treated as a strong link. The candidate can
 * be removed from any cell which is in the same unit as both vertices of a weak link, but not contained in either of
 * the vertices.
 */
def groupedXCyclesRule1(board: Board[Cell]): Seq[RemoveCandidates] =
  SudokuNumber.values.toSeq.flatMap { candidate =>
    getWeakEdgesInAlternatingCycle(buildGraphGroupedXCycles(board, candidate).trim).flatMap { edge =>
      val source = edge.source
      val target = edge.target

      def removeFromUnit(sourceUnitIndex: Option[Int], targetUnitIndex: Option[Int], getUnit: Int => Seq[Cell]) =
        sourceUnitIndex match
          case Some(sourceUnitIndex) =>
            targetUnitIndex match
              case Some(targetUnitIndex) if sourceUnitIndex == targetUnitIndex =>
                for
                  cell <- getUnit(sourceUnitIndex).collect { case cell: UnsolvedCell => cell }
                  if cell.candidates.contains(candidate) && !source.cells.contains(cell) && !target.cells.contains(cell)
                yield cell -> candidate
              case _ => Nil
          case None => Nil

      val rowRemovals = removeFromUnit(source.row, target.row, board.getRow)
      val columnRemovals = removeFromUnit(source.column, target.column, board.getColumn)
      val blockRemovals = removeFromUnit(Some(source.block), Some(target.block), board.getBlock)
      rowRemovals ++ columnRemovals ++ blockRemovals
    }
  }.mergeToRemoveCandidates

/*
 * Rule 2:
 *
 * If a Grouped X-Cycle has an odd number of vertices and the edges alternate between strong and weak, except for one
 * vertex which is a cell and is connected by two strong links, then the graph is a contradiction. Removing the
 * candidate from the vertex of interest implies that the candidate must be the solution for that vertex, thus causing
 * the cycle to contradict itself. However, considering the candidate to be the solution for that vertex does not cause
 * any contradiction in the cycle. Therefore, the candidate must be the solution for that vertex.
 */
def groupedXCyclesRule2(board: Board[Cell]): Seq[SetValue] =
  SudokuNumber.values.toSeq.flatMap { candidate =>
    val graph = buildGraphGroupedXCycles(board, candidate)
    graph.nodes
      .map(_.outer)
      .collect { case cell: UnsolvedCell if alternatingCycleExists(graph, cell, Strength.STRONG) => cell }
      .map(SetValue(_, candidate))
  }

/*
 * Rule 3:
 *
 * If a Grouped X-Cycle has an odd number of vertices and the edges alternate between strong and weak, except for one
 * vertex which is a cell and is connected by two weak links, then the graph is a contradiction. Considering the
 * candidate to be the solution for the vertex of interest implies that the candidate must be removed from that vertex,
 * thus causing the cycle to contradict itself. However, removing the candidate from that vertex does not cause any
 * contradiction in the cycle. Therefore, the candidate can be removed from the vertex.
 */
def groupedXCyclesRule3(board: Board[Cell]): Seq[RemoveCandidates] =
  SudokuNumber.values.toSeq.flatMap { candidate =>
    val graph = buildGraphGroupedXCycles(board, candidate)
    graph.nodes
      .map(_.outer)
      .collect { case cell: UnsolvedCell if alternatingCycleExists(graph, cell, Strength.WEAK) => cell }
      .map(_ -> candidate)
  }.mergeToRemoveCandidates

extension (graph: Graph[Node, StrengthEdge[Node]])
  def toDOT(candidate: SudokuNumber): String =
    graph.toDOTCommon(Some(candidate.toString), {
      case cell: UnsolvedCell => cell.getVertexLabel
      case node: Group => node.cells.map(_.getVertexLabel).mkString("{", ", ", "}")
    }, _.getEdgeAttributes)

private def buildGraphGroupedXCycles(board: Board[Cell], candidate: SudokuNumber): Graph[Node, StrengthEdge[Node]] =
  // Connect cells.
  val cellEdges = for
    unit <- board.units
    withCandidate = unit.collect { case cell: UnsolvedCell if cell.candidates.contains(candidate) => cell }
    strength = if withCandidate.size == 2 then Strength.STRONG else Strength.WEAK
    (a, b) <- withCandidate.zipEveryPair
  yield StrengthEdge(a, b, strength)

  // Add groups.
  def createGroups[G <: Group](units: IndexedSeq[Seq[Cell]], groupConstructor: Set[UnsolvedCell] => G) =
    units.flatMap { unit =>
      unit.collect { case cell: UnsolvedCell if cell.candidates.contains(candidate) => cell }
        .groupBy(_.block)
        .values
        .filter(_.size >= 2)
        .map(cells => groupConstructor(cells.toSet))
    }

  val rowGroups = createGroups(board.rows, RowGroup(_))
  val columnGroups = createGroups(board.columns, ColumnGroup(_))
  val groups = rowGroups ++ columnGroups

  // Connect groups to cells.
  def connectGroupsToCells[G <: Group](groups: Seq[G], getUnit: Int => Seq[Cell], getUnitIndex: G => Int) =
    groups.flatMap { group =>
      val otherCellsInUnit = getUnit(getUnitIndex(group)).collect { case cell: UnsolvedCell
        if cell.candidates.contains(candidate) && !group.cells.contains(cell) => cell
      }
      val strength = if otherCellsInUnit.size == 1 then Strength.STRONG else Strength.WEAK
      otherCellsInUnit.map(cell => StrengthEdge[Node](group, cell, strength))
    }

  val rowGroupsAndCellEdges = connectGroupsToCells(rowGroups, board.getRow, _.row)
  val columnGroupsAndCellEdges = connectGroupsToCells(columnGroups, board.getColumn, _.column)
  val groupsAndCellEdges = connectGroupsToCells(groups, board.getBlock, _.block)

  // Connect groups to groups.
  def connectGroupsToGroups[G <: Group](groups: IndexedSeq[G], getUnit: Int => Seq[Cell], getUnitIndex: G => Int) =
    for
      (a, b) <- groups.zipEveryPair
      if getUnitIndex(a) == getUnitIndex(b) && (a.cells & b.cells).isEmpty
    yield
      val otherCellsInUnit = getUnit(getUnitIndex(a)).collect { case cell: UnsolvedCell
        if cell.candidates.contains(candidate) && !a.cells.contains(cell) && !b.cells.contains(cell) => cell
      }
      val strength = if otherCellsInUnit.isEmpty then Strength.STRONG else Strength.WEAK
      StrengthEdge(a, b, strength)

  val rowGroupsEdges = connectGroupsToGroups(rowGroups, board.getRow, _.row)
  val columnGroupsEdges = connectGroupsToGroups(columnGroups, board.getColumn, _.column)
  val groupsEdges = connectGroupsToGroups(groups, board.getBlock, _.block)

  val edges = cellEdges ++
    rowGroupsAndCellEdges ++
    columnGroupsAndCellEdges ++
    groupsAndCellEdges ++
    rowGroupsEdges ++
    columnGroupsEdges ++
    groupsEdges
  Graph.from(edges)

type Node = UnsolvedCell | Group

sealed trait Group(val cells: Set[UnsolvedCell]):
  require(
    2 to UnitSizeSquareRoot contains cells.size,
    s"Group can only be constructed with 2 or $UnitSizeSquareRoot cells, but cells.size is ${cells.size}."
  )
  require(cells.map(_.block).size == 1, "Group cells must be in the same block.")
  val block: Int = cells.head.block

class RowGroup(cells: Set[UnsolvedCell]) extends Group(cells):
  require(cells.map(_.row).size == 1, "RowGroup cells must be in the same row.")
  val row: Int = cells.head.row

class ColumnGroup(cells: Set[UnsolvedCell]) extends Group(cells):
  require(cells.map(_.column).size == 1, "ColumnGroup cells must be in the same column.")
  val column: Int = cells.head.column

extension (node: Node)
  private def row: Option[Int] = node match
    case UnsolvedCell(row, _, _) => Some(row)
    case node: RowGroup => Some(node.row)
    case node: ColumnGroup => None

  private def column: Option[Int] = node match
    case UnsolvedCell(_, column, _) => Some(column)
    case node: RowGroup => None
    case node: ColumnGroup => Some(node.column)

  private def block: Int = node match
    case node: UnsolvedCell => node.block
    case node: Group => node.block

  private def cells: Set[UnsolvedCell] = node match
    case node: UnsolvedCell => Set(node)
    case node: Group => node.cells