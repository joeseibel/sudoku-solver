use crate::{
    board::{self, Board},
    board_modification::{BoardModification, IteratorRemoveCandidatesExt, SetValue},
    cell::{Cell, IteratorCellExt, LocatedCandidate, Location, UnsolvedCell},
    collections::IteratorZipExt,
    graphs::{self, Strength},
    sudoku_number::SudokuNumber,
};
use itertools::Itertools;
use petgraph::{
    Graph,
    dot::{Config, Dot},
    graph::{NodeIndex, UnGraph},
};
use std::{
    collections::{BTreeSet, HashSet},
    fmt::Debug,
};
use strum::IntoEnumIterator;

// https://www.sudokuwiki.org/Grouped_X_Cycles
//
// Grouped X-Cycles are an extension of X-Cycles in which a vertex can be a cell or a group of cells. Just like
// X-Cycles, a Grouped X-Cycles graph is for a single candidate. A group is a set of cells with the candidate which
// share two units. This means that a group exists in the same block and the same row, or it exists in the same block
// and the same column.
//
// Similar to X-Cycles, the edges of a Grouped X-Cycles graph are either strong or weak. Unlike X-Cycles, the edges can
// connect two cells, a cell and a group, or two groups. A strong link connects two vertices in a unit when they are the
// only non-overlapping vertices in that unit. A weak link connects two vertices in a unit when they are not the only
// non-overlapping vertices in that unit.
//
// Since a vertex can be a cell or a group of cells, it is possible for vertices to overlap and even for edges to
// overlap. For example, consider a unit which has three cells with a candidate, two of which form a group and one which
// is outside the group. In this case there would be four vertices: three vertices for the cells and one for the group.
// Two of the cell vertices overlap with the cells of the group. This example would also have one strong link and three
// weak links. The strong link would connect the group to the cell outside the group. This is a strong link because when
// we discount the cells that overlap with the group, there are only two vertices in the unit. The weak links connect
// all the individual cells. They are weak because there are more than two cell vertices in the unit when we discount
// the group.
//
// A Grouped X-Cycle is a cycle in the graph in which the edges alternate between strong and weak links. If one vertex
// of a link contains the solution, then the other vertex must not contain the solution. If one cell of a strong link
// does not contain the solution, then the other vertex must contain the solution. If a vertex is a group, containing
// the solution means that one of the cells of the group is the solution. If a vertex is a cell, containing the solution
// means that the cell is the solution.
//
// Note that this implementation of Grouped X-Cycles can handle cases in which the chain is not strictly alternating
// between strong and weak links. It is tolerant of cases in which a strong link takes the place of a weak link.
//
// Rule 1:
//
// If a Grouped X-Cycle has an even number of vertices and therefore continuously alternates between strong and weak,
// then the graph is perfect and has no flaws. Each of the weak links can be treated as a strong link. The candidate can
// be removed from any cell which is in the same unit as both vertices of a weak link, but not contained in either of
// the vertices.
pub fn grouped_x_cycles_rule_1(board: &Board<Cell>) -> Vec<BoardModification> {
    SudokuNumber::iter()
        .flat_map(|candidate| {
            let mut graph = build_graph(board, candidate);
            trim(&mut graph);
            graphs::get_weak_edges_in_alternating_cycle(&graph)
                .into_iter()
                .flat_map(move |edge_index| {
                    let (source_index, target_index) = graph.edge_endpoints(edge_index).unwrap();
                    let source = &graph[source_index];
                    let target = &graph[target_index];

                    fn remove_from_unit<'a, U: IteratorCellExt<'a>>(
                        candidate: SudokuNumber,
                        source: &dyn Node,
                        target: &dyn Node,
                        source_unit_index: Option<usize>,
                        target_unit_index: Option<usize>,
                        get_unit: impl FnOnce(usize) -> U,
                    ) -> impl Iterator<Item = LocatedCandidate<'a>> {
                        let removals = if source_unit_index == target_unit_index
                            && let Some(source_unit_index) = source_unit_index
                        {
                            let removals = get_unit(source_unit_index)
                                .unsolved_cells()
                                .filter(move |cell| {
                                    cell.candidates().contains(&candidate)
                                        && !source.cells().contains(cell)
                                        && !target.cells().contains(cell)
                                })
                                .map(move |cell| (cell, candidate));
                            Some(removals)
                        } else {
                            None
                        };
                        removals.into_iter().flatten()
                    }

                    let row_removals = remove_from_unit(
                        candidate,
                        source.as_ref(),
                        target.as_ref(),
                        source.row(),
                        target.row(),
                        |index| board.get_row(index),
                    );
                    let column_removals = remove_from_unit(
                        candidate,
                        source.as_ref(),
                        target.as_ref(),
                        source.column(),
                        target.column(),
                        |index| board.get_column(index),
                    );
                    let block_removals = remove_from_unit(
                        candidate,
                        source.as_ref(),
                        target.as_ref(),
                        Some(source.block()),
                        Some(target.block()),
                        |index| board.get_block(index),
                    );
                    row_removals
                        .chain(column_removals)
                        .chain(block_removals)
                        .collect::<Vec<_>>()
                })
        })
        .merge_to_remove_candidates()
}

// Rule 2:
//
// If a Grouped X-Cycle has an odd number of vertices and the edges alternate between strong and weak, except for one
// vertex which is a cell and is connected by two strong links, then the graph is a contradiction. Removing the
// candidate from the vertex of interest implies that the candidate must be the solution for that vertex, thus causing
// the cycle to contradict itself. However, considering the candidate to be the solution for that vertex does not cause
// any contradiction in the cycle. Therefore, the candidate must be the solution for that vertex.
pub fn grouped_x_cycles_rule_2(board: &Board<Cell>) -> Vec<BoardModification> {
    SudokuNumber::iter()
        .flat_map(|candidate| {
            let graph = build_graph(board, candidate);
            graph
                .node_indices()
                .flat_map(move |index| match graph[index].as_cell_node() {
                    Ok(cell)
                        if graphs::alternating_cycle_exists(&graph, index, Strength::Strong) =>
                    {
                        Some(SetValue::from_cell(cell, candidate))
                    }
                    _ => None,
                })
        })
        .collect()
}

// Rule 3:
//
// If a Grouped X-Cycle has an odd number of vertices and the edges alternate between strong and weak, except for one
// vertex which is a cell and is connected by two weak links, then the graph is a contradiction. Considering the
// candidate to be the solution for the vertex of interest implies that the candidate must be removed from that vertex,
// thus causing the cycle to contradict itself. However, removing the candidate from that vertex does not cause any
// contradiction in the cycle. Therefore, the candidate can be removed from the vertex.
pub fn grouped_x_cycles_rule_3(board: &Board<Cell>) -> Vec<BoardModification> {
    SudokuNumber::iter()
        .flat_map(|candidate| {
            let graph = build_graph(board, candidate);
            graph
                .node_indices()
                .flat_map(move |index| match graph[index].as_cell_node() {
                    Ok(cell) if graphs::alternating_cycle_exists(&graph, index, Strength::Weak) => {
                        Some((cell, candidate))
                    }
                    _ => None,
                })
        })
        .merge_to_remove_candidates()
}

#[allow(dead_code)]
fn to_dot<'a>(graph: &UnGraph<Box<dyn Node + 'a>, Strength>) -> String {
    let dot = Dot::with_attr_getters(
        graph,
        &[Config::EdgeNoLabel, Config::NodeNoLabel],
        &|_, edge| match edge.weight() {
            Strength::Strong => String::new(),
            Strength::Weak => String::from("style = dashed"),
        },
        &|_, (_, vertex)| format!(r#"label = "{}""#, vertex.vertex_label()),
    );
    format!("{dot:?}")
}

// In grouped_x_cycles, Graph is used instead of GraphMap. This is different from every other graph-based logical
// solution in Rust. Unfortuately, GraphMap cannot be used here because GraphMap requires that the vertex type implement
// Copy. grouped_x_cycles has a vertex type of Box<dyn Node> and Box does not implement Copy. The reason for this is
// that Copy cannot be implemented for any type that also implements Drop, which Box does. See this explaination:
// https://doc.rust-lang.org/std/marker/trait.Copy.html#when-cant-my-type-be-copy
fn build_graph(
    board: &Board<Cell>,
    candidate: SudokuNumber,
) -> UnGraph<Box<dyn Node<'_> + '_>, Strength> {
    let mut graph: UnGraph<Box<dyn Node>, _> = Graph::new_undirected();

    // Connect cells.
    board
        .units()
        .map(|unit| {
            unit.unsolved_cells()
                .filter(|cell| cell.candidates().contains(&candidate))
                .collect::<Vec<_>>()
        })
        .for_each(|with_candidate| {
            let strength = if with_candidate.len() == 2 {
                Strength::Strong
            } else {
                Strength::Weak
            };
            for (&a, &b) in with_candidate.iter().zip_every_pair() {
                let a_index = graph
                    .node_indices()
                    .find(|&index| {
                        graph[index]
                            .as_cell_node()
                            .is_ok_and(|cell_node| cell_node == a)
                    })
                    .unwrap_or_else(|| graph.add_node(Box::new(a)));
                let b_index = graph
                    .node_indices()
                    .find(|&index| {
                        graph[index]
                            .as_cell_node()
                            .is_ok_and(|cell_node| cell_node == b)
                    })
                    .unwrap_or_else(|| graph.add_node(Box::new(b)));
                graph.add_edge(a_index, b_index, strength);
            }
        });

    // Add groups.
    fn create_groups<'a>(
        candidate: SudokuNumber,
        graph: &mut UnGraph<Box<dyn Node<'a> + 'a>, Strength>,
        units: impl Iterator<Item = impl IteratorCellExt<'a>>,
        group_constructor: impl Fn(BTreeSet<&'a UnsolvedCell>) -> Box<dyn Node + 'a>,
    ) -> Vec<NodeIndex> {
        units
            .flat_map(|unit| {
                unit.unsolved_cells()
                    .filter(|cell| cell.candidates().contains(&candidate))
                    .into_group_map_by(|cell| cell.block())
                    .values()
                    .filter(|group| group.len() >= 2)
                    .map(|group| graph.add_node(group_constructor(group.iter().copied().collect())))
                    .collect::<Vec<_>>()
            })
            .collect()
    }

    let row_group_indices = create_groups(candidate, &mut graph, board.rows(), |group| {
        Box::new(RowGroup::new(group))
    });
    let column_group_indices = create_groups(candidate, &mut graph, board.columns(), |group| {
        Box::new(ColumnGroup::new(group))
    });
    let mut group_indices = row_group_indices.clone();
    group_indices.extend(column_group_indices.iter());

    // Connect groups to cels.
    fn connect_groups_to_cells<'a, U: IteratorCellExt<'a>>(
        candidate: SudokuNumber,
        graph: &mut UnGraph<Box<dyn Node + 'a>, Strength>,
        group_indices: &[NodeIndex],
        get_unit: impl Fn(usize) -> U,
        get_unit_index: impl Fn(&dyn Node) -> usize,
    ) {
        for &group_index in group_indices {
            let group = &graph[group_index];
            let other_cells_in_unit: Vec<_> = get_unit(get_unit_index(group.as_ref()))
                .unsolved_cells()
                .filter(|cell| {
                    cell.candidates().contains(&candidate) && !group.cells().contains(cell)
                })
                .collect();
            let strength = if other_cells_in_unit.len() == 1 {
                Strength::Strong
            } else {
                Strength::Weak
            };
            for cell in other_cells_in_unit {
                let cell_index = graph
                    .node_indices()
                    .find(|&index| {
                        graph[index]
                            .as_cell_node()
                            .is_ok_and(|cell_node| cell_node == cell)
                    })
                    .unwrap();
                graph.add_edge(group_index, cell_index, strength);
            }
        }
    }

    connect_groups_to_cells(
        candidate,
        &mut graph,
        &row_group_indices,
        |index| board.get_row(index),
        |node| node.row().unwrap(),
    );
    connect_groups_to_cells(
        candidate,
        &mut graph,
        &column_group_indices,
        |index| board.get_column(index),
        |node| node.column().unwrap(),
    );
    connect_groups_to_cells(
        candidate,
        &mut graph,
        &group_indices,
        |index| board.get_block(index),
        |node| node.block(),
    );

    // Connect groups to groups.
    fn connect_groups_to_groups<'a, U: IteratorCellExt<'a>>(
        candidate: SudokuNumber,
        graph: &mut UnGraph<Box<dyn Node + 'a>, Strength>,
        group_indices: &[NodeIndex],
        get_unit: impl Fn(usize) -> U,
        get_unit_index: impl Fn(&dyn Node) -> usize,
    ) {
        for (&a_index, &b_index) in group_indices.iter().zip_every_pair() {
            let a = &graph[a_index];
            let b = &graph[b_index];
            if get_unit_index(a.as_ref()) == get_unit_index(b.as_ref())
                && a.cells().intersection(&b.cells()).next().is_none()
            {
                let mut other_cells_in_unit = get_unit(get_unit_index(a.as_ref()))
                    .unsolved_cells()
                    .filter(|cell| {
                        cell.candidates().contains(&candidate)
                            && !a.cells().contains(cell)
                            && !b.cells().contains(cell)
                    });
                let strength = if other_cells_in_unit.next().is_none() {
                    Strength::Strong
                } else {
                    Strength::Weak
                };
                graph.add_edge(a_index, b_index, strength);
            }
        }
    }

    connect_groups_to_groups(
        candidate,
        &mut graph,
        &row_group_indices,
        |index| board.get_row(index),
        |node| node.row().unwrap(),
    );
    connect_groups_to_groups(
        candidate,
        &mut graph,
        &column_group_indices,
        |index| board.get_column(index),
        |node| node.column().unwrap(),
    );
    connect_groups_to_groups(
        candidate,
        &mut graph,
        &group_indices,
        |index| board.get_block(index),
        |node| node.block(),
    );

    graph
}

// For the Rust implementation of Node, I considered two different approaches: implement Node as an enum or implement it
// as a trait object. Implementing Node as an enum in Rust would have been very similar to the Swift version which also
// implements Node as an enum. This would probably be the more efficient option.
//
// Implementing Node as a trait object would be more similar to how the JVM languages implement Node. One of the
// advantages that Rust has over Java or Kotlin is that a separate CellNode type does not need to exist since Node can
// be implemented for &UnsolvedCell. Scala also has that advantage because Node in Scala is defined as a union type.
//
// Implementing Node as a trait object is likely to be less efficient than implementing Node as an enum, although I have
// not measured the difference. There are three specific inefficiencies with trait objects:
//   1. There is a level of indirection when calling methods on a trait object. Dynamic dispatch using a vtable is more
//      costly than static dispatch.
//   2. The compiler cannot inline method calls on a trait object.
//   3. When constructing a graph with trait objects as the graph's vertices, each trait object must be wrapped in a Box
//      and allocated on the heap. This is because the size of a graph's vertex type must be known at compile time and
//      the size of a dyn Node cannot be known at compile time, but the size of a Box<dyn Node> is known at compile
//      time. There is a cost to these added heap allocations and deallocations.
//
// In the end, I decided to implement Node as a trait object instead of an enum so that I could explore and become more
// familiar with trait objects. Even though implementing Node as an enum is likely more efficient, the knowledge gained
// by implementing a trait object is worth the performance hit, which I expect to be small.
trait Node<'a>: Debug {
    fn row(&self) -> Option<usize>;
    fn column(&self) -> Option<usize>;
    fn block(&self) -> usize;
    fn cells(&self) -> BTreeSet<&'a UnsolvedCell>;
    fn as_cell_node(&self) -> Result<&'a UnsolvedCell, ()>;
    fn vertex_label(&self) -> String;
}

impl<'a> Node<'a> for &'a UnsolvedCell {
    fn row(&self) -> Option<usize> {
        Some(UnsolvedCell::row(self))
    }

    fn column(&self) -> Option<usize> {
        Some(UnsolvedCell::column(self))
    }

    fn block(&self) -> usize {
        UnsolvedCell::block(self)
    }

    fn cells(&self) -> BTreeSet<&'a UnsolvedCell> {
        let mut set: BTreeSet<&UnsolvedCell> = BTreeSet::new();
        set.insert(self);
        set
    }

    fn as_cell_node(&self) -> Result<&'a UnsolvedCell, ()> {
        Ok(self)
    }

    fn vertex_label(&self) -> String {
        UnsolvedCell::vertex_label(self)
    }
}

#[derive(Debug)]
struct RowGroup<'a> {
    cells: BTreeSet<&'a UnsolvedCell>,
}

impl<'a> RowGroup<'a> {
    fn new(cells: BTreeSet<&'a UnsolvedCell>) -> Self {
        validate_group(&cells);
        assert_eq!(
            cells
                .iter()
                .map(|cell| cell.row())
                .collect::<HashSet<_>>()
                .len(),
            1,
            "RowGroup cells must be in the same row."
        );
        RowGroup { cells }
    }
}

impl<'a> Node<'a> for RowGroup<'a> {
    fn row(&self) -> Option<usize> {
        Some(UnsolvedCell::row(self.cells.first().unwrap()))
    }

    fn column(&self) -> Option<usize> {
        None
    }

    fn block(&self) -> usize {
        self.cells.first().unwrap().block()
    }

    fn cells(&self) -> BTreeSet<&'a UnsolvedCell> {
        self.cells.clone()
    }

    fn as_cell_node(&self) -> Result<&'a UnsolvedCell, ()> {
        Err(())
    }

    fn vertex_label(&self) -> String {
        format!(
            "{{{}}}",
            self.cells
                .iter()
                .map(|cell| cell.vertex_label())
                .collect::<Vec<_>>()
                .join(", ")
        )
    }
}

#[derive(Debug)]
struct ColumnGroup<'a> {
    cells: BTreeSet<&'a UnsolvedCell>,
}

impl<'a> ColumnGroup<'a> {
    fn new(cells: BTreeSet<&'a UnsolvedCell>) -> Self {
        validate_group(&cells);
        assert_eq!(
            cells
                .iter()
                .map(|cell| cell.column())
                .collect::<HashSet<_>>()
                .len(),
            1,
            "ColumnGroup cells must be in the same column."
        );
        ColumnGroup { cells }
    }
}

impl<'a> Node<'a> for ColumnGroup<'a> {
    fn row(&self) -> Option<usize> {
        None
    }

    fn column(&self) -> Option<usize> {
        Some(UnsolvedCell::column(self.cells.first().unwrap()))
    }

    fn block(&self) -> usize {
        self.cells.first().unwrap().block()
    }

    fn cells(&self) -> BTreeSet<&'a UnsolvedCell> {
        self.cells.clone()
    }

    fn as_cell_node(&self) -> Result<&'a UnsolvedCell, ()> {
        Err(())
    }

    fn vertex_label(&self) -> String {
        format!(
            "{{{}}}",
            self.cells
                .iter()
                .map(|cell| cell.vertex_label())
                .collect::<Vec<_>>()
                .join(", ")
        )
    }
}

fn validate_group(cells: &BTreeSet<&UnsolvedCell>) {
    assert!(
        (2..=board::UNIT_SIZE_SQUARE_ROOT).contains(&cells.len()),
        "Group can only be created with 2 or {} cells, but cells.len() is {}.",
        board::UNIT_SIZE_SQUARE_ROOT,
        cells.len()
    );
    assert_eq!(
        cells
            .iter()
            .map(|cell| cell.block())
            .collect::<HashSet<_>>()
            .len(),
        1,
        "Group cells must be in the same block."
    );
}

// Continuously trims the graph of vertices that cannot be part of a cycle for X-Cycles rule 1. The modified graph will
// either be empty or only contain vertices with a degree of two or more and be connected by at least one strong link
// and one weak link.
fn trim<'a>(graph: &mut UnGraph<Box<dyn Node + 'a>, Strength>) {
    loop {
        let to_remove = graph.node_indices().find(|&index| {
            let edges: Vec<_> = graph.edges(index).collect();
            edges.len() < 2 || !edges.iter().any(|edge| *edge.weight() == Strength::Strong)
        });
        match to_remove {
            Some(to_remove) => graph.remove_node(to_remove),
            None => break,
        };
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::{logic::assertions, remove_candidates};
    use indoc::indoc;

    #[test]
    fn test_to_dot() {
        let Cell::UnsolvedCell(a) = UnsolvedCell::with_all_candidates(6, 1) else {
            panic!()
        };
        let a = Box::new(&a);
        let b_cells = [
            UnsolvedCell::with_all_candidates(6, 6),
            UnsolvedCell::with_all_candidates(6, 7),
            UnsolvedCell::with_all_candidates(6, 8),
        ];
        let b = Box::new(RowGroup::new(b_cells.iter().unsolved_cells().collect()));
        let c_cells = [
            UnsolvedCell::with_all_candidates(6, 2),
            UnsolvedCell::with_all_candidates(8, 2),
        ];
        let c = Box::new(ColumnGroup::new(c_cells.iter().unsolved_cells().collect()));
        let mut graph: UnGraph<Box<dyn Node>, _> = Graph::new_undirected();
        let a_index = graph.add_node(a);
        let b_index = graph.add_node(b);
        let c_index = graph.add_node(c);
        graph.add_edge(a_index, b_index, Strength::Weak);
        graph.add_edge(a_index, c_index, Strength::Strong);
        let actual = to_dot(&graph);
        let expected = indoc! {r#"
            graph {
                0 [ label = "[6,1]"]
                1 [ label = "{[6,6], [6,7], [6,8]}"]
                2 [ label = "{[6,2], [8,2]}"]
                0 -- 1 [ style = dashed]
                0 -- 2 [ ]
            }
        "#};
        assert_eq!(expected, actual);
    }

    #[test]
    #[should_panic(expected = "Group can only be created with 2 or 3 cells, but cells.len() is 0.")]
    fn test_group_too_small() {
        RowGroup::new(BTreeSet::new());
    }

    #[test]
    #[should_panic(expected = "Group can only be created with 2 or 3 cells, but cells.len() is 4.")]
    fn test_group_too_large() {
        let cells = [
            UnsolvedCell::with_all_candidates(0, 0),
            UnsolvedCell::with_all_candidates(0, 1),
            UnsolvedCell::with_all_candidates(0, 2),
            UnsolvedCell::with_all_candidates(0, 3),
        ];
        RowGroup::new(cells.iter().unsolved_cells().collect());
    }

    #[test]
    #[should_panic(expected = "Group cells must be in the same block.")]
    fn test_group_not_in_same_block() {
        let cells = [
            UnsolvedCell::with_all_candidates(0, 0),
            UnsolvedCell::with_all_candidates(0, 3),
        ];
        RowGroup::new(cells.iter().unsolved_cells().collect());
    }

    #[test]
    #[should_panic(expected = "RowGroup cells must be in the same row.")]
    fn test_row_group_not_in_same_row() {
        let cells = [
            UnsolvedCell::with_all_candidates(0, 0),
            UnsolvedCell::with_all_candidates(1, 1),
        ];
        RowGroup::new(cells.iter().unsolved_cells().collect());
    }

    #[test]
    #[should_panic(expected = "ColumnGroup cells must be in the same column.")]
    fn test_column_group_not_in_same_column() {
        let cells = [
            UnsolvedCell::with_all_candidates(0, 0),
            UnsolvedCell::with_all_candidates(1, 1),
        ];
        ColumnGroup::new(cells.iter().unsolved_cells().collect());
    }

    #[test]
    fn rule_1_test_1() {
        let board = "\
            185{49}2637{49}\
            {234}6{234}{3579}{134}{1357}{2458}{28}{2589}\
            {234}97{345}{34}81{26}{2456}\
            {4678}1{48}{348}52{68}9{37}\
            {245789}{27}{2489}{348}6{34}{258}{13}{137}\
            {2568}3{28}179{2568}4{2568}\
            {2378}416{38}{37}95{238}\
            {23789}{27}{2389}{357}{1348}{13457}{2468}{12368}{123468}\
            {38}5629{134}7{138}{1348}\
        ";
        let expected = [
            remove_candidates!(2, 3, 4),
            remove_candidates!(2, 8, 4),
            remove_candidates!(7, 5, 4),
            remove_candidates!(7, 8, 4),
        ];
        assertions::assert_logical_solution(&expected, board, grouped_x_cycles_rule_1);
    }

    #[test]
    fn rule_1_test_2() {
        let board = "\
            3{279}1{89}{258}4{259}6{257}\
            8{279}4{69}{256}{59}{2359}1{2357}\
            56{29}713{289}{289}4\
            {147}3{578}{46}{56}2{16}{78}9\
            {147}{128}{2578}{346}9{578}{16}{2378}{2358}\
            6{289}{25789}1{3578}{578}{2358}4{2358}\
            {17}{18}{378}546{2389}{2389}{238}\
            256{389}{378}{789}4{38}1\
            94{38}2{38}1756\
        ";
        let expected = [
            remove_candidates!(2, 7, 8),
            remove_candidates!(4, 2, 8),
            remove_candidates!(4, 7, 8),
            remove_candidates!(5, 2, 8),
            remove_candidates!(5, 6, 8),
            remove_candidates!(6, 2, 8),
            remove_candidates!(6, 6, 8),
            remove_candidates!(6, 7, 8),
        ];
        assertions::assert_logical_solution(&expected, board, grouped_x_cycles_rule_1);
    }

    #[test]
    fn rule_1_test_3() {
        let board = "\
            3{279}1{89}{258}4{259}6{257}\
            8{279}4{69}{256}{59}{2359}1{2357}\
            56{29}7138{29}4\
            {147}3{578}{46}{56}2{16}{78}9\
            {147}{128}{2578}{346}9{578}{16}{2378}{2358}\
            6{289}{25789}1{3578}{578}{235}4{2358}\
            {17}{18}{378}546{239}{2389}{238}\
            256{389}{378}{789}4{38}1\
            94{38}2{38}1756\
        ";
        let expected = [
            remove_candidates!(4, 2, 8),
            remove_candidates!(4, 7, 8),
            remove_candidates!(5, 2, 8),
            remove_candidates!(6, 2, 8),
            remove_candidates!(6, 7, 8),
        ];
        assertions::assert_logical_solution(&expected, board, grouped_x_cycles_rule_1);
    }

    #[test]
    fn rule_2_test_1() {
        let board = "\
            {123}8{249}5{12}7{234}6{12349}\
            7{36}{25}94{126}{235}{1235}8\
            {125}{2456}{2459}38{26}7{259}{1249}\
            {56}7{456}{246}981{23}{235}\
            {26}18{26}53947\
            9{245}3{124}7{12}68{25}\
            8{23}1765{234}{239}{2349}\
            4{25}7{12}398{125}6\
            {356}9{26}8{12}4{235}7{123}\
        ";
        let expected = [SetValue::from_indices(4, 0, 2)];
        assertions::assert_logical_solution(&expected, board, grouped_x_cycles_rule_2);
    }

    #[test]
    fn rule_2_test_2() {
        let board = "\
            2{168}4{16}{36}79{38}5\
            {168}9{178}5{136}2{167}4{38}\
            {167}354982{167}{16}\
            {138}{178}6{78}295{13}4\
            {135}{1578}2{78}4{16}{67}9{136}\
            {17}4935{16}8{167}2\
            {1568}{156}3974{16}2{168}\
            92{18}{16}{168}3457\
            4{1678}{178}2{18}53{168}9\
        ";
        let expected = [
            SetValue::from_indices(0, 7, 8),
            SetValue::from_indices(6, 8, 8),
        ];
        assertions::assert_logical_solution(&expected, board, grouped_x_cycles_rule_2);
    }

    #[test]
    fn rule_3_test_1() {
        let board = "\
            {128}{124}37{89}65{2489}{49}\
            7{248}{48}5{2389}{38}6{2489}1\
            569{128}4{18}{38}7{238}\
            {1368}{148}2{489}{137}{348}{38}5{3679}\
            {38}956{378}241{378}\
            {1368}7{48}{489}{138}52{689}{3689}\
            9{28}6{14}5{14}73{28}\
            437{28}{268}91{68}5\
            {28}513{68}79{2468}{2468}\
        ";
        let expected = [remove_candidates!(2, 3, 8)];
        assertions::assert_logical_solution(&expected, board, grouped_x_cycles_rule_3);
    }

    #[test]
    fn rule_3_test_2() {
        let board = "\
            185{49}2637{49}\
            {234}6{234}{3579}{134}{1357}{2458}{28}{59}\
            {234}97{35}{34}81{26}{256}\
            {4678}1{48}{38}52{68}9{37}\
            {2579}{27}{29}{348}6{34}{258}{13}{137}\
            {2568}3{28}179{2568}4{2568}\
            {2378}416{38}{37}95{238}\
            {3789}{27}{2389}{357}{1348}{1357}{2468}{12368}{12368}\
            {38}5629{134}7{138}{1348}\
        ";
        let expected = [remove_candidates!(1, 6, 2)];
        assertions::assert_logical_solution(&expected, board, grouped_x_cycles_rule_3);
    }

    #[test]
    fn rule_3_test_3() {
        let board = "\
            185{49}2637{49}\
            {234}6{23}{3579}{134}{1357}{458}{28}{59}\
            {234}97{35}{34}81{26}{256}\
            {678}14{38}52{68}9{37}\
            {2579}{27}{29}{348}6{34}{258}{13}{137}\
            {2568}3{28}179{2568}4{2568}\
            {2378}416{38}{37}95{238}\
            {3789}{27}{2389}{357}{1348}{1357}{2468}{12368}{12368}\
            {38}5629{134}7{138}{1348}\
        ";
        let expected = [remove_candidates!(7, 6, 8), remove_candidates!(7, 7, 8)];
        assertions::assert_logical_solution(&expected, board, grouped_x_cycles_rule_3);
    }

    #[test]
    fn rule_3_test_4() {
        let board = "\
            185{49}2637{49}\
            {234}6{23}{3579}{134}{1357}{458}{28}{59}\
            {234}97{35}{34}81{26}{256}\
            {678}14{38}52{68}9{37}\
            {2579}{27}{29}{348}6{34}{258}{13}{137}\
            {2568}3{28}179{2568}4{2568}\
            {2378}416{38}{37}95{238}\
            {3789}{27}{2389}{357}{1348}{1357}{246}{12368}{12368}\
            {38}5629{134}7{138}{1348}\
        ";
        let expected = [remove_candidates!(7, 7, 8)];
        assertions::assert_logical_solution(&expected, board, grouped_x_cycles_rule_3);
    }

    #[test]
    fn rule_3_test_5() {
        let board = "\
            1{278}5{37}{238}946{278}\
            3496{28}{278}{2578}1{2578}\
            {268}{278}{268}1453{289}{2789}\
            {248}9{248}{58}1{468}{2568}73\
            56{238}{37}9{78}14{28}\
            71{348}2{3568}{46}{568}{589}{5689}\
            {2468}5{2468}971{268}3{2468}\
            {2468}{28}7{458}{2568}39{258}1\
            931{458}{2568}{268}{25678}{258}{245678}\
        ";
        let expected = [
            remove_candidates!(2, 0, 2),
            remove_candidates!(2, 2, 2),
            remove_candidates!(7, 1, 2),
        ];
        assertions::assert_logical_solution(&expected, board, grouped_x_cycles_rule_3);
    }

    #[test]
    fn rule_3_test_6() {
        let board = "\
            62{489}{48}53{489}71\
            31{489}{478}{24789}{249}{2489}{56}{56}\
            {45}{4589}71{2489}63{249}{48}\
            {2457}{4589}{2489}3{489}16{2459}{4578}\
            {247}{3489}{23489}{4568}{4689}{4589}{24789}1{3478}\
            1{34589}62{489}7{489}{459}{3458}\
            {24}{34}19{23478}{248}5{46}{467}\
            87{245}{56}{1246}{245}{14}39\
            96{345}{457}{1347}{45}{147}82\
        ";
        let expected = [
            remove_candidates!(2, 4, 4),
            remove_candidates!(3, 2, 4),
            remove_candidates!(4, 2, 4),
            remove_candidates!(4, 6, 4),
            remove_candidates!(5, 6, 4),
            remove_candidates!(6, 4, 4),
            remove_candidates!(6, 5, 4),
        ];
        assertions::assert_logical_solution(&expected, board, grouped_x_cycles_rule_3);
    }
}
