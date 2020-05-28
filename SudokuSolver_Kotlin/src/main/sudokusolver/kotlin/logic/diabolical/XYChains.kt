package sudokusolver.kotlin.logic.diabolical

import org.jgrapht.Graph
import org.jgrapht.Graphs
import org.jgrapht.graph.SimpleGraph
import org.jgrapht.graph.builder.GraphBuilder
import org.jgrapht.nio.DefaultAttribute
import org.jgrapht.nio.dot.DOTExporter
import sudokusolver.kotlin.Board
import sudokusolver.kotlin.Cell
import sudokusolver.kotlin.RemoveCandidates
import sudokusolver.kotlin.STRENGTH_EDGE_ATTRIBUTE_PROVIDER
import sudokusolver.kotlin.Strength
import sudokusolver.kotlin.StrengthEdge
import sudokusolver.kotlin.SudokuNumber
import sudokusolver.kotlin.UnsolvedCell
import sudokusolver.kotlin.mergeToRemoveCandidates
import sudokusolver.kotlin.zipEveryPair
import java.io.StringWriter

/*
 * https://www.sudokuwiki.org/XY_Chains
 *
 * XY-Chains is based on a graph type which shares many similarities to X-Cycles. Unlike X-Cycles, an XY-Chains graph
 * includes multiple candidates. This results in a single XY-Chains graph per board whereas there can be up to nine
 * X-Cycles graphs per board, one for each candidate. Each vertex in an XY-Chains graph is a particular candidate in a
 * cell and the edges are either strong or weak links. A strong link connects two candidates of a single cell when they
 * are the only candidates of that cell. A weal link connects two vertices which have the same candidate, are in
 * different cells, but are in the same unit. An XY-Chain is a chain between two vertices of the graph that have the
 * same candidate, the edges of the chain alternate between strong and weak links, and the last links on either end of
 * the chain are strong. If one vertex of a link is the solution, then the other vertex must not be the solution. If one
 * vertex of a strong link is not the solution, then the other vertex must be the solution. When there is a proper chain
 * in the graph it means that one of the two end points must be the solution. The candidate can be removed from any cell
 * of the board which is not an end point of the chain and that cell can see both end points.
 *
 * For each unsolved cell
 *   If the cell has two candidates
 *     Create a strong edge between the two candidates of the cell
 * For each pair of vertices
 *   If the vertices have the same candidate and they can see each other
 *     Create a weak edge between the pair of vertices
 * For each pair of vertices
 *   If the vertices have the same candidate
 *     If there is a chain between the two vertices
 *       If the chain starts and ends with strong edges
 *         If the other edges of the chain alternate between strong and weak
 *           For each unsolved cell
 *             If the cell has the same candidate as the end points of the chain
 *               If the cell is not either end point's cell
 *                 If the cell can see both end points
 *                   Remove the candidate from the cell
 */
fun xyChains(board: Board<Cell>): List<RemoveCandidates> {
    val graph = createStrongLinks(board).addWeakLinks()
    return graph.vertexSet().groupBy { (_, candidate) -> candidate }.flatMap { (candidate, vertices) ->
        vertices.zipEveryPair()
            .mapNotNull { (vertexA, vertexB) ->
                val (cellA, _) = vertexA
                val (cellB, _) = vertexB
                board.cells
                    .filterIsInstance<UnsolvedCell>()
                    .filter {
                        candidate in it.candidates &&
                                it != cellA &&
                                it != cellB &&
                                it isInSameUnit cellA &&
                                it isInSameUnit cellB
                    }
                    .takeIf { visibleCells ->
                        visibleCells.isNotEmpty() && alternatingPathExists(graph, vertexA, vertexB)
                    }
            }
            .flatten()
            .map { it to candidate }
    }.mergeToRemoveCandidates()
}

typealias XYChainsVertex = Pair<UnsolvedCell, SudokuNumber>

fun Graph<XYChainsVertex, StrengthEdge>.toDOT(): String {
    val writer = StringWriter()
    DOTExporter<XYChainsVertex, StrengthEdge>().apply {
        setVertexAttributeProvider { (cell, candidate) ->
            mapOf("label" to DefaultAttribute.createAttribute("[${cell.row},${cell.column}] : $candidate"))
        }
        setEdgeAttributeProvider(STRENGTH_EDGE_ATTRIBUTE_PROVIDER)
    }.exportGraph(this, writer)
    return writer.toString()
}

private fun createStrongLinks(board: Board<Cell>): Graph<XYChainsVertex, StrengthEdge> =
    board.cells
        .filterIsInstance<UnsolvedCell>()
        .filter { it.candidates.size == 2 }
        .fold(GraphBuilder(SimpleGraph<XYChainsVertex, StrengthEdge>(StrengthEdge::class.java))) { builder, cell ->
            val source = cell to cell.candidates.first()
            val target = cell to cell.candidates.last()
            builder.addEdge(source, target, StrengthEdge(Strength.STRONG))
        }
        .buildAsUnmodifiable()

private fun Graph<XYChainsVertex, StrengthEdge>.addWeakLinks(): Graph<XYChainsVertex, StrengthEdge> =
    vertexSet().toList()
        .zipEveryPair()
        .filter { (vertexA, vertexB) ->
            val (cellA, candidateA) = vertexA
            val (cellB, candidateB) = vertexB
            candidateA == candidateB && cellA isInSameUnit cellB
        }
        .fold(
            GraphBuilder(SimpleGraph<XYChainsVertex, StrengthEdge>(StrengthEdge::class.java)).addGraph(this)
        ) { builder, (vertexA, vertexB) ->
            builder.addEdge(vertexA, vertexB, StrengthEdge(Strength.WEAK))
        }
        .buildAsUnmodifiable()

private fun alternatingPathExists(
    graph: Graph<XYChainsVertex, StrengthEdge>,
    start: XYChainsVertex,
    end: XYChainsVertex
): Boolean {

    fun alternatingPathExists(
        currentVertex: XYChainsVertex,
        nextType: Strength,
        visited: Set<XYChainsVertex>
    ): Boolean {
        val nextVertices = graph.edgesOf(currentVertex)
            .filter { it.strength == nextType }
            .map { Graphs.getOppositeVertex(graph, it, currentVertex) }
            .let { it - visited }
        return end in nextVertices && nextType == Strength.STRONG || nextVertices.any { nextVertex ->
            alternatingPathExists(nextVertex, nextType.opposite, visited + setOf(currentVertex))
        }
    }

    return alternatingPathExists(start, Strength.STRONG, setOf(start))
}