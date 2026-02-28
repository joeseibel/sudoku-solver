use crate::{
    board::Board,
    board_modification::{BoardModification, IteratorRemoveCandidatesExt, SetValue},
    cell::{Cell, IteratorCellExt, LocatedCandidate, Location, UnsolvedCell},
    collections::IteratorZipExt,
    graphs::{self, Strength},
    sudoku_number::SudokuNumber,
};
use petgraph::prelude::{GraphMap, UnGraphMap};
use std::collections::HashSet;
use strum::IntoEnumIterator;

// https://www.sudokuwiki.org/Alternating_Inference_Chains
//
// Alternating Inference Chains are based on a graph type in which each vertex is a specific candidate in a cell and the
// edges can either be strong or weak links. A strong link connects two vertices in a unit that share a candidate when
// they are in the only unsolved cells in that unit with the candidate. A strong link also connects two vertices in a
// single cell when they are the only two candidates in that cell. A weak link connects two vertices in a unit that
// share a candidate when they are not the only unsolved cells in that unit with the candidate. A weak link also
// connects two vertices in a single cell when there are more than two candidates in that cell. An Alternating Inference
// Chain is a cycle in the graph in which the edges alternate between strong and weak links. If one vertex of a link is
// the solution, then the other vertex must not be the solution. If one vertex of a strong link is not the solution,
// then the other vertex must be the solution. Alternating Inference Chains are very similar to X-Cycles and Grouped
// X-Cycles.
//
// Note that this implementation of Alternating Inference Chains can handle cases in which the chain is not strictly
// alternating between strong and weak links. It is tolerant of cases in which a strong link takes the place of a weak
// link.
//
// Rule 1:
//
// If an Alternating Inference Chain has an even number of vertices and therefore continuously alternates between strong
// and weak, then the graph is perfect and has no flaws. Each of the weak links can be treated as a strong link.
// If a weak link connects a common candidate across two different cells, then that candidate can be removed from any
// other cell which is in the same unit as the two vertices. If a weak link connects two candidates of the same cell,
// then all other candidates can be removed from that cell.
pub fn alternating_inference_chains_rule_1(board: &Board<Cell>) -> Vec<BoardModification> {
    let mut graph = build_graph(board);
    trim(&mut graph);
    graphs::get_weak_edges_in_alternating_cycle(&graph)
        .iter()
        .flat_map(
            |&((source_cell, source_candidate), (target_cell, target_candidate))| {
                if source_cell == target_cell {
                    let mut candidates = source_cell.candidates().clone();
                    candidates.remove(&source_candidate);
                    candidates.remove(&target_candidate);
                    candidates
                        .iter()
                        .map(|&candidate| (source_cell, candidate))
                        .collect::<Vec<_>>()
                } else {
                    fn remove_from_unit<'a, U: Iterator<Item = &'a Cell>>(
                        source_cell: &UnsolvedCell,
                        source_candidate: SudokuNumber,
                        target_cell: &UnsolvedCell,
                        source_unit_index: usize,
                        target_unit_index: usize,
                        get_unit: impl FnOnce(usize) -> U,
                    ) -> impl Iterator<Item = LocatedCandidate<'a>> {
                        let removals = if source_unit_index == target_unit_index {
                            let removals = get_unit(source_unit_index)
                                .unsolved_cells()
                                .filter(move |&cell| {
                                    cell.candidates().contains(&source_candidate)
                                        && cell != source_cell
                                        && cell != target_cell
                                })
                                .map(move |cell| (cell, source_candidate));
                            Some(removals)
                        } else {
                            None
                        };
                        removals.into_iter().flatten()
                    }

                    let row_removals = remove_from_unit(
                        source_cell,
                        source_candidate,
                        target_cell,
                        source_cell.row(),
                        target_cell.row(),
                        |index| board.get_row(index),
                    );
                    let column_removals = remove_from_unit(
                        source_cell,
                        source_candidate,
                        target_cell,
                        source_cell.column(),
                        target_cell.column(),
                        |index| board.get_column(index),
                    );
                    let block_removals = remove_from_unit(
                        source_cell,
                        source_candidate,
                        target_cell,
                        source_cell.block(),
                        target_cell.block(),
                        |index| board.get_block(index),
                    );

                    row_removals
                        .chain(column_removals)
                        .chain(block_removals)
                        .collect()
                }
            },
        )
        .merge_to_remove_candidates()
}

// Rule 2:
//
// If an Alternating Inference Chain has an odd number of vertices and the edges alternate between strong and weak,
// except for one vertex which is connected by two strong links, then the graph is a contradiction. Removing the
// candidate from the cell of interest implies that the candidate must be the solution for that cell, thus causing the
// cycle to contradict itself. However, considering the candidate to be the solution for that cell does not cause any
// contradiction in the cycle. Therefore, the candidate must be the solution for that cell.
//
// Note that this implementation of rule 2 does not allow for a candidate to be revisited in the chain. A candidate can
// appear multiple times in a chain, but only if all the occurrences are consecutive.
pub fn alternating_inference_chains_rule_2(board: &Board<Cell>) -> Vec<BoardModification> {
    let mut graph = build_graph(board);
    trim(&mut graph);
    graph
        .nodes()
        .filter(|&vertex| alternating_cycle_exists(&graph, vertex, Strength::Strong))
        .map(|(cell, candidate)| SetValue::from_cell(cell, candidate))
        .collect()
}

// Rule 3:
//
// If an Alternating Inference Chain has an odd number of vertices and the edges alternate between strong and weak,
// except for one vertex which is connected by two weak links, then the graph is a contradiction. Considering the
// candidate to be the solution for the cell of interest implies that the candidate must be removed from that cell, thus
// causing the cycle to contradict itself. However, removing the candidate from that cell does not cause any
// contradiction in the cycle. Therefore, the candidate can be removed from the cell.
//
// Note that this implementation of rule 3 does not allow for a candidate to be revisited in the chain. A candidate can
// appear multiple times in a chain, but only if all the occurrences are consecutive.
pub fn alternating_inference_chains_rule_3(board: &Board<Cell>) -> Vec<BoardModification> {
    let graph = build_graph(board);
    graph
        .nodes()
        .filter(|&vertex| alternating_cycle_exists(&graph, vertex, Strength::Weak))
        .merge_to_remove_candidates()
}

fn build_graph(board: &Board<Cell>) -> UnGraphMap<LocatedCandidate<'_>, Strength> {
    // Connect cells.
    let same_candidate_edges = board.units().flat_map(|unit| {
        let unit: Vec<_> = unit.collect();
        SudokuNumber::iter().flat_map(move |candidate| {
            let with_candidates: Vec<_> = unit
                .iter()
                .copied()
                .unsolved_cells()
                .filter(|cell| cell.candidates().contains(&candidate))
                .collect();
            let strength = if with_candidates.len() == 2 {
                Strength::Strong
            } else {
                Strength::Weak
            };
            with_candidates
                .into_iter()
                .zip_every_pair()
                .map(move |(a, b)| ((a, candidate), (b, candidate), strength))
        })
    });

    // Connect candidates in cells.
    let same_cell_edges = board.cells().unsolved_cells().flat_map(|cell| {
        let strength = if cell.candidates().len() == 2 {
            Strength::Strong
        } else {
            Strength::Weak
        };
        cell.candidates()
            .iter()
            .zip_every_pair()
            .map(move |(&a, &b)| ((cell, a), (cell, b), strength))
    });

    GraphMap::from_edges(same_candidate_edges.chain(same_cell_edges))
}

fn alternating_cycle_exists(
    graph: &UnGraphMap<LocatedCandidate, Strength>,
    vertex @ (_, vertex_candidate): LocatedCandidate,
    adjacent_edges_type: Strength,
) -> bool {
    graph
        .edges(vertex)
        .filter(|&(_, _, &strength)| strength == adjacent_edges_type)
        .zip_every_pair()
        .any(|(edge_a, edge_b)| {
            let start @ (_, start_candidate) = graphs::get_opposite_vertex(edge_a, vertex);
            let end @ (_, end_candidate) = graphs::get_opposite_vertex(edge_b, vertex);

            fn alternating_cycle_exists(
                graph: &UnGraphMap<LocatedCandidate, Strength>,
                adjacent_edges_type: Strength,
                end: LocatedCandidate,
                current_vertex @ (_, current_candidate): LocatedCandidate,
                next_type: Strength,
                visited: HashSet<LocatedCandidate>,
                visisted_candidates: HashSet<SudokuNumber>,
            ) -> bool {
                let mut next_vertices: Vec<_> = graph
                    .edges(current_vertex)
                    .filter(|(_, _, strength)| strength.is_compatible_with(next_type))
                    .map(|edge| graphs::get_opposite_vertex(edge, current_vertex))
                    .filter(|&(_, opposite_candidate)| {
                        opposite_candidate == current_candidate
                            || !visisted_candidates.contains(&opposite_candidate)
                    })
                    .collect();
                adjacent_edges_type.opposite() == next_type && next_vertices.contains(&end) || {
                    next_vertices.retain(|&vertex| !visited.contains(&vertex) && vertex != end);
                    next_vertices
                        .iter()
                        .any(|&next_vertex @ (_, next_candidate)| {
                            let mut next_visited = visited.clone();
                            next_visited.insert(next_vertex);
                            let mut next_visited_candidates = visisted_candidates.clone();
                            if current_candidate != next_candidate {
                                next_visited_candidates.insert(next_candidate);
                            }
                            alternating_cycle_exists(
                                graph,
                                adjacent_edges_type,
                                end,
                                next_vertex,
                                next_type.opposite(),
                                next_visited,
                                next_visited_candidates,
                            )
                        })
                }
            }

            let mut visited = HashSet::new();
            visited.insert(vertex);
            visited.insert(start);
            let mut visited_candidates = HashSet::new();
            visited_candidates.insert(vertex_candidate);
            visited_candidates.insert(start_candidate);
            visited_candidates.remove(&end_candidate);
            alternating_cycle_exists(
                graph,
                adjacent_edges_type,
                end,
                start,
                adjacent_edges_type.opposite(),
                visited,
                visited_candidates,
            )
        })
}

// Continuously trims the graph of vertices that cannot be part of a cycle for X-Cycles rule 1. The modified graph will
// either be empty or only contain vertices with a degree of two or more and be connected by at least one strong link
// and one weak link.
fn trim(graph: &mut UnGraphMap<LocatedCandidate, Strength>) {
    loop {
        let to_remove = graph.nodes().find(|&vertex| {
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

#[cfg(test)]
mod tests {
    use super::*;
    use crate::{board_modification::SetValue, logic::assertions, remove_candidates};

    #[test]
    fn rule_1_test_1() {
        let board = "\
            {179}384{27}{125}{259}{1569}{269}\
            2{47}{17}9{67}{156}{45}38\
            {149}6538{12}7{19}{249}\
            {578}{589}{279}{2678}3{269}14{269}\
            6{4789}3{278}1{249}{289}{79}5\
            {478}1{279}{2678}5{2469}{289}{679}3\
            {78}{78}4593621\
            32{169}{16}{46}8{459}{59}7\
            {15}{59}{169}{126}{246}738{49}\
        ";
        let expected = [
            remove_candidates!(0, 0, 1),
            remove_candidates!(1, 2, 7),
            remove_candidates!(1, 5, 1),
            remove_candidates!(2, 0, 1),
            remove_candidates!(3, 0, 7),
            remove_candidates!(4, 1, 7),
            remove_candidates!(5, 0, 7),
            remove_candidates!(8, 4, 6),
        ];
        assertions::assert_logical_solution(&expected, board, alternating_inference_chains_rule_1);
    }

    #[test]
    fn rule_1_test_2() {
        let board = "\
            5{489}{469}{1248}7{248}{124}{2469}3\
            {39}1{3479}6{345}{2345}{2457}8{4579}\
            {368}{478}2{13458}{13458}9{1457}{456}{14567}\
            {123}6{1345}{234578}{13458}{234578}9{345}{457}\
            7{459}{13459}{1345}{134569}{3456}8{3456}2\
            {239}{2459}8{23457}{34569}{3457}{3457}1{4567}\
            {128}{2578}{157}9{3458}{34578}6{2345}{145}\
            {2689}3{569}{458}{4568}1{245}7{459}\
            4{579}{15679}{357}2{3567}{135}{359}8\
        ";
        let expected = [
            remove_candidates!(0, 7, 4, 9),
            remove_candidates!(7, 0, 8, 9),
        ];
        assertions::assert_logical_solution(&expected, board, alternating_inference_chains_rule_1);
    }

    #[test]
    fn rule_1_test_3() {
        let board = "\
            {4589}{48}613{479}{57}2{789}\
            {89}31{79}5264{789}\
            72{459}{69}8{469}3{59}1\
            26{49}5718{39}{34}\
            {159}{57}84632{179}{79}\
            {14}{47}3298{147}65\
            3{458}{245}{789}{12}{579}{1457}{157}6\
            {456}973{12}{56}{145}8{24}\
            {568}1{25}{68}4{567}9{357}{23}\
        ";
        let expected = [remove_candidates!(0, 0, 4), remove_candidates!(2, 5, 9)];
        assertions::assert_logical_solution(&expected, board, alternating_inference_chains_rule_1);
    }

    #[test]
    fn rule_2_test_1() {
        let board = "\
            {179}384{27}{125}{259}{1569}{269}\
            2{47}{17}9{67}{156}{45}38\
            {149}6538{12}7{19}{249}\
            {578}{589}{279}{2678}3{269}14{269}\
            6{4789}3{278}1{249}{289}{79}5\
            {478}1{279}{2678}5{2469}{289}{679}3\
            {78}{78}4593621\
            32{169}{16}{46}8{459}{59}7\
            {15}{59}{169}{126}{246}738{49}\
        ";
        let expected = [
            SetValue::from_indices(0, 4, 7),
            SetValue::from_indices(1, 2, 1),
            SetValue::from_indices(1, 4, 6),
            SetValue::from_indices(1, 6, 4),
            SetValue::from_indices(2, 0, 4),
            SetValue::from_indices(4, 1, 4),
            SetValue::from_indices(5, 5, 4),
            SetValue::from_indices(7, 3, 1),
            SetValue::from_indices(7, 4, 4),
            SetValue::from_indices(8, 8, 4),
        ];
        assertions::assert_logical_solution(&expected, board, alternating_inference_chains_rule_2);
    }

    #[test]
    fn rule_2_test_2() {
        let board = "\
            7{158}{168}9{136}{168}42{1358}\
            {156}92{34}{346}{18}{56}{367}{1578}\
            {468}{148}32579{168}{18}\
            3{16}478{56}2{156}9\
            97{18}{45}{46}2{36}{135}{138}\
            2{68}51937{68}4\
            {58}{358}96241{37}{57}\
            {146}{34}{16}{35}7{15}892\
            {15}278{13}9{35}46\
        ";
        let expected = [
            SetValue::from_indices(0, 1, 5),
            SetValue::from_indices(1, 5, 8),
            SetValue::from_indices(6, 0, 8),
        ];
        assertions::assert_logical_solution(&expected, board, alternating_inference_chains_rule_2);
    }

    #[test]
    fn rule_2_test_3() {
        let board = "\
            869{47}51{247}{23}{34}\
            347286915\
            521{479}{79}3{47}68\
            953{68}{26}{28}147\
            784319{25}{25}6\
            612547389\
            {14}{37}8{1679}{679}5{46}{39}2\
            {124}95{168}3{28}{468}7{14}\
            {12}{37}6{1789}{279}4{58}{359}{13}\
        ";
        let expected = [
            SetValue::from_indices(0, 3, 7),
            SetValue::from_indices(0, 6, 2),
            SetValue::from_indices(0, 7, 3),
            SetValue::from_indices(0, 8, 4),
            SetValue::from_indices(2, 3, 4),
            SetValue::from_indices(2, 4, 9),
            SetValue::from_indices(2, 6, 7),
            SetValue::from_indices(4, 6, 5),
            SetValue::from_indices(4, 7, 2),
            SetValue::from_indices(6, 1, 3),
            SetValue::from_indices(6, 7, 9),
            SetValue::from_indices(7, 8, 1),
            SetValue::from_indices(8, 6, 8),
            SetValue::from_indices(8, 7, 5),
            SetValue::from_indices(8, 8, 3),
        ];
        assertions::assert_logical_solution(&expected, board, alternating_inference_chains_rule_2);
    }

    #[test]
    fn rule_2_test_4() {
        let board = "\
            {689}{145}3{145}2{46}7{15689}{5689}\
            {69}{145}27{45}83{1569}{569}\
            {68}{15}7{135}9{36}{12}{12568}4\
            3942{58}16{58}7\
            1256{48}7{49}3{89}\
            786{45}39{124}{125}{25}\
            439862571\
            261975843\
            578{34}1{34}{29}{269}{269}\
        ";
        let expected = [
            SetValue::from_indices(1, 4, 4),
            SetValue::from_indices(2, 0, 8),
            SetValue::from_indices(3, 4, 5),
            SetValue::from_indices(3, 7, 8),
            SetValue::from_indices(4, 4, 8),
            SetValue::from_indices(4, 6, 4),
            SetValue::from_indices(4, 8, 9),
            SetValue::from_indices(5, 3, 4),
            SetValue::from_indices(8, 5, 4),
            SetValue::from_indices(8, 6, 9),
        ];
        assertions::assert_logical_solution(&expected, board, alternating_inference_chains_rule_2);
    }

    #[test]
    fn rule_2_test_5() {
        let board = "\
            9{1267}3{267}54{1267}{127}8\
            5{1267}{1267}{2367}8{3679}{1267}{12379}4\
            48{267}{2367}1{3679}{2567}{23579}{279}\
            1{379}542{37}86{79}\
            84{279}56{17}3{1279}{1279}\
            6{237}{27}{378}9{1378}4{127}5\
            3{16}894{56}{1257}{1257}{127}\
            2{169}{169}{68}7{568}{15}43\
            754132986\
        ";
        let expected = [
            SetValue::from_indices(2, 7, 5),
            SetValue::from_indices(6, 1, 6),
            SetValue::from_indices(6, 5, 5),
            SetValue::from_indices(7, 6, 5),
        ];
        assertions::assert_logical_solution(&expected, board, alternating_inference_chains_rule_2);
    }

    #[test]
    fn rule_2_test_6() {
        let board = "\
            415{69}{69}2387\
            382147{69}5{69}\
            796853142\
            95{34}{2347}16{27}{237}8\
            17{38}{239}{289}54{2369}{369}\
            62{348}{3479}{789}{48}{79}15\
            {258}{346}7{2456}{268}9{268}{236}1\
            {258}{346}9{24567}{2678}1{2678}{2367}{346}\
            {28}{46}1{2467}3{48}5{2679}{469}\
        ";
        let expected = [SetValue::from_indices(8, 7, 9)];
        assertions::assert_logical_solution(&expected, board, alternating_inference_chains_rule_2);
    }

    #[test]
    fn rule_2_test_7() {
        let board = "\
            {23468}1{234}{568}{689}{25}{79}{48}{479}\
            7{28}94{18}{12}365\
            {468}{458}{45}7{689}32{148}{149}\
            56{237}1{23}489{27}\
            9{234}1{358}{2358}76{45}{24}\
            {24}{247}89{25}6{17}{145}3\
            {48}{478}62{147}953{18}\
            19{357}{35}{357}8426\
            {2348}{23458}{2345}{356}{1456}{15}{19}7{189}\
        ";
        let expected = [
            SetValue::from_indices(4, 3, 8),
            SetValue::from_indices(8, 3, 6),
        ];
        assertions::assert_logical_solution(&expected, board, alternating_inference_chains_rule_2);
    }

    #[test]
    fn rule_2_test_8() {
        let board = "\
            79{36}21{36}584\
            {356}284{357}{367}{139}{1369}{16}\
            14{356}{56}897{236}{26}\
            {2369}5{1367}8{379}{367}4{12369}{1267}\
            {369}{67}4{156}{3579}2{139}{1369}8\
            {2369}8{1367}{16}{379}4{1239}5{1267}\
            {56}{67}{567}921843\
            439768{12}{12}5\
            812345679\
        ";
        let expected = [
            SetValue::from_indices(1, 4, 5),
            SetValue::from_indices(1, 5, 7),
            SetValue::from_indices(2, 2, 5),
            SetValue::from_indices(4, 3, 5),
            SetValue::from_indices(6, 0, 5),
        ];
        assertions::assert_logical_solution(&expected, board, alternating_inference_chains_rule_2);
    }

    #[test]
    fn rule_2_test_9() {
        let board = "\
            {23468}1{234}{568}{689}{25}{79}{48}{479}\
            7{28}94{18}{12}365\
            {468}{458}{45}7{689}32{148}{149}\
            56{237}1{23}489{27}\
            9{34}1{358}{2358}76{45}{24}\
            {24}{247}89{25}6{17}{145}3\
            {48}{4578}62{147}9{15}3{18}\
            19{357}{35}{357}8426\
            {2348}{23458}{2345}{356}{1456}{15}{159}7{189}\
        ";
        let expected = [
            SetValue::from_indices(4, 3, 8),
            SetValue::from_indices(6, 6, 5),
        ];
        assertions::assert_logical_solution(&expected, board, alternating_inference_chains_rule_2);
    }

    #[test]
    fn rule_3_test_1() {
        let board = "\
            {4589}{48}613{479}{57}2{789}\
            {89}31{79}5264{789}\
            72{459}{69}8{469}3{59}1\
            26{49}5718{39}{34}\
            {159}{57}84632{179}{79}\
            {14}{47}3298{147}65\
            3{458}{245}{789}{12}{579}{1457}{157}6\
            {456}973{12}{56}{145}8{24}\
            {568}1{25}{678}4{567}9{357}{23}\
        ";
        let expected = [remove_candidates!(2, 5, 9), remove_candidates!(8, 3, 7)];
        assertions::assert_logical_solution(&expected, board, alternating_inference_chains_rule_3);
    }

    #[test]
    fn rule_3_test_2() {
        let board = "\
            {4589}{458}613{479}{57}2{789}\
            {89}31{79}5264{789}\
            72{459}{69}8{469}3{59}1\
            26{49}5718{39}{34}\
            {159}{57}84632{179}{79}\
            {14}{47}3298{147}65\
            3{458}{245}{789}{12}{579}{1457}{157}6\
            {456}973{12}{56}{145}8{24}\
            {568}1{25}{678}4{567}9{357}{23}\
        ";
        let expected = [remove_candidates!(0, 1, 5), remove_candidates!(8, 3, 7)];
        assertions::assert_logical_solution(&expected, board, alternating_inference_chains_rule_3);
    }

    #[test]
    fn rule_3_test_3() {
        let board = "\
            {23468}1{234}{568}{689}{25}{379}{348}{479}\
            7{238}94{18}{12}{13}65\
            {468}{458}{45}7{1689}32{148}{1489}\
            56{237}1{23}489{27}\
            9{234}1{358}{2358}76{45}{24}\
            {24}{247}89{25}6{157}{145}3\
            {348}{4578}62{1457}9{135}{1358}{18}\
            19{357}{35}{357}8426\
            {2348}{23458}{2345}{356}{1456}{15}{1359}7{189}\
        ";
        let expected = [
            remove_candidates!(0, 6, 3),
            remove_candidates!(4, 1, 2),
            remove_candidates!(6, 7, 1),
            remove_candidates!(8, 6, 3),
        ];
        assertions::assert_logical_solution(&expected, board, alternating_inference_chains_rule_3);
    }
}
