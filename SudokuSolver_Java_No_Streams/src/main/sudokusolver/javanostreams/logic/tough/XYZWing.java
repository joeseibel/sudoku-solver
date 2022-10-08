package sudokusolver.javanostreams.logic.tough;

import sudokusolver.javanostreams.Board;
import sudokusolver.javanostreams.Cell;
import sudokusolver.javanostreams.Pair;
import sudokusolver.javanostreams.Removals;
import sudokusolver.javanostreams.RemoveCandidates;
import sudokusolver.javanostreams.UnsolvedCell;

import java.util.EnumSet;
import java.util.List;

/*
 * https://www.sudokuwiki.org/XYZ_Wing
 *
 * Given a hinge cell and two wing cells such that the hinge can see both wings, the hinge has three candidates, the
 * wings have two candidates each, there is one candidate shared among all three cells, two candidates are shared
 * between the hinge and one wing and two candidates between the hinge and the other wing, and the common candidate is
 * the only one shared between the wings, then the common candidate must be the solution to one of the cells. The common
 * candidate can be removed from any cell which can see all three cells.
 */
public class XYZWing {
    public static List<RemoveCandidates> xyzWing(Board<Cell> board) {
        var removals = new Removals();
        for (var cell : board.getCells()) {
            if (cell instanceof UnsolvedCell hinge && hinge.candidates().size() == 3) {
                for (var pair : Pair.zipEveryPair(board.getCells())) {
                    if (pair.first() instanceof UnsolvedCell wingA &&
                            wingA.candidates().size() == 2 &&
                            hinge.isInSameUnit(wingA) &&
                            pair.second() instanceof UnsolvedCell wingB &&
                            wingB.candidates().size() == 2 &&
                            hinge.isInSameUnit(wingB)
                    ) {
                        var allCandidates = EnumSet.copyOf(hinge.candidates());
                        allCandidates.addAll(wingA.candidates());
                        allCandidates.addAll(wingB.candidates());
                        if (allCandidates.size() == 3) {
                            var toRemove = EnumSet.copyOf(wingA.candidates());
                            toRemove.retainAll(wingB.candidates());
                            if (toRemove.size() == 1) {
                                var candidate = toRemove.iterator().next();
                                for (var removalCell : board.getCells()) {
                                    if (removalCell instanceof UnsolvedCell unsolved &&
                                            !unsolved.equals(hinge) &&
                                            !unsolved.equals(wingA) &&
                                            !unsolved.equals(wingB) &&
                                            unsolved.candidates().contains(candidate) &&
                                            unsolved.isInSameUnit(hinge) &&
                                            unsolved.isInSameUnit(wingA) &&
                                            unsolved.isInSameUnit(wingB)
                                    ) {
                                        removals.add(unsolved, candidate);
                                    }
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