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
    return breadthFirst.asSequence().associateWith { cell ->
        if (breadthFirst.getDepth(cell) % 2 == 0) VertexColor.COLOR_ONE else VertexColor.COLOR_TWO
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
    },

    WEAK {
        override val opposite: Strength
            get() = STRONG
    };

    abstract val opposite: Strength
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
            edges.none { it.strength == Strength.STRONG } || edges.none { it.strength == Strength.WEAK }
        }
        if (toRemove.isNotEmpty()) {
            graph.removeAllVertices(toRemove)
            trimHelper()
        }
    }

    trimHelper()
    return AsUnmodifiableGraph(graph)
}

fun <V> alternatingCycleExists(graph: Graph<V, StrengthEdge>, vertex: V, adjacentEdgesType: Strength): Boolean =
    graph.edgesOf(vertex).filter { it.strength == adjacentEdgesType }.zipEveryPair().any { (edgeA, edgeB) ->
        val start = Graphs.getOppositeVertex(graph, edgeA, vertex)
        val end = Graphs.getOppositeVertex(graph, edgeB, vertex)

        fun alternatingCycleExists(currentVertex: V, nextType: Strength, visited: Set<V>): Boolean {
            val nextVertices = graph.edgesOf(currentVertex)
                .filter { it.strength == nextType }
                .map { Graphs.getOppositeVertex(graph, it, currentVertex) }
            return adjacentEdgesType.opposite == nextType && end in nextVertices ||
                    (nextVertices - visited - end).any { nextVertex ->
                        alternatingCycleExists(nextVertex, nextType.opposite, visited + setOf(nextVertex))
                    }
        }

        alternatingCycleExists(start, adjacentEdgesType.opposite, setOf(vertex, start))
    }