package sudokusolver.javanostreams.logic.diabolical;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.builder.GraphBuilder;
import org.jgrapht.nio.dot.DOTExporter;
import sudokusolver.javanostreams.Board;
import sudokusolver.javanostreams.Cell;
import sudokusolver.javanostreams.LocatedCandidate;
import sudokusolver.javanostreams.Removals;
import sudokusolver.javanostreams.RemoveCandidates;
import sudokusolver.javanostreams.Strength;
import sudokusolver.javanostreams.StrengthEdge;
import sudokusolver.javanostreams.SudokuNumber;
import sudokusolver.javanostreams.UnsolvedCell;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*
 * https://www.sudokuwiki.org/XY_Chains
 *
 * XY-Chains is based on a graph type which shares many similarities to X-Cycles. Unlike X-Cycles, an XY-Chains graph
 * includes multiple candidates. This results in a single XY-Chains graph per board whereas there can be up to nine
 * X-Cycles graphs per board, one for each candidate. Each vertex in an XY-Chains graph is a particular candidate in a
 * cell and the edges are either strong or weak links. A strong link connects two candidates of a single cell when they
 * are the only candidates of that cell. A weak link connects two vertices which have the same candidate, are in
 * different cells, but are in the same unit. An XY-Chain is a chain between two vertices of the graph that have the
 * same candidate, the edges of the chain alternate between strong and weak links, and the last links on either end of
 * the chain are strong. If one vertex of a link is the solution, then the other vertex must not be the solution. If one
 * vertex of a strong link is not the solution, then the other vertex must be the solution. When there is a proper chain
 * in the graph it means that one of the two end points must be the solution. The candidate can be removed from any cell
 * of the board which is not an end point of the chain and that cell can see both end points.
 *
 * Note that this implementation of XY-Chains can handle cases in which the chain is not strictly alternating between
 * strong and weak links. It is tolerant of cases in which a strong link takes the place of a weak link.
 */
public class XYChains {
    public static List<RemoveCandidates> xyChains(Board<Cell> board) {
        var removals = new Removals();
        var graph = createStrongLinks(board);
        addWeakLinks(graph);
        var grouping = new HashMap<SudokuNumber, List<LocatedCandidate>>();
        for (var vertex : graph.vertexSet()) {
            grouping.computeIfAbsent(vertex.candidate(), key -> new ArrayList<>()).add(vertex);
        }
        for (var entry : grouping.entrySet()) {
            var candidate = entry.getKey();
            var vertices = entry.getValue();
            for (var i = 0; i < vertices.size() - 1; i++) {
                var vertexA = vertices.get(i);
                var cellA = vertexA.cell();
                for (var j = i + 1; j < vertices.size(); j++) {
                    var vertexB = vertices.get(j);
                    var cellB = vertexB.cell();
                    var visibleCells = new ArrayList<UnsolvedCell>();
                    for (var cell : board.getCells()) {
                        if (cell instanceof UnsolvedCell unsolved &&
                                unsolved.candidates().contains(candidate) &&
                                !unsolved.equals(cellA) && !unsolved.equals(cellB) &&
                                unsolved.isInSameUnit(cellA) && unsolved.isInSameUnit(cellB)
                        ) {
                            visibleCells.add(unsolved);
                        }
                    }
                    if (!visibleCells.isEmpty() && alternatingPathExists(graph, vertexA, vertexB)) {
                        for (var cell : visibleCells) {
                            removals.add(cell, candidate);
                        }
                    }
                }
            }
        }
        return removals.toList();
    }

    public static String toDOT(Graph<LocatedCandidate, StrengthEdge> graph) {
        var writer = new StringWriter();
        var exporter = new DOTExporter<LocatedCandidate, StrengthEdge>();
        exporter.setVertexAttributeProvider(LocatedCandidate.LOCATED_CANDIDATE_ATTRIBUTE_PROVIDER);
        exporter.setEdgeAttributeProvider(Strength.STRENGTH_EDGE_ATTRIBUTE_PROVIDER);
        exporter.exportGraph(graph, writer);
        return writer.toString();
    }

    private static Graph<LocatedCandidate, StrengthEdge> createStrongLinks(Board<Cell> board) {
        var builder = new GraphBuilder<>(new SimpleGraph<LocatedCandidate, StrengthEdge>(StrengthEdge.class));
        for (var cell : board.getCells()) {
            if (cell instanceof UnsolvedCell unsolved && unsolved.candidates().size() == 2) {
                var candidates = unsolved.candidates().toArray(SudokuNumber[]::new);
                var source = new LocatedCandidate(unsolved, candidates[0]);
                var target = new LocatedCandidate(unsolved, candidates[1]);
                builder.addEdge(source, target, new StrengthEdge(Strength.STRONG));
            }
        }
        return builder.build();
    }

    private static void addWeakLinks(Graph<LocatedCandidate, StrengthEdge> graph) {
        var vertices = graph.vertexSet().toArray(LocatedCandidate[]::new);
        for (var i = 0; i < vertices.length - 1; i++) {
            var vertexA = vertices[i];
            var cellA = vertexA.cell();
            var candidateA = vertexA.candidate();
            for (var j = i + 1; j < vertices.length; j++) {
                var vertexB = vertices[j];
                var cellB = vertexB.cell();
                var candidateB = vertexB.candidate();
                if (candidateA == candidateB && cellA.isInSameUnit(cellB)) {
                    graph.addEdge(vertexA, vertexB, new StrengthEdge(Strength.WEAK));
                }
            }
        }
    }

    private static boolean alternatingPathExists(
            Graph<LocatedCandidate, StrengthEdge> graph,
            LocatedCandidate start,
            LocatedCandidate end
    ) {
        return alternatingPathExists(graph, end, start, Strength.STRONG, Set.of(start));
    }

    private static boolean alternatingPathExists(
            Graph<LocatedCandidate, StrengthEdge> graph,
            LocatedCandidate end,
            LocatedCandidate currentVertex,
            Strength nextType,
            Set<LocatedCandidate> visited
    ) {
        var nextVertices = new ArrayList<LocatedCandidate>();
        for (var edge : graph.edgesOf(currentVertex)) {
            if (edge.getStrength().isCompatibleWith(nextType)) {
                nextVertices.add(Graphs.getOppositeVertex(graph, edge, currentVertex));
            }
        }
        if (nextType == Strength.STRONG && nextVertices.contains(end)) {
            return true;
        } else {
            nextVertices.removeAll(visited);
            nextVertices.remove(end);
            for (var nextVertex : nextVertices) {
                var nextVisited = new HashSet<>(visited);
                nextVisited.add(nextVertex);
                if (alternatingPathExists(graph, end, nextVertex, nextType.getOpposite(), nextVisited)) {
                    return true;
                }
            }
            return false;
        }
    }
}
