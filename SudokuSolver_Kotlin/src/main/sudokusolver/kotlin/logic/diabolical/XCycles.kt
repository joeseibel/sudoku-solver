package sudokusolver.kotlin.logic.diabolical

import org.jgrapht.Graph
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
import sudokusolver.kotlin.SudokuNumber
import sudokusolver.kotlin.UnsolvedCell
import sudokusolver.kotlin.mergeToRemoveCandidates
import sudokusolver.kotlin.toSimpleString
import sudokusolver.kotlin.zipEveryPair
import java.io.StringWriter

class XCyclesEdge(val isStrong: Boolean)

fun <V : Cell> Graph<V, XCyclesEdge>.toDOT(candidate: SudokuNumber): String {
    val writer = StringWriter()
    DOTExporter<V, XCyclesEdge>(
        IntegerComponentNameProvider(),
        ComponentNameProvider { "[${it.row},${it.column}]" },
        null,
        null,
        ComponentAttributeProvider {
            it.takeUnless { it.isStrong }?.let { mapOf("style" to DefaultAttribute.createAttribute("dashed")) }
        },
        ComponentNameProvider { candidate.toString() }
    ).exportGraph(this, writer)
    return writer.toString()
}

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
 * will be necessary. Such a test case will case a NotImplementedError to be thrown.
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
                    throw NotImplementedError("Need to find all cycles for board: ${board.toSimpleString()}")
                }
                graph.edgeSet().filter { !it.isStrong }.flatMap { edge ->
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

private fun createStrongLinks(board: Board<Cell>, candidate: SudokuNumber): Graph<UnsolvedCell, XCyclesEdge> =
    board.units
        .map { unit -> unit.filterIsInstance<UnsolvedCell>().filter { candidate in it.candidates } }
        .filter { withCandidate -> withCandidate.size == 2 }
        .fold(GraphBuilder(SimpleGraph<UnsolvedCell, XCyclesEdge>(XCyclesEdge::class.java))) { builder, (a, b) ->
            builder.addEdge(a, b, XCyclesEdge(true))
        }
        .buildAsUnmodifiable()

private fun Graph<UnsolvedCell, XCyclesEdge>.addWeakLinks(): Graph<UnsolvedCell, XCyclesEdge> =
    vertexSet()
        .toList()
        .zipEveryPair()
        .filter { (a, b) -> a isInSameUnit b && !containsEdge(a, b) }
        .fold(
            GraphBuilder(SimpleGraph<UnsolvedCell, XCyclesEdge>(XCyclesEdge::class.java)).addGraph(this)
        ) { builder, (a, b) ->
            builder.addEdge(a, b, XCyclesEdge(false))
        }
        .buildAsUnmodifiable()

/*
 * Continuously trims the graph of vertices that cannot be part of a cycle for rule 1. The returned graph will either be
 * empty or only contain vertices with a degree of two or more and be connected by at least one strong link and one weak
 * link.
 */
private fun Graph<UnsolvedCell, XCyclesEdge>.trim(): Graph<UnsolvedCell, XCyclesEdge> {
    val graph = GraphBuilder(SimpleGraph<UnsolvedCell, XCyclesEdge>(XCyclesEdge::class.java))
        .addGraph(this).build()

    tailrec fun trimHelper() {
        val toRemove = graph.vertexSet().filter { vertex ->
            graph.edgesOf(vertex).map { it.isStrong }.toSet().size != 2
        }
        if (toRemove.isNotEmpty()) {
            graph.removeAllVertices(toRemove)
            trimHelper()
        }
    }

    trimHelper()
    return AsUnmodifiableGraph(graph)
}