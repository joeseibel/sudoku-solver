use crate::{
    board::Board,
    board_modification::{BoardModification, IteratorRemoveCandidatesExt},
    cell::{Cell, IteratorCellExt, LocatedCandidate, LocatedCandidateExt},
    collections::IteratorZipExt,
    graphs::{self, Strength},
};
use itertools::Itertools;
use petgraph::prelude::{GraphMap, UnGraphMap};
use std::collections::HashSet;

// https://www.sudokuwiki.org/XY_Chains
//
// XY-Chains is based on a graph type which shares many similarities to X-Cycles. Unlike X-Cycles, an XY-Chains graph
// includes multiple candidates. This results in a single XY-Chains graph per board whereas there can be up to nine
// X-Cycles graphs per board, one for each candidate. Each vertex in an XY-Chains graph is a particular candidate in a
// cell and the edges are either strong or weak links. A strong link connects two candidates of a single cell when they
// are the only candidates of that cell. A weak link connects two vertices which have the same candidate, are in
// different cells, but are in the same unit. An XY-Chain is a chain between two vertices of the graph that have the
// same candidate, the edges of the chain alternate between strong and weak links, and the last links on either end of
// the chain are strong. If one vertex of a link is the solution, then the other vertex must not be the solution. If one
// vertex of a strong link is not the solution, then the other vertex must be the solution. When there is a proper chain
// in the graph it means that one of the two end points must be the solution. The candidate can be removed from any cell
// of the board which is not an end point of the chain and that cell can see both end points.
//
// Note that this implementation of XY-Chains can handle cases in which the chain is not strictly alternating between
// strong and weak links. It is tolerant of cases in which a strong link takes the place of a weak link.
pub fn xy_chains(board: &Board<Cell>) -> Vec<BoardModification> {
    let mut graph = create_strong_links(board);
    add_weak_links(&mut graph);
    graph
        .nodes()
        .into_group_map_by(|&(_, candidate)| candidate)
        .into_iter()
        .flat_map(|(candidate, vertices)| {
            vertices
                .into_iter()
                .zip_every_pair()
                .flat_map({
                    let graph = &graph;
                    move |(vertex_a @ (cell_a, _), vertex_b @ (cell_b, _))| {
                        let mut visible_cells = board
                            .cells()
                            .unsolved_cells()
                            .filter(move |&cell| {
                                cell.candidates().contains(&candidate)
                                    && cell != cell_a
                                    && cell != cell_b
                                    && cell.is_in_same_unit(cell_a)
                                    && cell.is_in_same_unit(cell_b)
                            })
                            .peekable();
                        if visible_cells.peek().is_some()
                            && alternating_path_exists(graph, vertex_a, vertex_b)
                        {
                            Some(visible_cells)
                        } else {
                            None
                        }
                    }
                })
                .flatten()
                .map(move |cell| (cell, candidate))
        })
        .merge_to_remove_candidates()
}

#[allow(dead_code)]
fn to_dot(graph: &UnGraphMap<LocatedCandidate, Strength>) -> String {
    graphs::to_dot(graph, graphs::edge_attributes, |(vertex, _)| {
        vertex.vertex_label()
    })
}

fn create_strong_links(board: &Board<Cell>) -> UnGraphMap<LocatedCandidate<'_>, Strength> {
    let edges = board
        .cells()
        .unsolved_cells()
        .filter(|cell| cell.candidates().len() == 2)
        .map(|cell| {
            let source = (cell, *cell.candidates().first().unwrap());
            let target = (cell, *cell.candidates().last().unwrap());
            (source, target, Strength::Strong)
        });
    GraphMap::from_edges(edges)
}

fn add_weak_links(graph: &mut UnGraphMap<LocatedCandidate, Strength>) {
    let edges: Vec<_> = graph
        .nodes()
        .zip_every_pair()
        .filter(|((cell_a, candidate_a), (cell_b, candidate_b))| {
            candidate_a == candidate_b && cell_a.is_in_same_unit(cell_b)
        })
        .map(|(vertex_a, vertex_b)| (vertex_a, vertex_b, Strength::Weak))
        .collect();
    graph.extend(edges);
}

fn alternating_path_exists(
    graph: &UnGraphMap<LocatedCandidate, Strength>,
    start: LocatedCandidate,
    end: LocatedCandidate,
) -> bool {
    fn alternating_path_exists(
        graph: &UnGraphMap<LocatedCandidate, Strength>,
        end: LocatedCandidate,
        current_vertex: LocatedCandidate,
        next_type: Strength,
        visited: HashSet<LocatedCandidate>,
    ) -> bool {
        let mut next_vertices: HashSet<_> = graph
            .edges(current_vertex)
            .filter(|(_, _, strength)| strength.is_compatible_with(next_type))
            .map(|edge| graphs::get_opposite_vertex(edge, current_vertex))
            .collect();
        next_type == Strength::Strong && next_vertices.contains(&end) || {
            for visited_vertex in &visited {
                next_vertices.remove(visited_vertex);
            }
            next_vertices.remove(&end);
            next_vertices.iter().any(|&next_vertex| {
                let mut next_visited = visited.clone();
                next_visited.insert(next_vertex);
                alternating_path_exists(graph, end, next_vertex, next_type.opposite(), next_visited)
            })
        }
    }

    let mut visited = HashSet::new();
    visited.insert(start);
    alternating_path_exists(graph, end, start, Strength::Strong, visited)
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::{
        cell::UnsolvedCell, logic::assertions, remove_candidates, sudoku_number::SudokuNumber,
    };
    use indoc::indoc;

    #[test]
    fn test_to_dot() {
        let cells = [
            UnsolvedCell::with_all_candidates(0, 0),
            UnsolvedCell::with_all_candidates(0, 0),
            UnsolvedCell::with_all_candidates(0, 4),
        ];
        let cells: Vec<_> = cells.iter().unsolved_cells().collect();
        let a = (cells[0], SudokuNumber::Two);
        let b = (cells[1], SudokuNumber::Six);
        let c = (cells[2], SudokuNumber::Two);
        let graph = GraphMap::from_edges([(a, b, Strength::Strong), (a, c, Strength::Weak)]);
        let actual = to_dot(&graph);
        let expected = indoc! {r#"
            graph {
                0 [ label = "[0,0] : 2"]
                1 [ label = "[0,0] : 6"]
                2 [ label = "[0,4] : 2"]
                0 -- 1 [ ]
                0 -- 2 [ style = dashed]
            }
        "#};
        assert_eq!(expected, actual);
    }

    #[test]
    fn test_1() {
        let board = "\
            {26}8{245}1{29}3{59}7{456}\
            {37}9{24}5{27}6{18}{14}{348}\
            {37}{56}14{79}8{359}2{356}\
            578241639\
            143659782\
            926837451\
            {68}379{16}52{14}{48}\
            {268}{56}{25}3{16}4{18}97\
            419782{35}6{35}\
        ";
        let expected = [
            remove_candidates!(0, 2, 2, 5),
            remove_candidates!(1, 8, 4),
            remove_candidates!(2, 6, 5),
            remove_candidates!(2, 8, 5),
            remove_candidates!(7, 0, 6),
        ];
        assertions::assert_logical_solution(&expected, board, xy_chains);
    }

    #[test]
    fn test_2() {
        let board = "\
            {48}92{145}{18}{158}376\
            {478}1{68}{24679}3{2689}5{28}{248}\
            3{567}{568}{2467}{2678}{268}19{248}\
            93{46}85{26}7{24}1\
            {78}{567}{1568}3{126}4{689}{258}{289}\
            2{56}{14568}{16}97{68}{458}3\
            689{257}{27}341{57}\
            523{179}4{189}{89}6{789}\
            147{569}{68}{5689}23{589}\
        ";
        let expected = [
            remove_candidates!(1, 0, 8),
            remove_candidates!(1, 5, 8),
            remove_candidates!(1, 8, 8),
            remove_candidates!(2, 2, 6),
            remove_candidates!(4, 2, 6),
            remove_candidates!(4, 7, 2),
            remove_candidates!(5, 2, 6),
        ];
        assertions::assert_logical_solution(&expected, board, xy_chains);
    }

    #[test]
    fn test_3() {
        let board = "\
            931672458\
            672854193\
            {58}4{58}913762\
            {28}{169}{48}5{349}7{369}{128}{49}\
            3{69}{45}{12}{49}8{569}{12}7\
            {258}{19}7{12}{349}6{359}{128}{459}\
            486321{59}7{59}\
            153789246\
            729465831\
        ";
        let expected = [remove_candidates!(3, 4, 9), remove_candidates!(4, 6, 9)];
        assertions::assert_logical_solution(&expected, board, xy_chains);
    }

    #[test]
    fn test_4() {
        let board = "\
            {45}938{24}716{25}\
            286591437\
            {145}7{14}6{234}{34}89{25}\
            {479}{13}{47}2{37}5{69}8{169}\
            {89}{13}546{38}27{19}\
            {78}621{78}9543\
            32{17}9{148}{48}{67}5{46}\
            {17}583{14}6{79}2{49}\
            649752318\
        ";
        let expected = [
            remove_candidates!(2, 0, 4),
            remove_candidates!(2, 4, 3, 4),
            remove_candidates!(2, 5, 4),
            remove_candidates!(3, 0, 7, 9),
            remove_candidates!(3, 1, 3),
            remove_candidates!(3, 4, 7),
            remove_candidates!(3, 6, 9),
            remove_candidates!(3, 8, 1, 6),
            remove_candidates!(4, 0, 8),
            remove_candidates!(4, 1, 1),
            remove_candidates!(4, 5, 3),
            remove_candidates!(4, 8, 9),
            remove_candidates!(5, 0, 7),
            remove_candidates!(5, 4, 8),
            remove_candidates!(6, 2, 7),
            remove_candidates!(6, 4, 1, 4),
            remove_candidates!(6, 6, 6),
            remove_candidates!(6, 8, 4),
            remove_candidates!(7, 0, 1),
            remove_candidates!(7, 4, 4),
            remove_candidates!(7, 6, 7),
            remove_candidates!(7, 8, 9),
        ];
        assertions::assert_logical_solution(&expected, board, xy_chains);
    }

    #[test]
    fn test_5() {
        let board = "\
            9{246}3{458}{267}1{478}{245}{2578}\
            8{246}{46}{345}{2367}{56}{3479}{1245}{12579}\
            751{348}{23}9{348}6{28}\
            187{35}{36}{56}294\
            {35}{34}{45}792186\
            2{69}{69}148573\
            67{58}913{48}{245}{258}\
            {35}{39}2684{79}{15}{1579}\
            41{89}25763{89}\
        ";
        let expected = [
            remove_candidates!(0, 8, 8),
            remove_candidates!(1, 1, 6),
            remove_candidates!(1, 4, 3, 6),
            remove_candidates!(6, 8, 8),
        ];
        assertions::assert_logical_solution(&expected, board, xy_chains);
    }
}
