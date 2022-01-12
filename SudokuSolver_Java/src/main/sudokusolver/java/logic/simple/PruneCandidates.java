package sudokusolver.java.logic.simple;

import sudokusolver.java.Board;
import sudokusolver.java.Cell;
import sudokusolver.java.RemoveCandidates;
import sudokusolver.java.SolvedCell;
import sudokusolver.java.SudokuNumber;
import sudokusolver.java.UnsolvedCell;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
 * If a cell is solved, then no other cells in the same unit can have that number as a candidate.
 */
public class PruneCandidates {
    public static List<RemoveCandidates> pruneCandidates(Board<Cell> board) {
        return board.getCells()
                .stream()
                .filter(UnsolvedCell.class::isInstance)
                .map(UnsolvedCell.class::cast)
                .flatMap(cell -> {
                    var sameUnits = new ArrayList<Cell>();
                    sameUnits.addAll(board.getRow(cell.row()));
                    sameUnits.addAll(board.getColumn(cell.column()));
                    sameUnits.addAll(board.getBlock(cell.block()));
                    var toRemove = sameUnits.stream()
                            .filter(SolvedCell.class::isInstance)
                            .map(SolvedCell.class::cast)
                            .map(SolvedCell::value)
                            .collect(Collectors.toCollection(() -> EnumSet.noneOf(SudokuNumber.class)));
                    toRemove.retainAll(cell.candidates());
                    return toRemove.isEmpty() ? Stream.empty() : Stream.of(new RemoveCandidates(cell, toRemove));
                })
                .toList();
    }
}
