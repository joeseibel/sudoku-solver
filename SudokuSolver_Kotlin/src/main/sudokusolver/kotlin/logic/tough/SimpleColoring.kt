package sudokusolver.kotlin.logic.tough

import org.jgrapht.Graph
import org.jgrapht.alg.connectivity.BiconnectivityInspector
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.SimpleGraph
import org.jgrapht.graph.builder.GraphBuilder
import org.jgrapht.nio.dot.DOTExporter
import sudokusolver.kotlin.Board
import sudokusolver.kotlin.Cell
import sudokusolver.kotlin.RemoveCandidates
import sudokusolver.kotlin.SudokuNumber
import sudokusolver.kotlin.UnsolvedCell
import sudokusolver.kotlin.colorToLists
import sudokusolver.kotlin.colorToMap
import sudokusolver.kotlin.mergeToRemoveCandidates
import sudokusolver.kotlin.zipEveryPair
import java.io.StringWriter

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
fun simpleColoringRule2(board: Board<Cell>): List<RemoveCandidates> =
    SudokuNumber.values().flatMap { candidate ->
        createConnectedComponents(board, candidate).mapNotNull { graph ->
            val colors = graph.colorToMap()
            graph.vertexSet()
                .toList()
                .zipEveryPair()
                .find { (a, b) -> colors[a] == colors[b] && a isInSameUnit b }
                ?.let { (a, _) -> colors[a] }
                ?.let { colorToRemove ->
                    graph.vertexSet().filter { colors[it] == colorToRemove }.map { it to candidate }
                }
        }.flatten()
    }.mergeToRemoveCandidates()

/*
 * Rule 4: Two colors 'elsewhere'
 *
 * If an unsolved cell with a given candidate is outside the chain, and it is in the same units as two differently
 * colored vertices, then one of those two vertices must be the solution for the candidate. The candidate can be removed
 * from the cell outside the chain.
 */
fun simpleColoringRule4(board: Board<Cell>): List<RemoveCandidates> =
    SudokuNumber.values().flatMap { candidate ->
        createConnectedComponents(board, candidate).flatMap { graph ->
            val (colorOne, colorTwo) = graph.colorToLists()
            board.cells
                .filterIsInstance<UnsolvedCell>()
                .filter { cell ->
                    candidate in cell.candidates &&
                            cell !in graph.vertexSet() &&
                            colorOne.any(cell::isInSameUnit) &&
                            colorTwo.any(cell::isInSameUnit)
                }
                .map { it to candidate }
        }
    }.mergeToRemoveCandidates()

fun Graph<UnsolvedCell, DefaultEdge>.toDOT(candidate: SudokuNumber): String {
    val writer = StringWriter()
    DOTExporter<UnsolvedCell, DefaultEdge>().apply {
        setGraphIdProvider(candidate::toString)
        setVertexAttributeProvider { it.vertexAttributes }
    }.exportGraph(this, writer)
    return writer.toString()
}

private fun createConnectedComponents(
    board: Board<Cell>,
    candidate: SudokuNumber
): Set<Graph<UnsolvedCell, DefaultEdge>> =
    board.units
        .map { unit -> unit.filterIsInstance<UnsolvedCell>().filter { candidate in it.candidates } }
        .filter { withCandidate -> withCandidate.size == 2 }
        .fold(GraphBuilder(SimpleGraph<UnsolvedCell, DefaultEdge>(DefaultEdge::class.java))) { builder, (a, b) ->
            builder.addEdge(a, b)
        }
        .buildAsUnmodifiable()
        .let { graph -> BiconnectivityInspector(graph).connectedComponents }