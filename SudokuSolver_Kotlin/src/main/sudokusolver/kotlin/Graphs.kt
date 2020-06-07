package sudokusolver.kotlin

import org.jgrapht.Graph
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