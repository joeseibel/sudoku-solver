package sudokusolver.javanostreams.logic.tough;

import sudokusolver.javanostreams.Board;
import sudokusolver.javanostreams.Cell;
import sudokusolver.javanostreams.Removals;
import sudokusolver.javanostreams.RemoveCandidates;
import sudokusolver.javanostreams.SudokuNumber;
import sudokusolver.javanostreams.UnsolvedCell;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;

/*
 * https://www.sudokuwiki.org/Sword_Fish_Strategy
 *
 * For a triple of rows, if a candidate appears in two or three cells for each row and the candidate appears in exactly
 * three columns across the three rows, forming a three by three grid, then the candidate must be placed in three of the
 * nine cells. The candidate can be removed from cells which are in the three columns, but different rows.
 *
 * For a triple of columns, if a candidate appears in two or three cells for each column and the candidate appears in
 * exactly three rows across the three columns, forming a three by three grid, then the candidate must be placed in
 * three of the nine cells. The candidate can be removed from cells which are in the three rows, but different columns.
 */
public class Swordfish {
    public static List<RemoveCandidates> swordfish(Board<Cell> board) {
        var removals = new Removals();
        for (var candidate : SudokuNumber.values()) {
            swordfish(removals, candidate, board.rows(), board::getColumn, Cell::column);
            swordfish(removals, candidate, board.getColumns(), board::getRow, Cell::row);
        }
        return removals.toList();
    }

    private static void swordfish(
            Removals removals,
            SudokuNumber candidate,
            List<List<Cell>> units,
            IntFunction<List<Cell>> getOtherUnit,
            ToIntFunction<Cell> getOtherUnitIndex
    ) {
        for (var i = 0; i < units.size() - 2; i++) {
            var unitA = units.get(i);
            var aWithCandidate = new ArrayList<UnsolvedCell>();
            for (var cell : unitA) {
                if (cell instanceof UnsolvedCell unsolved && unsolved.candidates().contains(candidate)) {
                    aWithCandidate.add(unsolved);
                }
            }
            for (var j = i + 1; j < units.size() - 1; j++) {
                var unitB = units.get(j);
                var bWithCandidate = new ArrayList<UnsolvedCell>();
                for (var cell : unitB) {
                    if (cell instanceof UnsolvedCell unsolved && unsolved.candidates().contains(candidate)) {
                        bWithCandidate.add(unsolved);
                    }
                }
                for (var k = j + 1; k < units.size(); k++) {
                    var unitC = units.get(k);
                    var cWithCandidate = new ArrayList<UnsolvedCell>();
                    for (var cell : unitC) {
                        if (cell instanceof UnsolvedCell unsolved && unsolved.candidates().contains(candidate)) {
                            cWithCandidate.add(unsolved);
                        }
                    }
                    if ((aWithCandidate.size() == 2 || aWithCandidate.size() == 3) &&
                            (bWithCandidate.size() == 2 || bWithCandidate.size() == 3) &&
                            (cWithCandidate.size() == 2 || cWithCandidate.size() == 3)
                    ) {
                        var withCandidate = new ArrayList<>(aWithCandidate);
                        withCandidate.addAll(bWithCandidate);
                        withCandidate.addAll(cWithCandidate);
                        var otherUnitIndices = new HashSet<Integer>();
                        for (var cell : withCandidate) {
                            otherUnitIndices.add(getOtherUnitIndex.applyAsInt(cell));
                        }
                        if (otherUnitIndices.size() == 3) {
                            for (var otherUnitIndex : otherUnitIndices) {
                                for (var cell : getOtherUnit.apply(otherUnitIndex)) {
                                    if (cell instanceof UnsolvedCell unsolved &&
                                            unsolved.candidates().contains(candidate) &&
                                            !withCandidate.contains(unsolved)
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
    }
}