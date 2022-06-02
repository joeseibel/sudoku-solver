package sudokusolver.javanostreams.logic.simple;

import sudokusolver.javanostreams.Board;
import sudokusolver.javanostreams.Cell;
import sudokusolver.javanostreams.LocatedCandidate;
import sudokusolver.javanostreams.Quad;
import sudokusolver.javanostreams.RemoveCandidates;
import sudokusolver.javanostreams.UnsolvedCell;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Stream;

/*
 * https://www.sudokuwiki.org/Naked_Candidates#NQ
 *
 * If a unit has four unsolved cells with a total of four candidates among them, then those four candidates must be
 * placed in those four cells. The four candidates can be removed from every other cell in the unit.
 */
public class NakedQuads {
    public static List<RemoveCandidates> nakedQuads(Board<Cell> board) {
        return board.getUnits()
                .stream()
                .flatMap(unit -> unit.stream()
                        .filter(UnsolvedCell.class::isInstance)
                        .map(UnsolvedCell.class::cast)
                        .collect(Quad.zipEveryQuad())
                        .flatMap(quad -> {
                            var a = quad.first();
                            var b = quad.second();
                            var c = quad.third();
                            var d = quad.fourth();
                            var unionOfCandidates = EnumSet.copyOf(a.candidates());
                            unionOfCandidates.addAll(b.candidates());
                            unionOfCandidates.addAll(c.candidates());
                            unionOfCandidates.addAll(d.candidates());
                            if (unionOfCandidates.size() == 4) {
                                return unit.stream()
                                        .filter(UnsolvedCell.class::isInstance)
                                        .map(UnsolvedCell.class::cast)
                                        .filter(cell -> !cell.equals(a) &&
                                                !cell.equals(b) &&
                                                !cell.equals(c) &&
                                                !cell.equals(d))
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
