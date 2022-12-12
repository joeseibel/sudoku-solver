package sudokusolver.javanostreams.logic.extreme;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.builder.GraphBuilder;
import sudokusolver.javanostreams.Board;
import sudokusolver.javanostreams.Cell;
import sudokusolver.javanostreams.LocatedCandidate;
import sudokusolver.javanostreams.Removals;
import sudokusolver.javanostreams.RemoveCandidates;
import sudokusolver.javanostreams.SetValue;
import sudokusolver.javanostreams.Strength;
import sudokusolver.javanostreams.StrengthEdge;
import sudokusolver.javanostreams.SudokuNumber;
import sudokusolver.javanostreams.UnsolvedCell;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;

/*
 * https://www.sudokuwiki.org/Alternating_Inference_Chains
 *
 * Alternating Inference Chains are based on a graph type in which each vertex is a specific candidate in a cell and the
 * edges can either be strong or weak links. A strong link connects two vertices in a unit that share a candidate when
 * they are in the only unsolved cells in that unit with the candidate. A strong link also connects two vertices in a
 * single cell when they are the only two candidates in that cell. A weak link connects two vertices in a unit that
 * share a candidate when they are not the only unsolved cells in that unit with the candidate. A weak link also
 * connects two vertices in a single cell when there are more than two candidates in that cell. An Alternating Inference
 * Chain is a cycle in the graph in which the edges alternate between strong and weak links. If one vertex of a link is
 * the solution, then the other vertex must not be the solution. If one vertex of a strong link is not the solution,
 * then the other vertex must be the solution. Alternating Inference Chains are very similar to X-Cycles and Grouped
 * X-Cycles.
 *
 * Note that this implementation of Alternating Inference Chains can handle cases in which the chain is not strictly
 * alternating between strong and weak links. It is tolerant of cases in which a strong link takes the place of a weak
 * link.
 */
public class AlternatingInferenceChains {
    /*
     * Rule 1:
     *
     * If an Alternating Inference Chain has an even number of vertices and therefore continuously alternates between
     * strong and weak, then the graph is perfect and has no flaws. Each of the weak links can be treated as a strong
     * link. If a weak link connects a common candidate across two different cells, then that candidate can be removed
     * from any other cell which is in the same unit as the two vertices. If a weak link connects two candidates of the
     * same cell, then all other candidates can be removed from that cell.
     */
    public static List<RemoveCandidates> alternatingInferenceChainsRule1(Board<Cell> board) {
        var graph = buildGraph(board);
        Strength.trim(graph);
        var removals = new Removals();
        for (var edge : Strength.getWeakEdgesInAlternatingCycle(graph)) {
            var source = graph.getEdgeSource(edge);
            var sourceCell = source.cell();
            var sourceCandidate = source.candidate();
            var target = graph.getEdgeTarget(edge);
            var targetCell = target.cell();
            var targetCandidate = target.candidate();
            if (sourceCell.equals(targetCell)) {
                var candidates = EnumSet.copyOf(sourceCell.candidates());
                candidates.remove(sourceCandidate);
                candidates.remove(targetCandidate);
                for (var candidate : candidates) {
                    removals.add(sourceCell, candidate);
                }
            } else {
                removeFromUnit(removals, sourceCell, sourceCandidate, targetCell, Cell::row, board::getRow);
                removeFromUnit(removals, sourceCell, sourceCandidate, targetCell, Cell::column, board::getColumn);
                removeFromUnit(removals, sourceCell, sourceCandidate, targetCell, Cell::block, board::getBlock);
            }
        }
        return removals.toList();
    }

    private static void removeFromUnit(
            Removals removals,
            UnsolvedCell sourceCell,
            SudokuNumber sourceCandidate,
            UnsolvedCell targetCell,
            ToIntFunction<Cell> getUnitIndex,
            IntFunction<List<Cell>> getUnit
    ) {
        var sourceUnitIndex = getUnitIndex.applyAsInt(sourceCell);
        if (sourceUnitIndex == getUnitIndex.applyAsInt(targetCell)) {
            for (var cell : getUnit.apply(sourceUnitIndex)) {
                if (cell instanceof UnsolvedCell unsolved &&
                        unsolved.candidates().contains(sourceCandidate) &&
                        !unsolved.equals(sourceCell) &&
                        !unsolved.equals(targetCell)
                ) {
                    removals.add(unsolved, sourceCandidate);
                }
            }
        }
    }

    /*
     * Rule 2:
     *
     * If an Alternating Inference Chain has an odd number of vertices and the edges alternate between strong and weak,
     * except for one vertex which is connected by two strong links, then the graph is a contradiction. Removing the
     * candidate from the cell of interest implies that the candidate must be the solution for that cell, thus causing
     * the cycle to contradict itself. However, considering the candidate to be the solution for that cell does not
     * cause any contradiction in the cycle. Therefore, the candidate must be the solution for that cell.
     *
     * Note that this implementation of rule 2 does not allow for a candidate to be revisited in the chain. A candidate
     * can appear multiple times in a chain, but only if all the occurrences are consecutive.
     */
    public static List<SetValue> alternatingInferenceChainsRule2(Board<Cell> board) {
        var graph = buildGraph(board);
        Strength.trim(graph);
        var modifications = new ArrayList<SetValue>();
        for (var vertex : graph.vertexSet()) {
            if (alternatingCycleExists(graph, vertex, Strength.STRONG)) {
                modifications.add(new SetValue(vertex.cell(), vertex.candidate()));
            }
        }
        return modifications;
    }

    /*
     * Rule 3:
     *
     * If an Alternating Inference Chain has an odd number of vertices and the edges alternate between strong and weak,
     * except for one vertex which is connected by two weak links, then the graph is a contradiction. Considering the
     * candidate to be the solution for the cell of interest implies that the candidate must be removed from that cell,
     * thus causing the cycle to contradict itself. However, removing the candidate from that cell does not cause any
     * contradiction in the cycle. Therefore, the candidate can be removed from the cell.
     *
     * Note that this implementation of rule 3 does not allow for a candidate to be revisited in the chain. A candidate
     * can appear multiple times in a chain, but only if all the occurrences are consecutive.
     */
    public static List<RemoveCandidates> alternatingInferenceChainsRule3(Board<Cell> board) {
        var graph = buildGraph(board);
        var removals = new Removals();
        for (var vertex : graph.vertexSet()) {
            if (alternatingCycleExists(graph, vertex, Strength.WEAK)) {
                removals.add(vertex.cell(), vertex.candidate());
            }
        }
        return removals.toList();
    }

    private static Graph<LocatedCandidate, StrengthEdge> buildGraph(Board<Cell> board) {
        var builder = new GraphBuilder<>(new SimpleGraph<LocatedCandidate, StrengthEdge>(StrengthEdge.class));

        //Connect cells.
        for (var unit : board.getUnits()) {
            for (var candidate : SudokuNumber.values()) {
                var withCandidates = new ArrayList<UnsolvedCell>();
                for (var cell : unit) {
                    if (cell instanceof UnsolvedCell unsolved && unsolved.candidates().contains(candidate)) {
                        withCandidates.add(unsolved);
                    }
                }
                var strength = withCandidates.size() == 2 ? Strength.STRONG : Strength.WEAK;
                for (var i = 0; i < withCandidates.size() - 1; i++) {
                    var a = withCandidates.get(i);
                    for (var j = i + 1; j < withCandidates.size(); j++) {
                        var b = withCandidates.get(j);
                        builder.addEdge(
                                new LocatedCandidate(a, candidate),
                                new LocatedCandidate(b, candidate),
                                new StrengthEdge(strength)
                        );
                    }
                }
            }
        }

        //Connect candidates in cells.
        for (var cell : board.getCells()) {
            if (cell instanceof UnsolvedCell unsolved) {
                var candidates = unsolved.candidates().toArray(SudokuNumber[]::new);
                var strength = candidates.length == 2 ? Strength.STRONG : Strength.WEAK;
                for (var i = 0; i < candidates.length - 1; i++) {
                    var a = candidates[i];
                    for (var j = i + 1; j < candidates.length; j++) {
                        var b = candidates[j];
                        builder.addEdge(
                                new LocatedCandidate(unsolved, a),
                                new LocatedCandidate(unsolved, b),
                                new StrengthEdge(strength)
                        );
                    }
                }
            }
        }

        return builder.build();
    }

    private static boolean alternatingCycleExists(
            Graph<LocatedCandidate, StrengthEdge> graph,
            LocatedCandidate vertex,
            Strength adjacentEdgesType
    ) {
        var vertices = graph.edgesOf(vertex).toArray(StrengthEdge[]::new);
        for (var i = 0; i < vertices.length - 1; i++) {
            var edgeA = vertices[i];
            if (edgeA.getStrength() == adjacentEdgesType) {
                var start = Graphs.getOppositeVertex(graph, edgeA, vertex);
                for (var j = i + 1; j < vertices.length; j++) {
                    var edgeB = vertices[j];
                    if (edgeB.getStrength() == adjacentEdgesType) {
                        var end = Graphs.getOppositeVertex(graph, edgeB, vertex);
                        var visitedCandidates = EnumSet.of(vertex.candidate(), start.candidate());
                        visitedCandidates.remove(end.candidate());
                        var cycleExists = alternatingCycleExists(
                                graph,
                                adjacentEdgesType,
                                end,
                                start,
                                adjacentEdgesType.getOpposite(),
                                Set.of(vertex, start),
                                visitedCandidates
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

    private static boolean alternatingCycleExists(
            Graph<LocatedCandidate, StrengthEdge> graph,
            Strength adjacentEdgesType,
            LocatedCandidate end,
            LocatedCandidate currentVertex,
            Strength nextType,
            Set<LocatedCandidate> visited,
            EnumSet<SudokuNumber> visitedCandidates
    ) {
        var nextVertices = new ArrayList<LocatedCandidate>();
        for (var edge : graph.edgesOf(currentVertex)) {
            if (edge.getStrength().isCompatibleWith(nextType)) {
                var opposite = Graphs.getOppositeVertex(graph, edge, currentVertex);
                if (opposite.candidate() == currentVertex.candidate() ||
                        !visitedCandidates.contains(opposite.candidate())
                ) {
                    nextVertices.add(opposite);
                }
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
                EnumSet<SudokuNumber> nextVisitedCandidates;
                if (currentVertex.candidate() == nextVertex.candidate()) {
                    nextVisitedCandidates = visitedCandidates;
                } else {
                    nextVisitedCandidates = EnumSet.copyOf(visitedCandidates);
                    nextVisitedCandidates.add(nextVertex.candidate());
                }
                var cycleExists = alternatingCycleExists(
                        graph,
                        adjacentEdgesType,
                        end,
                        nextVertex,
                        nextType.getOpposite(),
                        nextVisited,
                        nextVisitedCandidates
                );
                if (cycleExists) {
                    return true;
                }
            }
            return false;
        }
    }
}