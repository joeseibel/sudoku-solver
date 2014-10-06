package sudokusolver.solutions;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.jgrapht.graph.Pseudograph;

import sudokusolver.Cell;
import sudokusolver.PossibleNumberInCell;
import sudokusolver.Puzzle;
import sudokusolver.SudokuEdge;
import sudokusolver.SudokuNumber;

public class AlternatingInferenceChains {
	public static boolean alternatingInferenceChains(final Puzzle puzzle) {
		final Pseudograph<PossibleNumberInCell, SudokuEdge> graph = new Pseudograph<PossibleNumberInCell, SudokuEdge>(SudokuEdge.class);
		for (Iterable<Cell> row : puzzle.getAllRows()) {
			addConjugatePairsToGraph(row, graph);
		}
		for (Iterable<Cell> column : puzzle.getAllColumns()) {
			addConjugatePairsToGraph(column, graph);
		}
		for (Iterable<Cell> block : puzzle.getAllBlocks()) {
			addConjugatePairsToGraph(block, graph);
		}
		for (Cell cell : puzzle.getAllEmptyCells()) {
			if (cell.getPossibleValues().size() == 2) {
				Iterator<SudokuNumber> iter = cell.getPossibleValues().iterator();
				PossibleNumberInCell firstVertex = new PossibleNumberInCell(cell, iter.next());
				PossibleNumberInCell secondVertex = new PossibleNumberInCell(cell, iter.next());
				graph.addVertex(firstVertex);
				graph.addVertex(secondVertex);
				graph.addEdge(firstVertex, secondVertex).setLinkType(SudokuEdge.LinkType.STRONG_LINK);
			}
		}
		ArrayList<PossibleNumberInCell> verticies = new ArrayList<PossibleNumberInCell>(graph.vertexSet());
		for (int i = 0; i < verticies.size() - 1; i++) {
			for (int j = i + 1; j < verticies.size(); j++) {
				PossibleNumberInCell a = verticies.get(i);
				PossibleNumberInCell b = verticies.get(j);
				if (a.getCell().isInSameUnit(b.getCell()) && a.getPossibleNumber().equals(b.getPossibleNumber()) && !graph.containsEdge(a, b)) {
					graph.addEdge(a, b).setLinkType(SudokuEdge.LinkType.WEAK_LINK);
				}
			}
		}
		for (final PossibleNumberInCell vertex : graph.vertexSet()) {
			ArrayList<SudokuEdge> strongLinks = new ArrayList<SudokuEdge>();
			for (SudokuEdge edge : graph.edgesOf(vertex)) {
				if (edge.getLinkType().equals(SudokuEdge.LinkType.STRONG_LINK)) {
					strongLinks.add(edge);
				}
			}
			for (int i = 0; i < strongLinks.size() - 1; i++) {
				PossibleNumberInCell nextVertex = graph.getEdgeSource(strongLinks.get(i));
				if (nextVertex.equals(vertex)) {
					nextVertex = graph.getEdgeTarget(strongLinks.get(i));
				}
				if (!vertex.getCell().equals(nextVertex.getCell())) {
					for (int j = i + 1; j < strongLinks.size(); j++) {
						PossibleNumberInCell lastVertex = graph.getEdgeSource(strongLinks.get(j));
						if (lastVertex.equals(vertex)) {
							lastVertex = graph.getEdgeTarget(strongLinks.get(j));
						}
						if (!vertex.getCell().equals(lastVertex.getCell())) {
							final ArrayDeque<PossibleNumberInCell> cycle = new ArrayDeque<PossibleNumberInCell>();
							cycle.push(vertex);
							cycle.push(nextVertex);
							if (findAlternatingLinkCycle(graph, strongLinks.get(j), cycle, nextVertex, false)) {
								puzzle.setValueAndUpdatePossibleValues(vertex.getCell(), vertex.getPossibleNumber());
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}
	
	private static void addConjugatePairsToGraph(Iterable<Cell> unit, Pseudograph<PossibleNumberInCell, SudokuEdge> possibleGraph) {
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
			if (entry.getValue().size() == 2) {
				PossibleNumberInCell firstVertex = new PossibleNumberInCell(entry.getValue().get(0), entry.getKey());
				PossibleNumberInCell secondVertex = new PossibleNumberInCell(entry.getValue().get(1), entry.getKey());
				if (!possibleGraph.containsEdge(firstVertex, secondVertex)) {
					possibleGraph.addVertex(firstVertex);
					possibleGraph.addVertex(secondVertex);
					possibleGraph.addEdge(firstVertex, secondVertex).setLinkType(SudokuEdge.LinkType.STRONG_LINK);
				}
			}
		}
	}
	
	private static boolean findAlternatingLinkCycle(Pseudograph<PossibleNumberInCell, SudokuEdge> graph, SudokuEdge finalEdge, ArrayDeque<PossibleNumberInCell> cycle,
			PossibleNumberInCell vertex, boolean nextLinkShouldBeStrong) {
		ArrayList<PossibleNumberInCell> possibleNextVerticies = new ArrayList<PossibleNumberInCell>();
		for (SudokuEdge nextEdge : graph.edgesOf(vertex)) {
			if (nextEdge.equals(finalEdge)) {
				if (nextLinkShouldBeStrong && nextEdge.getLinkType().equals(SudokuEdge.LinkType.STRONG_LINK)) {
					return true;
				}
			} else {
				PossibleNumberInCell nextVertex = graph.getEdgeSource(nextEdge);
				if (nextVertex.equals(vertex)) {
					nextVertex = graph.getEdgeTarget(nextEdge);
				}
				if (!cycle.contains(nextVertex) &&
						((nextLinkShouldBeStrong && nextEdge.getLinkType().equals(SudokuEdge.LinkType.STRONG_LINK)) || !nextLinkShouldBeStrong)) {
					possibleNextVerticies.add(nextVertex);
				}
			}
		}
		for (PossibleNumberInCell nextVertex : possibleNextVerticies) {
			cycle.push(nextVertex);
			if (findAlternatingLinkCycle(graph, finalEdge, cycle, nextVertex, !nextLinkShouldBeStrong)) {
				return true;
			}
			cycle.pop();
		}
		return false;
	}
}