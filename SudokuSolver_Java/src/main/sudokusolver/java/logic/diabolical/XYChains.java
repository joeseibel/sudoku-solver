package sudokusolver.java.logic.diabolical;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.builder.GraphBuilder;
import org.jgrapht.nio.dot.DOTExporter;
import sudokusolver.java.Board;
import sudokusolver.java.Cell;
import sudokusolver.java.LocatedCandidate;
import sudokusolver.java.Pair;
import sudokusolver.java.RemoveCandidates;
import sudokusolver.java.Strength;
import sudokusolver.java.StrengthEdge;
import sudokusolver.java.SudokuNumber;
import sudokusolver.java.UnsolvedCell;

import java.io.StringWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        var graph = createStrongLinks(board);
        addWeakLinks(graph);
        return graph.vertexSet()
                .stream()
                .collect(Collectors.groupingBy(LocatedCandidate::candidate))
                .entrySet()
                .stream()
                .flatMap(entry -> {
                    var candidate = entry.getKey();
                    var vertices = entry.getValue();
                    return vertices.stream()
                            .collect(Pair.zipEveryPair())
                            .flatMap(pair -> {
                                var vertexA = pair.first();
                                var vertexB = pair.second();
                                var cellA = vertexA.cell();
                                var cellB = vertexB.cell();
                                var visibleCells = board.getCells()
                                        .stream()
                                        .filter(UnsolvedCell.class::isInstance)
                                        .map(UnsolvedCell.class::cast)
                                        .filter(cell -> cell.candidates().contains(candidate) &&
                                                !cell.equals(cellA) && !cell.equals(cellB) &&
                                                cell.isInSameUnit(cellA) && cell.isInSameUnit(cellB))
                                        .toList();
                                if (!visibleCells.isEmpty() && alternatingPathExists(graph, vertexA, vertexB)) {
                                    return visibleCells.stream().map(cell -> new LocatedCandidate(cell, candidate));
                                } else {
                                    return Stream.empty();
                                }
                            });
                })
                .collect(LocatedCandidate.mergeToRemoveCandidates());
    }

    public static String toDOT(Graph<LocatedCandidate, StrengthEdge> graph) {
        var writer = new StringWriter();
        var exporter = new DOTExporter<LocatedCandidate, StrengthEdge>();
        exporter.setVertexAttributeProvider(LocatedCandidate::getVertexAttributes);
        exporter.setEdgeAttributeProvider(StrengthEdge::getEdgeAttributes);
        exporter.exportGraph(graph, writer);
        return writer.toString();
    }

    private static Graph<LocatedCandidate, StrengthEdge> createStrongLinks(Board<Cell> board) {
        var builder = new GraphBuilder<>(new SimpleGraph<LocatedCandidate, StrengthEdge>(StrengthEdge.class));
        board.getCells()
                .stream()
                .filter(UnsolvedCell.class::isInstance)
                .map(UnsolvedCell.class::cast)
                .filter(cell -> cell.candidates().size() == 2)
                .forEach(cell -> {
                    var candidates = cell.candidates().toArray(SudokuNumber[]::new);
                    var source = new LocatedCandidate(cell, candidates[0]);
                    var target = new LocatedCandidate(cell, candidates[1]);
                    builder.addEdge(source, target, new StrengthEdge(Strength.STRONG));
                });
        return builder.build();
    }

    private static void addWeakLinks(Graph<LocatedCandidate, StrengthEdge> graph) {
        graph.vertexSet()
                .stream()
                .collect(Pair.zipEveryPair())
                .filter(pair -> {
                    var vertexA = pair.first();
                    var cellA = vertexA.cell();
                    var candidateA = vertexA.candidate();
                    var vertexB = pair.second();
                    var cellB = vertexB.cell();
                    var candidateB = vertexB.candidate();
                    return candidateA == candidateB && cellA.isInSameUnit(cellB);
                })
                .forEach(pair -> {
                    var vertexA = pair.first();
                    var vertexB = pair.second();
                    graph.addEdge(vertexA, vertexB, new StrengthEdge(Strength.WEAK));
                });
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
        var nextVertices = graph.edgesOf(currentVertex)
                .stream()
                .filter(edge -> edge.getStrength().isCompatibleWith(nextType))
                .map(edge -> Graphs.getOppositeVertex(graph, edge, currentVertex))
                .collect(Collectors.toList());
        if (nextType == Strength.STRONG && nextVertices.contains(end)) {
            return true;
        } else {
            nextVertices.removeAll(visited);
            nextVertices.remove(end);
            return nextVertices.stream().anyMatch(nextVertex -> {
                var nextVisited = new HashSet<>(visited);
                nextVisited.add(nextVertex);
                return alternatingPathExists(graph, end, nextVertex, nextType.getOpposite(), nextVisited);
            });
        }
    }
}
