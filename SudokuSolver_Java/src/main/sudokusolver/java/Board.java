package sudokusolver.java;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
