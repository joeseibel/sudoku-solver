package sudokusolver.javanostreams;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;

public class Rectangle {
    private final List<UnsolvedCell> cells;
    private final EnumSet<SudokuNumber> commonCandidates;

    private Rectangle(List<UnsolvedCell> cells) {
        this.cells = cells;
        commonCandidates = EnumSet.allOf(SudokuNumber.class);
        for (var cell : cells) {
            commonCandidates.retainAll(cell.candidates());
        }
    }

    public List<UnsolvedCell> getCells() {
        return cells;
    }

    public EnumSet<SudokuNumber> getCommonCandidates() {
        return commonCandidates;
    }

    public List<UnsolvedCell> getFloor() {
        var floor = new ArrayList<UnsolvedCell>();
        for (var cell : cells) {
            if (cell.candidates().size() == 2) {
                floor.add(cell);
            }
        }
        return floor;
    }

    public List<UnsolvedCell> getRoof() {
        var roof = new ArrayList<UnsolvedCell>();
        for (var cell : cells) {
            if (cell.candidates().size() > 2) {
                roof.add(cell);
            }
        }
        return roof;
    }

    public static List<Rectangle> createRectangles(Board<Cell> board) {
        var rectangles = new ArrayList<Rectangle>();
        for (var rowA = 0; rowA < Board.UNIT_SIZE - 1; rowA++) {
            for (var rowB = rowA + 1; rowB < Board.UNIT_SIZE; rowB++) {
                for (var columnA = 0; columnA < Board.UNIT_SIZE - 1; columnA++) {
                    for (var columnB = columnA + 1; columnB < Board.UNIT_SIZE; columnB++) {
                        if (board.get(rowA, columnA) instanceof UnsolvedCell cellA &&
                                board.get(rowA, columnB) instanceof UnsolvedCell cellB &&
                                board.get(rowB, columnA) instanceof UnsolvedCell cellC &&
                                board.get(rowB, columnB) instanceof UnsolvedCell cellD
                        ) {
                            var cells = List.of(cellA, cellB, cellC, cellD);
                            var blocks = new HashSet<Integer>();
                            for (var cell : cells) {
                                blocks.add(cell.block());
                            }
                            if (blocks.size() == 2) {
                                var rectangle = new Rectangle(cells);
                                if (rectangle.commonCandidates.size() == 2) {
                                    rectangles.add(rectangle);
                                }
                            }
                        }
                    }
                }
            }
        }
        return rectangles;
    }
}