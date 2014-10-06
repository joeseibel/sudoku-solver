package sudokusolver.solutions;

import java.util.ArrayList;
import java.util.EnumSet;

import sudokusolver.Cell;
import sudokusolver.Puzzle;
import sudokusolver.SudokuNumber;

public class XYZWing {
	public static boolean xyzWing(Puzzle puzzle) {
		ArrayList<Cell> cellsWithThreePossibleValues = new ArrayList<Cell>();
		ArrayList<Cell> cellsWithTwoPossibleValues = new ArrayList<Cell>();
		for (Cell cell : puzzle.getAllEmptyCells()) {
			if (cell.getPossibleValues().size() == 3) {
				cellsWithThreePossibleValues.add(cell);
			} else if (cell.getPossibleValues().size() == 2) {
				cellsWithTwoPossibleValues.add(cell);
			}
		}
		for (Cell pivotCell : cellsWithThreePossibleValues) {
			ArrayList<Cell> wingCandidates = new ArrayList<Cell>();
			for (Cell cell : cellsWithTwoPossibleValues) {
				if (pivotCell.isInSameUnit(cell) && pivotCell.getPossibleValues().containsAll(cell.getPossibleValues())) {
					wingCandidates.add(cell);
				}
			}
			for (int i = 0; i < wingCandidates.size() - 1; i++) {
				for (int j = i + 1; j < wingCandidates.size(); j++) {
					EnumSet<SudokuNumber> possibleNumberIntersection = EnumSet.copyOf(wingCandidates.get(i).getPossibleValues());
					possibleNumberIntersection.retainAll(wingCandidates.get(j).getPossibleValues());
					if (possibleNumberIntersection.size() == 1 && !wingCandidates.get(i).isInSameUnit(wingCandidates.get(j))) {
						boolean changeMade = false;
						for (Cell emptyCell : puzzle.getAllEmptyCells()) {
							if (!emptyCell.equals(pivotCell) && !emptyCell.equals(wingCandidates.get(i)) &&
									!emptyCell.equals(wingCandidates.get(j)) && emptyCell.isInSameUnit(pivotCell) &&
									emptyCell.isInSameUnit(wingCandidates.get(i)) && emptyCell.isInSameUnit(wingCandidates.get(j)) &&
									puzzle.removePossibleValues(emptyCell, possibleNumberIntersection)) {
								changeMade = true;
							}
						}
						if (changeMade) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}
}