package sudokusolver.java;

import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.DefaultAttribute;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public record LocatedCandidate(UnsolvedCell cell, SudokuNumber candidate) {
    public Map<String, Attribute> getVertexAttributes() {
        return Map.of("label", DefaultAttribute.createAttribute(cell.getVertexLabel() + " : " + candidate));
    }

    /*
     * Returns a collector that takes all the supplied LocatedCandidates in a stream and produces a list of
     * RemoveCandidates with at most one RemoveCandidates per cell. This allows the logic functions to focus on simply
     * marking the numbers to be removed, then use this collector at the end.
     */
    public static Collector<LocatedCandidate, ?, List<RemoveCandidates>> mergeToRemoveCandidates() {
        return Collectors.collectingAndThen(
                Collectors.groupingBy(
                        LocatedCandidate::cell,
                        Collectors.mapping(
                                LocatedCandidate::candidate,
                                Collectors.toCollection(() -> EnumSet.noneOf(SudokuNumber.class))
                        )
                ),
                grouped -> grouped.entrySet()
                        .stream()
                        .map(entry -> new RemoveCandidates(entry.getKey(), entry.getValue()))
                        .toList()
        );
    }
}