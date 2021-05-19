package sudokusolver.kotlin.logic.diabolical

import org.jgrapht.Graph
import org.jgrapht.alg.connectivity.ConnectivityInspector
import org.jgrapht.graph.SimpleGraph
import org.jgrapht.graph.builder.GraphBuilder
import org.jgrapht.nio.DefaultAttribute
import org.jgrapht.nio.dot.DOTExporter
import sudokusolver.kotlin.Board
import sudokusolver.kotlin.Cell
import sudokusolver.kotlin.RemoveCandidates
import sudokusolver.kotlin.STRENGTH_EDGE_ATTRIBUTE_PROVIDER
import sudokusolver.kotlin.SetValue
import sudokusolver.kotlin.Strength
import sudokusolver.kotlin.StrengthEdge
import sudokusolver.kotlin.SudokuNumber
import sudokusolver.kotlin.UnsolvedCell
import sudokusolver.kotlin.alternatingCycleExists
import sudokusolver.kotlin.mergeToRemoveCandidates
import sudokusolver.kotlin.toSimpleString
import sudokusolver.kotlin.trim
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
                graph.edgeSet().filter { it.strength == Strength.WEAK }.flatMap { edge ->
                    val source = graph.getEdgeSource(edge)
                    val target = graph.getEdgeTarget(edge)

                    fun removeFromUnit(getUnitIndex: (Cell) -> Int, getUnit: (Int) -> List<Cell>) =
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
            .filter { vertex -> alternatingCycleExists(graph, vertex, Strength.STRONG) }
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
            .filter { vertex -> alternatingCycleExists(graph, vertex, Strength.WEAK) }
            .map { it to candidate }
    }.mergeToRemoveCandidates()

fun <V : Cell> Graph<V, StrengthEdge>.toDOT(candidate: SudokuNumber): String {
    val writer = StringWriter()
    DOTExporter<V, StrengthEdge>().apply {
        setGraphIdProvider { candidate.toString() }
        setVertexAttributeProvider {
            mapOf("label" to DefaultAttribute.createAttribute("[${it.row},${it.column}]"))
        }
        setEdgeAttributeProvider(STRENGTH_EDGE_ATTRIBUTE_PROVIDER)
    }.exportGraph(this, writer)
    return writer.toString()
}

private fun createStrongLinks(board: Board<Cell>, candidate: SudokuNumber): Graph<UnsolvedCell, StrengthEdge> =
    board.units
        .map { unit -> unit.filterIsInstance<UnsolvedCell>().filter { candidate in it.candidates } }
        .filter { withCandidate -> withCandidate.size == 2 }
        .fold(GraphBuilder(SimpleGraph<UnsolvedCell, StrengthEdge>(StrengthEdge::class.java))) { builder, (a, b) ->
            builder.addEdge(a, b, StrengthEdge(Strength.STRONG))
        }
        .buildAsUnmodifiable()

private fun Graph<UnsolvedCell, StrengthEdge>.addWeakLinks(): Graph<UnsolvedCell, StrengthEdge> =
    vertexSet().toList()
        .zipEveryPair()
        .filter { (a, b) -> a isInSameUnit b && !containsEdge(a, b) }
        .fold(
            GraphBuilder(SimpleGraph<UnsolvedCell, StrengthEdge>(StrengthEdge::class.java)).addGraph(this)
        ) { builder, (a, b) ->
            builder.addEdge(a, b, StrengthEdge(Strength.WEAK))
        }
        .buildAsUnmodifiable()

private fun Graph<UnsolvedCell, StrengthEdge>.additionalWeakLinks(
    board: Board<Cell>,
    candidate: SudokuNumber
): Graph<UnsolvedCell, StrengthEdge> =
    board.cells
        .filterIsInstance<UnsolvedCell>()
        .filter { candidate in it.candidates && it !in vertexSet() }
        .fold(
            GraphBuilder(SimpleGraph<UnsolvedCell, StrengthEdge>(StrengthEdge::class.java)).addGraph(this)
        ) { outerBuilder, cell ->
            vertexSet()
                .filter { it isInSameUnit cell }
                .fold(outerBuilder) { builder, vertex -> builder.addEdge(vertex, cell, StrengthEdge(Strength.WEAK)) }
        }
        .buildAsUnmodifiable()