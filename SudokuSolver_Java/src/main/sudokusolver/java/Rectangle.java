package sudokusolver.java;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Rectangle {
    private final List<UnsolvedCell> cells;
    private final EnumSet<SudokuNumber> commonCandidates;

    private Rectangle(List<UnsolvedCell> cells) {
        this.cells = cells;
        commonCandidates = EnumSet.allOf(SudokuNumber.class);
        cells.forEach(cell -> commonCandidates.retainAll(cell.candidates()));
    }

    public List<UnsolvedCell> getCells() {
        return cells;
    }

    public EnumSet<SudokuNumber> getCommonCandidates() {
        return commonCandidates;
    }

    public List<UnsolvedCell> getFloor() {
        return cells.stream().filter(cell -> cell.candidates().size() == 2).toList();
    }

    public List<UnsolvedCell> getRoof() {
        return cells.stream().filter(cell -> cell.candidates().size() > 2).toList();
    }

    public static List<Rectangle> createRectangles(Board<Cell> board) {
        return IntStream.range(0, Board.UNIT_SIZE)
                .boxed()
                .gather(Pair.zipEveryPair())
                .flatMap(rowIndices -> {
                    var rowA = rowIndices.first();
                    var rowB = rowIndices.second();
                    return IntStream.range(0, Board.UNIT_SIZE)
                            .boxed()
                            .gather(Pair.zipEveryPair())
                            .map(columnIndices -> {
                                var columnA = columnIndices.first();
                                var columnB = columnIndices.second();
                                var cellA = board.get(rowA, columnA);
                                var cellB = board.get(rowA, columnB);
                                var cellC = board.get(rowB, columnA);
                                var cellD = board.get(rowB, columnB);
                                return Stream.of(cellA, cellB, cellC, cellD)
                                        .filter(UnsolvedCell.class::isInstance)
                                        .map(UnsolvedCell.class::cast)
                                        .toList();
                            })
                            .filter(cells -> cells.size() == 4)
                            .map(Rectangle::new);
                })
                .filter(rectangle -> rectangle.commonCandidates.size() == 2 &&
                        rectangle.cells.stream().map(Cell::block).collect(Collectors.toSet()).size() == 2)
                .toList();
    }
}