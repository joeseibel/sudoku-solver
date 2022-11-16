package sudokusolver.javanostreams.logic.diabolical;

import sudokusolver.javanostreams.Board;
import sudokusolver.javanostreams.Cell;
import sudokusolver.javanostreams.Removals;
import sudokusolver.javanostreams.RemoveCandidates;
import sudokusolver.javanostreams.SudokuNumber;
import sudokusolver.javanostreams.UnsolvedCell;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.function.BiFunction;

/*
 * https://www.sudokuwiki.org/Extended_Unique_Rectangles
 *
 * Extended Unique Rectangles are like Unique Rectangles except that they are 2x3 instead of 2x2. The cells in the
 * rectangle must be spread over three blocks and the dimension that has three elements must be spread over three units
 * (rows or columns). If there are only three candidates found among the six cells, then such a rectangle is the Deadly
 * Pattern. If there is one cell with additional candidates, then the removal of such candidates would lead to a Deadly
 * Pattern. The common candidates can be removed from the cell leaving only the additional candidates remaining.
 */
public class ExtendedUniqueRectangles {
    public static List<RemoveCandidates> extendedUniqueRectangles(Board<Cell> board) {
        var removals = new Removals();
        getRemovals(removals, board::get);
        getRemovals(removals, (columnIndex, rowIndex) -> board.get(rowIndex, columnIndex));
        return removals.toList();
    }

    private static void getRemovals(Removals removals, BiFunction<Integer, Integer, Cell> getCell) {
        for (var unitIndexA = 0; unitIndexA < Board.UNIT_SIZE - 1; unitIndexA++) {
            for (var unitIndexB = unitIndexA + 1; unitIndexB < Board.UNIT_SIZE; unitIndexB++) {
                for (var otherUnitIndexA = 0; otherUnitIndexA < Board.UNIT_SIZE - 2; otherUnitIndexA++) {
                    for (var otherUnitIndexB = otherUnitIndexA + 1;
                         otherUnitIndexB < Board.UNIT_SIZE - 1;
                         otherUnitIndexB++
                    ) {
                        for (var otherUnitIndexC = otherUnitIndexB + 1;
                             otherUnitIndexC < Board.UNIT_SIZE;
                             otherUnitIndexC++
                        ) {
                            getRemovals(removals,
                                    unitIndexA, unitIndexB,
                                    otherUnitIndexA, otherUnitIndexB, otherUnitIndexC,
                                    getCell
                            );
                        }
                    }
                }
            }
        }
    }

    /*
     * This method used to be a part of the previous method, but was extracted to resolve the warning, "Method
     * 'getRemovals' is too complex to analyze by data flow algorithm."
     */
    private static void getRemovals(
            Removals removals,
            int unitIndexA, int unitIndexB,
            int otherUnitIndexA, int otherUnitIndexB, int otherUnitIndexC,
            BiFunction<Integer, Integer, Cell> getCell
    ) {
        var unitA = new ArrayList<UnsolvedCell>();
        if (getCell.apply(unitIndexA, otherUnitIndexA) instanceof UnsolvedCell unsolved) {
            unitA.add(unsolved);
        }
        if (getCell.apply(unitIndexA, otherUnitIndexB) instanceof UnsolvedCell unsolved) {
            unitA.add(unsolved);
        }
        if (getCell.apply(unitIndexA, otherUnitIndexC) instanceof UnsolvedCell unsolved) {
            unitA.add(unsolved);
        }
        if (unitA.size() == 3) {
            var unitB = new ArrayList<UnsolvedCell>();
            if (getCell.apply(unitIndexB, otherUnitIndexA) instanceof UnsolvedCell unsolved) {
                unitB.add(unsolved);
            }
            if (getCell.apply(unitIndexB, otherUnitIndexB) instanceof UnsolvedCell unsolved) {
                unitB.add(unsolved);
            }
            if (getCell.apply(unitIndexB, otherUnitIndexC) instanceof UnsolvedCell unsolved) {
                unitB.add(unsolved);
            }
            if (unitB.size() == 3) {
                var blockIndices = new HashSet<Integer>();
                for (var cell : unitA) {
                    blockIndices.add(cell.block());
                }
                for (var cell : unitB) {
                    blockIndices.add(cell.block());
                }
                if (blockIndices.size() == 3) {
                    var unitACandidates = EnumSet.noneOf(SudokuNumber.class);
                    for (var cell : unitA) {
                        unitACandidates.addAll(cell.candidates());
                    }
                    var unitBCandidates = EnumSet.noneOf(SudokuNumber.class);
                    for (var cell : unitB) {
                        unitBCandidates.addAll(cell.candidates());
                    }
                    if (unitACandidates.size() == 3) {
                        getRemovals(removals, unitACandidates, unitB, unitBCandidates);
                    } else if (unitBCandidates.size() == 3) {
                        getRemovals(removals, unitBCandidates, unitA, unitACandidates);
                    }
                }
            }
        }
    }

    private static void getRemovals(
            Removals removals,
            EnumSet<SudokuNumber> commonCandidates,
            List<UnsolvedCell> unit,
            EnumSet<SudokuNumber> unitCandidates
    ) {
        if (unitCandidates.size() > 3 && unitCandidates.containsAll(commonCandidates)) {
            var withAdditionalList = new ArrayList<UnsolvedCell>();
            for (var cell : unit) {
                if (!commonCandidates.containsAll(cell.candidates())) {
                    withAdditionalList.add(cell);
                }
            }
            if (withAdditionalList.size() == 1) {
                var withAdditional = withAdditionalList.get(0);
                var toRemove = EnumSet.copyOf(withAdditional.candidates());
                toRemove.retainAll(commonCandidates);
                removals.add(withAdditional, toRemove);
            }
        }
    }
}