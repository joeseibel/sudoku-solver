package sudokusolver;

public class PossibleNumberInCell {
	private final Cell cell;
	private final SudokuNumber possibleNumber;
	
	public PossibleNumberInCell(Cell cell, SudokuNumber possibleNumber) {
		this.cell = cell;
		this.possibleNumber = possibleNumber;
	}
	
	public Cell getCell() {
		return cell;
	}
	
	public SudokuNumber getPossibleNumber() {
		return possibleNumber;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PossibleNumberInCell) {
			return cell.equals(((PossibleNumberInCell)obj).cell) && possibleNumber.equals(((PossibleNumberInCell)obj).possibleNumber);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return cell.hashCode() ^ possibleNumber.hashCode();
	}
}