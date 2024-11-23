package sudokusolver.java.logic.extreme;

import sudokusolver.java.Board;
import sudokusolver.java.Cell;
import sudokusolver.java.LocatedCandidate;
import sudokusolver.java.Pair;
import sudokusolver.java.Quad;
import sudokusolver.java.RemoveCandidates;
import sudokusolver.java.SudokuNumber;
import sudokusolver.java.Triple;
import sudokusolver.java.UnsolvedCell;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
 * https://www.sudokuwiki.org/Sue_De_Coq
 *
 * This solution starts with looking for two or three cells in the same linear unit (row or column) and block and the
 * union of candidates across the cells has a size which is at least two more than the number of cells. In other words,
 * if two cells are selected, then they must have at least four candidates. If three cells are selected, they must have
 * at least five candidates. These cells are the main group in this solution.
 *
 * Once the main group is identified, this solution then searches for an Almost Locked Set in the same linear unit as
 * the main group and also for an ALS in the same block as the main group. As a reminder, an ALS is a set of n unsolved
 * cells, all of which can see each other, and there are n + 1 candidates across all n cells. The two ALSs can only
 * contain candidates found in the main group, they must contain all the candidates of the main group, and there can be
 * no common candidates across the two ALSs.
 *
 * Once we have the main group and the two ALSs, it is then certain that each of the common candidates must appear in
 * one of the three groups. Therefore, for any common candidate, that candidate cannot be the solution for any cell
 * which can see the main group and can see the ALS that has the candidate. The candidates of the linear unit ALS can be
 * removed from other cells of that linear unit which are not a part of the main group. The candidates of the block unit
 * ALS can be removed from other cells of that block which are not a part of the main group.
 */
public class SueDeCoq {
    public static List<RemoveCandidates> sueDeCoq(Board<Cell> board) {
        var rowRemovals = sueDeCoq(board, board.rows(), Cell::row);
        var columnRemovals = sueDeCoq(board, board.getColumns(), Cell::column);
        return Stream.concat(rowRemovals, columnRemovals).collect(LocatedCandidate.mergeToRemoveCandidates());
    }

    private static Stream<LocatedCandidate> sueDeCoq(
            Board<Cell> board,
            List<List<Cell>> units,
            ToIntFunction<Cell> getUnitIndex
    ) {
        return units.stream()
                .map(unit -> unit.stream()
                        .filter(UnsolvedCell.class::isInstance)
                        .map(UnsolvedCell.class::cast).toList())
                .flatMap(unit -> unit.stream()
                        .collect(Collectors.groupingBy(Cell::block))
                        .entrySet()
                        .stream()
                        .flatMap(entry -> {
                            var blockIndex = entry.getKey();
                            var unitByBlock = entry.getValue();
                            var otherCellsInUnit = unit.stream()
                                    .filter(cell -> cell.block() != blockIndex)
                                    .toList();
                            var block = board.getBlock(blockIndex)
                                    .stream()
                                    .filter(UnsolvedCell.class::isInstance)
                                    .map(UnsolvedCell.class::cast)
                                    .toList();
                            var otherCellsInBlock = block.stream()
                                    .filter(cell -> getUnitIndex.applyAsInt(cell) !=
                                            getUnitIndex.applyAsInt(unitByBlock.getFirst()))
                                    .toList();
                            if (unitByBlock.size() == 2) {
                                return getGroupRemovals(unit, otherCellsInUnit, block, otherCellsInBlock, unitByBlock);
                            } else if (unitByBlock.size() == 3) {
                                var allThree = getGroupRemovals(
                                        unit,
                                        otherCellsInUnit,
                                        block,
                                        otherCellsInBlock,
                                        unitByBlock
                                );
                                var byPairs = unitByBlock.stream()
                                        .collect(Pair.zipEveryPair())
                                        .flatMap(pair -> {
                                            var a = pair.first();
                                            var b = pair.second();
                                            return getGroupRemovals(
                                                    unit,
                                                    otherCellsInUnit,
                                                    block,
                                                    otherCellsInBlock,
                                                    List.of(a, b)
                                            );
                                        });
                                return Stream.concat(allThree, byPairs);
                            } else {
                                return Stream.empty();
                            }
                        }));
    }

    private static Stream<LocatedCandidate> getGroupRemovals(
            List<UnsolvedCell> unit,
            List<UnsolvedCell> otherCellsInUnit,
            List<UnsolvedCell> block,
            List<UnsolvedCell> otherCellsInBlock,
            List<UnsolvedCell> group
    ) {
        var candidates = group.stream()
                .flatMap(cell -> cell.candidates().stream())
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(SudokuNumber.class)));
        if (candidates.size() >= group.size() + 2) {
            return getAlmostLockedSets(otherCellsInUnit, candidates)
                    .flatMap(unitALS -> getAlmostLockedSets(otherCellsInBlock, candidates)
                            .filter(blockALS -> {
                                var intersection = EnumSet.copyOf(unitALS.candidates());
                                intersection.retainAll(blockALS.candidates());
                                return intersection.isEmpty() &&
                                        unitALS.candidates().size() + blockALS.candidates().size() == candidates.size();
                            })
                            .flatMap(blockALS -> {
                                var unitRemovals = unit.stream()
                                        .filter(cell -> !group.contains(cell) && !unitALS.cells().contains(cell))
                                        .flatMap(cell -> {
                                            var intersection = EnumSet.copyOf(cell.candidates());
                                            intersection.retainAll(unitALS.candidates());
                                            return intersection.stream()
                                                    .map(candidate -> new LocatedCandidate(cell, candidate));
                                        });
                                var blockRemovals = block.stream()
                                        .filter(cell -> !group.contains(cell) && !blockALS.cells().contains(cell))
                                        .flatMap(cell -> {
                                            var intersection = EnumSet.copyOf(cell.candidates());
                                            intersection.retainAll(blockALS.candidates());
                                            return intersection.stream()
                                                    .map(candidate -> new LocatedCandidate(cell, candidate));
                                        });
                                return Stream.concat(unitRemovals, blockRemovals);
                            }));
        } else {
            return Stream.empty();
        }
    }

    private record ALS(Set<UnsolvedCell> cells, EnumSet<SudokuNumber> candidates) {
    }

    private static Stream<ALS> getAlmostLockedSets(List<UnsolvedCell> cells, EnumSet<SudokuNumber> groupCandidates) {
        var almostLockedSets1 = cells.stream()
                .filter(cell -> cell.candidates().size() == 2 && groupCandidates.containsAll(cell.candidates()))
                .map(cell -> new ALS(Set.of(cell), cell.candidates()));
        var almostLockedSets2 = cells.stream()
                .collect(Pair.zipEveryPair())
                .map(pair -> {
                    var a = pair.first();
                    var b = pair.second();
                    var candidates = EnumSet.copyOf(a.candidates());
                    candidates.addAll(b.candidates());
                    return new ALS(Set.of(a, b), candidates);
                })
                .filter(als -> als.candidates().size() == 3 && groupCandidates.containsAll(als.candidates()));
        var almostLockedSets3 = cells.stream()
                .collect(Triple.zipEveryTriple())
                .map(triple -> {
                    var a = triple.first();
                    var b = triple.second();
                    var c = triple.third();
                    var candidates = EnumSet.copyOf(a.candidates());
                    candidates.addAll(b.candidates());
                    candidates.addAll(c.candidates());
                    return new ALS(Set.of(a, b, c), candidates);
                })
                .filter(als -> als.candidates().size() == 4 && groupCandidates.containsAll(als.candidates()));
        var almostLockedSets4 = cells.stream()
                .collect(Quad.zipEveryQuad())
                .map(quad -> {
                    var a = quad.first();
                    var b = quad.second();
                    var c = quad.third();
                    var d = quad.fourth();
                    var candidates = EnumSet.copyOf(a.candidates());
                    candidates.addAll(b.candidates());
                    candidates.addAll(c.candidates());
                    candidates.addAll(d.candidates());
                    return new ALS(Set.of(a, b, c, d), candidates);
                })
                .filter(als -> als.candidates().size() == 5 && groupCandidates.containsAll(als.candidates()));
        return Stream.of(almostLockedSets1, almostLockedSets2, almostLockedSets3, almostLockedSets4)
                .flatMap(Function.identity());
    }
}