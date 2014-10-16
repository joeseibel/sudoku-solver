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
		for (Entry<SudokuNumber, Pseudograph<Cell, SudokuEdge>> entry : chains.entrySet()) {
			if (xCyclesNiceLoopsRule2(puzzle, entry.getValue(), entry.getKey()) || xCyclesNiceLoopsRule3(puzzle, entry.getValue(), entry.getKey())) {
				return true;
			}
		}
		return false;
	}
	
	private static boolean xCyclesNiceLoopsRule2(Puzzle puzzle, Pseudograph<Cell, SudokuEdge> graph, SudokuNumber possibleNumber) {
		for (Cell vertex : graph.vertexSet()) {
			ArrayList<SudokuEdge> strongLinks = new ArrayList<>();
			for (SudokuEdge edge : graph.edgesOf(vertex)) {
				if (edge.getLinkType().equals(SudokuEdge.LinkType.STRONG_LINK)) {
					strongLinks.add(edge);
				}
			}
			for (int i = 0; i < strongLinks.size() - 1; i++) {
				Cell nextVertex = Common.getOtherVertex(graph, strongLinks.get(i), vertex);
				for (int j = i + 1; j < strongLinks.size(); j++) {
					ArrayDeque<Cell> cycle = new ArrayDeque<>();
					cycle.push(vertex);
					cycle.push(nextVertex);
					if (Common.findAlternatingLinkCycle(graph, strongLinks.get(j), true, cycle, nextVertex, false)) {
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
					ArrayDeque<Cell> cycle = new ArrayDeque<>();
					cycle.push(vertex);
					cycle.push(nextVertex);
					if (Common.findAlternatingLinkCycle(graph, edges[j], false, cycle, nextVertex, true)) {
						assert cycle.size() % 2 == 1;
						puzzle.removePossibleValue(vertex, possibleNumber);
						return true;
					}
				}
			}
		}
		return false;
	}
}