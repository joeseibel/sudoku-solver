package sudokusolver.java;

import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.DefaultAttribute;

import java.util.EnumSet;
import java.util.Map;
import java.util.function.Function;

public record UnsolvedCell(int row, int column, EnumSet<SudokuNumber> candidates) implements Cell {
    public static final Function<UnsolvedCell, Map<String, Attribute>> UNSOLVED_CELL_ATTRIBUTE_PROVIDER = vertex -> {
        var label = "[" + vertex.row() + ',' + vertex.column() + ']';
        return Map.of("label", DefaultAttribute.createAttribute(label));
    };

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

    @Override
    public String toString() {
        return "0";
    }
}
