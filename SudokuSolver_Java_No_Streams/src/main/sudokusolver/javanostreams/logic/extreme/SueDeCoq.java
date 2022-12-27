package sudokusolver.javanostreams.logic.extreme;

import sudokusolver.javanostreams.Board;
import sudokusolver.javanostreams.Cell;
import sudokusolver.javanostreams.Removals;
import sudokusolver.javanostreams.RemoveCandidates;
import sudokusolver.javanostreams.SudokuNumber;
import sudokusolver.javanostreams.UnsolvedCell;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.function.ToIntFunction;

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
        var removals = new Removals();
        sueDeCoq(removals, board, board.rows(), Cell::row);
        sueDeCoq(removals, board, board.getColumns(), Cell::column);
        return removals.toList();
    }

    private static void sueDeCoq(
            Removals removals,
            Board<Cell> board,
            List<List<Cell>> units,
            ToIntFunction<Cell> getUnitIndex
    ) {
        for (var unit : units) {
            var unsolvedUnit = new ArrayList<UnsolvedCell>();
            for (var cell : unit) {
                if (cell instanceof UnsolvedCell unsolved) {
                    unsolvedUnit.add(unsolved);
                }
            }
            var grouping = new HashMap<Integer, List<UnsolvedCell>>();
            for (var cell : unsolvedUnit) {
                grouping.computeIfAbsent(cell.block(), key -> new ArrayList<>()).add(cell);
            }
            for (var entry : grouping.entrySet()) {
                var blockIndex = entry.getKey();
                var unitByBlock = entry.getValue();
                var otherCellsInUnit = new ArrayList<UnsolvedCell>();
                for (var cell : unsolvedUnit) {
                    if (cell.block() != blockIndex) {
                        otherCellsInUnit.add(cell);
                    }
                }
                var block = new ArrayList<UnsolvedCell>();
                for (var cell : board.getBlock(blockIndex)) {
                    if (cell instanceof UnsolvedCell unsolved) {
                        block.add(unsolved);
                    }
                }
                var otherCellsInBlock = new ArrayList<UnsolvedCell>();
                for (var cell : block) {
                    if (getUnitIndex.applyAsInt(cell) != getUnitIndex.applyAsInt(unitByBlock.get(0))) {
                        otherCellsInBlock.add(cell);
                    }
                }
                if (unitByBlock.size() == 2) {
                    getGroupRemovals(removals, unsolvedUnit, otherCellsInUnit, block, otherCellsInBlock, unitByBlock);
                } else if (unitByBlock.size() == 3) {
                    for (var i = 0; i < unitByBlock.size() - 1; i++) {
                        var a = unitByBlock.get(i);
                        for (var j = i + 1; j < unitByBlock.size(); j++) {
                            var b = unitByBlock.get(j);
                            getGroupRemovals(
                                    removals,
                                    unsolvedUnit,
                                    otherCellsInUnit,
                                    block,
                                    otherCellsInBlock,
                                    List.of(a, b)
                            );
                        }
                    }
                }
            }
        }
    }

    private static void getGroupRemovals(
            Removals removals,
            List<UnsolvedCell> unit,
            List<UnsolvedCell> otherCellsInUnit,
            List<UnsolvedCell> block,
            List<UnsolvedCell> otherCellsInBlock,
            List<UnsolvedCell> group
    ) {
        var candidates = EnumSet.noneOf(SudokuNumber.class);
        for (var cell : group) {
            candidates.addAll(cell.candidates());
        }
        if (candidates.size() >= group.size() + 2) {
            for (var unitALS : getAlmostLockedSets(otherCellsInUnit, candidates)) {
                for (var blockALS : getAlmostLockedSets(otherCellsInBlock, candidates)) {
                    var alsIntersection = EnumSet.copyOf(unitALS.candidates());
                    alsIntersection.retainAll(blockALS.candidates());
                    if (alsIntersection.isEmpty() &&
                            unitALS.candidates().size() + blockALS.candidates().size() == candidates.size()
                    ) {
                        for (var cell : unit) {
                            if (!group.contains(cell) && !unitALS.cells().contains(cell)) {
                                var cellIntersection = EnumSet.copyOf(cell.candidates());
                                cellIntersection.retainAll(unitALS.candidates());
                                removals.add(cell, cellIntersection);
                            }
                        }
                        for (var cell : block) {
                            if (!group.contains(cell) && !blockALS.cells().contains(cell)) {
                                var cellIntersection = EnumSet.copyOf(cell.candidates());
                                cellIntersection.retainAll(blockALS.candidates());
                                removals.add(cell, cellIntersection);
                            }
                        }
                    }
                }
            }
        }
    }

    private record ALS(Set<UnsolvedCell> cells, EnumSet<SudokuNumber> candidates) {
    }

    private static List<ALS> getAlmostLockedSets(List<UnsolvedCell> cells, EnumSet<SudokuNumber> groupCandidates) {
        var almostLockedSets = new ArrayList<ALS>();
        for (var cell : cells) {
            if (cell.candidates().size() == 2 && groupCandidates.containsAll(cell.candidates())) {
                almostLockedSets.add(new ALS(Set.of(cell), cell.candidates()));
            }
        }
        for (var i = 0; i < cells.size() - 1; i++) {
            var a = cells.get(i);
            for (var j = i + 1; j < cells.size(); j++) {
                var b = cells.get(j);
                var candidates = EnumSet.copyOf(a.candidates());
                candidates.addAll(b.candidates());
                if (candidates.size() == 3 && groupCandidates.containsAll(candidates)) {
                    almostLockedSets.add(new ALS(Set.of(a, b), candidates));
                }
            }
        }
        for (var i = 0; i < cells.size() - 2; i++) {
            var a = cells.get(i);
            for (var j = i + 1; j < cells.size() - 1; j++) {
                var b = cells.get(j);
                for (var k = j + 1; k < cells.size(); k++) {
                    var c = cells.get(k);
                    var candidates = EnumSet.copyOf(a.candidates());
                    candidates.addAll(b.candidates());
                    candidates.addAll(c.candidates());
                    if (candidates.size() == 4 && groupCandidates.containsAll(candidates)) {
                        almostLockedSets.add(new ALS(Set.of(a, b, c), candidates));
                    }
                }
            }
        }
        for (var i = 0; i < cells.size() - 3; i++) {
            var a = cells.get(i);
            for (var j = i + 1; j < cells.size() - 2; j++) {
                var b = cells.get(j);
                for (var k = j + 1; k < cells.size() - 1; k++) {
                    var c = cells.get(k);
                    for (var l = k + 1; l < cells.size(); l++) {
                        var d = cells.get(l);
                        var candidates = EnumSet.copyOf(a.candidates());
                        candidates.addAll(b.candidates());
                        candidates.addAll(c.candidates());
                        candidates.addAll(d.candidates());
                        if (candidates.size() == 5 && groupCandidates.containsAll(candidates)) {
                            almostLockedSets.add(new ALS(Set.of(a, b, c, d), candidates));
                        }
                    }
                }
            }
        }
        return almostLockedSets;
    }
}