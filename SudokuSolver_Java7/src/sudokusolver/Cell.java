package sudokusolver;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public class Cell {
	private final int row;
	private final int column;
	
	private SudokuNumber value = null;
	private final EnumSet<SudokuNumber> modifiablePossibleValues;
	private final Set<SudokuNumber> unmodifiablePossibleValues;
	
	public Cell(int row, int column, SudokuNumber value) {
		this.row = row;
		this.column = column;
		this.value = value;
		modifiablePossibleValues = value == null ? EnumSet.allOf(SudokuNumber.class) : EnumSet.noneOf(SudokuNumber.class);
		unmodifiablePossibleValues = Collections.unmodifiableSet(modifiablePossibleValues);
	}
	
	public int getRow() {
		return row;
	}
	
	public int getColumn() {
		return column;
	}
	
	public int getBlock() {
		return row - row % Puzzle.UNIT_SIZE_SQUARE_ROOT + column / Puzzle.UNIT_SIZE_SQUARE_ROOT;
	}
	
	public SudokuNumber getValue() {
		return value;
	}
	
	public void setValue(SudokuNumber value) {
		this.value = value;
	}
	
	public Set<SudokuNumber> getPossibleValues() {
		return unmodifiablePossibleValues;
	}
	
	public EnumSet<SudokuNumber> getModifiablePossibleValues() {
		return modifiablePossibleValues;
	}
	
	public boolean isInSameRow(Cell otherCell) {
		return row == otherCell.row;
	}
	
	public boolean isInSameColumn(Cell otherCell) {
		return column == otherCell.column;
	}
	
	public boolean isInSameBlock(Cell otherCell) {
		return row / Puzzle.UNIT_SIZE_SQUARE_ROOT == otherCell.row / Puzzle.UNIT_SIZE_SQUARE_ROOT &&
				column / Puzzle.UNIT_SIZE_SQUARE_ROOT == otherCell.column / Puzzle.UNIT_SIZE_SQUARE_ROOT;
	}
	
	public boolean isInSameUnit(Cell otherCell) {
		return isInSameRow(otherCell) || isInSameColumn(otherCell) || isInSameBlock(otherCell);
	}
	
	@Override
	public String toString() {
		return "[" + row  + "][" + column + "]: " + (value == null ? "0" : value) + " " + unmodifiablePossibleValues;
	}
}