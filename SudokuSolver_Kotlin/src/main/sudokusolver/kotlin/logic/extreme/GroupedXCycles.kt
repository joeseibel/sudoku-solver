package sudokusolver.kotlin.logic.extreme

import org.jgrapht.Graph
import org.jgrapht.graph.SimpleGraph
import org.jgrapht.graph.builder.GraphBuilder
import org.jgrapht.nio.DefaultAttribute
import org.jgrapht.nio.dot.DOTExporter
import sudokusolver.kotlin.Board
import sudokusolver.kotlin.Cell
import sudokusolver.kotlin.RemoveCandidates
import sudokusolver.kotlin.SetValue
import sudokusolver.kotlin.Strength
import sudokusolver.kotlin.StrengthEdge
import sudokusolver.kotlin.SudokuNumber
import sudokusolver.kotlin.UNIT_SIZE_SQUARE_ROOT
import sudokusolver.kotlin.UnsolvedCell
import sudokusolver.kotlin.alternatingCycleExists
import sudokusolver.kotlin.getWeakEdgesInAlternatingCycle
import sudokusolver.kotlin.mergeToRemoveCandidates
import sudokusolver.kotlin.trim
import sudokusolver.kotlin.zipEveryPair
import java.io.StringWriter

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
fun groupedXCyclesRule1(board: Board<Cell>): List<RemoveCandidates> =
    SudokuNumber.entries.map { candidate ->
        val graph = buildGraph(board, candidate).trim()
        getWeakEdgesInAlternatingCycle(graph).flatMap { edge ->
            val source = graph.getEdgeSource(edge)
            val target = graph.getEdgeTarget(edge)

            fun removeFromUnit(sourceUnitIndex: Int?, targetUnitIndex: Int?, getUnit: (Int) -> List<Cell>) =
                sourceUnitIndex?.takeIf { it == targetUnitIndex }
                    ?.let(getUnit)
                    ?.filterIsInstance<UnsolvedCell>()
                    ?.filter { candidate in it.candidates }
                    ?.let { it - source.cells - target.cells }
                    ?.map { it to candidate }
                    ?: emptyList()

            val rowRemovals = removeFromUnit(source.row, target.row, board::getRow)
            val columnRemovals = removeFromUnit(source.column, target.column, board::getColumn)
            val blockRemovals = removeFromUnit(source.block, target.block, board::getBlock)
            rowRemovals + columnRemovals + blockRemovals
        }
    }.flatten().mergeToRemoveCandidates()

/*
 * Rule 2:
 *
 * If a Grouped X-Cycle has an odd number of vertices and the edges alternate between strong and weak, except for one
 * vertex which is a cell and is connected by two strong links, then the graph is a contradiction. Removing the
 * candidate from the vertex of interest implies that the candidate must be the solution for that vertex, thus causing
 * the cycle to contradict itself. However, considering the candidate to be the solution for that vertex does not cause
 * any contradiction in the cycle. Therefore, the candidate must be the solution for that vertex.
 */
fun groupedXCyclesRule2(board: Board<Cell>): List<SetValue> =
    SudokuNumber.entries.flatMap { candidate ->
        val graph = buildGraph(board, candidate)
        graph.vertexSet()
            .filterIsInstance<CellNode>()
            .filter { alternatingCycleExists(graph, it, Strength.STRONG) }
            .map { SetValue(it.cell, candidate) }
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
fun groupedXCyclesRule3(board: Board<Cell>): List<RemoveCandidates> =
    SudokuNumber.entries.flatMap { candidate ->
        val graph = buildGraph(board, candidate)
        graph.vertexSet()
            .filterIsInstance<CellNode>()
            .filter { alternatingCycleExists(graph, it, Strength.WEAK) }
            .map { it.cell to candidate }
    }.mergeToRemoveCandidates()

fun Graph<Node, StrengthEdge>.toDOT(candidate: SudokuNumber): String {
    val writer = StringWriter()
    DOTExporter<Node, StrengthEdge>().apply {
        setGraphIdProvider { candidate.toString() }
        setVertexAttributeProvider { mapOf("label" to DefaultAttribute.createAttribute(it.toString())) }
        setEdgeAttributeProvider { it.edgeAttributes }
    }.exportGraph(this, writer)
    return writer.toString()
}

private fun buildGraph(board: Board<Cell>, candidate: SudokuNumber): Graph<Node, StrengthEdge> {
    val builder = GraphBuilder(SimpleGraph<Node, StrengthEdge>(StrengthEdge::class.java))

    //Connect cells.
    board.units
        .map { unit -> unit.filterIsInstance<UnsolvedCell>().filter { candidate in it.candidates } }
        .forEach { withCandidate ->
            val strength = if (withCandidate.size == 2) Strength.STRONG else Strength.WEAK
            withCandidate.zipEveryPair().forEach { (a, b) ->
                builder.addEdge(CellNode(a), CellNode(b), StrengthEdge(strength))
            }
        }

    //Add groups.
    fun <G : Group> createGroups(units: List<List<Cell>>, groupConstructor: (Set<UnsolvedCell>) -> G) =
        units.flatMap { unit ->
            unit.filterIsInstance<UnsolvedCell>()
                .filter { candidate in it.candidates }
                .groupBy { it.block }
                .values
                .filter { it.size >= 2 }
                .map { groupConstructor(it.toSet()) }
        }

    val rowGroups = createGroups(board.rows, ::RowGroup)
    val columnGroups = createGroups(board.columns, ::ColumnGroup)
    val groups = rowGroups + columnGroups
    builder.addVertices(*groups.toTypedArray())

    //Connect groups to cells.
    fun <G : Group> connectGroupsToCells(groups: List<G>, getUnit: (Int) -> List<Cell>, getUnitIndex: (G) -> Int) {
        groups.forEach { group ->
            val otherCellsInUnit = getUnit(getUnitIndex(group))
                .filterIsInstance<UnsolvedCell>()
                .filter { candidate in it.candidates }
                .let { it - group.cells }
            val strength = if (otherCellsInUnit.size == 1) Strength.STRONG else Strength.WEAK
            otherCellsInUnit.forEach { cell -> builder.addEdge(group, CellNode(cell), StrengthEdge(strength)) }
        }
    }

    connectGroupsToCells(rowGroups, board::getRow, RowGroup::row)
    connectGroupsToCells(columnGroups, board::getColumn, ColumnGroup::column)
    connectGroupsToCells(groups, board::getBlock, Group::block)

    //Connect groups to groups.
    fun <G : Group> connectGroupsToGroups(groups: List<G>, getUnit: (Int) -> List<Cell>, getUnitIndex: (G) -> Int) {
        groups.zipEveryPair()
            .filter { (a, b) -> getUnitIndex(a) == getUnitIndex(b) && (a.cells intersect b.cells).isEmpty() }
            .forEach { (a, b) ->
                val otherCellsInUnit = getUnit(getUnitIndex(a))
                    .filterIsInstance<UnsolvedCell>()
                    .filter { candidate in it.candidates }
                    .let { it - a.cells - b.cells }
                val strength = if (otherCellsInUnit.isEmpty()) Strength.STRONG else Strength.WEAK
                builder.addEdge(a, b, StrengthEdge(strength))
            }
    }

    connectGroupsToGroups(rowGroups, board::getRow, RowGroup::row)
    connectGroupsToGroups(columnGroups, board::getColumn, ColumnGroup::column)
    connectGroupsToGroups(groups, board::getBlock, Group::block)

    return builder.buildAsUnmodifiable()
}

interface Node {
    val row: Int?
    val column: Int?
    val block: Int
    val cells: Set<UnsolvedCell>
}

data class CellNode(val cell: UnsolvedCell) : Node {
    override val row: Int = cell.row
    override val column: Int = cell.column
    override val block: Int = cell.block
    override val cells: Set<UnsolvedCell> by lazy { setOf(cell) }

    override fun toString(): String = cell.vertexLabel
}

abstract class Group(final override val cells: Set<UnsolvedCell>) : Node {
    init {
        require(cells.size in 2..UNIT_SIZE_SQUARE_ROOT) {
            "Group can only be constructed with 2 or $UNIT_SIZE_SQUARE_ROOT cells, but cells.size is ${cells.size}."
        }
        require(cells.map { it.block }.toSet().size == 1) { "Group cells must be in the same block." }
    }

    override val block: Int = cells.first().block

    override fun equals(other: Any?): Boolean = this === other || other is Group && cells == other.cells
    override fun hashCode(): Int = cells.hashCode()
    override fun toString(): String = cells.joinToString(prefix = "{", postfix = "}") { it.vertexLabel }
}

class RowGroup(cells: Set<UnsolvedCell>) : Group(cells) {
    override val row: Int = cells.first().row
    override val column: Int? = null

    init {
        require(cells.map { it.row }.toSet().size == 1) { "RowGroup cells must be in the same row." }
    }
}

class ColumnGroup(cells: Set<UnsolvedCell>) : Group(cells) {
    override val row: Int? = null
    override val column: Int = cells.first().column

    init {
        require(cells.map { it.column }.toSet().size == 1) { "ColumnGroup cells must be in the same column." }
    }
}