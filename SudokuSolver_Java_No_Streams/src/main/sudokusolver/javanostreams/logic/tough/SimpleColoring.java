package sudokusolver.javanostreams.logic.tough;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.connectivity.BiconnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.nio.dot.DOTExporter;
import sudokusolver.javanostreams.Board;
import sudokusolver.javanostreams.Cell;
import sudokusolver.javanostreams.Removals;
import sudokusolver.javanostreams.RemoveCandidates;
import sudokusolver.javanostreams.SudokuNumber;
import sudokusolver.javanostreams.UnsolvedCell;
import sudokusolver.javanostreams.VertexColor;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/*
 * https://www.sudokuwiki.org/Singles_Chains
 *
 * A single's chain is a graph for a particular candidate that connects two cells when those are the only two cells in a
 * unit with that candidate. Each chain is colored with alternating colors such that for a given vertex with a given
 * color, all adjacent vertices have the opposite color. The two colors represent the two possible solutions for each
 * cell in the chain. Either the first color is the solution for the chain or the second color is.
 */
public class SimpleColoring {
    /*
     * Rule 2: Twice in a Unit
     *
     * If there are two or more vertices with the same color that are in the same unit, then that color cannot be the
     * solution. All candidates with that color in that chain can be removed.
     */
    public static List<RemoveCandidates> simpleColoringRule2(Board<Cell> board) {
        var removals = new Removals();
        for (var candidate : SudokuNumber.values()) {
            for (var graph : createConnectedComponents(board, candidate)) {
                var colors = VertexColor.colorToMap(graph);
                var vertices = List.copyOf(graph.vertexSet());
                outerLoop:
                for (var i = 0; i < vertices.size() - 1; i++) {
                    var a = vertices.get(i);
                    for (var j = i + 1; j < vertices.size(); j++) {
                        var b = vertices.get(j);
                        if (colors.get(a) == colors.get(b) && a.isInSameUnit(b)) {
                            var colorToRemove = colors.get(a);
                            for (var cell : graph.vertexSet()) {
                                if (colors.get(cell) == colorToRemove) {
                                    removals.add(cell, candidate);
                                }
                            }
                            break outerLoop;
                        }
                    }
                }
            }
        }
        return removals.toList();
    }

    /*
     * Rule 4: Two colors 'elsewhere'
     *
     * If an unsolved cell with a given candidate is outside the chain, and it is in the same units as two differently
     * colored vertices, then one of those two vertices must be the solution for the candidate. The candidate can be
     * removed from the cell outside the chain.
     */
    public static List<RemoveCandidates> simpleColoringRule4(Board<Cell> board) {
        var removals = new Removals();
        for (var candidate : SudokuNumber.values()) {
            for (var graph : createConnectedComponents(board, candidate)) {
                var colors = VertexColor.colorToLists(graph);
                var colorOne = colors.get(VertexColor.COLOR_ONE);
                var colorTwo = colors.get(VertexColor.COLOR_TWO);
                for (var cell : board.getCells()) {
                    if (cell instanceof UnsolvedCell unsolved &&
                            unsolved.candidates().contains(candidate) &&
                            !graph.vertexSet().contains(unsolved)
                    ) {
                        var canSeeColorOne = false;
                        for (var colorOneCell : colorOne) {
                            if (unsolved.isInSameUnit(colorOneCell)) {
                                canSeeColorOne = true;
                                break;
                            }
                        }
                        if (canSeeColorOne) {
                            var canSeeColorTwo = false;
                            for (var colorTwoCell : colorTwo) {
                                if (unsolved.isInSameUnit(colorTwoCell)) {
                                    canSeeColorTwo = true;
                                    break;
                                }
                            }
                            if (canSeeColorTwo) {
                                removals.add(unsolved, candidate);
                            }
                        }
                    }
                }
            }
        }
        return removals.toList();
    }

    public static String toDOT(Graph<UnsolvedCell, DefaultEdge> graph, SudokuNumber candidate) {
        var writer = new StringWriter();
        var exporter = new DOTExporter<UnsolvedCell, DefaultEdge>();
        exporter.setGraphIdProvider(candidate::toString);
        exporter.setVertexAttributeProvider(UnsolvedCell::getVertexAttributes);
        exporter.exportGraph(graph, writer);
        return writer.toString();
    }

    private static Set<Graph<UnsolvedCell, DefaultEdge>> createConnectedComponents(
            Board<Cell> board,
            SudokuNumber candidate
    ) {
        var graph = new SimpleGraph<UnsolvedCell, DefaultEdge>(DefaultEdge.class);
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
                Graphs.addEdgeWithVertices(graph, a, b);
            }
        }
        return new BiconnectivityInspector<>(graph).getConnectedComponents();
    }
}