package sudokusolver.kotlin.logic.diabolical

import org.jgrapht.Graph
import org.jgrapht.Graphs
import org.jgrapht.alg.connectivity.ConnectivityInspector
import org.jgrapht.graph.AsUnmodifiableGraph
import org.jgrapht.graph.SimpleGraph
import org.jgrapht.graph.builder.GraphBuilder
import org.jgrapht.io.ComponentAttributeProvider
import org.jgrapht.io.ComponentNameProvider
import org.jgrapht.io.DOTExporter
import org.jgrapht.io.DefaultAttribute
import org.jgrapht.io.IntegerComponentNameProvider
import sudokusolver.kotlin.Board
import sudokusolver.kotlin.Cell
import sudokusolver.kotlin.RemoveCandidates
import sudokusolver.kotlin.SetValue
import sudokusolver.kotlin.SudokuNumber
import sudokusolver.kotlin.UnsolvedCell
import sudokusolver.kotlin.mergeToRemoveCandidates
import sudokusolver.kotlin.toSimpleString
import sudokusolver.kotlin.zipEveryPair
import java.io.StringWriter

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
 * Rule 1:
 *
 * If an X-Cycle has an even number of vertices and therefore continuously alternates between strong and weak, then the
 * graph is perfect and has no flaws. Each of the weak links can be treated as a strong link. The candidate can be
 * removed from any cell outside of the graph, but in the same unit as a weak link.
 *
 * For each candidate
 *   For each unit
 *     If the candidate appears in two unsolved cells of the unit
 *       Create a strong edge between the two cells
 *   For each pair of vertices
 *     If there is not an edge between the vertices and they can see each other
 *       Create a weak edge between the pair of vertices
 *   For each cycle in the graph
 *     If the cycle has an even number of vertices
 *       If the edges of the cycle alternate between strong and weak
 *         For each weak edge in the cycle
 *           If the edge connects two cells of the same row
 *             For each unsolved cell of that row
 *               If the cell has the candidate and is not a vertex
 *                 Remove the candidate from the cell
 *           If the edge connects two cells of the same column
 *             For each unsolved cell of that column
 *               If the cell has the candidate and is not a vertex
 *                 Remove the candidate from the cell
 *           If the edge connects two cells of the same block
 *             For each unsolved cell of that block
 *               If the cell has the candidate and is not a vertex
 *                 Remove the candidate from the cell
 *
 * Note that the current implementation does not iterate through the cycles of the graph. It simply trims the graph of
 * vertices that can't be a part of an alternating cycle. This works because the test case for rule 1 doesn't contain
 * any trimmed graphs with multiple cycles. I'm waiting to encounter a test case in which iterating through the cycles
 * will be necessary. Such a test case will cause a NotImplementedError to be thrown.
 */
fun xCyclesRule1(board: Board<Cell>): List<RemoveCandidates> =
    SudokuNumber.values().mapNotNull { candidate ->
        createStrongLinks(board, candidate)
            .addWeakLinks()
            .trim()
            .takeIf { it.vertexSet().isNotEmpty() }
            ?.let { graph ->
                if (!ConnectivityInspector(graph).isConnected) {
                    throw NotImplementedError("Need to split graph for board: ${board.toSimpleString()}")
                }
                if (graph.vertexSet().any { graph.degreeOf(it) > 2 }) {
                    /*
                     * Read about undirected cycles here:
                     * https://stackoverflow.com/questions/12367801/finding-all-cycles-in-undirected-graphs/18388696
                     * https://www.codeproject.com/Articles/1158232/Enumerating-All-Cycles-in-an-Undirected-Graph
                     * https://jgrapht.org/javadoc/org/jgrapht/alg/cycle/PatonCycleBase.html
                     */
                    throw NotImplementedError("Need to find all cycles for board: ${board.toSimpleString()}")
                }
                graph.edgeSet().filter { it.type == EdgeType.WEAK }.flatMap { edge ->
                    val source = graph.getEdgeSource(edge)
                    val target = graph.getEdgeTarget(edge)

                    fun <T> removeFromUnit(getUnitIndex: (Cell) -> T, getUnit: (T) -> List<Cell>) =
                        if (getUnitIndex(source) == getUnitIndex(target)) {
                            getUnit(getUnitIndex(source))
                                .filterIsInstance<UnsolvedCell>()
                                .filter { candidate in it.candidates && it !in graph.vertexSet() }
                                .map { it to candidate }
                        } else {
                            emptyList()
                        }

                    val rowRemovals = removeFromUnit(Cell::row, board::getRow)
                    val columnRemovals = removeFromUnit(Cell::column, board::getColumn)
                    val blockRemovals = removeFromUnit(Cell::block, board::getBlock)
                    rowRemovals + columnRemovals + blockRemovals
                }
            }
    }.flatten().mergeToRemoveCandidates()

/*
 * Rule 2:
 *
 * If an X-Cycle has an odd number of vertices and the edges alternate between strong and weak, except for one vertex
 * which is connected by two strong links, then the graph is a contradiction. Removing the candidate from the vertex of
 * interest implies that the candidate must be the solution for that vertex, thus causing the cycle to contradict
 * itself. However, considering the candidate to be the solution for that vertex does not cause any contradiction in the
 * cycle. Therefore, the candidate must be the solution for that vertex.
 *
 * For each candidate
 *   For each unit
 *     If the candidate appears in two unsolved cells of the unit
 *       Create a strong edge between the two cells
 *   For each pair of vertices
 *     If there is not an edge between the vertices and they can see each other
 *       Create a weak edge between the pair of vertices
 *   For each cycle in the graph
 *     If the cycle has an odd number of vertices
 *       If there is exactly one vertex connected by two strong edges
 *         If the other edges of the cycle alternate between strong and weak
 *           Set the candidate as the value for the vertex
 */
fun xCyclesRule2(board: Board<Cell>): List<SetValue> =
    SudokuNumber.values().flatMap { candidate ->
        val graph = createStrongLinks(board, candidate).addWeakLinks()
        graph.vertexSet()
            .filter { vertex -> alternatingPathExists(graph, vertex, EdgeType.STRONG) }
            .map { SetValue(it, candidate) }
    }

/*
 * Rule 3:
 *
 * If an X-Cycle has an odd number of vertices and the edges alternate between strong and weak, except for one vertex
 * which is connected by two weak links, then the graph is a contradiction. Considering the candidate to be the solution
 * for the vertex of interest implies that the candidate must be removed from that vertex, thus causing the cycle to
 * contradict itself. However, removing the candidate from that vertex does not cause any contradiction in the cycle.
 * Therefore, the candidate can be removed from the vertex.
 *
 * For each candidate
 *   For each unit
 *     If the candidate appears in two unsolved cells of the unit
 *       Create a strong edge between the two cells
 *     If the candidate appears in more than two unsolved cells of the unit
 *       For each pair of unsolved cells with the candidate in the unit
 *         Create a weak edge between the pair of cells
 *   For each cycle in the graph
 *     If the cycle has an odd number of vertices
 *       If there is exactly one vertex connected by two weak edges
 *         If the other edges of the cycle alternate between strong and weak
 *           Remove the candidate from the vertex
 */
fun xCyclesRule3(board: Board<Cell>): List<RemoveCandidates> =
    SudokuNumber.values().flatMap { candidate ->
        val graph = createStrongLinks(board, candidate).addWeakLinks().additionalWeakLinks(board, candidate)
        graph.vertexSet()
            .filter { vertex -> alternatingPathExists(graph, vertex, EdgeType.WEAK) }
            .map { it to candidate }
    }.mergeToRemoveCandidates()

enum class EdgeType {
    STRONG {
        override val opposite: EdgeType
            get() = WEAK
    },

    WEAK {
        override val opposite: EdgeType
            get() = STRONG
    };

    abstract val opposite: EdgeType
}

class XCyclesEdge(val type: EdgeType)

fun <V : Cell> Graph<V, XCyclesEdge>.toDOT(candidate: SudokuNumber): String {
    val writer = StringWriter()
    DOTExporter<V, XCyclesEdge>(
        IntegerComponentNameProvider(),
        ComponentNameProvider { "[${it.row},${it.column}]" },
        null,
        null,
        ComponentAttributeProvider {
            it.takeIf { it.type == EdgeType.WEAK }
                ?.let { mapOf("style" to DefaultAttribute.createAttribute("dashed")) }
        },
        ComponentNameProvider { candidate.toString() }
    ).exportGraph(this, writer)
    return writer.toString()
}

private fun createStrongLinks(board: Board<Cell>, candidate: SudokuNumber): Graph<UnsolvedCell, XCyclesEdge> =
    board.units
        .map { unit -> unit.filterIsInstance<UnsolvedCell>().filter { candidate in it.candidates } }
        .filter { withCandidate -> withCandidate.size == 2 }
        .fold(GraphBuilder(SimpleGraph<UnsolvedCell, XCyclesEdge>(XCyclesEdge::class.java))) { builder, (a, b) ->
            builder.addEdge(a, b, XCyclesEdge(EdgeType.STRONG))
        }
        .buildAsUnmodifiable()

private fun Graph<UnsolvedCell, XCyclesEdge>.addWeakLinks(): Graph<UnsolvedCell, XCyclesEdge> =
    vertexSet().toList()
        .zipEveryPair()
        .filter { (a, b) -> a isInSameUnit b && !containsEdge(a, b) }
        .fold(
            GraphBuilder(SimpleGraph<UnsolvedCell, XCyclesEdge>(XCyclesEdge::class.java)).addGraph(this)
        ) { builder, (a, b) ->
            builder.addEdge(a, b, XCyclesEdge(EdgeType.WEAK))
        }
        .buildAsUnmodifiable()

private fun Graph<UnsolvedCell, XCyclesEdge>.additionalWeakLinks(
    board: Board<Cell>,
    candidate: SudokuNumber
): Graph<UnsolvedCell, XCyclesEdge> =
    board.cells
        .filterIsInstance<UnsolvedCell>()
        .filter { candidate in it.candidates && it !in vertexSet() }
        .fold(
            GraphBuilder(SimpleGraph<UnsolvedCell, XCyclesEdge>(XCyclesEdge::class.java)).addGraph(this)
        ) { outerBuilder, cell ->
            vertexSet()
                .filter { it isInSameUnit cell }
                .fold(outerBuilder) { builder, vertex -> builder.addEdge(vertex, cell, XCyclesEdge(EdgeType.WEAK)) }
        }
        .buildAsUnmodifiable()

/*
 * Continuously trims the graph of vertices that cannot be part of a cycle for rule 1. The returned graph will either be
 * empty or only contain vertices with a degree of two or more and be connected by at least one strong link and one weak
 * link.
 */
private fun <V> Graph<V, XCyclesEdge>.trim(): Graph<V, XCyclesEdge> {
    val graph = GraphBuilder(SimpleGraph<V, XCyclesEdge>(XCyclesEdge::class.java)).addGraph(this).build()

    tailrec fun trimHelper() {
        val toRemove = graph.vertexSet().filter { vertex -> graph.edgesOf(vertex).map { it.type }.toSet().size != 2 }
        if (toRemove.isNotEmpty()) {
            graph.removeAllVertices(toRemove)
            trimHelper()
        }
    }

    trimHelper()
    return AsUnmodifiableGraph(graph)
}

private fun <V> alternatingPathExists(graph: Graph<V, XCyclesEdge>, vertex: V, adjacentEdgesType: EdgeType): Boolean =
    graph.edgesOf(vertex).filter { it.type == adjacentEdgesType }.zipEveryPair().any { (edgeA, edgeB) ->
        val start = Graphs.getOppositeVertex(graph, edgeA, vertex)
        val end = Graphs.getOppositeVertex(graph, edgeB, vertex)

        fun alternatingPathExists(currentVertex: V, nextType: EdgeType, visited: Set<V>): Boolean {
            val nextVertices = graph.edgesOf(currentVertex)
                .filter { it.type == nextType }
                .map { Graphs.getOppositeVertex(graph, it, currentVertex) }
                .let { it - visited }
            return end in nextVertices && adjacentEdgesType.opposite == nextType || nextVertices.any { nextVertex ->
                alternatingPathExists(nextVertex, nextType.opposite, visited + setOf(currentVertex))
            }
        }

        alternatingPathExists(start, adjacentEdgesType.opposite, setOf(vertex, start))
    }