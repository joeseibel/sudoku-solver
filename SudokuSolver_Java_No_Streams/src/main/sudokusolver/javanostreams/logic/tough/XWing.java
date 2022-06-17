package sudokusolver.javanostreams.logic.tough;

import sudokusolver.javanostreams.Board;
import sudokusolver.javanostreams.Cell;
import sudokusolver.javanostreams.Removals;
import sudokusolver.javanostreams.RemoveCandidates;
import sudokusolver.javanostreams.SudokuNumber;
import sudokusolver.javanostreams.UnsolvedCell;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;

/*
 * https://www.sudokuwiki.org/X_Wing_Strategy
 *
 * For a pair of rows, if a candidate appears in only two columns of both rows and the columns are the same, forming a
 * rectangle, then the candidate must be placed in opposite corners of the rectangle. The candidate can be removed from
 * cells which are in the two columns, but different rows.
 *
 * For a pair of columns, if a candidate appears in only two rows of both columns and the rows are the ame, forming a
 * rectangle, then the candidate must be placed in opposite corners of the rectangle. The candidate can be removed from
 * cells which are in the two rows, but different columns.
 */
public class XWing {
    public static List<RemoveCandidates> xWing(Board<Cell> board) {
        var removals = new Removals();
        for (var candidate : SudokuNumber.values()) {
            xWing(removals, candidate, board.rows(), board::getColumn, Cell::column);
            xWing(removals, candidate, board.getColumns(), board::getRow, Cell::row);
        }
        return removals.toList();
    }

    private static void xWing(
            Removals removals,
            SudokuNumber candidate,
            List<List<Cell>> units,
            IntFunction<List<Cell>> getOtherUnit,
            ToIntFunction<Cell> getOtherUnitIndex
    ) {
        for (var i = 0; i < units.size() - 1; i++) {
            var unitA = units.get(i);
            var aWithCandidate = new ArrayList<UnsolvedCell>();
            for (var cell : unitA) {
                if (cell instanceof UnsolvedCell unsolved && unsolved.candidates().contains(candidate)) {
                    aWithCandidate.add(unsolved);
                }
            }
            for (var j = i + 1; j < units.size(); j++) {
                var unitB = units.get(j);
                var bWithCandidate = new ArrayList<UnsolvedCell>();
                for (var cell : unitB) {
                    if (cell instanceof UnsolvedCell unsolved && unsolved.candidates().contains(candidate)) {
                        bWithCandidate.add(unsolved);
                    }
                }
                if (aWithCandidate.size() == 2 && bWithCandidate.size() == 2 &&
                        getOtherUnitIndex.applyAsInt(aWithCandidate.get(0)) ==
                                getOtherUnitIndex.applyAsInt(bWithCandidate.get(0)) &&
                        getOtherUnitIndex.applyAsInt(aWithCandidate.get(1)) ==
                                getOtherUnitIndex.applyAsInt(bWithCandidate.get(1))
                ) {
                    for (var cellInUnitA : aWithCandidate) {
                        for (var cell : getOtherUnit.apply(getOtherUnitIndex.applyAsInt(cellInUnitA))) {
                            if (cell instanceof UnsolvedCell unsolved &&
                                    unsolved.candidates().contains(candidate) &&
                                    !unitA.contains(unsolved) &&
                                    !unitB.contains(unsolved)
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