package sudokusolver.solutions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.UnmodifiableUndirectedGraph;

import sudokusolver.Cell;
import sudokusolver.Puzzle;
import sudokusolver.SudokuEdge;
import sudokusolver.SudokuNumber;
import sudokusolver.VertexColor;

public class SimpleColoring {
	public static boolean simpleColoring(Puzzle puzzle, Map<SudokuNumber, UnmodifiableUndirectedGraph<Cell, SudokuEdge>> chains) {
		boolean changeMade = false;
		for (Entry<SudokuNumber, UnmodifiableUndirectedGraph<Cell, SudokuEdge>> entry : chains.entrySet()) {
			ArrayList<HashMap<Cell, VertexColor>> coloredChains = new ArrayList<>();
			for (UndirectedGraph<Cell, SudokuEdge> chain : Common.getConnectedSubgraphs(entry.getValue(), SudokuEdge.class)) {
				HashMap<Cell, VertexColor> vertexColors = new HashMap<>();
				Cell firstCell = chain.vertexSet().iterator().next();
				vertexColors.put(firstCell, VertexColor.BLACK);
				Common.colorGraph(chain, vertexColors, firstCell, VertexColor.BLUE);
				coloredChains.add(vertexColors);
			}
			//Intentionally used bitwise or operator.  We want to execute both methods.
			if (simpleColoringRule2TwiceInAUnit(puzzle, coloredChains, entry.getKey()) |
					simpleColoringRule5TwoColorsElsewhere(puzzle, coloredChains, entry.getKey())) {
				changeMade = true;
			}
		}
		return changeMade;
	}
	
	private static boolean simpleColoringRule2TwiceInAUnit(Puzzle puzzle, ArrayList<HashMap<Cell, VertexColor>> coloredChains,
			SudokuNumber possibleNumber) {
		boolean changeMade = false;
		for (HashMap<Cell, VertexColor> vertexColors : coloredChains) {
			VertexColor contradictingColor = searchForSameColorInSameUnit(vertexColors);
			if (contradictingColor != null) {
				removePossibleValuesFromColor(puzzle, vertexColors, contradictingColor, possibleNumber);
				changeMade = true;
			}
		}
		return changeMade;
	}
	
	private static VertexColor searchForSameColorInSameUnit(HashMap<Cell, VertexColor> cellColors) {
		ArrayList<Entry<Cell, VertexColor>> entryList = new ArrayList<>(cellColors.entrySet());
		for (int i = 0; i < entryList.size() - 1; i++) {
			for (int j = i + 1; j < entryList.size(); j++) {
				if (entryList.get(i).getValue().equals(entryList.get(j).getValue()) &&
						entryList.get(i).getKey().isInSameUnit(entryList.get(j).getKey())) {
					return entryList.get(i).getValue();
				}
			}
		}
		return null;
	}
	
	private static void removePossibleValuesFromColor(Puzzle puzzle, HashMap<Cell, VertexColor> cellColors, VertexColor contradictingColor,
			SudokuNumber possibleNumber) {
		for (Entry<Cell, VertexColor> entry : cellColors.entrySet()) {
			if (entry.getValue().equals(contradictingColor)) {
				puzzle.removePossibleValue(entry.getKey(), possibleNumber);
			}
		}
	}
	
	private static boolean simpleColoringRule5TwoColorsElsewhere(Puzzle puzzle, ArrayList<HashMap<Cell, VertexColor>> coloredChains,
			SudokuNumber possibleNumber) {
		for (HashMap<Cell, VertexColor> vertexColors : coloredChains) {
			boolean changeMade = false;
			for (Cell cell : puzzle.getAllEmptyCells()) {
				if (!vertexColors.containsKey(cell) && canCellSeeColoredCell(cell, vertexColors, VertexColor.BLACK) &&
						canCellSeeColoredCell(cell, vertexColors, VertexColor.BLUE) && puzzle.removePossibleValue(cell, possibleNumber)) {
					changeMade = true;
				}
			}
			if (changeMade) {
				return true;
			}
		}
		return false;
	}
	
	private static boolean canCellSeeColoredCell(Cell cell, HashMap<Cell, VertexColor> cellColors, VertexColor cellColor) {
		for (Entry<Cell, VertexColor> entry : cellColors.entrySet()) {
			if (entry.getValue().equals(cellColor) && cell.isInSameUnit(entry.getKey())) {
				return true;
			}
		}
		return false;
	}
}