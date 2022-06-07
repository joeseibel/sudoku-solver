package sudokusolver.javanostreams.logic.simple;

import sudokusolver.javanostreams.Board;
import sudokusolver.javanostreams.Cell;
import sudokusolver.javanostreams.Removals;
import sudokusolver.javanostreams.RemoveCandidates;
import sudokusolver.javanostreams.Triple;
import sudokusolver.javanostreams.UnsolvedCell;

import java.util.EnumSet;
import java.util.List;

/*
 * https://www.sudokuwiki.org/Naked_Candidates#NT
 *
 * If a unit has three unsolved cells with a total of three candidates among them, then those three candidates must be
 * placed in those three cells. the three candidates can be removed from every other cell in the unit.
 */
public class NakedTriples {
    public static List<RemoveCandidates> nakedTriples(Board<Cell> board) {
        var removals = new Removals();
        for (var unit : board.getUnits()) {
            for (var triple : Triple.zipEveryTriple(unit)) {
                if (triple.first() instanceof UnsolvedCell a &&
                        triple.second() instanceof UnsolvedCell b &&
                        triple.third() instanceof UnsolvedCell c
                ) {
                    var unionOfCandidates = EnumSet.copyOf(a.candidates());
                    unionOfCandidates.addAll(b.candidates());
                    unionOfCandidates.addAll(c.candidates());
                    if (unionOfCandidates.size() == 3) {
                        for (var cell : unit) {
                            if (cell instanceof UnsolvedCell unsolved &&
                                    !unsolved.equals(a) &&
                                    !unsolved.equals(b) &&
                                    !unsolved.equals(c)
                            ) {
                                var toRemove = EnumSet.copyOf(unsolved.candidates());
                                toRemove.retainAll(unionOfCandidates);
                                if (!toRemove.isEmpty()) {
                                    removals.add(unsolved, toRemove);
                                }
                            }
                        }
                    }
                }
            }
        }
        return removals.toList();
    }
}