package sudokusolver.javanostreams;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

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
        for (var index = 0; index < rows.size(); index++) {
            var row = rows.get(index);
            if (row.size() != UNIT_SIZE) {
                var message = "rows.get(" + index + ") size is " + row.size() + ", must be " + UNIT_SIZE + '.';
                throw new IllegalArgumentException(message);
            }
        }
        this.rows = new ArrayList<>();
        for (var row : rows) {
            this.rows.add(new ArrayList<>(row));
        }
    }

    public List<List<T>> getColumns() {
        var columns = new ArrayList<List<T>>();
        for (var index = 0; index < UNIT_SIZE; index++) {
            var column = new ArrayList<T>();
            for (var row : rows) {
                column.add(row.get(index));
            }
            columns.add(column);
        }
        return columns;
    }

    public List<List<T>> getBlocks() {
        var blocks = new ArrayList<List<T>>();
        for (var index = 0; index < UNIT_SIZE; index++) {
            blocks.add(getBlock(index));
        }
        return blocks;
    }

    public List<List<T>> getUnits() {
        var units = new ArrayList<>(rows);
        units.addAll(getColumns());
        units.addAll(getBlocks());
        return units;
    }

    public List<T> getCells() {
        var cells = new ArrayList<T>();
        for (var row : rows) {
            cells.addAll(row);
        }
        return cells;
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
        var column = new ArrayList<T>();
        for (var row : rows) {
            column.add(row.get(columnIndex));
        }
        return column;
    }

    public List<T> getBlock(int blockIndex) {
        if (blockIndex < 0 || blockIndex >= UNIT_SIZE) {
            var message = "blockIndex is " + blockIndex + ", must be between 0 and " + (UNIT_SIZE - 1) + '.';
            throw new IllegalArgumentException(message);
        }
        var startRowIndex = blockIndex / UNIT_SIZE_SQUARE_ROOT * UNIT_SIZE_SQUARE_ROOT;
        var endRowIndex = startRowIndex + UNIT_SIZE_SQUARE_ROOT;
        var startColumnIndex = blockIndex % UNIT_SIZE_SQUARE_ROOT * UNIT_SIZE_SQUARE_ROOT;
        var block = new ArrayList<T>();
        for (var rowIndex = startRowIndex; rowIndex < endRowIndex; rowIndex++) {
            for (var columnIndex = startColumnIndex;
                 columnIndex < startColumnIndex + UNIT_SIZE_SQUARE_ROOT;
                 columnIndex++
            ) {
                block.add(rows.get(rowIndex).get(columnIndex));
            }
        }
        return block;
    }

    public <R> Board<R> mapCells(Function<? super T, R> mapper) {
        var mappedBoard = new ArrayList<List<R>>();
        for (var row : rows) {
            var mappedRow = new ArrayList<R>();
            for (var cell : row) {
                mappedRow.add(mapper.apply(cell));
            }
            mappedBoard.add(mappedRow);
        }
        return new Board<>(mappedBoard);
    }

    @Override
    public String toString() {
        var builder = new StringBuilder();
        joinRows(builder, 0, UNIT_SIZE_SQUARE_ROOT);
        builder.append("\n------+-------+------\n");
        joinRows(builder, UNIT_SIZE_SQUARE_ROOT, UNIT_SIZE_SQUARE_ROOT * 2);
        builder.append("\n------+-------+------\n");
        joinRows(builder, UNIT_SIZE_SQUARE_ROOT * 2, UNIT_SIZE);
        return builder.toString();
    }

    public static String toSimpleString(Board<Cell> board) {
        var builder = new StringBuilder();
        for (var row : board.rows) {
            for (var cell : row) {
                builder.append(cell instanceof SolvedCell solvedCell ? solvedCell.value() : '0');
            }
        }
        return builder.toString();
    }

    public static String toStringWithCandidates(Board<Cell> board) {
        var builder = new StringBuilder();
        for (var row = board.rows.iterator(); row.hasNext();) {
            for (var cell : row.next()) {
                if (cell instanceof SolvedCell solvedCell) {
                    builder.append(solvedCell.value().toString());
                } else if (cell instanceof UnsolvedCell unsolvedCell) {
                    builder.append('{');
                    for (var candidate : unsolvedCell.candidates()) {
                        builder.append(candidate);
                    }
                    builder.append('}');
                } else {
                    throw new IllegalStateException("Unexpected cell: " + cell);
                }
            }
            if (row.hasNext()) {
                builder.append('\n');
            }
        }
        return builder.toString();
    }

    private void joinRows(StringBuilder builder, int fromIndex, int toIndex) {
        for (var rowIterator = rows.subList(fromIndex, toIndex).iterator(); rowIterator.hasNext();) {
            var row = rowIterator.next();
            joinCells(builder, row, 0, UNIT_SIZE_SQUARE_ROOT);
            builder.append(" | ");
            joinCells(builder, row, UNIT_SIZE_SQUARE_ROOT, UNIT_SIZE_SQUARE_ROOT * 2);
            builder.append(" | ");
            joinCells(builder, row, UNIT_SIZE_SQUARE_ROOT * 2, UNIT_SIZE);
            if (rowIterator.hasNext()) {
                builder.append('\n');
            }
        }
    }

    private void joinCells(StringBuilder builder, List<T> row, int fromIndex, int toIndex) {
        for (var cell = row.subList(fromIndex, toIndex).iterator(); cell.hasNext();) {
            builder.append(cell.next());
            if (cell.hasNext()) {
                builder.append(' ');
            }
        }
    }

    public static int getBlockIndex(int rowIndex, int columnIndex) {
        return rowIndex / UNIT_SIZE_SQUARE_ROOT * UNIT_SIZE_SQUARE_ROOT + columnIndex / UNIT_SIZE_SQUARE_ROOT;
    }
}