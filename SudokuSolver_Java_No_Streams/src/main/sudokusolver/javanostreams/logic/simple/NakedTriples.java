package sudokusolver.javanostreams.logic.simple;

import sudokusolver.javanostreams.Board;
import sudokusolver.javanostreams.Cell;
import sudokusolver.javanostreams.LocatedCandidate;
import sudokusolver.javanostreams.RemoveCandidates;
import sudokusolver.javanostreams.Triple;
import sudokusolver.javanostreams.UnsolvedCell;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Stream;

/*
 * https://www.sudokuwiki.org/Naked_Candidates#NT
 *
 * If a unit has three unsolved cells with a total of three candidates among them, then those three candidates must be
 * placed in those three cells. the three candidates can be removed from every other cell in the unit.
 */
public class NakedTriples {
    public static List<RemoveCandidates> nakedTriples(Board<Cell> board) {
        return board.getUnits()
                .stream()
                .flatMap(unit -> unit.stream()
                        .filter(UnsolvedCell.class::isInstance)
                        .map(UnsolvedCell.class::cast)
                        .collect(Triple.zipEveryTriple())
                        .flatMap(triple -> {
                            var a = triple.first();
                            var b = triple.second();
                            var c = triple.third();
                            var unionOfCandidates = EnumSet.copyOf(a.candidates());
                            unionOfCandidates.addAll(b.candidates());
                            unionOfCandidates.addAll(c.candidates());
                            if (unionOfCandidates.size() == 3) {
                                return unit.stream()
                                        .filter(UnsolvedCell.class::isInstance)
                                        .map(UnsolvedCell.class::cast)
                                        .filter(cell -> !cell.equals(a) && !cell.equals(b) && !cell.equals(c))
                                        .flatMap(cell -> {
                                            var toRemove = EnumSet.copyOf(cell.candidates());
                                            toRemove.retainAll(unionOfCandidates);
                                            return toRemove.stream()
                                                    .map(candidate -> new LocatedCandidate(cell, candidate));
                                        });
                            } else {
                                return Stream.empty();
                            }
                        }))
                .collect(LocatedCandidate.mergeToRemoveCandidates());
    }
}