package sudokusolver.solutions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.Pseudograph;

import sudokusolver.VertexColor;

public class Common {
	public static <V, E> ArrayList<Pseudograph<V, E>> getConnectedSubgraphs(Pseudograph<V, E> graph, Class<? extends E> edgeClass) {
		ArrayList<Pseudograph<V, E>> connectedSubgraphs = new ArrayList<Pseudograph<V,E>>();
		ConnectivityInspector<V, E> inspector = new ConnectivityInspector<V, E>(graph);
		if (inspector.isGraphConnected()) {
			connectedSubgraphs.add(graph);
		} else {
			for (Set<V> subgraphVerticies : inspector.connectedSets()) {
				Pseudograph<V, E> subgraph = new Pseudograph<V, E>(edgeClass);
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
			V nextVertex = graph.getEdgeSource(connectingEdge);
			if (nextVertex.equals(previousVertex)) {
				nextVertex = graph.getEdgeTarget(connectingEdge);
			}
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
}