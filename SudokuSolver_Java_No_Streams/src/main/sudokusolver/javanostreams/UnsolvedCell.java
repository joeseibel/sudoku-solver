package sudokusolver.javanostreams;

import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.DefaultAttribute;

import java.util.EnumSet;
import java.util.Map;

public record UnsolvedCell(int row, int column, EnumSet<SudokuNumber> candidates) implements Cell {
    public UnsolvedCell {
        Board.validateRowAndColumn(row, column);
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("candidates must not be empty.");
        }
    }

    public UnsolvedCell(int row, int column) {
        this(row, column, EnumSet.allOf(SudokuNumber.class));
    }

    public boolean isInSameUnit(UnsolvedCell other) {
        return row == other.row || column == other.column || block() == other.block();
    }

    public String getVertexLabel() {
        return "[" + row + ',' + column + ']';
    }

    public Map<String, Attribute> getVertexAttributes() {
        return Map.of("label", DefaultAttribute.createAttribute(getVertexLabel()));
    }

    @Override
    public String toString() {
        return "0";
    }
}