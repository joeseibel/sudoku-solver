package sudokusolver.java.logic.tough;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.connectivity.BiconnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.nio.dot.DOTExporter;
import sudokusolver.java.Board;
import sudokusolver.java.Cell;
import sudokusolver.java.LocatedCandidate;
import sudokusolver.java.Pair;
import sudokusolver.java.RemoveCandidates;
import sudokusolver.java.SudokuNumber;
import sudokusolver.java.UnsolvedCell;
import sudokusolver.java.VertexColor;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

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
        return Arrays.stream(SudokuNumber.values())
                .flatMap(candidate -> createConnectedComponents(board, candidate).stream().flatMap(graph -> {
                    var colors = VertexColor.colorToMap(graph);
                    return graph.vertexSet()
                            .stream()
                            .collect(Pair.zipEveryPair())
                            .filter(pair -> {
                                var a = pair.first();
                                var b = pair.second();
                                return colors.get(a) == colors.get(b) && a.isInSameUnit(b);
                            })
                            .map(pair -> colors.get(pair.first()))
                            .findFirst()
                            .map(colorToRemove -> graph.vertexSet()
                                    .stream()
                                    .filter(cell -> colors.get(cell) == colorToRemove)
                                    .map(cell -> new LocatedCandidate(cell, candidate)))
                            .orElseGet(Stream::empty);
                }))
                .collect(LocatedCandidate.mergeToRemoveCandidates());
    }

    /*
     * Rule 4: Two colors 'elsewhere'
     *
     * If an unsolved cell with a given candidate is outside the chain, and it is in the same units as two differently
     * colored vertices, then one of those two vertices must be the solution for the candidate. The candidate can be
     * removed from the cell outside the chain.
     */
    public static List<RemoveCandidates> simpleColoringRule4(Board<Cell> board) {
        return Arrays.stream(SudokuNumber.values())
                .flatMap(candidate -> createConnectedComponents(board, candidate).stream().flatMap(graph -> {
                    var colors = VertexColor.colorToLists(graph);
                    var colorOne = colors.get(VertexColor.COLOR_ONE);
                    var colorTwo = colors.get(VertexColor.COLOR_TWO);
                    return board.getCells()
                            .stream()
                            .filter(UnsolvedCell.class::isInstance)
                            .map(UnsolvedCell.class::cast)
                            .filter(cell -> cell.candidates().contains(candidate) &&
                                    !graph.vertexSet().contains(cell) &&
                                    colorOne.stream().anyMatch(cell::isInSameUnit) &&
                                    colorTwo.stream().anyMatch(cell::isInSameUnit))
                            .map(cell -> new LocatedCandidate(cell, candidate));
                }))
                .collect(LocatedCandidate.mergeToRemoveCandidates());
    }

    public static String toDOT(Graph<UnsolvedCell, DefaultEdge> graph, SudokuNumber candidate) {
        var writer = new StringWriter();
        var exporter = new DOTExporter<UnsolvedCell, DefaultEdge>();
        exporter.setGraphIdProvider(candidate::toString);
        exporter.setVertexAttributeProvider(UnsolvedCell.UNSOLVED_CELL_ATTRIBUTE_PROVIDER);
        exporter.exportGraph(graph, writer);
        return writer.toString();
    }

    private static Set<Graph<UnsolvedCell, DefaultEdge>> createConnectedComponents(
            Board<Cell> board,
            SudokuNumber candidate
    ) {
        var graph = new SimpleGraph<UnsolvedCell, DefaultEdge>(DefaultEdge.class);
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
                    Graphs.addEdgeWithVertices(graph, a, b);
                });
        return new BiconnectivityInspector<>(graph).getConnectedComponents();
    }
}
