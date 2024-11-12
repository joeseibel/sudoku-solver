package sudokusolver.javanostreams.logic.diabolical;

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
 * https://www.sudokuwiki.org/Jelly_Fish_Strategy
 *
 * For a quad of rows, if a candidate appears in two, three, or four cells for each row and the candidate appears in
 * exactly four columns across the four rows, forming a four by four grid, then the candidate must be placed in four of
 * the sixteen cells. The candidate can be removed from cells which are in the four columns, but different rows.
 *
 * For a quad of columns, if a candidate appears in two, three, or four cells for each column and the candidate appears
 * in exactly four rows across the four columns, forming a four by four grid, then the candidate must be placed in four
 * of the sixteen cells. The candidate can be removed from cells which are in the four rows, but different columns
 */
public class Jellyfish {
    public static List<RemoveCandidates> jellyfish(Board<Cell> board) {
        var removals = new Removals();
        for (var candidate : SudokuNumber.values()) {
            jellyfish(removals, candidate, board.rows(), board::getColumn, Cell::column);
            jellyfish(removals, candidate, board.getColumns(), board::getRow, Cell::row);
        }
        return removals.toList();
    }

    private static void jellyfish(
            Removals removals,
            SudokuNumber candidate,
            List<List<Cell>> units,
            IntFunction<List<Cell>> getOtherUnit,
            ToIntFunction<Cell> getOtherUnitIndex
    ) {
        for (var i = 0; i < units.size() - 3; i++) {
            var aWithCandidate = getUnitWithCandidate(candidate, units, i);
            if (aWithCandidate.size() >= 2 && aWithCandidate.size() <= 4) {
                for (var j = i + 1; j < units.size() - 2; j++) {
                    var bWithCandidate = getUnitWithCandidate(candidate, units, j);
                    if (bWithCandidate.size() >= 2 && bWithCandidate.size() <= 4) {
                        for (var k = j + 1; k < units.size() - 1; k++) {
                            var cWithCandidate = getUnitWithCandidate(candidate, units, k);
                            if (cWithCandidate.size() >= 2 && cWithCandidate.size() <= 4) {
                                for (var l = k + 1; l < units.size(); l++) {
                                    var dWithCandidate = getUnitWithCandidate(candidate, units, l);
                                    if (dWithCandidate.size() >= 2 && dWithCandidate.size() <= 4) {
                                        var withCandidate = new ArrayList<UnsolvedCell>();
                                        withCandidate.addAll(aWithCandidate);
                                        withCandidate.addAll(bWithCandidate);
                                        withCandidate.addAll(cWithCandidate);
                                        withCandidate.addAll(dWithCandidate);
                                        var otherUnitIndices = new HashSet<Integer>();
                                        for (var cell : withCandidate) {
                                            otherUnitIndices.add(getOtherUnitIndex.applyAsInt(cell));
                                        }
                                        if (otherUnitIndices.size() == 4) {
                                            for (var otherUnitIndex : otherUnitIndices) {
                                                for (var cell : getOtherUnit.apply(otherUnitIndex)) {
                                                    if (cell instanceof UnsolvedCell unsolved) {
                                                        if (unsolved.candidates().contains(candidate) &&
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
                }
            }
        }
    }

    private static List<UnsolvedCell> getUnitWithCandidate(SudokuNumber candidate, List<List<Cell>> units, int index) {
        var withCandidate = new ArrayList<UnsolvedCell>();
        for (var cell : units.get(index)) {
            if (cell instanceof UnsolvedCell unsolved && unsolved.candidates().contains(candidate)) {
                withCandidate.add(unsolved);
            }
        }
        return withCandidate;
    }
}