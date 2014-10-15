package sudokusolver.solutions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.Pseudograph;

import sudokusolver.Cell;
import sudokusolver.PossibleNumberInCell;
import sudokusolver.Puzzle;
import sudokusolver.SudokuNumber;
import sudokusolver.VertexColor;

public class Medusa {
	public static boolean medusa(Puzzle puzzle) {
		Pseudograph<PossibleNumberInCell, DefaultEdge> graph = new Pseudograph<>(DefaultEdge.class);
		for (Iterable<Cell> row : puzzle.getAllRows()) {
			buildGraphForUnit(graph, row);
		}
		for (Iterable<Cell> column : puzzle.getAllColumns()) {
			buildGraphForUnit(graph, column);
		}
		for (Iterable<Cell> block : puzzle.getAllBlocks()) {
			buildGraphForUnit(graph, block);
		}
		for (Cell cell : puzzle.getAllEmptyCells()) {
			if (cell.getPossibleValues().size() == 2) {
				Iterator<SudokuNumber> iter = cell.getPossibleValues().iterator();
				PossibleNumberInCell source = new PossibleNumberInCell(cell, iter.next());
				PossibleNumberInCell target = new PossibleNumberInCell(cell, iter.next());
				graph.addVertex(source);
				graph.addVertex(target);
				graph.addEdge(source, target);
			}
		}
		if (!graph.vertexSet().isEmpty()) {
			for (Pseudograph<PossibleNumberInCell, DefaultEdge> subgraph : Common.getConnectedSubgraphs(graph, DefaultEdge.class)) {
				HashMap<PossibleNumberInCell, VertexColor> vertexColors = new HashMap<>();
				PossibleNumberInCell firstVertex = subgraph.vertexSet().iterator().next();
				vertexColors.put(firstVertex, VertexColor.BLACK);
				Common.colorGraph(subgraph, vertexColors, firstVertex, VertexColor.BLUE);
				//Intentionally used bitwise or operator.  We want to execute both methods.
				if (medusaTwoColorsElsewhere(puzzle, vertexColors) | medusaTwoColorsUnitPlusCell(puzzle, vertexColors)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private static void buildGraphForUnit(Pseudograph<PossibleNumberInCell, DefaultEdge> graph, Iterable<Cell> unit) {
		HashMap<SudokuNumber, ArrayList<Cell>> cellsForPossibleNumber = new HashMap<>();
		for (Cell cell : unit) {
			for (SudokuNumber possibleNumber : cell.getPossibleValues()) {
				Common.addToValueList(cellsForPossibleNumber, possibleNumber, cell);
			}
		}
		for (Entry<SudokuNumber, ArrayList<Cell>> entry : cellsForPossibleNumber.entrySet()) {
			if (entry.getValue().size() == 2) {
				PossibleNumberInCell source = new PossibleNumberInCell(entry.getValue().get(0), entry.getKey());
				PossibleNumberInCell target = new PossibleNumberInCell(entry.getValue().get(1), entry.getKey());
				graph.addVertex(source);
				graph.addVertex(target);
				graph.addEdge(source, target);
			}
		}
	}
	
	private static boolean medusaTwoColorsElsewhere(Puzzle puzzle, HashMap<PossibleNumberInCell, VertexColor> vertexColors) {
		HashSet<Cell> cellsContainingColors = new HashSet<>();
		for (PossibleNumberInCell possibleNumberInCell : vertexColors.keySet()) {
			cellsContainingColors.add(possibleNumberInCell.getCell());
		}
		boolean changeMade = false;
		for (Cell cell : puzzle.getAllEmptyCells()) {
			if (!cellsContainingColors.contains(cell)) {
				for (SudokuNumber possibleNumber : cell.getPossibleValues()) {
					boolean canSeeBlackNumber = false;
					boolean canSeeBlueNumber = false;
					for (Iterator<Entry<PossibleNumberInCell, VertexColor>> iter = vertexColors.entrySet().iterator();
							iter.hasNext() && (!canSeeBlackNumber || !canSeeBlueNumber);) {
						Entry<PossibleNumberInCell, VertexColor> coloredNumber = iter.next();
						if (coloredNumber.getKey().getPossibleNumber().equals(possibleNumber) &&
								coloredNumber.getKey().getCell().isInSameUnit(cell)) {
							switch (coloredNumber.getValue()) {
								case BLACK:
									canSeeBlackNumber = true;
									break;
								case BLUE:
									canSeeBlueNumber = true;
									break;
								default:
									assert false;
							}
						}
					}
					if (canSeeBlackNumber && canSeeBlueNumber) {
						puzzle.removePossibleValue(cell, possibleNumber);
						changeMade = true;
					}
				}
			}
		}
		return changeMade;
	}
	
	private static boolean medusaTwoColorsUnitPlusCell(Puzzle puzzle, HashMap<PossibleNumberInCell, VertexColor> vertexColors) {
		boolean changeMade = false;
		for (Entry<PossibleNumberInCell, VertexColor> currentColoredNumber : vertexColors.entrySet()) {
			if (removeOtherPossibleNumberIfItCanSeeOppositeColorInOtherCell(puzzle, vertexColors, currentColoredNumber.getKey().getCell(),
					currentColoredNumber.getValue())) {
				changeMade = true;
			}
		}
		return changeMade;
	}
	
	private static boolean removeOtherPossibleNumberIfItCanSeeOppositeColorInOtherCell(Puzzle puzzle, HashMap<PossibleNumberInCell,
			VertexColor> vertexColors, Cell currentCell, VertexColor currentColor) {
		for (SudokuNumber possibleNumberInCurrentCell : currentCell.getPossibleValues()) {
			if (!vertexColors.containsKey(new PossibleNumberInCell(currentCell, possibleNumberInCurrentCell))) {
				for (Entry<PossibleNumberInCell, VertexColor> otherColoredNumber : vertexColors.entrySet()) {
					Cell otherCell = otherColoredNumber.getKey().getCell();
					SudokuNumber otherPossibleNumber = otherColoredNumber.getKey().getPossibleNumber();
					VertexColor otherColor = otherColoredNumber.getValue();
					if (!otherCell.equals(currentCell) && otherCell.isInSameUnit(currentCell) &&
							otherPossibleNumber.equals(possibleNumberInCurrentCell) && otherColor.equals(currentColor.getOpposite())) {
						puzzle.removePossibleValue(currentCell, possibleNumberInCurrentCell);
						return true;
					}
				}
			}
		}
		return false;
	}
}