package sudokusolver.java.logic.diabolical;

import sudokusolver.java.Board;
import sudokusolver.java.Cell;
import sudokusolver.java.LocatedCandidate;
import sudokusolver.java.Pair;
import sudokusolver.java.RemoveCandidates;
import sudokusolver.java.SudokuNumber;
import sudokusolver.java.Triple;
import sudokusolver.java.UnsolvedCell;

import java.util.EnumSet;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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
        var rowRemovals = getRemovals(board::get);
        var columnRemovals = getRemovals((columnIndex, rowIndex) ->
                board.get(rowIndex, columnIndex));
        return Stream.concat(rowRemovals, columnRemovals).collect(LocatedCandidate.mergeToRemoveCandidates());
    }

    private static Stream<LocatedCandidate> getRemovals(BiFunction<Integer, Integer, Cell> getCell) {
        return IntStream.range(0, Board.UNIT_SIZE)
                .boxed()
                .collect(Pair.zipEveryPair())
                .flatMap(unitIndices -> {
                    var unitIndexA = unitIndices.first();
                    var unitIndexB = unitIndices.second();
                    return IntStream.range(0, Board.UNIT_SIZE)
                            .boxed()
                            .collect(Triple.zipEveryTriple())
                            .map(otherUnitIndices -> {
                                var otherUnitIndexA = otherUnitIndices.first();
                                var otherUnitIndexB = otherUnitIndices.second();
                                var otherUnitIndexC = otherUnitIndices.third();
                                var unitA = List.of(
                                        getCell.apply(unitIndexA, otherUnitIndexA),
                                        getCell.apply(unitIndexA, otherUnitIndexB),
                                        getCell.apply(unitIndexA, otherUnitIndexC)
                                );
                                var unitB = List.of(
                                        getCell.apply(unitIndexB, otherUnitIndexA),
                                        getCell.apply(unitIndexB, otherUnitIndexB),
                                        getCell.apply(unitIndexB, otherUnitIndexC)
                                );
                                return new Pair<>(unitA, unitB);
                            });
                })
                .filter(pair -> {
                    var unitA = pair.first();
                    var unitB = pair.second();
                    return unitA.stream().allMatch(UnsolvedCell.class::isInstance) &&
                            unitB.stream().allMatch(UnsolvedCell.class::isInstance) &&
                            Stream.concat(unitA.stream(), unitB.stream()).map(Cell::block).distinct().count() == 3;
                })
                .map(pair -> {
                    var unitA = pair.first().stream().map(UnsolvedCell.class::cast).toList();
                    var unitB = pair.second().stream().map(UnsolvedCell.class::cast).toList();
                    return new Pair<>(unitA, unitB);
                })
                .flatMap(pair -> {
                    var unitA = pair.first();
                    var unitB = pair.second();
                    var unitACandidates = unitA.stream()
                            .flatMap(cell -> cell.candidates().stream())
                            .collect(Collectors.toCollection(() -> EnumSet.noneOf(SudokuNumber.class)));
                    var unitBCandidates = unitB.stream()
                            .flatMap(cell -> cell.candidates().stream())
                            .collect(Collectors.toCollection(() -> EnumSet.noneOf(SudokuNumber.class)));
                    if (unitACandidates.size() == 3) {
                        return getRemovals(unitACandidates, unitB, unitBCandidates);
                    } else if (unitBCandidates.size() == 3) {
                        return getRemovals(unitBCandidates, unitA, unitACandidates);
                    } else {
                        return Stream.empty();
                    }
                });
    }

    private static Stream<LocatedCandidate> getRemovals(
            EnumSet<SudokuNumber> commonCandidates,
            List<UnsolvedCell> unit,
            EnumSet<SudokuNumber> unitCandidates
    ) {
        if (unitCandidates.size() > 3 && unitCandidates.containsAll(commonCandidates)) {
            var withAdditionalList = unit.stream()
                    .filter(cell -> !commonCandidates.containsAll(cell.candidates()))
                    .toList();
            if (withAdditionalList.size() == 1) {
                var withAdditional = withAdditionalList.getFirst();
                var removals = EnumSet.copyOf(withAdditional.candidates());
                removals.retainAll(commonCandidates);
                return removals.stream().map(candidate -> new LocatedCandidate(withAdditional, candidate));
            } else {
                return Stream.empty();
            }
        } else {
            return Stream.empty();
        }
    }
}