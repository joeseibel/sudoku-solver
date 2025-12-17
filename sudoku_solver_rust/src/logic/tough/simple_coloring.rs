use crate::{
    board::Board,
    board_modification::{BoardModification, IteratorRemoveCandidatesExt},
    cell::{Cell, IteratorCellExt, UnsolvedCell},
    collections::IteratorZipExt,
    sudoku_number::SudokuNumber,
};
use petgraph::{
    algo::scc::tarjan_scc,
    dot::{Config, Dot},
    prelude::{GraphMap, UnGraphMap},
    visit::{self, DfsEvent},
};
use std::collections::HashMap;
use strum::IntoEnumIterator;

// http://www.sudokuwiki.org/Singles_Chains
//
// A single's chain is a graph for a particular candidate that connects two cells when those are the only two cells in a
// unit with that candidate. Each chain is colored with alternating colors such that for a given vertex with a given
// color, all adjacent vertices have the opposite color. The two colors represent the two possible solutions for each
// cell in the chain. Either the first color is the solution for the chain or the second color is.
//
// Rule 2: Twice in a Unit
//
// If there are two or more vertices with the same color that are in the same unit, then that color cannot be the
// solution. All candidates with that color in that chain can be removed.
pub fn simple_coloring_rule_2(board: &Board<Cell>) -> Vec<BoardModification> {
    SudokuNumber::iter()
        .flat_map(|candidate| {
            create_connected_components(board, candidate)
                .flat_map(move |graph| {
                    let colors = color_to_map(&graph);
                    graph
                        .nodes()
                        .zip_every_pair()
                        .find(|(a, b)| colors[a] == colors[b] && a.is_in_same_unit(b))
                        .map(|(a, _)| colors[a])
                        .map(|color_to_remove| {
                            graph
                                .nodes()
                                .filter(|cell| colors[cell] == color_to_remove)
                                .map(|cell| (cell, candidate))
                                .collect::<Vec<_>>()
                        })
                })
                .flatten()
        })
        .merge_to_remove_candidates()
}

// Rule 4: Two colors 'elsewhere'
//
// If an unsolved cell with a given candidate is outside the chain, and it is in the same units as two differently
// colored vertices, then one of those two vertices must be the solution for the candidate. The candidate can be removed
// from the cell outside the chain.
pub fn simple_coloring_rule_4(board: &Board<Cell>) -> Vec<BoardModification> {
    SudokuNumber::iter()
        .flat_map(|candidate| {
            create_connected_components(board, candidate).flat_map(move |graph| {
                let (color_one, color_two) = color_to_lists(&graph);
                board
                    .cells()
                    .unsolved_cells()
                    .filter(move |cell| {
                        cell.candidates().contains(&candidate)
                            && !graph.contains_node(cell)
                            && color_one.iter().any(|color| cell.is_in_same_unit(color))
                            && color_two.iter().any(|color| cell.is_in_same_unit(color))
                    })
                    .map(move |cell| (cell, candidate))
            })
        })
        .merge_to_remove_candidates()
}

#[allow(dead_code)]
fn to_dot(graph: &UnGraphMap<&UnsolvedCell, ()>) -> String {
    let dot = Dot::with_attr_getters(
        graph,
        &[Config::EdgeNoLabel, Config::NodeNoLabel],
        &|_, _| String::new(),
        &|_, (cell, _)| format!(r#"label = "{}""#, cell.vertex_label()),
    );
    format!("{dot:?}")
}

fn create_connected_components(
    board: &Board<Cell>,
    candidate: SudokuNumber,
) -> impl Iterator<Item = UnGraphMap<&UnsolvedCell, ()>> {
    let edges = board
        .units()
        .map(|unit| {
            unit.unsolved_cells()
                .filter(|cell| cell.candidates().contains(&candidate))
                .collect::<Vec<_>>()
        })
        .filter(|with_candidate| with_candidate.len() == 2)
        .map(|with_candidate| (with_candidate[0], with_candidate[1]));
    let graph = GraphMap::from_edges(edges);
    connected_components(&graph).collect::<Vec<_>>().into_iter()
}

#[derive(Clone, Copy, PartialEq)]
enum VertexColor {
    ColorOne,
    ColorTwo,
}

impl VertexColor {
    fn opposite(&self) -> Self {
        match self {
            Self::ColorOne => Self::ColorTwo,
            Self::ColorTwo => Self::ColorOne,
        }
    }
}

fn color_to_map<'a>(
    graph: &UnGraphMap<&'a UnsolvedCell, ()>,
) -> HashMap<&'a UnsolvedCell, VertexColor> {
    let mut colors = HashMap::new();
    if let start_vertex_option @ Some(start_vertex) = graph.nodes().next() {
        colors.insert(start_vertex, VertexColor::ColorOne);
        visit::depth_first_search(graph, start_vertex_option, |event| {
            if let DfsEvent::TreeEdge(a, b) = event {
                colors.insert(b, colors[a].opposite());
            }
        })
    }
    colors
}

fn color_to_lists<'a>(
    graph: &UnGraphMap<&'a UnsolvedCell, ()>,
) -> (Vec<&'a UnsolvedCell>, Vec<&'a UnsolvedCell>) {
    let mut color_one = Vec::new();
    let mut color_two = Vec::new();
    if let start_vertex_option @ Some(start_vertex) = graph.nodes().next() {
        color_one.push(start_vertex);
        visit::depth_first_search(graph, start_vertex_option, |event| {
            if let DfsEvent::TreeEdge(a, b) = event {
                if color_one.contains(&a) {
                    color_two.push(b);
                } else {
                    color_one.push(b);
                }
            }
        });
    }
    (color_one, color_two)
}

fn connected_components<'a>(
    graph: &UnGraphMap<&'a UnsolvedCell, ()>,
) -> impl Iterator<Item = UnGraphMap<&'a UnsolvedCell, ()>> {
    let components = tarjan_scc::tarjan_scc(graph)
        .into_iter()
        .map(|graph_vertices| {
            let edges = graph_vertices
                .iter()
                .zip_every_pair()
                .filter(|(a, b)| graph.contains_edge(a, b))
                .map(|(&a, &b)| (a, b));
            GraphMap::from_edges(edges)
        });
    let mut vertex_count = 0;
    let mut edge_count = 0;
    for subgraph in components.clone() {
        vertex_count += subgraph.node_count();
        edge_count += subgraph.edge_count();
    }
    assert_eq!(graph.node_count(), vertex_count);
    assert_eq!(graph.edge_count(), edge_count);
    components
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::{logic::assertions, remove_candidates};
    use indoc::indoc;
    use std::iter;

    #[test]
    fn test_to_dot() {
        let cells = [
            UnsolvedCell::with_all_candidates(0, 0),
            UnsolvedCell::with_all_candidates(0, 3),
        ];
        let cells: Vec<_> = cells.iter().unsolved_cells().collect();
        let a = cells[0];
        let b = cells[1];
        let graph = GraphMap::from_edges(iter::once((a, b)));
        let actual = to_dot(&graph);
        let expected = indoc! {r#"
            graph {
                0 [ label = "[0,0]"]
                1 [ label = "[0,3]"]
                0 -- 1 [ ]
            }
        "#};
        assert_eq!(expected, actual);
    }

    #[test]
    fn rule_2_test_1() {
        let board = "\
            {145}{15}7{25}836{149}{1249}\
            {145}397{25}68{14}{124}\
            826419753\
            64{25}19{25}387\
            {159}8{12}367{245}{149}{1459}\
            {19}73{25}48{25}6{19}\
            39{15}87{14}{45}26\
            7649{25}{25}138\
            2{15}863{14}97{45}\
        ";
        let expected = [
            remove_candidates!(0, 1, 5),
            remove_candidates!(0, 3, 5),
            remove_candidates!(1, 0, 5),
            remove_candidates!(3, 5, 5),
            remove_candidates!(4, 0, 5),
            remove_candidates!(5, 6, 5),
            remove_candidates!(6, 2, 5),
            remove_candidates!(7, 4, 5),
            remove_candidates!(8, 8, 5),
        ];
        assertions::assert_logical_solution(&expected, board, simple_coloring_rule_2);
    }

    #[test]
    fn rule_2_test_2() {
        let board = "\
            2{79}{38}{38}41{79}56\
            4{379}56{78}2{789}1{37}\
            {78}16{37}95{278}{23}4\
            35{78}12964{78}\
            142{78}6{37}59{38}\
            {78}695{38}4{27}{23}1\
            584216379\
            92{37}4{37}8165\
            6{37}195{37}482\
        ";
        let expected = [
            remove_candidates!(1, 8, 7),
            remove_candidates!(2, 0, 7),
            remove_candidates!(2, 3, 7),
            remove_candidates!(3, 2, 7),
            remove_candidates!(4, 5, 7),
            remove_candidates!(5, 6, 7),
            remove_candidates!(7, 4, 7),
            remove_candidates!(8, 1, 7),
        ];
        assertions::assert_logical_solution(&expected, board, simple_coloring_rule_2);
    }

    #[test]
    fn rule_2_test_3() {
        let board = "\
            4{279}{259}8{279}6{25}13\
            {257}86{27}134{25}9\
            {23}{239}1{29}45867\
            {357}1{35}468{37}92\
            {27}{279}83{279}1645\
            64{239}{279}5{27}{37}81\
            1546{237}{27}9{237}8\
            9{23}75841{23}6\
            86{23}1{237}9{25}{2357}4\
        ";
        let expected = [
            remove_candidates!(0, 2, 9),
            remove_candidates!(0, 4, 9),
            remove_candidates!(2, 1, 9),
            remove_candidates!(4, 1, 9),
            remove_candidates!(5, 3, 9),
        ];
        assertions::assert_logical_solution(&expected, board, simple_coloring_rule_2);
    }

    #[test]
    fn rule_2_test_4() {
        let board = "\
            289{16}{46}{14}375\
            364{57}9{57}812\
            517283964\
            893{457}2{457}6{45}1\
            145836729\
            726{19}{45}{19}{45}83\
            451378296\
            {69}72{4569}1{459}{45}38\
            {69}38{4569}{56}21{45}7\
        ";
        let expected = [
            remove_candidates!(5, 6, 5),
            remove_candidates!(8, 4, 5),
            remove_candidates!(8, 7, 5),
        ];
        assertions::assert_logical_solution(&expected, board, simple_coloring_rule_2);
    }

    #[test]
    fn rule_4_test_1() {
        let board = "\
            {145}{15}7{25}836{149}{1249}\
            {145}397{25}68{14}{124}\
            826419753\
            64{25}19{25}387\
            {159}8{125}367{245}{149}{1459}\
            {19}73{25}48{25}6{19}\
            39{15}87{14}{45}26\
            7649{25}{25}138\
            2{15}863{14}97{45}\
        ";
        let expected = [remove_candidates!(4, 0, 5), remove_candidates!(4, 2, 5)];
        assertions::assert_logical_solution(&expected, board, simple_coloring_rule_4);
    }

    #[test]
    fn rule_4_test_2() {
        let board = "\
            2{3579}{3578}{378}41{789}{35}6\
            4{3579}{3578}6{3578}2{789}1{378}\
            {78}16{378}9{357}{278}{235}4\
            3{57}{578}12964{78}\
            142{378}6{37}59{378}\
            {78}695{378}4{278}{23}1\
            584216379\
            92{37}4{37}8165\
            6{37}19{357}{357}482\
        ";
        let expected = [remove_candidates!(1, 4, 3), remove_candidates!(1, 8, 8)];
        assertions::assert_logical_solution(&expected, board, simple_coloring_rule_4);
    }

    #[test]
    fn rule_4_test_3() {
        let board = "\
            12845{37}{37}96\
            {37}46{37}91285\
            9{37}582641{37}\
            {678}{67}35{678}2149\
            {678}91{367}4{37}{68}52\
            4521{68}9{68}{37}{37}\
            {36}{36}4{27}159{27}8\
            287934561\
            519{267}{67}8{37}{237}4\
        ";
        let expected = [
            remove_candidates!(3, 4, 7),
            remove_candidates!(4, 0, 7),
            remove_candidates!(8, 3, 7),
        ];
        assertions::assert_logical_solution(&expected, board, simple_coloring_rule_4);
    }

    #[test]
    fn rule_4_test_4() {
        let board = "\
            4{378}{2378}956{23}{238}1\
            6{35}9{24}18{2345}{234}7\
            1{58}{28}37{24}{2456}{2468}9\
            316{24}8975{24}\
            824537196\
            7956{24}18{24}3\
            2{34}{13}7659{134}8\
            9{367}{137}8{24}{24}{36}{1367}5\
            5{4678}{78}193{246}{2467}{24}\
        ";
        let expected = [remove_candidates!(1, 7, 2, 4)];
        assertions::assert_logical_solution(&expected, board, simple_coloring_rule_4);
    }

    #[test]
    fn rule_4_test_5() {
        let board = "\
            89{67}2{67}4351\
            {457}12{56}{5679}3{469}{467}8\
            3{46}{57}1{579}8{29}{27}{46}\
            {245}{24}9817{2456}{246}3\
            631{45}{45}2789\
            {2457}8{457}936{245}1{45}\
            9537{46}18{46}2\
            {24}{246}{46}385197\
            178{46}29{456}3{456}\
        ";
        let expected = [remove_candidates!(1, 7, 6)];
        assertions::assert_logical_solution(&expected, board, simple_coloring_rule_4);
    }

    #[test]
    fn rule_4_test_6() {
        let board = "\
            {38}62945{78}{1378}{178}\
            154378692\
            7{38}91624{358}{58}\
            62{57}831{57}49\
            {89}{789}34562{178}{178}\
            41{58}297{58}63\
            5{78}16239{78}4\
            24{68}7193{58}{568}\
            {39}{39}{67}58412{67}\
        ";
        let expected = [
            remove_candidates!(0, 7, 7, 8),
            remove_candidates!(2, 7, 8),
            remove_candidates!(4, 8, 7),
        ];
        assertions::assert_logical_solution(&expected, board, simple_coloring_rule_4);
    }
}
