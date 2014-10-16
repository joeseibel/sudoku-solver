package sudokusolver.solutions;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.Pseudograph;

import sudokusolver.SudokuEdge;
import sudokusolver.VertexColor;

public class Common {
	public static <V, E> ArrayList<Pseudograph<V, E>> getConnectedSubgraphs(Pseudograph<V, E> graph, Class<? extends E> edgeClass) {
		ArrayList<Pseudograph<V, E>> connectedSubgraphs = new ArrayList<>();
		ConnectivityInspector<V, E> inspector = new ConnectivityInspector<>(graph);
		if (inspector.isGraphConnected()) {
			connectedSubgraphs.add(graph);
		} else {
			for (Set<V> subgraphVerticies : inspector.connectedSets()) {
				Pseudograph<V, E> subgraph = new Pseudograph<>(edgeClass);
				for (V vertex : subgraphVerticies) {
					subgraph.addVertex(vertex);
				}
				for (E edge : graph.edgeSet()) {
					if (subgraphVerticies.contains(graph.getEdgeSource(edge))) {
						subgraph.addEdge(graph.getEdgeSource(edge), graph.getEdgeTarget(edge));
					}
				}
				connectedSubgraphs.add(subgraph);
			}
		}
		return connectedSubgraphs;
	}
	
	public static <V, E> void colorGraph(Pseudograph<V, E> graph, HashMap<V, VertexColor> vertexColors, V previousVertex,
			VertexColor nextColor) {
		for (E connectingEdge : graph.edgesOf(previousVertex)) {
			V nextVertex = getOtherVertex(graph, connectingEdge, previousVertex);
			VertexColor colorOfNextVertex = vertexColors.get(nextVertex);
			if (colorOfNextVertex == null) {
				vertexColors.put(nextVertex, nextColor);
				colorGraph(graph, vertexColors, nextVertex, nextColor.getOpposite());
			} else if (colorOfNextVertex.equals(nextColor)) {
				//No worries. Unwind call stack.
			} else {
				assert false;
			}
		}
	}
	
	public static <V, E> V getOtherVertex(Pseudograph<V, E> graph, E edge, V vertex) {
		V otherVertex = graph.getEdgeSource(edge);
		if (otherVertex.equals(vertex)) {
			otherVertex = graph.getEdgeTarget(edge);
		}
		return otherVertex;
	}
	
	public static <V> boolean findAlternatingLinkCycle(Pseudograph<V, SudokuEdge> graph, SudokuEdge finalEdge, boolean finalLinkMustBeStrong, ArrayDeque<V> cycle,
			V vertex, boolean nextLinkMustBeStrong) {
		ArrayList<V> possibleNextVerticies = new ArrayList<>();
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
				V nextVertex = Common.getOtherVertex(graph, nextEdge, vertex);
				if (!cycle.contains(nextVertex) &&
						((nextLinkMustBeStrong && nextEdge.getLinkType().equals(SudokuEdge.LinkType.STRONG_LINK)) || !nextLinkMustBeStrong)) {
					possibleNextVerticies.add(nextVertex);
				}
			}
		}
		for (V nextVertex : possibleNextVerticies) {
			cycle.push(nextVertex);
			if (findAlternatingLinkCycle(graph, finalEdge, finalLinkMustBeStrong, cycle, nextVertex, !nextLinkMustBeStrong)) {
				return true;
			}
			cycle.pop();
		}
		return false;
	}
}