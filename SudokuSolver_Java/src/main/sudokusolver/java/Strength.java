package sudokusolver.java;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
            toRemove = graph.vertexSet()
                    .stream()
                    .filter(vertex -> {
                        var edges = graph.edgesOf(vertex);
                        return edges.size() < 2 || edges.stream().noneMatch(edge -> edge.getStrength() == STRONG);
                    })
                    .toList();
            if (!toRemove.isEmpty()) {
                graph.removeAllVertices(toRemove);
            }
        } while (!toRemove.isEmpty());
    }

    public static <V> Set<StrengthEdge> getWeakEdgesInAlternatingCycle(Graph<V, StrengthEdge> graph) {
        var weakEdgesInAlternatingCycle = new HashSet<StrengthEdge>();
        graph.edgeSet()
                .stream()
                .filter(edge -> edge.getStrength() == WEAK && !weakEdgesInAlternatingCycle.contains(edge))
                .forEach(edge -> weakEdgesInAlternatingCycle.addAll(getAlternatingCycleWeakEdges(graph, edge)));
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
        assert weakEdges.stream().noneMatch(edge -> edge.getStrength() == STRONG) :
                "There are strong edges in the return value.";
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
        var nextVertices = graph.edgesOf(currentVertex)
                .stream()
                .filter(edge -> edge.getStrength().isCompatibleWith(nextType))
                .map(edge -> Graphs.getOppositeVertex(graph, edge, currentVertex))
                .toList();
        if (nextType == STRONG && nextVertices.contains(end)) {
            return weakEdges;
        } else {
            return nextVertices.stream()
                    .filter(nextVertex -> !nextVertex.equals(end) && !visited.contains(nextVertex))
                    .map(nextVertex -> {
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
                        return getAlternatingCycleWeakEdges(
                                graph,
                                end,
                                nextVertex,
                                nextType.getOpposite(),
                                nextVisited,
                                nextWeakEdges
                        );
                    })
                    .filter(nextResult -> !nextResult.isEmpty())
                    .findFirst()
                    .orElseGet(Collections::emptyList);
        }
    }

    public static <V> boolean alternatingCycleExists(
            Graph<V, StrengthEdge> graph,
            V vertex,
            Strength adjacentEdgesType
    ) {
        return graph.edgesOf(vertex)
                .stream()
                .filter(edge -> edge.getStrength() == adjacentEdgesType)
                .collect(Pair.zipEveryPair())
                .anyMatch(pair -> {
                    var edgeA = pair.first();
                    var edgeB = pair.second();
                    var start = Graphs.getOppositeVertex(graph, edgeA, vertex);
                    var end = Graphs.getOppositeVertex(graph, edgeB, vertex);
                    return alternatingCycleExists(
                            graph,
                            adjacentEdgesType,
                            end,
                            start,
                            adjacentEdgesType.getOpposite(),
                            Set.of(vertex, start)
                    );
                });
    }

    private static <V> boolean alternatingCycleExists(
            Graph<V, StrengthEdge> graph,
            Strength adjacentEdgesType,
            V end,
            V currentVertex,
            Strength nextType,
            Set<V> visited
    ) {
        var nextVertices = graph.edgesOf(currentVertex)
                .stream()
                .filter(edge -> edge.getStrength().isCompatibleWith(nextType))
                .map(edge -> Graphs.getOppositeVertex(graph, edge, currentVertex))
                .collect(Collectors.toList());
        if (adjacentEdgesType.getOpposite() == nextType && nextVertices.contains(end)) {
            return true;
        } else {
            nextVertices.removeAll(visited);
            nextVertices.remove(end);
            return nextVertices.stream().anyMatch(nextVertex -> {
                var nextVisited = new HashSet<>(visited);
                nextVisited.add(nextVertex);
                return alternatingCycleExists(
                        graph,
                        adjacentEdgesType,
                        end,
                        nextVertex,
                        nextType.getOpposite(),
                        nextVisited
                );
            });
        }
    }
}
