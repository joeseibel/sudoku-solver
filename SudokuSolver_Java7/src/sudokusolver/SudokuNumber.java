package sudokusolver;

public enum SudokuNumber {
	ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE;
	
	@Override
	public String toString() {
		return Integer.toString(ordinal() + 1);
	}
}