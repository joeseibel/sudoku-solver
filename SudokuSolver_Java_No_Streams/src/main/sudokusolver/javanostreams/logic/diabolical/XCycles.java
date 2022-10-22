package sudokusolver.javanostreams.logic.diabolical;

import org.jgrapht.Graph;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.builder.GraphBuilder;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.dot.DOTExporter;
import sudokusolver.javanostreams.Board;
import sudokusolver.javanostreams.Cell;
import sudokusolver.javanostreams.Removals;
import sudokusolver.javanostreams.RemoveCandidates;
import sudokusolver.javanostreams.SetValue;
import sudokusolver.javanostreams.Strength;
import sudokusolver.javanostreams.StrengthEdge;
import sudokusolver.javanostreams.SudokuNumber;
import sudokusolver.javanostreams.UnsolvedCell;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;

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
        var removals = new Removals();
        for (var candidate : SudokuNumber.values()) {
            var graph = createStrongLinks(board, candidate);
            addWeakLinks(graph);
            Strength.trim(graph);
            for (var edge : Strength.getWeakEdgesInAlternatingCycle(graph)) {
                var source = graph.getEdgeSource(edge);
                var target = graph.getEdgeTarget(edge);
                removeFromUnit(removals, candidate, source, target, Cell::row, board::getRow);
                removeFromUnit(removals, candidate, source, target, Cell::column, board::getColumn);
                removeFromUnit(removals, candidate, source, target, Cell::block, board::getBlock);
            }
        }
        return removals.toList();
    }

    private static void removeFromUnit(
            Removals removals,
            SudokuNumber candidate,
            UnsolvedCell source,
            UnsolvedCell target,
            ToIntFunction<Cell> getUnitIndex,
            IntFunction<List<Cell>> getUnit
    ) {
        if (getUnitIndex.applyAsInt(source) == getUnitIndex.applyAsInt(target)) {
            for (var cell : getUnit.apply(getUnitIndex.applyAsInt(source))) {
                if (cell instanceof UnsolvedCell unsolved &&
                        unsolved.candidates().contains(candidate) &&
                        !unsolved.equals(source) &&
                        !unsolved.equals(target)
                ) {
                    removals.add(unsolved, candidate);
                }
            }
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
        var modifications = new ArrayList<SetValue>();
        for (var candidate : SudokuNumber.values()) {
            var graph = createStrongLinks(board, candidate);
            addWeakLinks(graph);
            for (var vertex : graph.vertexSet()) {
                if (Strength.alternatingCycleExists(graph, vertex, Strength.STRONG)) {
                    modifications.add(new SetValue(vertex, candidate));
                }
            }
        }
        return modifications;
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
        var removals = new Removals();
        for (var candidate : SudokuNumber.values()) {
            var graph = createStrongLinks(board, candidate);
            addWeakLinks(graph);
            additionalWeakLinks(graph, board, candidate);
            for (var vertex : graph.vertexSet()) {
                if (Strength.alternatingCycleExists(graph, vertex, Strength.WEAK)) {
                    removals.add(vertex, candidate);
                }
            }
        }
        return removals.toList();
    }

    public static String toDOT(Graph<UnsolvedCell, StrengthEdge> graph, SudokuNumber candidate) {
        var writer = new StringWriter();
        var exporter = new DOTExporter<UnsolvedCell, StrengthEdge>();
        exporter.setGraphIdProvider(candidate::toString);
        exporter.setVertexAttributeProvider(vertex -> Map.of("label",
                DefaultAttribute.createAttribute("[" + vertex.row() + ',' + vertex.column() + ']')));
        exporter.setEdgeAttributeProvider(Strength.STRENGTH_EDGE_ATTRIBUTE_PROVIDER);
        exporter.exportGraph(graph, writer);
        return writer.toString();
    }

    private static Graph<UnsolvedCell, StrengthEdge> createStrongLinks(Board<Cell> board, SudokuNumber candidate) {
        var builder = new GraphBuilder<>(new SimpleGraph<UnsolvedCell, StrengthEdge>(StrengthEdge.class));
        for (var unit : board.getUnits()) {
            var withCandidate = new ArrayList<UnsolvedCell>();
            for (var cell : unit) {
                if (cell instanceof UnsolvedCell unsolved && unsolved.candidates().contains(candidate)) {
                    withCandidate.add(unsolved);
                }
            }
            if (withCandidate.size() == 2) {
                var a = withCandidate.get(0);
                var b = withCandidate.get(1);
                builder.addEdge(a, b, new StrengthEdge(Strength.STRONG));
            }
        }
        return builder.build();
    }

    private static void addWeakLinks(Graph<UnsolvedCell, StrengthEdge> graph) {
        var vertices = graph.vertexSet().toArray(UnsolvedCell[]::new);
        for (var i = 0; i < vertices.length - 1; i++) {
            var a = vertices[i];
            for (var j = i + 1; j < vertices.length; j++) {
                var b = vertices[j];
                if (a.isInSameUnit(b) && !graph.containsEdge(a, b)) {
                    graph.addEdge(a, b, new StrengthEdge(Strength.WEAK));
                }
            }
        }
    }

    private static void additionalWeakLinks(
            Graph<UnsolvedCell, StrengthEdge> graph,
            Board<Cell> board,
            SudokuNumber candidate
    ) {
        var vertices = Set.copyOf(graph.vertexSet());
        for (var cell : board.getCells()) {
            if (cell instanceof UnsolvedCell unsolved &&
                    unsolved.candidates().contains(candidate) &&
                    !vertices.contains(unsolved)
            ) {
                for (var vertex : vertices) {
                    if (vertex.isInSameUnit(unsolved)) {
                        graph.addVertex(unsolved);
                        graph.addEdge(vertex, unsolved, new StrengthEdge(Strength.WEAK));
                    }
                }
            }
        }
    }
}
