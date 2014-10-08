package sudokusolver.solutions;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.jgrapht.graph.Pseudograph;

import sudokusolver.Cell;
import sudokusolver.PossibleNumberInCell;
import sudokusolver.Puzzle;
import sudokusolver.SudokuEdge;
import sudokusolver.SudokuNumber;

public class AlternatingInferenceChains {
	public static boolean alternatingInferenceChains(Puzzle puzzle) {
		Pseudograph<PossibleNumberInCell, SudokuEdge> graph = new Pseudograph<PossibleNumberInCell, SudokuEdge>(SudokuEdge.class);
		for (Iterable<Cell> row : puzzle.getAllRows()) {
			addPairsToGraph(row, graph);
		}
		for (Iterable<Cell> column : puzzle.getAllColumns()) {
			addPairsToGraph(column, graph);
		}
		for (Iterable<Cell> block : puzzle.getAllBlocks()) {
			addPairsToGraph(block, graph);
		}
		for (Cell cell : puzzle.getAllEmptyCells()) {
			SudokuNumber[] possibleNumbers = cell.getPossibleValues().toArray(new SudokuNumber[cell.getPossibleValues().size()]);
			for (int i = 0; i < possibleNumbers.length - 1; i++) {
				PossibleNumberInCell firstVertex = new PossibleNumberInCell(cell, possibleNumbers[i]);
				for (int j = i + 1; j < possibleNumbers.length; j++) {
					PossibleNumberInCell secondVertex = new PossibleNumberInCell(cell, possibleNumbers[j]);
					assert !graph.containsEdge(firstVertex, secondVertex);
					graph.addVertex(firstVertex);
					graph.addVertex(secondVertex);
					graph.addEdge(firstVertex, secondVertex).setLinkType(possibleNumbers.length == 2 ? SudokuEdge.LinkType.STRONG_LINK : SudokuEdge.LinkType.WEAK_LINK);
				}
			}
		}
		for (PossibleNumberInCell vertex : graph.vertexSet()) {
			ArrayList<SudokuEdge> strongLinks = new ArrayList<SudokuEdge>();
			for (SudokuEdge edge : graph.edgesOf(vertex)) {
				if (edge.getLinkType().equals(SudokuEdge.LinkType.STRONG_LINK)) {
					strongLinks.add(edge);
				}
			}
			for (int i = 0; i < strongLinks.size() - 1; i++) {
				PossibleNumberInCell nextVertex = Common.getOtherVertex(graph, strongLinks.get(i), vertex);
				for (int j = i + 1; j < strongLinks.size(); j++) {
					final ArrayDeque<PossibleNumberInCell> cycle = new ArrayDeque<PossibleNumberInCell>();
					cycle.push(vertex);
					cycle.push(nextVertex);
					if (findAlternatingLinkCycle(graph, strongLinks.get(j), cycle, nextVertex, false)) {
						assert cycle.size() % 2 == 1;
						puzzle.setValueAndUpdatePossibleValues(vertex.getCell(), vertex.getPossibleNumber());
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private static void addPairsToGraph(Iterable<Cell> unit, Pseudograph<PossibleNumberInCell, SudokuEdge> possibleGraph) {
		HashMap<SudokuNumber, ArrayList<Cell>> cellsForPossibleNumber = new HashMap<SudokuNumber, ArrayList<Cell>>();
		for (Cell cell : unit) {
			for (SudokuNumber possibleNumber : cell.getPossibleValues()) {
				ArrayList<Cell> cellsForNumberList = cellsForPossibleNumber.get(possibleNumber);
				if (cellsForNumberList == null) {
					cellsForNumberList = new ArrayList<Cell>();
					cellsForPossibleNumber.put(possibleNumber, cellsForNumberList);
				}
				cellsForNumberList.add(cell);
			}
		}
		for (Entry<SudokuNumber, ArrayList<Cell>> entry : cellsForPossibleNumber.entrySet()) {
			for (int i = 0; i < entry.getValue().size() - 1; i++) {
				PossibleNumberInCell firstVertex = new PossibleNumberInCell(entry.getValue().get(i), entry.getKey());
				for (int j = i + 1; j < entry.getValue().size(); j++) {
					PossibleNumberInCell secondVertex = new PossibleNumberInCell(entry.getValue().get(j), entry.getKey());
					if (!possibleGraph.containsEdge(firstVertex, secondVertex)) {
						possibleGraph.addVertex(firstVertex);
						possibleGraph.addVertex(secondVertex);
						possibleGraph.addEdge(firstVertex, secondVertex).setLinkType(
								entry.getValue().size() == 2 ? SudokuEdge.LinkType.STRONG_LINK : SudokuEdge.LinkType.WEAK_LINK);
					}
				}
			}
		}
	}
	
	private static boolean findAlternatingLinkCycle(Pseudograph<PossibleNumberInCell, SudokuEdge> graph, SudokuEdge finalEdge, ArrayDeque<PossibleNumberInCell> cycle,
			PossibleNumberInCell vertex, boolean nextLinkMustBeStrong) {
		ArrayList<PossibleNumberInCell> possibleNextVerticies = new ArrayList<PossibleNumberInCell>();
		for (SudokuEdge nextEdge : graph.edgesOf(vertex)) {
			if (nextEdge.equals(finalEdge)) {
				if (nextLinkMustBeStrong && nextEdge.getLinkType().equals(SudokuEdge.LinkType.STRONG_LINK)) {
					return true;
				}
			} else {
				PossibleNumberInCell nextVertex = Common.getOtherVertex(graph, nextEdge, vertex);
				if (!cycle.contains(nextVertex) &&
						((nextLinkMustBeStrong && nextEdge.getLinkType().equals(SudokuEdge.LinkType.STRONG_LINK)) || !nextLinkMustBeStrong)) {
					possibleNextVerticies.add(nextVertex);
				}
			}
		}
		for (PossibleNumberInCell nextVertex : possibleNextVerticies) {
			cycle.push(nextVertex);
			if (findAlternatingLinkCycle(graph, finalEdge, cycle, nextVertex, !nextLinkMustBeStrong)) {
				return true;
			}
			cycle.pop();
		}
		return false;
	}
}