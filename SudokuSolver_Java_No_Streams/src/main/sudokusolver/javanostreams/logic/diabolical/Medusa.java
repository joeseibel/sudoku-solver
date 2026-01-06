package sudokusolver.javanostreams.logic.diabolical;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.connectivity.BiconnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.nio.dot.DOTExporter;
import sudokusolver.javanostreams.Board;
import sudokusolver.javanostreams.Cell;
import sudokusolver.javanostreams.LocatedCandidate;
import sudokusolver.javanostreams.Removals;
import sudokusolver.javanostreams.RemoveCandidates;
import sudokusolver.javanostreams.SetValue;
import sudokusolver.javanostreams.SudokuNumber;
import sudokusolver.javanostreams.UnsolvedCell;
import sudokusolver.javanostreams.VertexColor;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

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
        var modifications = new ArrayList<SetValue>();
        for (var graph : createConnectedComponents(board)) {
            var colors = VertexColor.colorToMap(graph);
            var vertices = graph.vertexSet().toArray(LocatedCandidate[]::new);
            outerLoop:
            for (var i = 0; i < vertices.length - 1; i++) {
                var a = vertices[i];
                var cellA = a.cell();
                for (var j = i + 1; j < vertices.length; j++) {
                    var b = vertices[j];
                    var cellB = b.cell();
                    if (cellA.equals(cellB) && colors.get(a) == colors.get(b)) {
                        var colorToSet = colors.get(a).getOpposite();
                        for (var vertex : graph.vertexSet()) {
                            if (colors.get(vertex) == colorToSet) {
                                modifications.add(new SetValue(vertex.cell(), vertex.candidate()));
                            }
                        }
                        break outerLoop;
                    }
                }
            }
        }
        return modifications;
    }

    /*
     * Rule 2: Twice in a Unit
     *
     * If there are two vertices with the same color and the same candidate that are in the same unit, then that color
     * cannot be the solution and the opposite color must be the solution. All vertices with the opposite color can be
     * set as the solution.
     */
    public static List<SetValue> medusaRule2(Board<Cell> board) {
        var modifications = new ArrayList<SetValue>();
        for (var graph : createConnectedComponents(board)) {
            var colors = VertexColor.colorToMap(graph);
            var vertices = graph.vertexSet().toArray(LocatedCandidate[]::new);
            outerLoop:
            for (var i = 0; i < vertices.length - 1; i++) {
                var a = vertices[i];
                var cellA = a.cell();
                var candidateA = a.candidate();
                for (var j = i + 1; j < vertices.length; j++) {
                    var b = vertices[j];
                    var cellB = b.cell();
                    var candidateB = b.candidate();
                    if (candidateA == candidateB && colors.get(a) == colors.get(b) && cellA.isInSameUnit(cellB)) {
                        var colorToSet = colors.get(a).getOpposite();
                        for (var vertex : graph.vertexSet()) {
                            if (colors.get(vertex) == colorToSet) {
                                modifications.add(new SetValue(vertex.cell(), vertex.candidate()));
                            }
                        }
                        break outerLoop;
                    }
                }
            }
        }
        return modifications;
    }

    /*
     * Rule 3: Two colors in a cell
     *
     * If there are two differently colored candidates in a cell, then the solution must be one of the two candidates.
     * All other candidates in the cell can be removed.
     */
    public static List<RemoveCandidates> medusaRule3(Board<Cell> board) {
        var removals = new Removals();
        for (var graph : createConnectedComponents(board)) {
            var colors = VertexColor.colorToMap(graph);
            var vertices = new ArrayList<LocatedCandidate>();
            for (var vertex : graph.vertexSet()) {
                if (vertex.cell().candidates().size() > 2) {
                    vertices.add(vertex);
                }
            }
            outerLoop:
            for (var i = 0; i < vertices.size() - 1; i++) {
                var a = vertices.get(i);
                var cellA = a.cell();
                for (var j = i + 1; j < vertices.size(); j++) {
                    var b = vertices.get(j);
                    var cellB = b.cell();
                    if (cellA.equals(cellB) && colors.get(a) != colors.get(b)) {
                        for (var candidate : cellA.candidates()) {
                            if (!graph.vertexSet().contains(new LocatedCandidate(cellA, candidate))) {
                                removals.add(cellA, candidate);
                            }
                        }
                        break outerLoop;
                    }
                }
            }
        }
        return removals.toList();
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
        var removals = new Removals();
        for (var graph : createConnectedComponents(board)) {
            var colors = VertexColor.colorToLists(graph);
            var colorOne = colors.get(VertexColor.COLOR_ONE);
            var colorTwo = colors.get(VertexColor.COLOR_TWO);
            for (var cell : board.getCells()) {
                if (cell instanceof UnsolvedCell unsolved) {
                    for (var candidate : unsolved.candidates()) {
                        if (!graph.vertexSet().contains(new LocatedCandidate(unsolved, candidate)) &&
                                canSeeColor(unsolved, candidate, colorOne) &&
                                canSeeColor(unsolved, candidate, colorTwo)
                        ) {
                            removals.add(unsolved, candidate);
                        }
                    }
                }
            }
        }
        return removals.toList();
    }

    private static boolean canSeeColor(UnsolvedCell cell, SudokuNumber candidate, List<LocatedCandidate> color) {
        for (var vertex : color) {
            var coloredCell = vertex.cell();
            var coloredCandidate = vertex.candidate();
            if (candidate == coloredCandidate && cell.isInSameUnit(coloredCell)) {
                return true;
            }
        }
        return false;
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
        var removals = new Removals();
        for (var graph : createConnectedComponents(board)) {
            var colors = VertexColor.colorToLists(graph);
            var colorOne = colors.get(VertexColor.COLOR_ONE);
            var colorTwo = colors.get(VertexColor.COLOR_TWO);
            for (var cell : board.getCells()) {
                if (cell instanceof UnsolvedCell unsolved) {
                    for (var candidate : unsolved.candidates()) {
                        if (!graph.vertexSet().contains(new LocatedCandidate(unsolved, candidate)) &&
                                (canSeeColor(unsolved, candidate, colorOne) && colorInCell(unsolved, colorTwo) ||
                                        canSeeColor(unsolved, candidate, colorTwo) && colorInCell(unsolved, colorOne))
                        ) {
                            removals.add(unsolved, candidate);
                        }
                    }
                }
            }
        }
        return removals.toList();
    }

    private static boolean colorInCell(UnsolvedCell cell, List<LocatedCandidate> color) {
        for (var otherCandidate : cell.candidates()) {
            if (color.contains(new LocatedCandidate(cell, otherCandidate))) {
                return true;
            }
        }
        return false;
    }

    /*
     * Rule 6: Cell Emptied by Color
     *
     * If there is an unsolved cell in which every candidate is uncolored and every candidate can see the same color,
     * then that color cannot be the solution since it would lead to the cell being emptied of candidates and still have
     * no solution. All vertices with the opposite color can be set as the solution.
     */
    public static List<SetValue> medusaRule6(Board<Cell> board) {
        var modifications = new ArrayList<SetValue>();
        for (var graph : createConnectedComponents(board)) {
            var colors = VertexColor.colorToLists(graph);
            var colorOne = colors.get(VertexColor.COLOR_ONE);
            var colorTwo = colors.get(VertexColor.COLOR_TWO);
            var oppositeColor = Collections.<LocatedCandidate>emptyList();
            cellLoop:
            for (var cell : board.getCells()) {
                if (cell instanceof UnsolvedCell unsolved) {
                    for (var candidate : unsolved.candidates()) {
                        if (graph.vertexSet().contains(new LocatedCandidate(unsolved, candidate))) {
                            continue cellLoop;
                        }
                    }
                    if (everyCandidateCanSeeColor(unsolved, colorOne)) {
                        oppositeColor = colorTwo;
                        break;
                    } else if (everyCandidateCanSeeColor(unsolved, colorTwo)) {
                        oppositeColor = colorOne;
                        break;
                    }
                }
            }
            for (var vertex : oppositeColor) {
                var coloredCell = vertex.cell();
                var coloredCandidate = vertex.candidate();
                modifications.add(new SetValue(coloredCell, coloredCandidate));
            }
        }
        return modifications;
    }

    private static boolean everyCandidateCanSeeColor(UnsolvedCell cell, List<LocatedCandidate> color) {
        for (var candidate : cell.candidates()) {
            var canSeeColor = false;
            for (var vertex : color) {
                var coloredCell = vertex.cell();
                var coloredCandidate = vertex.candidate();
                if (candidate == coloredCandidate && cell.isInSameUnit(coloredCell)) {
                    canSeeColor = true;
                    break;
                }
            }
            if (!canSeeColor) {
                return false;
            }
        }
        return true;
    }

    public static String toDOT(Graph<LocatedCandidate, DefaultEdge> graph) {
        var writer = new StringWriter();
        var exporter = new DOTExporter<LocatedCandidate, DefaultEdge>();
        exporter.setVertexAttributeProvider(LocatedCandidate::getVertexAttributes);
        exporter.exportGraph(graph, writer);
        return writer.toString();
    }

    private static Set<Graph<LocatedCandidate, DefaultEdge>> createConnectedComponents(Board<Cell> board) {
        var graph = new SimpleGraph<LocatedCandidate, DefaultEdge>(DefaultEdge.class);
        for (var cell : board.getCells()) {
            if (cell instanceof UnsolvedCell unsolved && unsolved.candidates().size() == 2) {
                var candidates = unsolved.candidates().toArray(SudokuNumber[]::new);
                var a = new LocatedCandidate(unsolved, candidates[0]);
                var b = new LocatedCandidate(unsolved, candidates[1]);
                Graphs.addEdgeWithVertices(graph, a, b);
            }
        }
        for (var candidate : SudokuNumber.values()) {
            for (var unit : board.getUnits()) {
                var withCandidate = new ArrayList<UnsolvedCell>();
                for (var cell : unit) {
                    if (cell instanceof UnsolvedCell unsolved && unsolved.candidates().contains(candidate)) {
                        withCandidate.add(unsolved);
                    }
                }
                if (withCandidate.size() == 2) {
                    var a = new LocatedCandidate(withCandidate.getFirst(), candidate);
                    var b = new LocatedCandidate(withCandidate.getLast(), candidate);
                    Graphs.addEdgeWithVertices(graph, a, b);
                }
            }
        }
        return new BiconnectivityInspector<>(graph).getConnectedComponents();
    }
}