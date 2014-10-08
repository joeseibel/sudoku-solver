package sudokusolver.solutions;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.jgrapht.graph.Pseudograph;

import sudokusolver.Cell;
import sudokusolver.Puzzle;
import sudokusolver.SudokuEdge;
import sudokusolver.SudokuNumber;

public class XCycles {
	public static boolean xCycles(Puzzle puzzle, HashMap<SudokuNumber, Pseudograph<Cell, SudokuEdge>> chains) {
		boolean changeMade = false;
		for (Entry<SudokuNumber, Pseudograph<Cell, SudokuEdge>> entry : chains.entrySet()) {
			buildWeakLinks(puzzle, entry.getKey(), entry.getValue());
			if (xCyclesNiceLoopsRule2(puzzle, entry.getValue(), entry.getKey()) || xCyclesNiceLoopsRule3(puzzle, entry.getValue(), entry.getKey())) {
				return true;
			}
		}
		return changeMade;
	}
	
	private static void buildWeakLinks(Puzzle puzzle, SudokuNumber possibleNumber, Pseudograph<Cell, SudokuEdge> graph) {
		for (Iterable<Cell> row : puzzle.getAllRows()) {
			addWeakPairToGraph(row, possibleNumber, graph);
		}
		for (Iterable<Cell> column : puzzle.getAllColumns()) {
			addWeakPairToGraph(column, possibleNumber, graph);
		}
		for (Iterable<Cell> block : puzzle.getAllBlocks()) {
			addWeakPairToGraph(block, possibleNumber, graph);
		}
	}
	
	private static void addWeakPairToGraph(Iterable<Cell> unit, SudokuNumber possibleNumber, Pseudograph<Cell, SudokuEdge> possibleGraph) {
		ArrayList<Cell> possibleCellsInUnit = new ArrayList<Cell>();
		for (Cell cell : unit) {
			if (cell.getPossibleValues().contains(possibleNumber)) {
				possibleCellsInUnit.add(cell);
			}
		}
		for (int i = 0; i < possibleCellsInUnit.size() - 1; i++) {
			Cell firstCell = possibleCellsInUnit.get(i);
			for (int j = i + 1; j < possibleCellsInUnit.size(); j++) {
				Cell secondCell = possibleCellsInUnit.get(j);
				if (!possibleGraph.containsEdge(firstCell, secondCell)) {
					possibleGraph.addVertex(firstCell);
					possibleGraph.addVertex(secondCell);
					possibleGraph.addEdge(firstCell, secondCell).setLinkType(SudokuEdge.LinkType.WEAK_LINK);
				}
			}
		}
	}
	
	private static boolean xCyclesNiceLoopsRule2(Puzzle puzzle, Pseudograph<Cell, SudokuEdge> graph, SudokuNumber possibleNumber) {
		for (Cell vertex : graph.vertexSet()) {
			ArrayList<SudokuEdge> strongLinks = new ArrayList<SudokuEdge>();
			for (SudokuEdge edge : graph.edgesOf(vertex)) {
				if (edge.getLinkType().equals(SudokuEdge.LinkType.STRONG_LINK)) {
					strongLinks.add(edge);
				}
			}
			for (int i = 0; i < strongLinks.size() - 1; i++) {
				Cell nextVertex = Common.getOtherVertex(graph, strongLinks.get(i), vertex);
				for (int j = i + 1; j < strongLinks.size(); j++) {
					ArrayDeque<Cell> cycle = new ArrayDeque<Cell>();
					cycle.push(vertex);
					cycle.push(nextVertex);
					if (findAlternatingLinkCycle(graph, strongLinks.get(j), true, cycle, nextVertex, false)) {
						assert cycle.size() % 2 == 1;
						puzzle.setValueAndUpdatePossibleValues(vertex, possibleNumber);
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private static boolean xCyclesNiceLoopsRule3(Puzzle puzzle, Pseudograph<Cell, SudokuEdge> graph, SudokuNumber possibleNumber) {
		for (Cell vertex : graph.vertexSet()) {
			SudokuEdge[] edges = graph.edgesOf(vertex).toArray(new SudokuEdge[0]);
			for (int i = 0; i < edges.length - 1; i++) {
				Cell nextVertex = Common.getOtherVertex(graph, edges[i], vertex);
				for (int j = i + 1; j < edges.length; j++) {
					ArrayDeque<Cell> cycle = new ArrayDeque<Cell>();
					cycle.push(vertex);
					cycle.push(nextVertex);
					if (findAlternatingLinkCycle(graph, edges[j], false, cycle, nextVertex, true)) {
						assert cycle.size() % 2 == 1;
						puzzle.removePossibleValue(vertex, possibleNumber);
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private static boolean findAlternatingLinkCycle(Pseudograph<Cell, SudokuEdge> graph, SudokuEdge finalEdge, boolean finalLinkMustBeStrong, ArrayDeque<Cell> cycle,
			Cell vertex, boolean nextLinkMustBeStrong) {
		ArrayList<Cell> possibleNextVerticies = new ArrayList<Cell>();
		for (SudokuEdge nextEdge : graph.edgesOf(vertex)) {
			if (nextEdge.equals(finalEdge)) {
				if (finalLinkMustBeStrong) {
					if (nextLinkMustBeStrong && nextEdge.getLinkType().equals(SudokuEdge.LinkType.STRONG_LINK)) {
						return true;
					}
				} else {
					if (cycle.size() % 2 == 1) {
						return true;
					}
				}
			} else {
				Cell nextVertex = Common.getOtherVertex(graph, nextEdge, vertex);
				if (!cycle.contains(nextVertex) &&
						((nextLinkMustBeStrong && nextEdge.getLinkType().equals(SudokuEdge.LinkType.STRONG_LINK)) || !nextLinkMustBeStrong)) {
					possibleNextVerticies.add(nextVertex);
				}
			}
		}
		for (Cell nextVertex : possibleNextVerticies) {
			cycle.push(nextVertex);
			if (findAlternatingLinkCycle(graph, finalEdge, finalLinkMustBeStrong, cycle, nextVertex, !nextLinkMustBeStrong)) {
				return true;
			}
			cycle.pop();
		}
		return false;
	}
}