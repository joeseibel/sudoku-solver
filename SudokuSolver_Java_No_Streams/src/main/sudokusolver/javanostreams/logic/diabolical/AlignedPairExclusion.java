package sudokusolver.javanostreams.logic.diabolical;

import sudokusolver.javanostreams.Board;
import sudokusolver.javanostreams.Cell;
import sudokusolver.javanostreams.LocatedCandidate;
import sudokusolver.javanostreams.Pair;
import sudokusolver.javanostreams.Quad;
import sudokusolver.javanostreams.RemoveCandidates;
import sudokusolver.javanostreams.SudokuNumber;
import sudokusolver.javanostreams.Triple;
import sudokusolver.javanostreams.UnsolvedCell;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

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
        return board.getCells()
                .stream()
                .filter(UnsolvedCell.class::isInstance)
                .map(UnsolvedCell.class::cast)
                .collect(Pair.zipEveryPair())
                .flatMap(pair -> {
                    var cellA = pair.first();
                    var cellB = pair.second();
                    var almostLockedSets = getAlmostLockedSets(board, cellA, cellB);
                    var validACandidates = EnumSet.noneOf(SudokuNumber.class);
                    var validBCandidates = EnumSet.noneOf(SudokuNumber.class);
                    cellA.candidates().forEach(candidateA -> cellB.candidates()
                            .stream()
                            .filter(candidateB -> {
                                if (candidateA == candidateB) {
                                    return !cellA.isInSameUnit(cellB);
                                } else {
                                    var pairAsSet = EnumSet.of(candidateA, candidateB);
                                    return almostLockedSets.stream().noneMatch(als -> als.containsAll(pairAsSet));
                                }
                            })
                            .forEach(candidateB -> {
                                validACandidates.add(candidateA);
                                validBCandidates.add(candidateB);
                            }));
                    var removalsA = cellA.candidates()
                            .stream()
                            .filter(candidate -> !validACandidates.contains(candidate))
                            .map(candidate -> new LocatedCandidate(cellA, candidate));
                    var removalsB = cellB.candidates()
                            .stream()
                            .filter(candidate -> !validBCandidates.contains(candidate))
                            .map(candidate -> new LocatedCandidate(cellB, candidate));
                    return Stream.concat(removalsA, removalsB);
                })
                .collect(LocatedCandidate.mergeToRemoveCandidates());
    }

    private static List<EnumSet<SudokuNumber>> getAlmostLockedSets(
            Board<Cell> board,
            UnsolvedCell cellA,
            UnsolvedCell cellB
    ) {
        var visible = board.getCells()
                .stream()
                .filter(UnsolvedCell.class::isInstance)
                .map(UnsolvedCell.class::cast)
                .filter(cell -> !cell.equals(cellA) &&
                        !cell.equals(cellB) &&
                        cell.isInSameUnit(cellA) &&
                        cell.isInSameUnit(cellB))
                .toList();
        var almostLockedSets1 = visible.stream()
                .map(UnsolvedCell::candidates)
                .filter(candidates -> candidates.size() == 2);
        var almostLockedSets2 = visible.stream()
                .collect(Pair.zipEveryPair())
                .filter(pair -> {
                    var alsA = pair.first();
                    var alsB = pair.second();
                    return alsA.isInSameUnit(alsB);
                })
                .map(pair -> {
                    var alsA = pair.first();
                    var alsB = pair.second();
                    var candidates = EnumSet.copyOf(alsA.candidates());
                    candidates.addAll(alsB.candidates());
                    return candidates;
                })
                .filter(candidates -> candidates.size() == 3);
        var almostLockedSets3 = visible.stream()
                .collect(Triple.zipEveryTriple())
                .filter(triple -> {
                    var alsA = triple.first();
                    var alsB = triple.second();
                    var alsC = triple.third();
                    return alsA.isInSameUnit(alsB) && alsA.isInSameUnit(alsC) && alsB.isInSameUnit(alsC);
                })
                .map(triple -> {
                    var alsA = triple.first();
                    var alsB = triple.second();
                    var alsC = triple.third();
                    var candidates = EnumSet.copyOf(alsA.candidates());
                    candidates.addAll(alsB.candidates());
                    candidates.addAll(alsC.candidates());
                    return candidates;
                })
                .filter(candidates -> candidates.size() == 4);
        var almostLockedSets4 = visible.stream()
                .collect(Quad.zipEveryQuad())
                .filter(quad -> {
                    var alsA = quad.first();
                    var alsB = quad.second();
                    var alsC = quad.third();
                    var alsD = quad.fourth();
                    return alsA.isInSameUnit(alsB) &&
                            alsA.isInSameUnit(alsC) &&
                            alsA.isInSameUnit(alsD) &&
                            alsB.isInSameUnit(alsC) &&
                            alsB.isInSameUnit(alsD) &&
                            alsC.isInSameUnit(alsD);
                }).map(quad -> {
                    var alsA = quad.first();
                    var alsB = quad.second();
                    var alsC = quad.third();
                    var alsD = quad.fourth();
                    var candidates = EnumSet.copyOf(alsA.candidates());
                    candidates.addAll(alsB.candidates());
                    candidates.addAll(alsC.candidates());
                    candidates.addAll(alsD.candidates());
                    return candidates;
                })
                .filter(candidates -> candidates.size() == 5);
        return Stream.of(almostLockedSets1, almostLockedSets2, almostLockedSets3, almostLockedSets4)
                .flatMap(Function.identity())
                .toList();
    }
}
