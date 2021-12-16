package sudokusolver.kotlin.logic.extreme

import org.jgrapht.Graph
import org.jgrapht.Graphs
import org.jgrapht.graph.AsUnmodifiableGraph
import org.jgrapht.graph.SimpleGraph
import org.jgrapht.graph.builder.GraphBuilder
import sudokusolver.kotlin.Board
import sudokusolver.kotlin.Cell
import sudokusolver.kotlin.LocatedCandidate
import sudokusolver.kotlin.RemoveCandidates
import sudokusolver.kotlin.SetValue
import sudokusolver.kotlin.Strength
import sudokusolver.kotlin.StrengthEdge
import sudokusolver.kotlin.SudokuNumber
import sudokusolver.kotlin.UnsolvedCell
import sudokusolver.kotlin.alternatingCycleExists
import sudokusolver.kotlin.enumMinus
import sudokusolver.kotlin.mergeToRemoveCandidates
import sudokusolver.kotlin.zipEveryPair
import java.util.EnumSet

/*
 * https://www.sudokuwiki.org/Alternating_Inference_Chains
 *
 * Alternating Inference Chains are based on a graph type in which each vertex is a specific candidate in a cell and the
 * edges can either be strong or weak links. A strong link connects two vertices in a unit that share a candidate when
 * they are in the only unsolved cells in that unit with the candidate. A strong link also connects two vertices in a
 * single cell when they are the only two candidates in that cell. A weak link connects two vertices in a unit that
 * share a candidate when they are not the only unsolved cells in that unit with the candidate. A weak link also
 * connects two vertices in a single cell when there are more than two candidates in that cell. An Alternating Inference
 * Chain is a cycle in the graph in which the edges alternate between strong and weak links. If one vertex of a link is
 * the solution, then the other vertex must not be the solution. If one vertex of a strong link is not the solution,
 * then the other vertex must be the solution. Alternating Inference Chains are very similar to X-Cycles and Grouped
 * X-Cycles.
 *
 * Rule 1:
 *
 * If an Alternating Inference Chain has an even number of vertices and therefore continuously alternates between strong
 * and weak, then the graph is perfect and has no flaws. Each of the weak links can be treated as a strong link.
 * If a weak link connects a common candidate across two different cells, then that candidate can be removed from any
 * other cell which is in the same unit as the two vertices. If a weak link connects two candidates of the same cell,
 * then all other candidates can be removed from that cell.
 *
 * Note that this implementation of rule 1 can handle cases in which the chain is not strictly alternating between
 * strong and weak links. It is tolerant of cases in which a strong link takes the place of a weak link. In this way,
 * this implementation of rule 1 is different from the rule 1 implementation of X-Cycles and Grouped X-Cycles. This was
 * done because all the examples that I found contain weak links that are actually strong links. Note that even though
 * a strong link can take the place of a weak link, the opposite is not true. A weak link cannot take the place of a
 * strong link. Therefore, valid chains alternate between links that must be strong and links that can be weak.
 */
fun alternatingInferenceChainsRule1(board: Board<Cell>): List<RemoveCandidates> {
    val graph = buildGraph(board).trimAIC()
    return getWeakEdgesInAlternatingCycle(graph).flatMap { edge ->
        val (sourceCell, sourceCandidate) = graph.getEdgeSource(edge)
        val (targetCell, targetCandidate) = graph.getEdgeTarget(edge)

        if (sourceCell == targetCell) {
            (sourceCell.candidates enumMinus EnumSet.of(sourceCandidate, targetCandidate)).map { sourceCell to it }
        } else {

            fun removeFromUnit(sourceUnitIndex: Int, targetUnitIndex: Int, getUnit: (Int) -> List<Cell>) =
                sourceUnitIndex.takeIf { it == targetUnitIndex }
                    ?.let(getUnit)
                    ?.filterIsInstance<UnsolvedCell>()
                    ?.filter { sourceCandidate in it.candidates }
                    ?.let { it - sourceCell - targetCell }
                    ?.map { it to sourceCandidate }
                    ?: emptyList()

            val rowRemovals = removeFromUnit(sourceCell.row, targetCell.row, board::getRow)
            val columnRemovals = removeFromUnit(sourceCell.column, targetCell.column, board::getColumn)
            val blockRemovals = removeFromUnit(sourceCell.block, targetCell.block, board::getBlock)

            rowRemovals + columnRemovals + blockRemovals
        }
    }.mergeToRemoveCandidates()
}

/*
 * Rule 2:
 *
 * If an Alternating Inference Chain has an odd number of vertices and the edges alternate between strong and weak,
 * except for one vertex which is connected by two strong links, then the graph is a contradiction. Removing the
 * candidate from the cell of interest implies that the candidate must be the solution for that cell, thus causing the
 * cycle to contradict itself. However, considering the candidate to be the solution for that cell does not cause any
 * contradiction in the cycle. Therefore, the candidate must be the solution for that cell.
 */
fun alternatingInferenceChainsRule2(board: Board<Cell>): List<SetValue> {
    val graph = buildGraph(board)
    return graph.vertexSet()
        .filter { alternatingCycleExists(graph, it, Strength.STRONG) }
        .map { (cell, candidate) -> SetValue(cell, candidate) }
}

/*
 * Rule 3:
 *
 * If an Alternating Inference Chain has an odd number of vertices and the edges alternate between strong and weak,
 * except for one vertex which is connected by two weak links, then the graph is a contradiction. Considering the
 * candidate to be the solution for the cell of interest implies that the candidate must be removed from that cell, thus
 * causing the cycle to contradict itself. However, removing the candidate from that cell does not cause any
 * contradiction in the cycle. Therefore, the candidate can be removed from the cell.
 */
fun alternatingInferenceChainsRule3(board: Board<Cell>): List<RemoveCandidates> {
    val graph = buildGraph(board)
    return graph.vertexSet().filter { alternatingCycleExists(graph, it, Strength.WEAK) }.mergeToRemoveCandidates()
}

private fun buildGraph(board: Board<Cell>): Graph<LocatedCandidate, StrengthEdge> {
    val builder = GraphBuilder(SimpleGraph<LocatedCandidate, StrengthEdge>(StrengthEdge::class.java))

    //Connect cells.
    board.units.forEach { unit ->
        SudokuNumber.values().forEach { candidate ->
            val withCandidates = unit.filterIsInstance<UnsolvedCell>().filter { candidate in it.candidates }
            val strength = if (withCandidates.size == 2) Strength.STRONG else Strength.WEAK
            withCandidates.zipEveryPair().forEach { (a, b) ->
                builder.addEdge(a to candidate, b to candidate, StrengthEdge(strength))
            }
        }
    }

    //Connect candidates in cells.
    board.cells.filterIsInstance<UnsolvedCell>().forEach { cell ->
        val strength = if (cell.candidates.size == 2) Strength.STRONG else Strength.WEAK
        cell.candidates.toList().zipEveryPair().forEach { (a, b) ->
            builder.addEdge(cell to a, cell to b, StrengthEdge(strength))
        }
    }

    return builder.buildAsUnmodifiable()
}

/*
 * Continuously trims the graph of vertices that cannot be part of a cycle for rule 1. This is mostly similar to the
 * sudokusolver.kotlin.trim function which can be found in Graphs.kt. However, this version allows for cycles in which a
 * strong link takes the place of a weak link. The returned graph will either be empty or only contain vertices with a
 * degree of two or more and be connected by at least one strong link.
 */
private fun Graph<LocatedCandidate, StrengthEdge>.trimAIC(): Graph<LocatedCandidate, StrengthEdge> {
    val graph = GraphBuilder(SimpleGraph<LocatedCandidate, StrengthEdge>(StrengthEdge::class.java))
        .addGraph(this)
        .build()

    tailrec fun trimHelper() {
        val toRemove = graph.vertexSet().filter { vertex ->
            val edges = graph.edgesOf(vertex)
            edges.size < 2 || edges.none { it.strength == Strength.STRONG }
        }
        if (toRemove.isNotEmpty()) {
            graph.removeAllVertices(toRemove)
            trimHelper()
        }
    }

    trimHelper()
    return AsUnmodifiableGraph(graph)
}

private fun getWeakEdgesInAlternatingCycle(graph: Graph<LocatedCandidate, StrengthEdge>): Set<StrengthEdge> {
    val weakEdgesInAlternatingCycle = mutableSetOf<StrengthEdge>()
    graph.edgeSet().filter { it.strength == Strength.WEAK }.forEach { edge ->
        if (edge !in weakEdgesInAlternatingCycle) {
            weakEdgesInAlternatingCycle += getAlternatingCycleWeakEdges(graph, edge)
        }
    }
    return weakEdgesInAlternatingCycle
}

/*
 * This function is very similar to getAlternatingCycleWeakEdges found in GroupedXCycles.kt. However, this version
 * allows for cycles in which a strong link takes the place of a weak link.
 */
private fun getAlternatingCycleWeakEdges(
    graph: Graph<LocatedCandidate, StrengthEdge>,
    startEdge: StrengthEdge
): List<StrengthEdge> {
    require(startEdge.strength == Strength.WEAK) { "startEdge must be weak." }
    val start = graph.getEdgeSource(startEdge)
    val end = graph.getEdgeTarget(startEdge)

    fun getAlternatingCycleWeakEdges(
        currentVertex: LocatedCandidate,
        nextType: Strength,
        visited: Set<LocatedCandidate>,
        weakEdges: List<StrengthEdge>
    ): List<StrengthEdge> {
        val nextEdgesAndVertices = graph.edgesOf(currentVertex)
            .filter { it.strength == Strength.STRONG || it.strength == nextType }
            .map { it to Graphs.getOppositeVertex(graph, it, currentVertex) }
        return if (nextType == Strength.STRONG && nextEdgesAndVertices.any { (_, nextVertex) -> nextVertex == end }) {
            weakEdges
        } else {
            nextEdgesAndVertices.asSequence()
                .filter { (_, nextVertex) -> nextVertex != end && nextVertex !in visited }
                .map { (nextEdge, nextVertex) ->
                    getAlternatingCycleWeakEdges(
                        nextVertex,
                        nextType.opposite,
                        visited + setOf(nextVertex),
                        if (nextEdge.strength == Strength.WEAK) weakEdges + nextEdge else weakEdges
                    )
                }
                .firstOrNull { it.isNotEmpty() }
                ?: emptyList()
        }
    }

    return getAlternatingCycleWeakEdges(start, Strength.STRONG, setOf(start), listOf(startEdge)).also { weakEdges ->
        assert(weakEdges.all { it.strength == Strength.WEAK }) { "There are strong edges in the return value." }
    }
}