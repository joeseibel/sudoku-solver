package sudokusolver.javanostreams.logic.simple;

import sudokusolver.javanostreams.Board;
import sudokusolver.javanostreams.Cell;
import sudokusolver.javanostreams.Removals;
import sudokusolver.javanostreams.RemoveCandidates;
import sudokusolver.javanostreams.SudokuNumber;
import sudokusolver.javanostreams.UnsolvedCell;

import java.util.HashSet;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;

/*
 * https://www.sudokuwiki.org/Intersection_Removal#IR
 *
 * For a given block, if a candidate appears in only one row, then the candidate for that row must be placed in that
 * block. The candidate can be removed from cells which are in the same row, but different blocks.
 *
 * For a given block, if a candidate appears in only one column, then the candidate for that column must be placed in
 * that block. The candidate can be removed from cells which are in the same column, but different blocks.
 */
public class PointingPairsPointingTriples {
    public static List<RemoveCandidates> pointingPairsPointingTriples(Board<Cell> board) {
        var removals = new Removals();
        for (var block : board.getBlocks()) {
            for (var candidate : SudokuNumber.values()) {
                pointingPairsPointingTriples(removals, block, candidate, board::getRow, Cell::row);
                pointingPairsPointingTriples(removals, block, candidate, board::getColumn, Cell::column);
            }
        }
        return removals.toList();
    }

    private static void pointingPairsPointingTriples(
            Removals removals,
            List<Cell> block,
            SudokuNumber candidate,
            IntFunction<List<Cell>> getUnit,
            ToIntFunction<Cell> getUnitIndex
    ) {
        var unitIndices = new HashSet<Integer>();
        for (var cell : block) {
            if (cell instanceof UnsolvedCell unsolved && unsolved.candidates().contains(candidate)) {
                unitIndices.add(getUnitIndex.applyAsInt(unsolved));
            }
        }
        if (unitIndices.size() == 1) {
            for (var cell : getUnit.apply(unitIndices.iterator().next())) {
                if (cell instanceof UnsolvedCell unsolved &&
                        unsolved.block() != block.get(0).block() &&
                        unsolved.candidates().contains(candidate)
                ) {
                    removals.add(unsolved, candidate);
                }
            }
        }
    }
}