package sudokusolver.kotlin.logic.diabolical

import org.jgrapht.Graph
import org.jgrapht.Graphs
import org.jgrapht.graph.SimpleGraph
import org.jgrapht.graph.builder.GraphBuilder
import org.jgrapht.nio.dot.DOTExporter
import sudokusolver.kotlin.Board
import sudokusolver.kotlin.Cell
import sudokusolver.kotlin.LocatedCandidate
import sudokusolver.kotlin.RemoveCandidates
import sudokusolver.kotlin.Strength
import sudokusolver.kotlin.StrengthEdge
import sudokusolver.kotlin.UnsolvedCell
import sudokusolver.kotlin.mergeToRemoveCandidates
import sudokusolver.kotlin.vertexAttributes
import sudokusolver.kotlin.zipEveryPair
import java.io.StringWriter

/*
 * https://www.sudokuwiki.org/XY_Chains
 *
 * XY-Chains is based on a graph type which shares many similarities to X-Cycles. Unlike X-Cycles, an XY-Chains graph
 * includes multiple candidates. This results in a single XY-Chains graph per board whereas there can be up to nine
 * X-Cycles graphs per board, one for each candidate. Each vertex in an XY-Chains graph is a particular candidate in a
 * cell and the edges are either strong or weak links. A strong link connects two candidates of a single cell when they
 * are the only candidates of that cell. A weak link connects two vertices which have the same candidate, are in
 * different cells, but are in the same unit. An XY-Chain is a chain between two vertices of the graph that have the
 * same candidate, the edges of the chain alternate between strong and weak links, and the last links on either end of
 * the chain are strong. If one vertex of a link is the solution, then the other vertex must not be the solution. If one
 * vertex of a strong link is not the solution, then the other vertex must be the solution. When there is a proper chain
 * in the graph it means that one of the two end points must be the solution. The candidate can be removed from any cell
 * of the board which is not an end point of the chain and that cell can see both end points.
 *
 * Note that this implementation of XY-Chains can handle cases in which the chain is not strictly alternating between
 * strong and weak links. It is tolerant of cases in which a strong link takes the place of a weak link.
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

fun Graph<LocatedCandidate, StrengthEdge>.toDOT(): String {
    val writer = StringWriter()
    DOTExporter<LocatedCandidate, StrengthEdge>().apply {
        setVertexAttributeProvider { it.vertexAttributes }
        setEdgeAttributeProvider { it.edgeAttributes }
    }.exportGraph(this, writer)
    return writer.toString()
}

private fun createStrongLinks(board: Board<Cell>): Graph<LocatedCandidate, StrengthEdge> =
    board.cells
        .filterIsInstance<UnsolvedCell>()
        .filter { it.candidates.size == 2 }
        .fold(GraphBuilder(SimpleGraph<LocatedCandidate, StrengthEdge>(StrengthEdge::class.java))) { builder, cell ->
            val source = cell to cell.candidates.first()
            val target = cell to cell.candidates.last()
            builder.addEdge(source, target, StrengthEdge(Strength.STRONG))
        }
        .buildAsUnmodifiable()

private fun Graph<LocatedCandidate, StrengthEdge>.addWeakLinks(): Graph<LocatedCandidate, StrengthEdge> =
    vertexSet().toList()
        .zipEveryPair()
        .filter { (vertexA, vertexB) ->
            val (cellA, candidateA) = vertexA
            val (cellB, candidateB) = vertexB
            candidateA == candidateB && cellA isInSameUnit cellB
        }
        .fold(
            GraphBuilder(SimpleGraph<LocatedCandidate, StrengthEdge>(StrengthEdge::class.java))
                .addGraph(this)
        ) { builder, (vertexA, vertexB) ->
            builder.addEdge(vertexA, vertexB, StrengthEdge(Strength.WEAK))
        }
        .buildAsUnmodifiable()

private fun alternatingPathExists(
    graph: Graph<LocatedCandidate, StrengthEdge>,
    start: LocatedCandidate,
    end: LocatedCandidate
): Boolean {

    fun alternatingPathExists(
        currentVertex: LocatedCandidate,
        nextType: Strength,
        visited: Set<LocatedCandidate>
    ): Boolean {
        val nextVertices = graph.edgesOf(currentVertex)
            .filter { it.strength.isCompatibleWith(nextType) }
            .map { Graphs.getOppositeVertex(graph, it, currentVertex) }
        return nextType == Strength.STRONG && end in nextVertices || (nextVertices - visited - end).any { nextVertex ->
            alternatingPathExists(nextVertex, nextType.opposite, visited + setOf(nextVertex))
        }
    }

    return alternatingPathExists(start, Strength.STRONG, setOf(start))
}