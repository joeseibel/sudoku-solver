package sudokusolver.javanostreams;

import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.DefaultAttribute;

import java.util.Map;
import java.util.function.Function;

public record LocatedCandidate(UnsolvedCell cell, SudokuNumber candidate) {
    public static final Function<LocatedCandidate, Map<String, Attribute>> LOCATED_CANDIDATE_ATTRIBUTE_PROVIDER =
            locatedCandidate -> {
                var cell = locatedCandidate.cell();
                var candidate = locatedCandidate.candidate();
                var label = "[" + cell.row() + ',' + cell.column() + "] : " + candidate;
                return Map.of("label", DefaultAttribute.createAttribute(label));
            };
}