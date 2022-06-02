package sudokusolver.javanostreams;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/*
 * For the Java implementation, I decided to have a single Board class whereas in Kotlin, I had three classes:
 * AbstractBoard, Board, and MutableBoard. This difference is reflective of general collections API differences between
 * Kotlin and Java.
 *
 * In Kotlin, there are separate interfaces for List and MutableList. The List interface does not even contain methods
 * such as set or add. This makes it very clear, especially for parameter and return types, whether a list is expected
 * to be mutable. In the Kotlin version of Board, I decided to reflect that so that it is very clear when we are dealing
 * with board that is expected to be mutated.
 *
 * I could have done a similar thing here is Java, but I decided to be more consistent with the Java collections APIs.
 * In Java, there is no interface distinction between mutable and immutable lists. While it is possible to create an
 * unmodifiable list in Java, that enforcement is only at runtime and not on the interface itself. When working with
 * Java lists, it is the programmer's responsibility to be clear about when a list should be mutated and when it
 * shouldn't. In that same spirit, I have decided to have a single Board class that supports mutability. It is my
 * responsibility to ensure that I only mutate a board at appropriate places.
 */
public record Board<T>(List<List<T>> rows) {
    public static final int UNIT_SIZE_SQUARE_ROOT = 3;
    public static final int UNIT_SIZE = UNIT_SIZE_SQUARE_ROOT * UNIT_SIZE_SQUARE_ROOT;
    public static final int UNIT_SIZE_SQUARED = UNIT_SIZE * UNIT_SIZE;

    public Board(List<List<T>> rows) {
        if (rows.size() != UNIT_SIZE) {
            throw new IllegalArgumentException("rows size is " + rows.size() + ", must be " + UNIT_SIZE + '.');
        }
        IntStream.range(0, rows.size()).forEach(index -> {
            var row = rows.get(index);
            if (row.size() != UNIT_SIZE) {
                var message = "rows.get(" + index + ") size is " + row.size() + ", must be " + UNIT_SIZE + '.';
                throw new IllegalArgumentException(message);
            }
        });
        this.rows = rows.stream().<List<T>>map(ArrayList::new).toList();
    }

    public List<List<T>> getColumns() {
        return IntStream.range(0, UNIT_SIZE)
                .mapToObj(index -> rows.stream().map(row -> row.get(index)).toList())
                .toList();
    }

    public List<List<T>> getBlocks() {
        return IntStream.range(0, UNIT_SIZE).mapToObj(this::getBlock).toList();
    }

    public List<List<T>> getUnits() {
        var units = new ArrayList<>(rows);
        units.addAll(getColumns());
        units.addAll(getBlocks());
        return units;
    }

    public List<T> getCells() {
        return rows.stream().flatMap(Collection::stream).toList();
    }

    public T get(int rowIndex, int columnIndex) {
        return rows.get(rowIndex).get(columnIndex);
    }

    public List<T> getRow(int rowIndex) {
        return rows.get(rowIndex);
    }

    public void set(int rowIndex, int columnIndex, T element) {
        rows.get(rowIndex).set(columnIndex, element);
    }

    public List<T> getColumn(int columnIndex) {
        return rows.stream().map(row -> row.get(columnIndex)).toList();
    }

    public List<T> getBlock(int blockIndex) {
        if (blockIndex < 0 || blockIndex >= UNIT_SIZE) {
            var message = "blockIndex is " + blockIndex + ", must be between 0 and " + (UNIT_SIZE - 1) + '.';
            throw new IllegalArgumentException(message);
        }
        var rowIndex = blockIndex / UNIT_SIZE_SQUARE_ROOT * UNIT_SIZE_SQUARE_ROOT;
        var columnIndex = blockIndex % UNIT_SIZE_SQUARE_ROOT * UNIT_SIZE_SQUARE_ROOT;
        return rows.stream().skip(rowIndex).limit(UNIT_SIZE_SQUARE_ROOT).flatMap(row ->
                row.stream().skip(columnIndex).limit(UNIT_SIZE_SQUARE_ROOT)
        ).toList();
    }

    public <R> Board<R> mapCells(Function<? super T, R> mapper) {
        return new Board<>(rows.stream().map(row -> row.stream().map(mapper).toList()).toList());
    }

    @Override
    public String toString() {
        return joinRows(0, UNIT_SIZE_SQUARE_ROOT) + "\n" +
                "------+-------+------\n" +
                joinRows(UNIT_SIZE_SQUARE_ROOT, UNIT_SIZE_SQUARE_ROOT * 2) + "\n" +
                "------+-------+------\n" +
                joinRows(UNIT_SIZE_SQUARE_ROOT * 2, UNIT_SIZE);
    }

    public static String toSimpleString(Board<Cell> board) {
        return board.rows
                .stream()
                .map(row -> row.stream()
                        .map(cell -> {
                            if (cell instanceof SolvedCell solvedCell) {
                                return solvedCell.value().toString();
                            } else {
                                return "0";
                            }
                        })
                        .collect(Collectors.joining()))
                .collect(Collectors.joining());
    }

    public static String toStringWithCandidates(Board<Cell> board) {
        return board.rows
                .stream()
                .map(row -> row.stream()
                        .map(cell -> {
                            if (cell instanceof SolvedCell solvedCell) {
                                return solvedCell.value().toString();
                            } else if (cell instanceof UnsolvedCell unsolvedCell) {
                                var candidates = unsolvedCell.candidates()
                                        .stream()
                                        .map(Object::toString)
                                        .collect(Collectors.joining());
                                return '{' + candidates + '}';
                            } else {
                                throw new IllegalStateException("Unexpected cell: " + cell);
                            }
                        })
                        .collect(Collectors.joining()))
                .collect(Collectors.joining("\n"));
    }

    private String joinRows(int fromIndex, int toIndex) {
        return rows.subList(fromIndex, toIndex)
                .stream()
                .map(row -> {
                    var first = joinCells(row, 0, UNIT_SIZE_SQUARE_ROOT);
                    var second = joinCells(row, UNIT_SIZE_SQUARE_ROOT, UNIT_SIZE_SQUARE_ROOT * 2);
                    var third = joinCells(row, UNIT_SIZE_SQUARE_ROOT * 2, UNIT_SIZE);
                    return first + " | " + second + " | " + third;
                })
                .collect(Collectors.joining("\n"));
    }

    private String joinCells(List<T> row, int fromIndex, int toIndex) {
        return row.subList(fromIndex, toIndex).stream().map(Object::toString).collect(Collectors.joining(" "));
    }

    public static int getBlockIndex(int rowIndex, int columnIndex) {
        return rowIndex / UNIT_SIZE_SQUARE_ROOT * UNIT_SIZE_SQUARE_ROOT + columnIndex / UNIT_SIZE_SQUARE_ROOT;
    }
}
