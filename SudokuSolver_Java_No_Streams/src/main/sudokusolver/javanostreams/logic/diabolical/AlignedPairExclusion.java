package sudokusolver.javanostreams.logic.diabolical;

import sudokusolver.javanostreams.Board;
import sudokusolver.javanostreams.Cell;
import sudokusolver.javanostreams.Pair;
import sudokusolver.javanostreams.Removals;
import sudokusolver.javanostreams.RemoveCandidates;
import sudokusolver.javanostreams.SudokuNumber;
import sudokusolver.javanostreams.UnsolvedCell;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/*
 * https://www.sudokuwiki.org/Aligned_Pair_Exclusion
 *
 * To understand Aligned Pair Exclusion, it is helpful to first define what an Almost Locked Set is. An ALS is a set of
 * n unsolved cells, all of which can see each other, and there are n + 1 candidates across all n cells. In the simplest
 * case, any unsolved cell with two candidates is an ALS; there is one cell and two candidates. A pair of cells is an
 * ALS if they can see each other and the union of candidates has a size of three. If there are three cells that see
 * each other and there are four candidates across those three cells, then those three cells are an ALS.
 *
 * Aligned Pair Exclusion considers a pair of unsolved cells, which may or may not see each other, and checks for
 * solution combinations for that pair which would cause problems for that pair or for Almost Locks Sets which are
 * visible to that pair. This will result in a list of solution combinations for the pair, some of which are known to be
 * invalid, and the others which could potentially be valid. If a particular candidate in one of the cells of the pair
 * only appears among the invalid combinations, then that candidate cannot be the solution to that cell and can be
 * removed.
 *
 * How is a solution combination for a pair checked for validity? The first simple thing to look at is if the candidates
 * of the combination are the same and the two cells can see each other, then the combination is invalid. If the
 * candidates are not the same, then it is time to look at the ALSs that are visible to both cells of the pair. If a
 * solution combination is a subset of the candidates of a visible ALS, then that combination would cause problems for
 * the ALS and the combination is invalid.
 *
 * The simplest case of checking an ALS is when the ALS has one cell and two candidates. If the solution combination has
 * the same candidates as the ALS, then the solution combination would empty the ALS. This is a very obvious case, but
 * it gets a little more complicated when an ALS has more than one cell and more than two candidates. The link at the
 * start of this comment has some examples with ALSs that have two cells and three cells. It is helpful to walk through
 * these examples to see how a solution combination which is a subset of the candidates of an ALS is invalid.
 */
public class AlignedPairExclusion {
    public static List<RemoveCandidates> alignedPairExclusion(Board<Cell> board) {
        var removals = new Removals();
        for (var pair : Pair.zipEveryPair(board.getCells())) {
            if (pair.first() instanceof UnsolvedCell cellA && pair.second() instanceof UnsolvedCell cellB) {
                var almostLockedSets = getAlmostLockedSets(board, cellA, cellB);
                var validACandidates = EnumSet.noneOf(SudokuNumber.class);
                var validBCandidates = EnumSet.noneOf(SudokuNumber.class);
                for (var candidateA : cellA.candidates()) {
                    for (var candidateB : cellB.candidates()) {
                        if (isValid(candidateA, candidateB, cellA, cellB, almostLockedSets)) {
                            validACandidates.add(candidateA);
                            validBCandidates.add(candidateB);
                        }
                    }
                }
                for (var candidate : cellA.candidates()) {
                    if (!validACandidates.contains(candidate)) {
                        removals.add(cellA, candidate);
                    }
                }
                for (var candidate : cellB.candidates()) {
                    if (!validBCandidates.contains(candidate)) {
                        removals.add(cellB, candidate);
                    }
                }
            }
        }
        return removals.toList();
    }

    private static List<EnumSet<SudokuNumber>> getAlmostLockedSets(
            Board<Cell> board,
            UnsolvedCell cellA,
            UnsolvedCell cellB
    ) {
        var visible = new ArrayList<UnsolvedCell>();
        for (var cell : board.getCells()) {
            if (cell instanceof UnsolvedCell unsolved &&
                    !unsolved.equals(cellA) &&
                    !unsolved.equals(cellB) &&
                    unsolved.isInSameUnit(cellA) &&
                    unsolved.isInSameUnit(cellB)
            ) {
                visible.add(unsolved);
            }
        }
        var almostLockedSets = new ArrayList<EnumSet<SudokuNumber>>();
        for (var cell : visible) {
            if (cell.candidates().size() == 2) {
                almostLockedSets.add(cell.candidates());
            }
        }
        for (var i = 0; i < visible.size() - 1; i++) {
            var alsA = visible.get(i);
            for (var j = i + 1; j < visible.size(); j++) {
                var alsB = visible.get(j);
                if (alsA.isInSameUnit(alsB)) {
                    var candidates = EnumSet.copyOf(alsA.candidates());
                    candidates.addAll(alsB.candidates());
                    if (candidates.size() == 3) {
                        almostLockedSets.add(candidates);
                    }
                }
            }
        }
        for (var i = 0; i < visible.size() - 2; i++) {
            var alsA = visible.get(i);
            for (var j = i + 1; j < visible.size() - 1; j++) {
                var alsB = visible.get(j);
                if (alsA.isInSameUnit(alsB)) {
                    for (var k = j + 1; k < visible.size(); k++) {
                        var alsC = visible.get(k);
                        if (alsA.isInSameUnit(alsC) && alsB.isInSameUnit(alsC)) {
                            var candidates = EnumSet.copyOf(alsA.candidates());
                            candidates.addAll(alsB.candidates());
                            candidates.addAll(alsC.candidates());
                            if (candidates.size() == 4) {
                                almostLockedSets.add(candidates);
                            }
                        }
                    }
                }
            }
        }
        for (var i = 0; i < visible.size() - 3; i++) {
            var alsA = visible.get(i);
            for (var j = i + 1; j < visible.size() - 2; j++) {
                var alsB = visible.get(j);
                if (alsA.isInSameUnit(alsB)) {
                    for (var k = j + 1; k < visible.size() - 1; k++) {
                        var alsC = visible.get(k);
                        if (alsA.isInSameUnit(alsC) && alsB.isInSameUnit(alsC)) {
                            for (var l = k + 1; l < visible.size(); l++) {
                                var alsD = visible.get(l);
                                if (alsA.isInSameUnit(alsD) && alsB.isInSameUnit(alsD) && alsC.isInSameUnit(alsD)) {
                                    var candidates = EnumSet.copyOf(alsA.candidates());
                                    candidates.addAll(alsB.candidates());
                                    candidates.addAll(alsC.candidates());
                                    candidates.addAll(alsD.candidates());
                                    if (candidates.size() == 5) {
                                        almostLockedSets.add(candidates);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return almostLockedSets;
    }

    private static boolean isValid(
            SudokuNumber candidateA,
            SudokuNumber candidateB,
            UnsolvedCell cellA,
            UnsolvedCell cellB,
            List<EnumSet<SudokuNumber>> almostLockedSets
    ) {
        if (candidateA == candidateB) {
            return !cellA.isInSameUnit(cellB);
        } else {
            return !isALS(candidateA, candidateB, almostLockedSets);
        }
    }

    private static boolean isALS(
            SudokuNumber candidateA,
            SudokuNumber candidateB,
            List<EnumSet<SudokuNumber>> almostLockedSets
    ) {
        var pairAsSet = EnumSet.of(candidateA, candidateB);
        for (var als : almostLockedSets) {
            if (als.containsAll(pairAsSet)) {
                return true;
            }
        }
        return false;
    }
}