package sudokusolver.javanostreams.logic.extreme;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

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
        var removals = new Removals();
        for (var candidate : SudokuNumber.values()) {
            var graph = buildGraph(board, candidate);
            Strength.trim(graph);
            for (var edge : Strength.getWeakEdgesInAlternatingCycle(graph)) {
                var source = graph.getEdgeSource(edge);
                var target = graph.getEdgeTarget(edge);
                removeFromUnit(removals, candidate, source, target, Node::hasRow, Node::getRow, board::getRow);
                removeFromUnit(removals, candidate, source, target, Node::hasColumn, Node::getColumn, board::getColumn);
                removeFromUnit(removals, candidate, source, target, node -> true, Node::getBlock, board::getBlock);
            }
        }
        return removals.toList();
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
    private static void removeFromUnit(
            Removals removals,
            SudokuNumber candidate,
            Node source,
            Node target,
            Predicate<Node> hasUnitIndex,
            ToIntFunction<Node> getUnitIndex,
            IntFunction<List<Cell>> getUnit
    ) {
        if (hasUnitIndex.test(source) && hasUnitIndex.test(target)) {
            var sourceUnitIndex = getUnitIndex.applyAsInt(source);
            if (sourceUnitIndex == getUnitIndex.applyAsInt(target)) {
                for (var cell : getUnit.apply(sourceUnitIndex)) {
                    if (cell instanceof UnsolvedCell unsolved &&
                            unsolved.candidates().contains(candidate) &&
                            !source.getCells().contains(unsolved) &&
                            !target.getCells().contains(unsolved)
                    ) {
                        removals.add(unsolved, candidate);
                    }
                }
            }
        }
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
        var modifications = new ArrayList<SetValue>();
        for (var candidate : SudokuNumber.values()) {
            var graph = buildGraph(board, candidate);
            for (var vertex : graph.vertexSet()) {
                if (vertex instanceof CellNode cellNode &&
                        Strength.alternatingCycleExists(graph, cellNode, Strength.STRONG)
                ) {
                    modifications.add(new SetValue(cellNode.cell(), candidate));
                }
            }
        }
        return modifications;
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
        var removals = new Removals();
        for (var candidate : SudokuNumber.values()) {
            var graph = buildGraph(board, candidate);
            for (var vertex : graph.vertexSet()) {
                if (vertex instanceof CellNode cellNode &&
                        Strength.alternatingCycleExists(graph, cellNode, Strength.WEAK)
                ) {
                    removals.add(cellNode.cell(), candidate);
                }
            }
        }
        return removals.toList();
    }

    public static String toDOT(Graph<Node, StrengthEdge> graph, SudokuNumber candidate) {
        var writer = new StringWriter();
        var exporter = new DOTExporter<Node, StrengthEdge>();
        exporter.setGraphIdProvider(candidate::toString);
        exporter.setVertexAttributeProvider(vertex ->
                Map.of("label", DefaultAttribute.createAttribute(vertex.toString())));
        exporter.setEdgeAttributeProvider(StrengthEdge::getEdgeAttributes);
        exporter.exportGraph(graph, writer);
        return writer.toString();
    }

    public static Graph<Node, StrengthEdge> buildGraph(Board<Cell> board, SudokuNumber candidate) {
        var builder = new GraphBuilder<>(new SimpleGraph<Node, StrengthEdge>(StrengthEdge.class));

        //Connect cells.
        for (var unit : board.getUnits()) {
            var withCandidate = new ArrayList<UnsolvedCell>();
            for (var cell : unit) {
                if (cell instanceof UnsolvedCell unsolved && unsolved.candidates().contains(candidate)) {
                    withCandidate.add(unsolved);
                }
            }
            var strength = withCandidate.size() == 2 ? Strength.STRONG : Strength.WEAK;
            for (var i = 0; i < withCandidate.size() - 1; i++) {
                var a = withCandidate.get(i);
                for (var j = i + 1; j < withCandidate.size(); j++) {
                    var b = withCandidate.get(j);
                    builder.addEdge(new CellNode(a), new CellNode(b), new StrengthEdge(strength));
                }
            }
        }

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
        var groups = new ArrayList<G>();
        for (var unit : units) {
            var groupings = new HashMap<Integer, Set<UnsolvedCell>>();
            for (var cell : unit) {
                if (cell instanceof UnsolvedCell unsolved && unsolved.candidates().contains(candidate)) {
                    groupings.computeIfAbsent(unsolved.block(), key -> new HashSet<>()).add(unsolved);
                }
            }
            for (var group : groupings.values()) {
                if (group.size() >= 2) {
                    groups.add(groupConstructor.apply(group));
                }
            }
        }
        return groups;
    }

    private static void connectGroupsToCells(
            SudokuNumber candidate,
            GraphBuilder<Node, StrengthEdge, ?> builder,
            List<? extends Group> groups,
            IntFunction<List<Cell>> getUnit,
            ToIntFunction<Group> getUnitIndex
    ) {
        for (var group : groups) {
            var otherCellsInUnit = new ArrayList<UnsolvedCell>();
            for (var cell : getUnit.apply(getUnitIndex.applyAsInt(group))) {
                if (cell instanceof UnsolvedCell unsolved &&
                        unsolved.candidates().contains(candidate) &&
                        !group.getCells().contains(unsolved)
                ) {
                    otherCellsInUnit.add(unsolved);
                }
            }
            var strength = otherCellsInUnit.size() == 1 ? Strength.STRONG : Strength.WEAK;
            for (var cell : otherCellsInUnit) {
                builder.addEdge(group, new CellNode(cell), new StrengthEdge(strength));
            }
        }
    }

    private static void connectGroupsToGroups(
            SudokuNumber candidate,
            GraphBuilder<Node, StrengthEdge, ?> builder,
            List<? extends Group> groups,
            IntFunction<List<Cell>> getUnit,
            ToIntFunction<Group> getUnitIndex
    ) {
        for (var i = 0; i < groups.size() - 1; i++) {
            var a = groups.get(i);
            for (var j = i + 1; j < groups.size(); j++) {
                var b = groups.get(j);
                var commonCells = new HashSet<>(a.getCells());
                commonCells.retainAll(b.getCells());
                if (getUnitIndex.applyAsInt(a) == getUnitIndex.applyAsInt(b) && commonCells.isEmpty()) {
                    var otherCellsInUnit = new ArrayList<UnsolvedCell>();
                    for (var cell : getUnit.apply(getUnitIndex.applyAsInt(a))) {
                        if (cell instanceof UnsolvedCell unsolved &&
                                unsolved.candidates().contains(candidate) &&
                                !a.getCells().contains(unsolved) &&
                                !b.getCells().contains(unsolved)
                        ) {
                            otherCellsInUnit.add(unsolved);
                        }
                    }
                    var strength = otherCellsInUnit.isEmpty() ? Strength.STRONG : Strength.WEAK;
                    builder.addEdge(a, b, new StrengthEdge(strength));
                }
            }
        }
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
            return cell.getVertexLabel();
        }
    }

    public abstract static class Group implements Node {
        private final Set<UnsolvedCell> cells;

        public Group(Set<UnsolvedCell> cells) {
            if (cells.size() < 2 || cells.size() > Board.UNIT_SIZE_SQUARE_ROOT) {
                throw new IllegalArgumentException("Group can only be constructed with 2 or " +
                        Board.UNIT_SIZE_SQUARE_ROOT + " cells, but cells.size() is " + cells.size() + '.');
            }
            var blockIndices = new HashSet<Integer>();
            for (var cell : cells) {
                blockIndices.add(cell.block());
            }
            if (blockIndices.size() != 1) {
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
            var builder = new StringBuilder();
            builder.append('{');
            for (var iterator = cells.iterator(); iterator.hasNext(); ) {
                builder.append(iterator.next().getVertexLabel());
                if (iterator.hasNext()) {
                    builder.append(", ");
                }
            }
            builder.append('}');
            return builder.toString();
        }
    }

    public static class RowGroup extends Group {
        public RowGroup(Set<UnsolvedCell> cells) {
            super(cells);
            var rowIndices = new HashSet<Integer>();
            for (var cell : cells) {
                rowIndices.add(cell.row());
            }
            if (rowIndices.size() != 1) {
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
            var columnIndices = new HashSet<Integer>();
            for (var cell : cells) {
                columnIndices.add(cell.column());
            }
            if (columnIndices.size() != 1) {
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