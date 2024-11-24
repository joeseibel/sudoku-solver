package sudokusolver.javanostreams.logic.simple;

import sudokusolver.javanostreams.Board;
import sudokusolver.javanostreams.Cell;
import sudokusolver.javanostreams.RemoveCandidates;
import sudokusolver.javanostreams.SolvedCell;
import sudokusolver.javanostreams.SudokuNumber;
import sudokusolver.javanostreams.UnsolvedCell;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/*
 * If a cell is solved, then no other cells in the same unit can have that number as a candidate.
 */
public class PruneCandidates {
    public static List<RemoveCandidates> pruneCandidates(Board<Cell> board) {
        var removals = new ArrayList<RemoveCandidates>();
        for (var cell : board.getCells()) {
            if (cell instanceof UnsolvedCell unsolved) {
                var toRemove = EnumSet.noneOf(SudokuNumber.class);
                collectCandidates(board.getRow(cell.row()), toRemove);
                collectCandidates(board.getColumn(cell.column()), toRemove);
                collectCandidates(board.getBlock(cell.block()), toRemove);
                toRemove.retainAll(unsolved.candidates());
                if (!toRemove.isEmpty()) {
                    removals.add(new RemoveCandidates(unsolved, toRemove));
                }
            }
        }
        return removals;
    }

    private static void collectCandidates(List<Cell> unit, EnumSet<SudokuNumber> toRemove) {
        for (var cell : unit) {
            if (cell instanceof SolvedCell(_, _, var value)) {
                toRemove.add(value);
            }
        }
    }
}