package sudokusolver.javanostreams.logic.simple;

import sudokusolver.javanostreams.Board;
import sudokusolver.javanostreams.Cell;
import sudokusolver.javanostreams.Removals;
import sudokusolver.javanostreams.RemoveCandidates;
import sudokusolver.javanostreams.SudokuNumber;
import sudokusolver.javanostreams.UnsolvedCell;

import java.util.HashSet;
import java.util.List;
import java.util.function.ToIntFunction;

/*
 * https://www.sudokuwiki.org/Intersection_Removal#LBR
 *
 * For a given row, if a candidate appears in only one block, then the candidate for that block must be placed in that
 * row. The candidate can be removed from the cells which are in the same block, but different rows.
 *
 * For a given column, if a candidate appears in only one block, then the candidate for that block must be placed in
 * that column. The candidate can be removed from cells which are in the same block, but different columns.
 */
public class BoxLineReduction {
    public static List<RemoveCandidates> boxLineReduction(Board<Cell> board) {
        var removals = new Removals();
        for (var candidate : SudokuNumber.values()) {
            boxLineReduction(board, removals, candidate, board.rows(), Cell::row);
            boxLineReduction(board, removals, candidate, board.getColumns(), Cell::column);
        }
        return removals.toList();
    }

    private static void boxLineReduction(
            Board<Cell> board,
            Removals removals,
            SudokuNumber candidate,
            List<List<Cell>> units,
            ToIntFunction<Cell> getUnitIndex
    ) {
        for (var unit : units) {
            var blockIndices = new HashSet<Integer>();
            for (var cell : unit) {
                if (cell instanceof UnsolvedCell unsolved && unsolved.candidates().contains(candidate)) {
                    blockIndices.add(unsolved.block());
                }
            }
            if (blockIndices.size() == 1) {
                var unitIndex = getUnitIndex.applyAsInt(unit.get(0));
                for (var cell : board.getBlock(blockIndices.iterator().next())) {
                    if (cell instanceof UnsolvedCell unsolved &&
                            getUnitIndex.applyAsInt(unsolved) != unitIndex &&
                            unsolved.candidates().contains(candidate)
                    ) {
                        removals.add(unsolved, candidate);
                    }
                }
            }
        }
    }
}