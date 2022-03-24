package sudokusolver.java.logic.extreme;

import org.jgrapht.Graph;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.builder.GraphBuilder;
import org.jgrapht.nio.DefaultAttribute;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
 * https://www.sudokuwiki.org/Grouped_X_Cycles
 *
 * Grouped X-Cycles are an extension of X-Cycles in which a vertex can be a cell or a group of cells. Just like
 * X-Cycles, a Grouped X-Cycles graph is for a single candidate. A group is a set of cells with the candidate which
 * share two units. This means that a group exists in the same block and the same row, or it exists in the same block
 * and the same column.
 *
 * Similar to X-Cycles, the edges of a Grouped X-Cycles graph are either strong or weak. Unlike X-Cycles, the edges can
 * connect two cells, a cell and a group, or two groups. A strong link connects two vertices in a unit when they are the
 * only non-overlapping vertices in that unit. A weak link connects two vertices in a unit when they are not the only
 * non-overlapping vertices in that unit.
 *
 * Since a vertex can be a cell or a group of cells, it is possible for vertices to overlap and even for edges to
 * overlap. For example, consider a unit which has three cells with a candidate, two of which form a group and one which
 * is outside the group. In this case there would be four vertices: three vertices for the cells and one for the group.
 * Two of the cell vertices overlap with the cells of the group. This example would also have one strong link and three
 * weak links. The strong link would connect the group to the cell outside the group. This is a strong link because when
 * we discount the cells that overlap with the group, there are only two vertices in the unit. The weak links connect
 * all the individual cells. They are weak because there are more than two cell vertices in the unit when we discount
 * the group.
 *
 * A Grouped X-Cycle is a cycle in the graph in which the edges alternate between strong and weak links. If one vertex
 * of a link contains the solution, then the other vertex must not contain the solution. If one cell of a strong link
 * does not contain the solution, then the other vertex must contain the solution. If a vertex is a group, containing
 * the solution means that one of the cells of the group is the solution. If a vertex is a cell, containing the solution
 * means that the cell is the solution.
 *
 * Note that this implementation of Grouped X-Cycles can handle cases in which the chain is not strictly alternating
 * between strong and weak links. It is tolerant of cases in which a strong link takes the place of a weak link.
 */
public class GroupedXCycles {
    /*
     * Rule 1:
     *
     * If a Grouped X-Cycle has an even number of vertices and therefore continuously alternates between strong and
     * weak, then the graph is perfect and has no flaws. Each of the weak links can be treated as a strong link. The
     * candidate can be removed from any cell which is in the same unit as both vertices of a weak link, but not
     * contained in either of the vertices.
     */
    public static List<RemoveCandidates> groupedXCyclesRule1(Board<Cell> board) {
        return Arrays.stream(SudokuNumber.values())
                .flatMap(candidate -> {
                    var graph = buildGraph(board, candidate);
                    Strength.trim(graph);
                    return Strength.getWeakEdgesInAlternatingCycle(graph)
                            .stream()
                            .flatMap(edge -> {
                                var source = graph.getEdgeSource(edge);
                                var target = graph.getEdgeTarget(edge);
                                var rowRemovals = removeFromUnit(
                                        candidate,
                                        source,
                                        target,
                                        Node::hasRow,
                                        Node::getRow,
                                        board::getRow
                                );
                                var columnRemovals = removeFromUnit(
                                        candidate,
                                        source,
                                        target,
                                        Node::hasColumn,
                                        Node::getColumn,
                                        board::getColumn
                                );
                                var blockRemovals = removeFromUnit(
                                        candidate,
                                        source,
                                        target,
                                        node -> true,
                                        Node::getBlock,
                                        board::getBlock
                                );
                                return Stream.of(rowRemovals, columnRemovals, blockRemovals)
                                        .flatMap(Function.identity());
                            });
                })
                .collect(LocatedCandidate.mergeToRemoveCandidates());
    }

    /*
     * This method illustrates some interesting differences between Java's Optional and Kotlin's nullable types.
     *
     * When converting this method from Kotlin to Java, my initial idea was to have the parameters sourceUnitIndex and
     * targetUnitIndex be of the type OptionalInt. Since they are Int? in Kotlin, OptionalInt was the obvious Java type
     * to use. However, IntelliJ produces a warning when OptionalInt is used as a parameter's type. Unlike Kotlin's
     * nullable types, Java's Optional is only meant to be used as a return type, not a parameter type.
     *
     * This warning is the reason why this method doesn't take sourceUnitIndex and targetUnitIndex, but instead takes
     * source and target along with functional arguments to determine if a Node has an index and to retrieve that index.
     * With this in mind, it no longer made sense to have Node.getRow() and Node.getColumn() return OptionalInt, but to
     * instead return int and to add the methods hasRow() and hasColumn().
     *
     * I much prefer the Kotlin approach to this implementation. I would've also preferred using OptionalInt, but this
     * version is the more proper way of doing this in Java.
     *
     * Even if I did implement this using OptionalInt, there still would have been a couple ways in which Kotlin's Int?
     * has advantages over OptionalInt. The first advantage is that in Kotlin, a non-nullable and a nullable can be
     * directly compared with each other for equality. In my Kotlin implementation, I only needed to directly check if
     * sourceUnitIndex is null, then I could check if sourceUnitIndex equals targetUnitIndex without having to check if
     * targetUnitIndex is null. However, in Java, an int can't be directly compared with an OptionalInt. If I were to
     * have used OptionalInt, then both sourceUnitIndex and targetUnitIndex would need to be unwrapped and then the
     * equality check could be performed.
     *
     * The other advantage of Kotlin's nullable types is that the type T is a subtype of T?. In my Kotlin
     * implementation, Node.row is an Int? while RowGroup.row is an Int. Therefore, when working with something that is
     * a Node at compile time, row must be checked for null. However, when working with something that is a RowGroup at
     * compile time, row is guaranteed to not be null. Unfortunately, this approach does not work in Java. An int is not
     * a subtype of OptionalInt which means that the return type of both Node.getRow() and RowGroup.getRow() would need
     * to be OptionalInt. Unwrapping the optional would be required even when working with something that is a RowGroup
     * at compile time.
     */
    private static Stream<LocatedCandidate> removeFromUnit(
            SudokuNumber candidate,
            Node source,
            Node target,
            Predicate<Node> hasUnitIndex,
            Function<Node, Integer> getUnitIndex,
            Function<Integer, List<Cell>> getUnit
    ) {
        if (hasUnitIndex.test(source) && hasUnitIndex.test(target)) {
            var sourceUnitIndex = getUnitIndex.apply(source);
            if (sourceUnitIndex.equals(getUnitIndex.apply(target))) {
                return getUnit.apply(sourceUnitIndex)
                        .stream()
                        .filter(UnsolvedCell.class::isInstance)
                        .map(UnsolvedCell.class::cast)
                        .filter(cell -> cell.candidates().contains(candidate) &&
                                !source.getCells().contains(cell) &&
                                !target.getCells().contains(cell))
                        .map(cell -> new LocatedCandidate(cell, candidate));
            }
        }
        return Stream.empty();
    }

    /*
     * Rule 2:
     *
     * If a Grouped X-Cycle has an odd number of vertices and the edges alternate between strong and weak, except for
     * one vertex which is a cell and is connected by two strong links, then the graph is a contradiction. Removing the
     * candidate from the vertex of interest implies that the candidate must be the solution for that vertex, thus
     * causing the cycle to contradict itself. However, considering the candidate to be the solution for that vertex
     * does not cause any contradiction in the cycle. Therefore, the candidate must be the solution for that vertex.
     */
    public static List<SetValue> groupedXCyclesRule2(Board<Cell> board) {
        return Arrays.stream(SudokuNumber.values())
                .flatMap(candidate -> {
                    var graph = buildGraph(board, candidate);
                    return graph.vertexSet()
                            .stream()
                            .filter(CellNode.class::isInstance)
                            .map(CellNode.class::cast)
                            .filter(vertex -> Strength.alternatingCycleExists(graph, vertex, Strength.STRONG))
                            .map(vertex -> new SetValue(vertex.cell(), candidate));
                })
                .toList();
    }

    /*
     * Rule 3:
     *
     * If a Grouped X-Cycle has an odd number of vertices and the edges alternate between strong and weak, except for
     * one vertex which is a cell and is connected by two weak links, then the graph is a contradiction. Considering the
     * candidate to be the solution for the vertex of interest implies that the candidate must be removed from that
     * vertex, thus causing the cycle to contradict itself. However, removing the candidate from that vertex does not
     * cause any contradiction in the cycle. Therefore, the candidate can be removed from the vertex.
     */
    public static List<RemoveCandidates> groupedXCyclesRule3(Board<Cell> board) {
        return Arrays.stream(SudokuNumber.values())
                .flatMap(candidate -> {
                    var graph = buildGraph(board, candidate);
                    return graph.vertexSet()
                            .stream()
                            .filter(CellNode.class::isInstance)
                            .map(CellNode.class::cast)
                            .filter(vertex -> Strength.alternatingCycleExists(graph, vertex, Strength.WEAK))
                            .map(vertex -> new LocatedCandidate(vertex.cell(), candidate));
                })
                .collect(LocatedCandidate.mergeToRemoveCandidates());
    }

    public static String toDOT(Graph<Node, StrengthEdge> graph, SudokuNumber candidate) {
        var writer = new StringWriter();
        var exporter = new DOTExporter<Node, StrengthEdge>();
        exporter.setGraphIdProvider(candidate::toString);
        exporter.setVertexAttributeProvider(vertex ->
                Map.of("label", DefaultAttribute.createAttribute(vertex.toString())));
        exporter.setEdgeAttributeProvider(Strength.STRENGTH_EDGE_ATTRIBUTE_PROVIDER);
        exporter.exportGraph(graph, writer);
        return writer.toString();
    }

    public static Graph<Node, StrengthEdge> buildGraph(Board<Cell> board, SudokuNumber candidate) {
        var builder = new GraphBuilder<>(new SimpleGraph<Node, StrengthEdge>(StrengthEdge.class));

        //Connect cells.
        board.getUnits()
                .stream()
                .map(unit -> unit.stream()
                        .filter(UnsolvedCell.class::isInstance)
                        .map(UnsolvedCell.class::cast)
                        .filter(cell -> cell.candidates().contains(candidate))
                        .toList())
                .forEach(withCandidate -> {
                    var strength = withCandidate.size() == 2 ? Strength.STRONG : Strength.WEAK;
                    withCandidate.stream().collect(Pair.zipEveryPair()).forEach(pair -> {
                        var a = pair.first();
                        var b = pair.second();
                        builder.addEdge(new CellNode(a), new CellNode(b), new StrengthEdge(strength));
                    });
                });

        //Add groups.
        var rowGroups = createGroups(candidate, board.rows(), RowGroup::new);
        var columnGroups = createGroups(candidate, board.getColumns(), ColumnGroup::new);
        var groups = new ArrayList<Group>(rowGroups);
        groups.addAll(columnGroups);
        builder.addVertices(groups.toArray(Node[]::new));

        //Connect groups to cells.
        connectGroupsToCells(candidate, builder, rowGroups, board::getRow, Node::getRow);
        connectGroupsToCells(candidate, builder, columnGroups, board::getColumn, Node::getColumn);
        connectGroupsToCells(candidate, builder, groups, board::getBlock, Node::getBlock);

        //Connect groups to groups.
        connectGroupsToGroups(candidate, builder, rowGroups, board::getRow, Node::getRow);
        connectGroupsToGroups(candidate, builder, columnGroups, board::getColumn, Node::getColumn);
        connectGroupsToGroups(candidate, builder, groups, board::getBlock, Node::getBlock);

        return builder.build();
    }

    private static <G extends Group> List<G> createGroups(
            SudokuNumber candidate,
            List<List<Cell>> units,
            Function<Set<UnsolvedCell>, G> groupConstructor
    ) {
        return units.stream()
                .flatMap(unit -> unit.stream()
                        .filter(UnsolvedCell.class::isInstance)
                        .map(UnsolvedCell.class::cast)
                        .filter(cell -> cell.candidates().contains(candidate))
                        .collect(Collectors.groupingBy(Cell::block, Collectors.toSet()))
                        .values()
                        .stream()
                        .filter(group -> group.size() >= 2)
                        .map(groupConstructor))
                .toList();
    }

    private static void connectGroupsToCells(
            SudokuNumber candidate,
            GraphBuilder<Node, StrengthEdge, ?> builder,
            List<? extends Group> groups,
            Function<Integer, List<Cell>> getUnit,
            Function<Group, Integer> getUnitIndex
    ) {
        groups.forEach(group -> {
            var otherCellsInUnit = getUnit.apply(getUnitIndex.apply(group))
                    .stream()
                    .filter(UnsolvedCell.class::isInstance)
                    .map(UnsolvedCell.class::cast)
                    .filter(cell -> cell.candidates().contains(candidate) && !group.getCells().contains(cell))
                    .toList();
            var strength = otherCellsInUnit.size() == 1 ? Strength.STRONG : Strength.WEAK;
            otherCellsInUnit.forEach(cell -> builder.addEdge(group, new CellNode(cell), new StrengthEdge(strength)));
        });
    }

    private static void connectGroupsToGroups(
            SudokuNumber candidate,
            GraphBuilder<Node, StrengthEdge, ?> builder,
            List<? extends Group> groups,
            Function<Integer, List<Cell>> getUnit,
            Function<Group, Integer> getUnitIndex
    ) {
        groups.stream()
                .collect(Pair.zipEveryPair())
                .filter(pair -> {
                    var a = pair.first();
                    var b = pair.second();
                    var commonCells = new HashSet<>(a.getCells());
                    commonCells.retainAll(b.getCells());
                    return getUnitIndex.apply(a).equals(getUnitIndex.apply(b)) && commonCells.isEmpty();
                })
                .forEach(pair -> {
                    var a = pair.first();
                    var b = pair.second();
                    var otherCellsInUnit = getUnit.apply(getUnitIndex.apply(a))
                            .stream()
                            .filter(UnsolvedCell.class::isInstance)
                            .map(UnsolvedCell.class::cast)
                            .filter(cell -> cell.candidates().contains(candidate) &&
                                    !a.getCells().contains(cell) &&
                                    !b.getCells().contains(cell))
                            .toList();
                    var strength = otherCellsInUnit.isEmpty() ? Strength.STRONG : Strength.WEAK;
                    builder.addEdge(a, b, new StrengthEdge(strength));
                });
    }

    public interface Node {
        boolean hasRow();
        int getRow();
        boolean hasColumn();
        int getColumn();
        int getBlock();
        Set<UnsolvedCell> getCells();
    }

    public record CellNode(UnsolvedCell cell) implements Node {
        @Override
        public boolean hasRow() {
            return true;
        }

        @Override
        public int getRow() {
            return cell.row();
        }

        @Override
        public boolean hasColumn() {
            return true;
        }

        @Override
        public int getColumn() {
            return cell.column();
        }

        @Override
        public int getBlock() {
            return cell.block();
        }

        @Override
        public Set<UnsolvedCell> getCells() {
            return Set.of(cell);
        }

        @Override
        public String toString() {
            return "[" + cell.row() + ',' + cell.column() + ']';
        }
    }

    public abstract static class Group implements Node {
        private final Set<UnsolvedCell> cells;

        public Group(Set<UnsolvedCell> cells) {
            if (cells.size() < 2 || cells.size() > Board.UNIT_SIZE_SQUARE_ROOT) {
                throw new IllegalArgumentException("Group can only be constructed with 2 or " +
                        Board.UNIT_SIZE_SQUARE_ROOT + " cells, but cells.size() is " + cells.size() + '.');
            }
            if (cells.stream().map(Cell::block).collect(Collectors.toSet()).size() != 1) {
                throw new IllegalArgumentException("Group cells must be in the same block.");
            }
            this.cells = cells;
        }

        @Override
        public int getBlock() {
            return cells.iterator().next().block();
        }

        @Override
        public Set<UnsolvedCell> getCells() {
            return cells;
        }

        @Override
        public boolean equals(Object o) {
            return this == o || o instanceof Group other && cells.equals(other.cells);
        }

        @Override
        public int hashCode() {
            return cells.hashCode();
        }

        @Override
        public String toString() {
            return cells.stream()
                    .map(cell -> "[" + cell.row() + ',' + cell.column() + ']')
                    .collect(Collectors.joining(", ", "{", "}"));
        }
    }

    public static class RowGroup extends Group {
        public RowGroup(Set<UnsolvedCell> cells) {
            super(cells);
            if (cells.stream().map(Cell::row).collect(Collectors.toSet()).size() != 1) {
                throw new IllegalArgumentException("RowGroup cells must be in the same row.");
            }
        }

        @Override
        public boolean hasRow() {
            return true;
        }

        @Override
        public int getRow() {
            return getCells().iterator().next().row();
        }

        @Override
        public boolean hasColumn() {
            return false;
        }

        @Override
        public int getColumn() {
            throw new UnsupportedOperationException();
        }
    }

    public static class ColumnGroup extends Group {
        public ColumnGroup(Set<UnsolvedCell> cells) {
            super(cells);
            if (cells.stream().map(Cell::column).collect(Collectors.toSet()).size() != 1) {
                throw new IllegalArgumentException("ColumnGroup cells must be in the same column.");
            }
        }

        @Override
        public boolean hasRow() {
            return false;
        }

        @Override
        public int getRow() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean hasColumn() {
            return true;
        }

        @Override
        public int getColumn() {
            return getCells().iterator().next().column();
        }
    }
}
