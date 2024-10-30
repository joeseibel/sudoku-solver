package sudokusolver.kotlin

import org.jgrapht.Graph
import org.jgrapht.Graphs
import org.jgrapht.graph.AsUnmodifiableGraph
import org.jgrapht.graph.SimpleGraph
import org.jgrapht.graph.builder.GraphBuilder
import org.jgrapht.nio.Attribute
import org.jgrapht.nio.DefaultAttribute
import org.jgrapht.traverse.BreadthFirstIterator

enum class VertexColor {
    COLOR_ONE {
        override val opposite: VertexColor
            get() = COLOR_TWO
    },

    COLOR_TWO {
        override val opposite: VertexColor
            get() = COLOR_ONE
    };

    abstract val opposite: VertexColor
}

fun <V, E> Graph<V, E>.colorToMap(): Map<V, VertexColor> {
    val breadthFirst = BreadthFirstIterator(this)
    return breadthFirst.asSequence().associateWith { vertex ->
        if (breadthFirst.getDepth(vertex) % 2 == 0) VertexColor.COLOR_ONE else VertexColor.COLOR_TWO
    }
}

fun <V, E> Graph<V, E>.colorToLists(): Pair<List<V>, List<V>> {
    val breadthFirst = BreadthFirstIterator(this)
    return breadthFirst.asSequence().partition { breadthFirst.getDepth(it) % 2 == 0 }
}

class StrengthEdge(val strength: Strength)

enum class Strength {
    STRONG {
        override val opposite: Strength
            get() = WEAK

        override fun isCompatibleWith(requiredType: Strength): Boolean = true
    },

    WEAK {
        override val opposite: Strength
            get() = STRONG

        override fun isCompatibleWith(requiredType: Strength): Boolean = requiredType == WEAK
    };

    abstract val opposite: Strength

    /*
     * For solutions that look for alternating edge types in a graph, it can sometimes be the case that a strong link
     * can take the place of a weak link. In those cases, this method should be called instead of performing an equality
     * check.
     */
    abstract fun isCompatibleWith(requiredType: Strength): Boolean
}

val UNSOLVED_CELL_ATTRIBUTE_PROVIDER: (UnsolvedCell) -> Map<String, Attribute> = {
    mapOf("label" to DefaultAttribute.createAttribute("[${it.row},${it.column}]"))
}

val LOCATED_CANDIDATE_ATTRIBUTE_PROVIDER: (LocatedCandidate) -> Map<String, Attribute> = { (cell, candidate) ->
    mapOf("label" to DefaultAttribute.createAttribute("[${cell.row},${cell.column}] : $candidate"))
}

val STRENGTH_EDGE_ATTRIBUTE_PROVIDER: (StrengthEdge) -> Map<String, Attribute>? = {
    it.takeIf { it.strength == Strength.WEAK }
        ?.let { mapOf("style" to DefaultAttribute.createAttribute("dashed")) }
}

/*
 * Continuously trims the graph of vertices that cannot be part of a cycle for X-Cycles rule 1. The returned graph will
 * either be empty or only contain vertices with a degree of two or more and be connected by at least one strong link
 * and one weak link.
 */
fun <V> Graph<V, StrengthEdge>.trim(): Graph<V, StrengthEdge> {
    val graph = GraphBuilder(SimpleGraph<V, StrengthEdge>(StrengthEdge::class.java)).addGraph(this).build()

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

fun <V> getWeakEdgesInAlternatingCycle(graph: Graph<V, StrengthEdge>): Set<StrengthEdge> {
    val weakEdgesInAlternatingCycle = mutableSetOf<StrengthEdge>()
    graph.edgeSet().filter { it.strength == Strength.WEAK }.forEach { edge ->
        if (edge !in weakEdgesInAlternatingCycle) {
            weakEdgesInAlternatingCycle += getAlternatingCycleWeakEdges(graph, edge)
        }
    }
    return weakEdgesInAlternatingCycle
}

private fun <V> getAlternatingCycleWeakEdges(
    graph: Graph<V, StrengthEdge>,
    startEdge: StrengthEdge
): List<StrengthEdge> {
    require(startEdge.strength == Strength.WEAK) { "startEdge must be weak." }
    val start = graph.getEdgeSource(startEdge)
    val end = graph.getEdgeTarget(startEdge)

    fun getAlternatingCycleWeakEdges(
        currentVertex: V,
        nextType: Strength,
        visited: Set<V>,
        weakEdges: List<StrengthEdge>
    ): List<StrengthEdge> {
        val nextEdgesAndVertices = graph.edgesOf(currentVertex)
            .filter { it.strength.isCompatibleWith(nextType) }
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
        assert(weakEdges.none { it.strength == Strength.STRONG }) { "There are strong edges in the return value." }
    }
}

fun <V> alternatingCycleExists(graph: Graph<V, StrengthEdge>, vertex: V, adjacentEdgesType: Strength): Boolean =
    graph.edgesOf(vertex).filter { it.strength == adjacentEdgesType }.zipEveryPair().any { (edgeA, edgeB) ->
        val start = Graphs.getOppositeVertex(graph, edgeA, vertex)
        val end = Graphs.getOppositeVertex(graph, edgeB, vertex)

        fun alternatingCycleExists(currentVertex: V, nextType: Strength, visited: Set<V>): Boolean {
            val nextVertices = graph.edgesOf(currentVertex)
                .filter { it.strength.isCompatibleWith(nextType) }
                .map { Graphs.getOppositeVertex(graph, it, currentVertex) }
            return adjacentEdgesType.opposite == nextType && end in nextVertices ||
                    (nextVertices - visited - end).any { nextVertex ->
                        alternatingCycleExists(nextVertex, nextType.opposite, visited + setOf(nextVertex))
                    }
        }

        alternatingCycleExists(start, adjacentEdgesType.opposite, setOf(vertex, start))
    }