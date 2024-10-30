package sudokusolver.java.logic.diabolical;

import org.jgrapht.Graph;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.builder.GraphBuilder;
import org.jgrapht.nio.dot.DOTExporter;
import sudokusolver.java.Board;
import sudokusolver.java.Cell;
import sudokusolver.java.LocatedCandidate;
import sudokusolver.java.Pair;
import sudokusolver.java.RemoveCandidates;
import sudokusolver.java.SetValue;
import sudokusolver.java.Strength;
import sudokusolver.java.StrengthEdge;
import sudokusolver.java.SudokuNumber;
import sudokusolver.java.UnsolvedCell;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;

/*
 * http://www.sudokuwiki.org/X_Cycles
 * http://www.sudokuwiki.org/X_Cycles_Part_2
 *
 * X-Cycles is based on a graph type which is an extension of single's chain. An X-Cycles graph is for a single
 * candidate and can have either strong or weak links. A strong link connects two cells in a unit when they are the only
 * unsolved cells in that unit with the candidate. A weak link connects two cells in a unit when they are not the only
 * unsolved cells in that unit with the candidate. An X-Cycle is a cycle in the graph in which the edges alternate
 * between strong and weak links. If one cell of a link is the solution, then the other cell must not be the solution.
 * If one cell of a strong link is not the solution, then the other cell must be the solution.
 *
 * Note that this implementation of X-Cycles can handle cases in which the chain is not strictly alternating between
 * strong and weak links. It is tolerant of cases in which a strong link takes the place of a weak link.
 */
public class XCycles {
    /*
     * Rule 1:
     *
     * If an X-Cycle has an even number of vertices and therefore continuously alternates between strong and weak, then
     * the graph is perfect and has no flaws. Each of the weak links can be treated as a strong link. The candidate can
     * be removed from any other cell which is in the same unit as both vertices of a weak link.
     */
    public static List<RemoveCandidates> xCyclesRule1(Board<Cell> board) {
        return Arrays.stream(SudokuNumber.values())
                .flatMap(candidate -> {
                    var graph = createStrongLinks(board, candidate);
                    addWeakLinks(graph);
                    Strength.trim(graph);
                    return Strength.getWeakEdgesInAlternatingCycle(graph).stream().flatMap(edge -> {
                        var source = graph.getEdgeSource(edge);
                        var target = graph.getEdgeTarget(edge);
                        var rowRemovals = removeFromUnit(
                                candidate,
                                source,
                                target,
                                Cell::row,
                                board::getRow
                        );
                        var columnRemovals = removeFromUnit(
                                candidate,
                                source,
                                target,
                                Cell::column,
                                board::getColumn
                        );
                        var blockRemovals = removeFromUnit(
                                candidate,
                                source,
                                target,
                                Cell::block,
                                board::getBlock
                        );
                        return Stream.of(rowRemovals, columnRemovals, blockRemovals).flatMap(Function.identity());
                    });
                })
                .collect(LocatedCandidate.mergeToRemoveCandidates());
    }

    private static Stream<LocatedCandidate> removeFromUnit(
            SudokuNumber candidate,
            UnsolvedCell source,
            UnsolvedCell target,
            ToIntFunction<Cell> getUnitIndex,
            IntFunction<List<Cell>> getUnit
    ) {
        if (getUnitIndex.applyAsInt(source) == getUnitIndex.applyAsInt(target)) {
            return getUnit.apply(getUnitIndex.applyAsInt(source))
                    .stream()
                    .filter(UnsolvedCell.class::isInstance)
                    .map(UnsolvedCell.class::cast)
                    .filter(cell -> cell.candidates().contains(candidate) &&
                            !cell.equals(source) && !cell.equals(target))
                    .map(cell -> new LocatedCandidate(cell, candidate));
        } else {
            return Stream.empty();
        }
    }

    /*
     * Rule 2:
     *
     * If an X-Cycle has an odd number of vertices and the edges alternate between strong and weak, except for one
     * vertex which is connected by two strong links, then the graph is a contradiction. Removing the candidate from the
     * vertex of interest implies that the candidate must be the solution for that vertex, thus causing the cycle to
     * contradict itself. However, considering the candidate to be the solution for that vertex does not cause any
     * contradiction in the cycle. Therefore, the candidate must be the solution for that vertex.
     */
    public static List<SetValue> xCyclesRule2(Board<Cell> board) {
        return Arrays.stream(SudokuNumber.values())
                .flatMap(candidate -> {
                    var graph = createStrongLinks(board, candidate);
                    addWeakLinks(graph);
                    return graph.vertexSet()
                            .stream()
                            .filter(vertex -> Strength.alternatingCycleExists(graph, vertex, Strength.STRONG))
                            .map(vertex -> new SetValue(vertex, candidate));
                })
                .toList();
    }

    /*
     * Rule 3:
     *
     * If an X-Cycle has an odd number of vertices and the edges alternate between strong and weak, except for one
     * vertex which is connected by two weak links, then the graph is a contradiction. Considering the candidate to be
     * the solution for the vertex of interest implies that the candidate must be removed from that vertex, thus causing
     * the cycle to contradict itself. However, removing the candidate from that vertex does not cause any contradiction
     * in the cycle. Therefore, the candidate can be removed from the vertex.
     */
    public static List<RemoveCandidates> xCyclesRule3(Board<Cell> board) {
        return Arrays.stream(SudokuNumber.values())
                .flatMap(candidate -> {
                    var graph = createStrongLinks(board, candidate);
                    addWeakLinks(graph);
                    additionalWeakLinks(graph, board, candidate);
                    return graph.vertexSet()
                            .stream()
                            .filter(vertex -> Strength.alternatingCycleExists(graph, vertex, Strength.WEAK))
                            .map(vertex -> new LocatedCandidate(vertex, candidate));
                })
                .collect(LocatedCandidate.mergeToRemoveCandidates());
    }

    public static String toDOT(Graph<UnsolvedCell, StrengthEdge> graph, SudokuNumber candidate) {
        var writer = new StringWriter();
        var exporter = new DOTExporter<UnsolvedCell, StrengthEdge>();
        exporter.setGraphIdProvider(candidate::toString);
        exporter.setVertexAttributeProvider(UnsolvedCell.UNSOLVED_CELL_ATTRIBUTE_PROVIDER);
        exporter.setEdgeAttributeProvider(Strength.STRENGTH_EDGE_ATTRIBUTE_PROVIDER);
        exporter.exportGraph(graph, writer);
        return writer.toString();
    }

    private static Graph<UnsolvedCell, StrengthEdge> createStrongLinks(Board<Cell> board, SudokuNumber candidate) {
        var builder = new GraphBuilder<>(new SimpleGraph<UnsolvedCell, StrengthEdge>(StrengthEdge.class));
        board.getUnits()
                .stream()
                .map(unit -> unit.stream()
                        .filter(UnsolvedCell.class::isInstance)
                        .map(UnsolvedCell.class::cast)
                        .filter(cell -> cell.candidates().contains(candidate))
                        .toList())
                .filter(withCandidate -> withCandidate.size() == 2)
                .forEach(withCandidate -> {
                    var a = withCandidate.get(0);
                    var b = withCandidate.get(1);
                    builder.addEdge(a, b, new StrengthEdge(Strength.STRONG));
                });
        return builder.build();
    }

    private static void addWeakLinks(Graph<UnsolvedCell, StrengthEdge> graph) {
        graph.vertexSet()
                .stream()
                .collect(Pair.zipEveryPair())
                .filter(pair -> {
                    var a = pair.first();
                    var b = pair.second();
                    return a.isInSameUnit(b) && !graph.containsEdge(a, b);
                })
                .forEach(pair -> {
                    var a = pair.first();
                    var b = pair.second();
                    graph.addEdge(a, b, new StrengthEdge(Strength.WEAK));
                });
    }

    private static void additionalWeakLinks(
            Graph<UnsolvedCell, StrengthEdge> graph,
            Board<Cell> board,
            SudokuNumber candidate
    ) {
        var vertices = Set.copyOf(graph.vertexSet());
        board.getCells()
                .stream()
                .filter(UnsolvedCell.class::isInstance)
                .map(UnsolvedCell.class::cast)
                .filter(cell -> cell.candidates().contains(candidate) && !vertices.contains(cell))
                .forEach(cell -> vertices.stream()
                        .filter(vertex -> vertex.isInSameUnit(cell))
                        .forEach(vertex -> {
                            graph.addVertex(cell);
                            graph.addEdge(vertex, cell, new StrengthEdge(Strength.WEAK));
                        }));
    }
}
