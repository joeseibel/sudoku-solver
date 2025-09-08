package sudokusolver.java.logic.extreme;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.builder.GraphBuilder;
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

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        return Strength.getWeakEdgesInAlternatingCycle(graph)
                .stream()
                .flatMap(edge -> {
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
                        return candidates.stream().map(candidate -> new LocatedCandidate(sourceCell, candidate));
                    } else {
                        var rowRemovals = removeFromUnit(
                                sourceCell,
                                sourceCandidate,
                                targetCell,
                                Cell::row,
                                board::getRow
                        );
                        var columnRemovals = removeFromUnit(
                                sourceCell,
                                sourceCandidate,
                                targetCell,
                                Cell::column,
                                board::getColumn
                        );
                        var blockRemovals = removeFromUnit(
                                sourceCell,
                                sourceCandidate,
                                targetCell,
                                Cell::block,
                                board::getBlock
                        );
                        return Stream.of(rowRemovals, columnRemovals, blockRemovals).flatMap(Function.identity());
                    }
                })
                .collect(LocatedCandidate.mergeToRemoveCandidates());
    }

    private static Stream<LocatedCandidate> removeFromUnit(
            UnsolvedCell sourceCell,
            SudokuNumber sourceCandidate,
            UnsolvedCell targetCell,
            ToIntFunction<Cell> getUnitIndex,
            IntFunction<List<Cell>> getUnit
    ) {
        var sourceUnitIndex = getUnitIndex.applyAsInt(sourceCell);
        if (sourceUnitIndex == getUnitIndex.applyAsInt(targetCell)) {
            return getUnit.apply(sourceUnitIndex)
                    .stream()
                    .filter(UnsolvedCell.class::isInstance)
                    .map(UnsolvedCell.class::cast)
                    .filter(cell -> cell.candidates().contains(sourceCandidate) &&
                            !cell.equals(sourceCell) &&
                            !cell.equals(targetCell))
                    .map(cell -> new LocatedCandidate(cell, sourceCandidate));
        } else {
            return Stream.empty();
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
        return graph.vertexSet()
                .stream()
                .filter(vertex -> alternatingCycleExists(graph, vertex, Strength.STRONG))
                .map(vertex -> new SetValue(vertex.cell(), vertex.candidate()))
                .toList();
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
        return graph.vertexSet()
                .stream()
                .filter(vertex -> alternatingCycleExists(graph, vertex, Strength.WEAK))
                .collect(LocatedCandidate.mergeToRemoveCandidates());
    }

    private static Graph<LocatedCandidate, StrengthEdge> buildGraph(Board<Cell> board) {
        var builder = new GraphBuilder<>(new SimpleGraph<LocatedCandidate, StrengthEdge>(StrengthEdge.class));

        // Connect cells.
        board.getUnits().forEach(unit -> Arrays.stream(SudokuNumber.values()).forEach(candidate -> {
            var withCandidates = unit.stream()
                    .filter(UnsolvedCell.class::isInstance)
                    .map(UnsolvedCell.class::cast)
                    .filter(cell -> cell.candidates().contains(candidate))
                    .toList();
            var strength = withCandidates.size() == 2 ? Strength.STRONG : Strength.WEAK;
            withCandidates.stream().collect(Pair.zipEveryPair()).forEach(pair -> {
                var a = pair.first();
                var b = pair.second();
                builder.addEdge(
                        new LocatedCandidate(a, candidate),
                        new LocatedCandidate(b, candidate),
                        new StrengthEdge(strength)
                );
            });
        }));

        // Connect candidates in cells.
        board.getCells().stream().filter(UnsolvedCell.class::isInstance).map(UnsolvedCell.class::cast).forEach(cell -> {
            var strength = cell.candidates().size() == 2 ? Strength.STRONG : Strength.WEAK;
            cell.candidates().stream().collect(Pair.zipEveryPair()).forEach(pair -> {
                var a = pair.first();
                var b = pair.second();
                builder.addEdge(
                        new LocatedCandidate(cell, a),
                        new LocatedCandidate(cell, b),
                        new StrengthEdge(strength)
                );
            });
        });

        return builder.build();
    }

    private static boolean alternatingCycleExists(
            Graph<LocatedCandidate, StrengthEdge> graph,
            LocatedCandidate vertex,
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
                    var visitedCandidates = EnumSet.of(vertex.candidate(), start.candidate());
                    visitedCandidates.remove(end.candidate());
                    return alternatingCycleExists(
                            graph,
                            adjacentEdgesType,
                            end,
                            start,
                            adjacentEdgesType.getOpposite(),
                            Set.of(vertex, start),
                            visitedCandidates
                    );
                });
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
        var nextVertices = graph.edgesOf(currentVertex)
                .stream()
                .filter(edge -> edge.getStrength().isCompatibleWith(nextType))
                .map(edge -> Graphs.getOppositeVertex(graph, edge, currentVertex))
                .filter(opposite -> opposite.candidate() == currentVertex.candidate() ||
                        !visitedCandidates.contains(opposite.candidate()))
                .collect(Collectors.toList());
        if (adjacentEdgesType.getOpposite() == nextType && nextVertices.contains(end)) {
            return true;
        } else {
            nextVertices.removeAll(visited);
            nextVertices.remove(end);
            return nextVertices.stream().anyMatch(nextVertex -> {
                var nextVisited = new HashSet<>(visited);
                nextVisited.add(nextVertex);
                EnumSet<SudokuNumber> nextVisitedCandidates;
                if (currentVertex.candidate() == nextVertex.candidate()) {
                    nextVisitedCandidates = visitedCandidates;
                } else {
                    nextVisitedCandidates = EnumSet.copyOf(visitedCandidates);
                    nextVisitedCandidates.add(nextVertex.candidate());
                }
                return alternatingCycleExists(
                        graph,
                        adjacentEdgesType,
                        end,
                        nextVertex,
                        nextType.getOpposite(),
                        nextVisited,
                        nextVisitedCandidates
                );
            });
        }
    }
}