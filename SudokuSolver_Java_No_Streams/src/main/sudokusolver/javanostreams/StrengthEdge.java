package sudokusolver.javanostreams;

import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.DefaultAttribute;

import java.util.Map;

@SuppressWarnings("ClassCanBeRecord")
public class StrengthEdge {
    private final Strength strength;

    public StrengthEdge(Strength strength) {
        this.strength = strength;
    }

    public Strength getStrength() {
        return strength;
    }

    public Map<String, Attribute> getEdgeAttributes() {
        return strength == Strength.WEAK ? Map.of("style", DefaultAttribute.createAttribute("dashed")) : null;
    }
}
