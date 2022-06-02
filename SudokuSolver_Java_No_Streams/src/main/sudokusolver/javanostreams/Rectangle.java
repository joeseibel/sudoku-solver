package sudokusolver.javanostreams;

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
                .collect(Pair.zipEveryPair())
                .flatMap(rowIndices -> {
                    var rowA = rowIndices.first();
                    var rowB = rowIndices.second();
                    return IntStream.range(0, Board.UNIT_SIZE)
                            .boxed()
                            .collect(Pair.zipEveryPair())
                            .map(columnIndices -> {
                                var columnA = columnIndices.first();
                                var columnB = columnIndices.second();
                                var cellA = board.get(rowA, columnA);
                                var cellB = board.get(rowA, columnB);
                                var cellC = board.get(rowB, columnA);
                                var cellD = board.get(rowB, columnB);
                                return Stream.of(cellA, cellB, cellC, cellD)
                                        .filter(UnsolvedCell.class::isInstance)
                                        .map(UnsolvedCell.class::cast);
                            })
                            /*
                             * Something weird is happening here with the Java compiler keeping track of the types
                             * through this chain. I initially wanted to put the following toList conversion within and
                             * at the end of the previous map. However, even though the IDE is happy with that, the Java
                             * compiler fails at the end of this chain when constructing a new Rectangle.
                             *
                             * The compiler error that shows up at the end of this chain is the following
                             *     java: incompatible types: invalid constructor reference
                             *         incompatible types: java.lang.Object cannot be converted to java.util.List<sudokusolver.java.UnsolvedCell>
                             *
                             * Obviously, the compiler has somehow lost the type of the stream at
                             * ".map(Rectangle::new)". However, if I don't use a method reference, but instead have a
                             * normal constructor call in a normal lambda, then the compiler is happy, but the IDE gives
                             * a warning stating that the lambda can be converted to a method reference.
                             *
                             * Also, I can explicitly add type information to the above map so that it looks like this:
                             * ".<List<UnsolvedCell>>map(columnIndices -> ...". In that case, the compiler is happy, but
                             * the IDE gives a warning stating that the types can be inferred.
                             *
                             * It would be worthwhile to do two things about this. First, I should switch from using the
                             * Azul Zulu compiler and see if this happens with OpenJDK. Secondly, if the problem
                             * persists with OpenJDK, then I should create a minimal example and file a bug.
                             */
                            .map(Stream::toList)
                            .filter(cells -> cells.size() == 4)
                            .map(Rectangle::new);
                })
                .filter(rectangle -> rectangle.commonCandidates.size() == 2 &&
                        rectangle.cells.stream().map(Cell::block).collect(Collectors.toSet()).size() == 2)
                .toList();
    }
}
