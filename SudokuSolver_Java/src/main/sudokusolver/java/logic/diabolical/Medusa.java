package sudokusolver.java.logic.diabolical;

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
import sudokusolver.java.SetValue;
import sudokusolver.java.SudokuNumber;
import sudokusolver.java.UnsolvedCell;
import sudokusolver.java.VertexColor;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

/*
 * https://www.sudokuwiki.org/3D_Medusa
 *
 * A 3D Medusa is a graph type in which each vertex is a particular candidate in a cell and each edge is a strong link.
 * A strong link is an edge such that if one vertex of the link is the solution, then the other vertex must not be the
 * solution. A strong link also means that if one vertex of the link is not the solution, then the other vertex must be
 * the solution. When a candidate is in only two cells of a unit, there is an edge between the candidate of those two
 * cells. Additionally, when a cell contains only two candidates, there is an edge between the two candidates of that
 * cell. Each medusa is colored with alternating colors such that for a given vertex with a given color, all adjacent
 * vertices have the opposite color. The two colors represent the two possible solutions. Either the first color is the
 * solution for the medusa or the second color is.
 */
public class Medusa {
    /*
     * Rule 1: Twice in a Cell
     *
     * If there are two vertices with the same color that are in the same cell, then that color cannot be the solution
     * and the opposite color must be the solution. All vertices with the opposite color can be set as the solution.
     */
    public static List<SetValue> medusaRule1(Board<Cell> board) {
        return createConnectedComponents(board).stream()
                .flatMap(graph -> {
                    var colors = VertexColor.colorToMap(graph);
                    return graph.vertexSet()
                            .stream()
                            .collect(Pair.zipEveryPair())
                            .filter(pair -> {
                                var a = pair.first();
                                var b = pair.second();
                                var cellA = a.cell();
                                var cellB = b.cell();
                                return cellA.equals(cellB) && colors.get(a) == colors.get(b);
                            })
                            .findFirst()
                            .map(pair -> colors.get(pair.first()).getOpposite())
                            .stream()
                            .flatMap(colorToSet -> graph.vertexSet()
                                    .stream()
                                    .filter(vertex -> colors.get(vertex) == colorToSet)
                                    .map(vertex -> new SetValue(vertex.cell(), vertex.candidate())));
                })
                .toList();
    }

    /*
     * Rule 2: Twice in a Unit
     *
     * If there are two vertices with the same color and the same candidate that are in the same unit, then that color
     * cannot be the solution and the opposite color must be the solution. All vertices with the opposite color can be
     * set as the solution.
     */
    public static List<SetValue> medusaRule2(Board<Cell> board) {
        return createConnectedComponents(board).stream()
                .flatMap(graph -> {
                    var colors = VertexColor.colorToMap(graph);
                    return graph.vertexSet()
                            .stream()
                            .collect(Pair.zipEveryPair())
                            .filter(pair -> {
                                var a = pair.first();
                                var b = pair.second();
                                var cellA = a.cell();
                                var candidateA = a.candidate();
                                var cellB = b.cell();
                                var candidateB = b.candidate();
                                return candidateA == candidateB &&
                                        colors.get(a) == colors.get(b) &&
                                        cellA.isInSameUnit(cellB);
                            })
                            .findFirst()
                            .map(pair -> colors.get(pair.first()).getOpposite())
                            .stream()
                            .flatMap(colorToSet -> graph.vertexSet()
                                    .stream()
                                    .filter(vertex -> colors.get(vertex) == colorToSet)
                                    .map(vertex -> new SetValue(vertex.cell(), vertex.candidate())));
                })
                .toList();
    }

    /*
     * Rule 3: Two colors in a cell
     *
     * If there are two differently colored candidates in a cell, then the solution must be one of the two candidates.
     * All other candidates in the cell can be removed.
     */
    public static List<RemoveCandidates> medusaRule3(Board<Cell> board) {
        return createConnectedComponents(board).stream()
                .flatMap(graph -> {
                    var colors = VertexColor.colorToMap(graph);
                    return graph.vertexSet()
                            .stream()
                            .filter(vertex -> vertex.cell().candidates().size() > 2)
                            .collect(Pair.zipEveryPair())
                            .filter(pair -> {
                                var a = pair.first();
                                var b = pair.second();
                                var cellA = a.cell();
                                var cellB = b.cell();
                                return cellA.equals(cellB) && colors.get(a) != colors.get(b);
                            })
                            .findFirst()
                            .map(pair -> pair.first().cell())
                            .stream()
                            .flatMap(cell -> cell.candidates()
                                    .stream()
                                    .map(candidate -> new LocatedCandidate(cell, candidate))
                                    .filter(removal -> !graph.vertexSet().contains(removal)));
                })
                .collect(LocatedCandidate.mergeToRemoveCandidates());
    }

    /*
     * Rule 4: Two colors 'elsewhere'
     *
     * Given a candidate, if there is an unsolved cell with that candidate, it is uncolored, and the cell can see two
     * other cells which both have that candidate, and they are differently colored, then the candidate must be the
     * solution to one of the other cells, and it cannot be the solution to the first cell with the uncolored candidate.
     * The uncolored candidate can be removed from the first cell.
     */
    public static List<RemoveCandidates> medusaRule4(Board<Cell> board) {
        return createConnectedComponents(board).stream()
                .flatMap(graph -> {
                    var colors = VertexColor.colorToLists(graph);
                    var colorOne = colors.get(VertexColor.COLOR_ONE);
                    var colorTwo = colors.get(VertexColor.COLOR_TWO);
                    return board.getCells()
                            .stream()
                            .filter(UnsolvedCell.class::isInstance)
                            .map(UnsolvedCell.class::cast)
                            .flatMap(cell -> cell.candidates()
                                    .stream()
                                    .map(candidate -> new LocatedCandidate(cell, candidate)))
                            .filter(removal -> !graph.vertexSet().contains(removal) &&
                                    canSeeColor(removal, colorOne) && canSeeColor(removal, colorTwo));
                })
                .collect(LocatedCandidate.mergeToRemoveCandidates());
    }

    private static boolean canSeeColor(LocatedCandidate removal, List<LocatedCandidate> color) {
        var cell = removal.cell();
        var candidate = removal.candidate();
        return color.stream().anyMatch(vertex -> {
            var coloredCell = vertex.cell();
            var coloredCandidate = vertex.candidate();
            return candidate == coloredCandidate && cell.isInSameUnit(coloredCell);
        });
    }

    /*
     * Rule 5: Two colors Unit + Cell
     *
     * If there is an unsolved cell with an uncolored candidate, that candidate can see a colored candidate of the same
     * number, and the unsolved cell contains a candidate colored with the opposite color, then either the candidate in
     * the same unit is the solution for that cell or the candidate in the same cell is the solution. In either case,
     * the uncolored candidate cannot be the solution and can be removed from the unsolved cell.
     */
    public static List<RemoveCandidates> medusaRule5(Board<Cell> board) {
        return createConnectedComponents(board).stream()
                .flatMap(graph -> {
                    var colors = VertexColor.colorToLists(graph);
                    var colorOne = colors.get(VertexColor.COLOR_ONE);
                    var colorTwo = colors.get(VertexColor.COLOR_TWO);
                    return board.getCells()
                            .stream()
                            .filter(UnsolvedCell.class::isInstance)
                            .map(UnsolvedCell.class::cast)
                            .flatMap(cell -> cell.candidates()
                                    .stream().map(candidate -> new LocatedCandidate(cell, candidate)))
                            .filter(removal -> !graph.vertexSet().contains(removal))
                            .filter(removal -> canSeeColor(removal, colorOne) && colorInCell(removal, colorTwo) ||
                                    canSeeColor(removal, colorTwo) && colorInCell(removal, colorOne));
                })
                .collect(LocatedCandidate.mergeToRemoveCandidates());
    }

    private static boolean colorInCell(LocatedCandidate removal, List<LocatedCandidate> color) {
        var cell = removal.cell();
        return cell.candidates()
                .stream()
                .anyMatch(otherCandidate -> color.contains(new LocatedCandidate(cell, otherCandidate)));
    }

    /*
     * Rule 6: Cell Emptied by Color
     *
     * If there is an unsolved cell in which every candidate is uncolored and every candidate can see the same color,
     * then that color cannot be the solution since it would lead to the cell being emptied of candidates and still have
     * no solution. All vertices with the opposite color can be set as the solution.
     */
    public static List<SetValue> medusaRule6(Board<Cell> board) {
        return createConnectedComponents(board).stream()
                .flatMap(graph -> {
                    var colors = VertexColor.colorToLists(graph);
                    var colorOne = colors.get(VertexColor.COLOR_ONE);
                    var colorTwo = colors.get(VertexColor.COLOR_TWO);
                    return board.getCells()
                            .stream()
                            .filter(UnsolvedCell.class::isInstance)
                            .map(UnsolvedCell.class::cast)
                            .filter(cell -> cell.candidates()
                                    .stream()
                                    .noneMatch(candidate -> graph.vertexSet()
                                            .contains(new LocatedCandidate(cell, candidate))))
                            .map(cell -> {
                                if (everyCandidateCanSeeColor(cell, colorOne)) {
                                    return colorTwo.stream();
                                } else if (everyCandidateCanSeeColor(cell, colorTwo)) {
                                    return colorOne.stream();
                                } else {
                                    return null;
                                }
                            })
                            .filter(Objects::nonNull)
                            .findFirst()
                            .orElse(Stream.empty())
                            .map(vertex -> {
                                var coloredCell = vertex.cell();
                                var coloredCandidate = vertex.candidate();
                                return new SetValue(coloredCell, coloredCandidate);
                            });
                })
                .toList();
    }

    public static String toDOT(Graph<LocatedCandidate, DefaultEdge> graph) {
        var writer = new StringWriter();
        var exporter = new DOTExporter<LocatedCandidate, DefaultEdge>();
        exporter.setVertexAttributeProvider(LocatedCandidate.LOCATED_CANDIDATE_ATTRIBUTE_PROVIDER);
        exporter.exportGraph(graph, writer);
        return writer.toString();
    }

    private static boolean everyCandidateCanSeeColor(UnsolvedCell cell, List<LocatedCandidate> color) {
        return cell.candidates().stream().allMatch(candidate -> color.stream().anyMatch(vertex -> {
            var coloredCell = vertex.cell();
            var coloredCandidate = vertex.candidate();
            return candidate == coloredCandidate && cell.isInSameUnit(coloredCell);
        }));
    }

    private static Set<Graph<LocatedCandidate, DefaultEdge>> createConnectedComponents(Board<Cell> board) {
        var graph = new SimpleGraph<LocatedCandidate, DefaultEdge>(DefaultEdge.class);
        board.getCells()
                .stream()
                .filter(UnsolvedCell.class::isInstance)
                .map(UnsolvedCell.class::cast)
                .filter(cell -> cell.candidates().size() == 2)
                .forEach(cell -> {
                    var candidates = cell.candidates().toArray(SudokuNumber[]::new);
                    var a = new LocatedCandidate(cell, candidates[0]);
                    var b = new LocatedCandidate(cell, candidates[1]);
                    Graphs.addEdgeWithVertices(graph, a, b);
                });
        Arrays.stream(SudokuNumber.values()).forEach(candidate -> board.getUnits()
                .stream()
                .map(unit -> unit.stream()
                        .filter(UnsolvedCell.class::isInstance)
                        .map(UnsolvedCell.class::cast)
                        .filter(cell -> cell.candidates().contains(candidate))
                        .toList())
                .filter(unit -> unit.size() == 2)
                .forEach(unit -> {
                    var a = new LocatedCandidate(unit.get(0), candidate);
                    var b = new LocatedCandidate(unit.get(1), candidate);
                    Graphs.addEdgeWithVertices(graph, a, b);
                }));
        return new BiconnectivityInspector<>(graph).getConnectedComponents();
    }
}
