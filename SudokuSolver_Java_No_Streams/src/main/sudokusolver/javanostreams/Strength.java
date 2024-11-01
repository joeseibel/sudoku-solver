package sudokusolver.javanostreams;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public enum Strength {
    STRONG {
        @Override
        public Strength getOpposite() {
            return WEAK;
        }

        @Override
        public boolean isCompatibleWith(Strength requiredType) {
            return true;
        }
    },

    WEAK {
        @Override
        public Strength getOpposite() {
            return STRONG;
        }

        @Override
        public boolean isCompatibleWith(Strength requiredType) {
            return requiredType == WEAK;
        }
    };

    public abstract Strength getOpposite();

    /*
     * For solutions that look for alternating edge types in a graph, it can sometimes be the case that a strong link
     * can take the place of a weak link. In those cases, this method should be called instead of performing an equality
     * check.
     */
    public abstract boolean isCompatibleWith(Strength requiredType);

    /*
     * Continuously trims the graph of vertices that cannot be part of a cycle for X-Cycles rule 1. The modified graph
     * will either be empty or only contain vertices with a degree of two or more and be connected by at least one
     * strong link and one weak link.
     */
    public static <V> void trim(Graph<V, StrengthEdge> graph) {
        List<V> toRemove;
        do {
            toRemove = new ArrayList<>();
            outerLoop:
            for (var vertex : graph.vertexSet()) {
                var edges = graph.edgesOf(vertex);
                if (edges.size() >= 2) {
                    for (var edge : edges) {
                        if (edge.getStrength() == STRONG) {
                            continue outerLoop;
                        }
                    }
                }
                toRemove.add(vertex);
            }
            if (!toRemove.isEmpty()) {
                graph.removeAllVertices(toRemove);
            }
        } while (!toRemove.isEmpty());
    }

    public static <V> Set<StrengthEdge> getWeakEdgesInAlternatingCycle(Graph<V, StrengthEdge> graph) {
        var weakEdgesInAlternatingCycle = new HashSet<StrengthEdge>();
        for (var edge : graph.edgeSet()) {
            if (edge.getStrength() == WEAK && !weakEdgesInAlternatingCycle.contains(edge)) {
                weakEdgesInAlternatingCycle.addAll(getAlternatingCycleWeakEdges(graph, edge));
            }
        }
        return weakEdgesInAlternatingCycle;
    }

    private static <V> List<StrengthEdge> getAlternatingCycleWeakEdges(
            Graph<V, StrengthEdge> graph,
            StrengthEdge startEdge
    ) {
        if (startEdge.getStrength() == STRONG) {
            throw new IllegalArgumentException("startEdge must be weak.");
        }
        var start = graph.getEdgeSource(startEdge);
        var end = graph.getEdgeTarget(startEdge);
        var weakEdges = getAlternatingCycleWeakEdges(
                graph,
                end,
                start,
                STRONG,
                Set.of(start),
                List.of(startEdge)
        );
        for (var edge : weakEdges) {
            assert edge.getStrength() == WEAK : "There are strong edges in the return value.";
        }
        return weakEdges;
    }

    private static <V> List<StrengthEdge> getAlternatingCycleWeakEdges(
            Graph<V, StrengthEdge> graph,
            V end,
            V currentVertex,
            Strength nextType,
            Set<V> visited,
            List<StrengthEdge> weakEdges
    ) {
        var nextVertices = new ArrayList<V>();
        for (var edge : graph.edgesOf(currentVertex)) {
            if (edge.getStrength().isCompatibleWith(nextType)) {
                nextVertices.add(Graphs.getOppositeVertex(graph, edge, currentVertex));
            }
        }
        if (nextType == STRONG && nextVertices.contains(end)) {
            return weakEdges;
        } else {
            for (var nextVertex : nextVertices) {
                if (!nextVertex.equals(end) && !visited.contains(nextVertex)) {
                    var nextVisited = new HashSet<>(visited);
                    nextVisited.add(nextVertex);
                    var nextEdge = graph.getEdge(currentVertex, nextVertex);
                    List<StrengthEdge> nextWeakEdges;
                    if (nextEdge.getStrength() == WEAK) {
                        nextWeakEdges = new ArrayList<>(weakEdges);
                        nextWeakEdges.add(nextEdge);
                    } else {
                        nextWeakEdges = weakEdges;
                    }
                    var nextResult = getAlternatingCycleWeakEdges(
                            graph,
                            end,
                            nextVertex,
                            nextType.getOpposite(),
                            nextVisited,
                            nextWeakEdges
                    );
                    if (!nextResult.isEmpty()) {
                        return nextResult;
                    }
                }
            }
            return Collections.emptyList();
        }
    }

    public static <V> boolean alternatingCycleExists(
            Graph<V, StrengthEdge> graph,
            V vertex,
            Strength adjacentEdgesType
    ) {
        var edges = graph.edgesOf(vertex).toArray(StrengthEdge[]::new);
        for (var i = 0; i < edges.length - 1; i++) {
            var edgeA = edges[i];
            if (edgeA.getStrength() == adjacentEdgesType) {
                var start = Graphs.getOppositeVertex(graph, edgeA, vertex);
                for (var j = i + 1; j < edges.length; j++) {
                    var edgeB = edges[j];
                    if (edgeB.getStrength() == adjacentEdgesType) {
                        var end = Graphs.getOppositeVertex(graph, edgeB, vertex);
                        var cycleExists = alternatingCycleExists(
                                graph,
                                adjacentEdgesType,
                                end,
                                start,
                                adjacentEdgesType.getOpposite(),
                                Set.of(vertex, start)
                        );
                        if (cycleExists) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private static <V> boolean alternatingCycleExists(
            Graph<V, StrengthEdge> graph,
            Strength adjacentEdgesType,
            V end,
            V currentVertex,
            Strength nextType,
            Set<V> visited
    ) {
        var nextVertices = new ArrayList<V>();
        for (var edge : graph.edgesOf(currentVertex)) {
            if (edge.getStrength().isCompatibleWith(nextType)) {
                nextVertices.add(Graphs.getOppositeVertex(graph, edge, currentVertex));
            }
        }
        if (adjacentEdgesType.getOpposite() == nextType && nextVertices.contains(end)) {
            return true;
        } else {
            nextVertices.removeAll(visited);
            nextVertices.remove(end);
            for (var nextVertex : nextVertices) {
                var nextVisited = new HashSet<>(visited);
                nextVisited.add(nextVertex);
                var cycleExists = alternatingCycleExists(
                        graph,
                        adjacentEdgesType,
                        end,
                        nextVertex,
                        nextType.getOpposite(),
                        nextVisited
                );
                if (cycleExists) {
                    return true;
                }
            }
            return false;
        }
    }
}