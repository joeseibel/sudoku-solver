use crate::{
    board::Board,
    board_modification::{BoardModification, IteratorRemoveCandidatesExt, SetValue},
    cell::{Cell, IteratorCellExt, LocatedCandidate, Location, UnsolvedCell},
    collections::IteratorZipExt,
    graphs::{self, Strength},
    sudoku_number::SudokuNumber,
};
use petgraph::{
    dot::{Config, Dot},
    prelude::{GraphMap, UnGraphMap},
};
use std::collections::HashSet;
use strum::IntoEnumIterator;

// http://www.sudokuwiki.org/X_Cycles
// http://www.sudokuwiki.org/X_Cycles_Part_2
//
// X-Cycles is based on a graph type which is an extension of single's chain. An X-Cycles graph is for a single
// candidate and can have either strong or weak links. A strong link connects two cells in a unit when they are the only
// unsolved cells in that unit with the candidate. A weak link connects two cells in a unit when they are not the only
// unsolved cells in that unit with the candidate. An X-Cycle is a cycle in the graph in which the edges alternate
// between strong and weak links. If one cell of a link is the solution, then the other cell must not be the solution.
// If one cell of a strong link is not the solution, then the other cell must be the solution.
//
// Note that this implementation of X-Cycles can handle cases in which the chain is not strictly alternating between
// strong and weak links. It is tolerant of cases in which a strong link takes the place of a weak link.
//
// Rule 1:
//
// If an X-Cycle has an even number of vertices and therefore continuously alternates between strong and weak, then the
// graph is perfect and has no flaws. Each of the weak links can be treated as a strong link. The candidate can be
// removed from any other cell which is in the same unit as both vertices of a weak link.
pub fn x_cycles_rule_1(board: &Board<Cell>) -> Vec<BoardModification> {
    SudokuNumber::iter()
        .flat_map(|candidate| {
            let mut graph = create_strong_links(board, candidate);
            add_weak_links(&mut graph);
            trim(&mut graph);
            get_weak_edges_in_alternating_cycle(&graph)
                .iter()
                .flat_map(|(source, target, _)| {
                    fn remove_from_unit<'a, U: IteratorCellExt<'a>>(
                        candidate: SudokuNumber,
                        source: &UnsolvedCell,
                        target: &UnsolvedCell,
                        get_unit_index: impl Fn(&UnsolvedCell) -> usize,
                        get_unit: impl FnOnce(usize) -> U,
                    ) -> impl Iterator<Item = LocatedCandidate<'a>> {
                        let removals = if get_unit_index(source) == get_unit_index(target) {
                            let removals = get_unit(get_unit_index(source))
                                .unsolved_cells()
                                .filter(move |&cell| {
                                    cell.candidates().contains(&candidate)
                                        && cell != source
                                        && cell != target
                                })
                                .map(move |cell| (cell, candidate));
                            Some(removals)
                        } else {
                            None
                        };
                        removals.into_iter().flatten()
                    }

                    let row_removals =
                        remove_from_unit(candidate, source, target, Location::row, |index| {
                            board.get_row(index)
                        });
                    let column_removals =
                        remove_from_unit(candidate, source, target, Location::column, |index| {
                            board.get_column(index)
                        });
                    let block_removals =
                        remove_from_unit(candidate, source, target, UnsolvedCell::block, |index| {
                            board.get_block(index)
                        });
                    row_removals.chain(column_removals).chain(block_removals)
                })
                .collect::<Vec<_>>()
        })
        .merge_to_remove_candidates()
}

// Rule 2:
//
// If an X-Cycle has an odd number of vertices and the edges alternate between strong and weak, except for one vertex
// which is connected by two strong links, then the graph is a contradiction. Removing the candidate from the vertex of
// interest implies that the candidate must be the solution for that vertex, thus causing the cycle to contradict
// itself. However, considering the candidate to be the solution for that vertex does not cause any contradiction in the
// cycle. Therefore, the candidate must be the solution for that vertex.
pub fn x_cycles_rule_2(board: &Board<Cell>) -> Vec<BoardModification> {
    SudokuNumber::iter()
        .flat_map(|candidate| {
            let mut graph = create_strong_links(board, candidate);
            add_weak_links(&mut graph);
            graph
                .nodes()
                .filter(|vertex| alternating_cycle_exists(&graph, vertex, Strength::Strong))
                .map(move |vertex| SetValue::from_cell(vertex, candidate))
                .collect::<Vec<_>>()
        })
        .collect()
}

// Rule 3:
//
// If an X-Cycle has an odd number of vertices and the edges alternate between strong and weak, except for one vertex
// which is connected by two weak links, then the graph is a contradiction. Considering the candidate to be the solution
// for the vertex of interest implies that the candidate must be removed from that vertex, thus causing the cycle to
// contradict itself. However, removing the candidate from that vertex does not cause any contradiction in the cycle.
// Therefore, the candidate can be removed from the vertex.
pub fn x_cycles_rule_3(board: &Board<Cell>) -> Vec<BoardModification> {
    SudokuNumber::iter()
        .flat_map(|candidate| {
            let mut graph = create_strong_links(board, candidate);
            add_weak_links(&mut graph);
            additional_weak_links(&mut graph, board, candidate);
            graph
                .nodes()
                .filter(|vertex| alternating_cycle_exists(&graph, vertex, Strength::Weak))
                .map(move |vertex| (vertex, candidate))
                .collect::<Vec<_>>()
        })
        .merge_to_remove_candidates()
}

#[allow(dead_code)]
fn to_dot(graph: &UnGraphMap<&UnsolvedCell, Strength>) -> String {
    let dot = Dot::with_attr_getters(
        graph,
        &[Config::EdgeNoLabel, Config::NodeNoLabel],
        &|_, (_, _, strength)| match strength {
            Strength::Strong => String::new(),
            Strength::Weak => String::from("style = dashed"),
        },
        &|_, (cell, _)| format!(r#"label = "{}""#, cell.vertex_label()),
    );
    format!("{dot:?}")
}

fn create_strong_links(
    board: &Board<Cell>,
    candidate: SudokuNumber,
) -> UnGraphMap<&UnsolvedCell, Strength> {
    let edges = board
        .units()
        .map(|unit| {
            unit.unsolved_cells()
                .filter(|cell| cell.candidates().contains(&candidate))
                .collect::<Vec<_>>()
        })
        .filter_map(|with_candidate| match with_candidate[..] {
            [a, b] => Some((a, b, Strength::Strong)),
            _ => None,
        });
    GraphMap::from_edges(edges)
}

fn add_weak_links(graph: &mut UnGraphMap<&UnsolvedCell, Strength>) {
    let edges: Vec<_> = graph
        .nodes()
        .zip_every_pair()
        .filter(|(a, b)| a.is_in_same_unit(b) && !graph.contains_edge(a, b))
        .map(|(a, b)| (a, b, Strength::Weak))
        .collect();
    graph.extend(edges);
}

fn additional_weak_links<'a>(
    graph: &mut UnGraphMap<&'a UnsolvedCell, Strength>,
    board: &'a Board<Cell>,
    candidate: SudokuNumber,
) {
    let edges: Vec<_> = board
        .cells()
        .unsolved_cells()
        .filter(|cell| cell.candidates().contains(&candidate) && !graph.contains_node(cell))
        .flat_map(|cell| {
            graph
                .nodes()
                .filter(|vertex| vertex.is_in_same_unit(cell))
                .map(move |vertex| (vertex, cell, Strength::Weak))
        })
        .collect();
    graph.extend(edges);
}

// Continuously trims the graph of vertices that cannot be part of a cycle for X-Cycles rule 1. The modified graph will
// either be empty or only contain vertices with a degree of two or more and be connected by at least one strong link
// and one weak link.
fn trim(graph: &mut UnGraphMap<&UnsolvedCell, Strength>) {
    loop {
        let to_remove = graph.nodes().find(|vertex| {
            let edges: Vec<_> = graph.edges(vertex).collect();
            edges.len() < 2
                || !edges
                    .iter()
                    .any(|&(_, _, &strength)| strength == Strength::Strong)
        });
        match to_remove {
            Some(to_remove) => graph.remove_node(to_remove),
            None => break,
        };
    }
}

fn get_weak_edges_in_alternating_cycle<'a, 'b>(
    graph: &'a UnGraphMap<&'b UnsolvedCell, Strength>,
) -> HashSet<(&'b UnsolvedCell, &'b UnsolvedCell, &'a Strength)> {
    let mut weak_edges_in_alternating_cycle = HashSet::new();
    for edge in graph
        .all_edges()
        .filter(|&(_, _, &strength)| strength == Strength::Weak)
    {
        if !weak_edges_in_alternating_cycle.contains(&edge) {
            weak_edges_in_alternating_cycle.extend(get_alternating_cycle_weak_edges(graph, edge));
        }
    }
    weak_edges_in_alternating_cycle
}

fn get_alternating_cycle_weak_edges<'a, 'b>(
    graph: &'a UnGraphMap<&'b UnsolvedCell, Strength>,
    start_edge: (&'b UnsolvedCell, &'b UnsolvedCell, &'a Strength),
) -> Vec<(&'b UnsolvedCell, &'b UnsolvedCell, &'a Strength)> {
    let (start, end, &strength) = start_edge;
    assert!(strength == Strength::Weak, "start_edge must be weak.");

    fn get_alternating_cycle_weak_edges<'a, 'b>(
        graph: &'a UnGraphMap<&'b UnsolvedCell, Strength>,
        end: &UnsolvedCell,
        current_vertex: &'b UnsolvedCell,
        next_type: Strength,
        visited: HashSet<&UnsolvedCell>,
        weak_edges: Vec<(&'b UnsolvedCell, &'b UnsolvedCell, &'a Strength)>,
    ) -> Vec<(&'b UnsolvedCell, &'b UnsolvedCell, &'a Strength)> {
        let next_edges_and_vertices = graph
            .edges(current_vertex)
            .filter(|(_, _, strength)| strength.is_compatible_with(next_type))
            .map(|edge| (edge, graphs::get_opposite_vertex(edge, current_vertex)));
        if next_type == Strength::Strong
            && next_edges_and_vertices
                .clone()
                .any(|(_, next_vertex)| next_vertex == end)
        {
            weak_edges
        } else {
            next_edges_and_vertices
                .filter(|&(_, next_vertex)| next_vertex != end && !visited.contains(next_vertex))
                .map(|(next_edge, next_vertex)| {
                    let mut next_visited = visited.clone();
                    next_visited.insert(next_vertex);
                    let mut next_weak_edges = weak_edges.clone();
                    if let (_, _, Strength::Weak) = next_edge {
                        next_weak_edges.push(next_edge);
                    }
                    get_alternating_cycle_weak_edges(
                        graph,
                        end,
                        next_vertex,
                        next_type.opposite(),
                        next_visited,
                        next_weak_edges,
                    )
                })
                .find(|next_result| !next_result.is_empty())
                .unwrap_or_default()
        }
    }

    let mut visited = HashSet::new();
    visited.insert(start);
    let weak_edges = get_alternating_cycle_weak_edges(
        graph,
        end,
        start,
        Strength::Strong,
        visited,
        vec![start_edge],
    );
    assert!(
        !weak_edges
            .iter()
            .any(|&(_, _, &strength)| strength == Strength::Strong),
        "There are strong edges in the return value."
    );
    weak_edges
}

fn alternating_cycle_exists(
    graph: &UnGraphMap<&UnsolvedCell, Strength>,
    vertex: &UnsolvedCell,
    adjacent_edges_type: Strength,
) -> bool {
    graph
        .edges(vertex)
        .filter(|&(_, _, &strength)| strength == adjacent_edges_type)
        .zip_every_pair()
        .any(|(edge_a, edge_b)| {
            let start = graphs::get_opposite_vertex(edge_a, vertex);
            let end = graphs::get_opposite_vertex(edge_b, vertex);

            fn alternating_cycle_exists(
                graph: &UnGraphMap<&UnsolvedCell, Strength>,
                adjacent_edges_type: Strength,
                end: &UnsolvedCell,
                current_vertex: &UnsolvedCell,
                next_type: Strength,
                visited: HashSet<&UnsolvedCell>,
            ) -> bool {
                let mut next_vertices: HashSet<_> = graph
                    .edges(current_vertex)
                    .filter(|(_, _, strength)| strength.is_compatible_with(next_type))
                    .map(|edge| graphs::get_opposite_vertex(edge, current_vertex))
                    .collect();
                if adjacent_edges_type.opposite() == next_type && next_vertices.contains(end) {
                    true
                } else {
                    next_vertices.retain(|&vertex| !visited.contains(vertex) && vertex != end);
                    next_vertices.iter().any(|next_vertex| {
                        let mut next_visited = visited.clone();
                        next_visited.insert(next_vertex);
                        alternating_cycle_exists(
                            graph,
                            adjacent_edges_type,
                            end,
                            next_vertex,
                            next_type.opposite(),
                            next_visited,
                        )
                    })
                }
            }

            let mut visited = HashSet::new();
            visited.insert(vertex);
            visited.insert(start);
            alternating_cycle_exists(
                graph,
                adjacent_edges_type,
                end,
                start,
                adjacent_edges_type.opposite(),
                visited,
            )
        })
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::{logic::assertions, remove_candidates};
    use indoc::indoc;

    #[test]
    fn test_to_dot() {
        let cells = [
            UnsolvedCell::with_all_candidates(0, 0),
            UnsolvedCell::with_all_candidates(0, 3),
            UnsolvedCell::with_all_candidates(2, 5),
        ];
        let cells: Vec<_> = cells.iter().unsolved_cells().collect();
        let a = cells[0];
        let b = cells[1];
        let c = cells[2];
        let graph = GraphMap::from_edges([(a, b, Strength::Strong), (b, c, Strength::Weak)]);
        let actual = to_dot(&graph);
        let expected = indoc! {r#"
            graph {
                0 [ label = "[0,0]"]
                1 [ label = "[0,3]"]
                2 [ label = "[2,5]"]
                0 -- 1 [ ]
                1 -- 2 [ style = dashed]
            }
        "#};
        assert_eq!(expected, actual);
    }

    #[test]
    fn rule_1() {
        let board = "\
            {59}241{35}{58}67{389}\
            {59}6{38}{238}7{258}41{389}\
            7{18}{138}964{58}2{358}\
            246591387\
            135487296\
            879623154\
            4{18}{128}{38}{35}976{258}\
            35{28}71694{28}\
            697{28}4{258}{58}31\
        ";
        let expected = [
            remove_candidates!(2, 2, 8),
            remove_candidates!(2, 8, 8),
            remove_candidates!(6, 2, 8),
            remove_candidates!(6, 8, 8),
        ];
        assertions::assert_logical_solution(&expected, board, x_cycles_rule_1);
    }

    #[test]
    fn rule_2() {
        let board = "\
            8{19}4537{169}{126}{12}\
            {79}23614{79}85\
            6{17}5982{17}34\
            {349}{346}{269}1{469}587{29}\
            5{49}{12}7{49}83{12}6\
            {179}8{1679}2{69}345{19}\
            2{467}{167}859{16}{146}3\
            {49}5{69}3712{469}8\
            {139}{39}84265{19}7\
        ";
        let expected = [SetValue::from_indices(8, 0, 1)];
        assertions::assert_logical_solution(&expected, board, x_cycles_rule_2);
    }

    #[test]
    fn rule_3_test_1() {
        let board = "\
            {158}762{35}{89}4{589}{1389}\
            {58}941{35}7{2358}6{238}\
            2{13}{1358}46{89}{1589}{589}7\
            {589}6{258}371{2589}{24589}{2489}\
            74{38}592{38}16\
            {159}{123}{1235}684{2359}7{239}\
            3{12}97{124}6{128}{248}5\
            68{12}9{124}573{124}\
            4578{12}36{29}{129}\
        ";
        let expected = [remove_candidates!(2, 2, 1)];
        assertions::assert_logical_solution(&expected, board, x_cycles_rule_3);
    }

    #[test]
    fn rule_3_test_2() {
        let board = "\
            {2478}{23}{247}{357}1{357}96{28}\
            {127}{1239}{1279}68{37}45{12}\
            {18}569423{18}7\
            {1247}{126}{12457}{157}{36}8{17}{137}9\
            38{17}{17}94625\
            9{16}{157}2{36}{157}{178}{1378}4\
            673{18}2954{18}\
            5{129}8476{12}{19}3\
            {12}4{129}{138}5{13}{1278}{1789}6\
        ";
        let expected = [remove_candidates!(5, 7, 1), remove_candidates!(8, 2, 1)];
        assertions::assert_logical_solution(&expected, board, x_cycles_rule_3);
    }

    #[test]
    fn rule_3_test_3() {
        let board = "\
            {12456}{145}{1256}978{24}{2346}{236}\
            {27}83{46}{46}159{27}\
            {467}9{67}253{478}1{678}\
            {29}74586{129}{23}{1239}\
            86{29}134{279}5{279}\
            {15}3{15}792684\
            32{156}8{146}9{14}7{156}\
            {145679}{145}8{46}{146}{57}3{246}{12569}\
            {145679}{145}{15679}32{57}{1489}{46}{15689}\
        ";
        let expected = [remove_candidates!(4, 8, 2, 9)];
        assertions::assert_logical_solution(&expected, board, x_cycles_rule_3);
    }

    #[test]
    fn rule_3_test_4() {
        let board = "\
            {23678}{23489}{24689}{168}5{69}{12378}{13467}{23467}\
            {67}{48}12{48}39{67}5\
            {2368}5{24689}{168}{489}7{1238}{1346}{2346}\
            973421658\
            165738429\
            4{28}{28}965{37}{37}1\
            {236}{12349}{2469}57{269}{123}8{2346}\
            {268}{1289}73{89}45{169}{26}\
            5{23489}{24689}{68}1{269}{237}{34679}{23467}\
        ";
        let expected = [remove_candidates!(8, 1, 8), remove_candidates!(8, 2, 8)];
        assertions::assert_logical_solution(&expected, board, x_cycles_rule_3);
    }
}
