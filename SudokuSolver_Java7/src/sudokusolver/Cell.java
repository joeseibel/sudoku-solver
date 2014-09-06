package sudokusolver;

import java.util.EnumSet;

public class Cell {
	private final int row;
	private final int column;
	
	private SudokuNumber value = null;
	private final EnumSet<SudokuNumber> possibleValues;
	
	public Cell(int row, int column, SudokuNumber value) {
		this.row = row;
		this.column = column;
		this.value = value;
		possibleValues = value == null ? EnumSet.allOf(SudokuNumber.class) : EnumSet.noneOf(SudokuNumber.class);
	}
	
	public int getRow() {
		return row;
	}
	
	public int getColumn() {
		return column;
	}
	
	public int getBlock() {
		return row - row % 3 + column / 3;
	}
	
	public SudokuNumber getValue() {
		return value;
	}
	
	public void setValue(SudokuNumber value) {
		this.value = value;
	}
	
	public EnumSet<SudokuNumber> getPossibleValues() {
		return possibleValues;
	}
	
	public boolean isInSameRow(Cell otherCell) {
		return row == otherCell.row;
	}
	
	public boolean isInSameColumn(Cell otherCell) {
		return column == otherCell.column;
	}
	
	public boolean isInSameBlock(Cell otherCell) {
		return row / 3 == otherCell.row / 3 && column / 3 == otherCell.column / 3;
	}
	
	public boolean isInSameUnit(Cell otherCell) {
		return isInSameRow(otherCell) || isInSameColumn(otherCell) || isInSameBlock(otherCell);
	}
	
	@Override
	public String toString() {
		return "[" + row  + "][" + column + "]: " + (value == null ? "0" : value) + " " + possibleValues;
	}
}