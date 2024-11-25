package sudokusolver.javanostreams;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * For the No Streams version of the Java implementation, the solutions that remove candidates collect the removals in a
 * map and then convert that map to a list of RemoveCandidates at the end. The conversion and the setting of initial map
 * values are common enough operations that it made sense to create this class.
 */
public class Removals {
    private final Map<UnsolvedCell, EnumSet<SudokuNumber>> removals = new HashMap<>();

    public void add(UnsolvedCell cell, SudokuNumber candidate) {
        removals.computeIfAbsent(cell, _ -> EnumSet.noneOf(SudokuNumber.class)).add(candidate);
    }

    public void add(UnsolvedCell cell, EnumSet<SudokuNumber> candidates) {
        if (!candidates.isEmpty()) {
            removals.computeIfAbsent(cell, _ -> EnumSet.noneOf(SudokuNumber.class)).addAll(candidates);
        }
    }

    public List<RemoveCandidates> toList() {
        var removeCandidates = new ArrayList<RemoveCandidates>();
        for (var entry : removals.entrySet()) {
            removeCandidates.add(new RemoveCandidates(entry.getKey(), entry.getValue()));
        }
        return removeCandidates;
    }
}