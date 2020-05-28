package sudokusolver.kotlin

import org.jgrapht.nio.Attribute
import org.jgrapht.nio.DefaultAttribute

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