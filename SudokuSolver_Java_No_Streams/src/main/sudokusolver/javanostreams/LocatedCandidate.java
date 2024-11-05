package sudokusolver.javanostreams;

import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.DefaultAttribute;

import java.util.Map;

public record LocatedCandidate(UnsolvedCell cell, SudokuNumber candidate) {
    public Map<String, Attribute> getVertexAttributes() {
        return Map.of("label", DefaultAttribute.createAttribute(cell.getVertexLabel() + " : " + candidate));
    }
}