use crate::{
    board::Board,
    board_modification::{BoardModification, IteratorRemoveCandidatesExt, SetValue},
    cell::{Cell, IteratorCellExt, LocatedCandidate, UnsolvedCell},
    collections::IteratorZipExt,
    graphs,
    sudoku_number::SudokuNumber,
};
use petgraph::{
    dot::{Config, Dot},
    prelude::{GraphMap, UnGraphMap},
};
use strum::IntoEnumIterator;

// https://www.sudokuwiki.org/3D_Medusa
//
// A 3D Medusa is a graph type in which each vertex is a particular candidate in a cell and each edge is a strong link.
// A strong link is an edge such that if one vertex of the link is the solution, then the other vertex must not be the
// solution. A strong link also means that if one vertex of the link is not the solution, then the other vertex must be
// the solution. When a candidate is in only two cells of a unit, there is an edge between the candidate of those two
// cells. Additionally, when a cell contains only two candidates, there is an edge between the two candidates of that
// cell. Each medusa is colored with alternating colors such that for a given vertex with a given color, all adjacent
// vertices have the opposite color. The two colors represent the two possible solutions. Either the first color is the
// solution for the medusa or the second color is.
//
// Rule 1: Twice in a Cell
//
// If there are two vertices with the same color that are in the same cell, then that color cannot be the solution and
// the opposite color must be the solution. All vertices with the opposite color can be set as the solution.
pub fn medusa_rule_1(board: &Board<Cell>) -> Vec<BoardModification> {
    create_connected_components(board)
        .flat_map(|graph| {
            let colors = graphs::color_to_map(&graph);
            graph
                .nodes()
                .zip_every_pair()
                .find(|(a @ (cell_a, _), b @ (cell_b, _))| {
                    cell_a == cell_b && colors[a] == colors[b]
                })
                .map(|(a, _)| colors[&a])
                .map(|color| color.opposite())
                .iter()
                .flat_map(|&color_to_set| {
                    graph
                        .nodes()
                        .filter({
                            let colors = &colors;
                            move |vertex| colors[vertex] == color_to_set
                        })
                        .map(|(cell, candidate)| SetValue::from_cell(cell, candidate))
                })
                .collect::<Vec<_>>()
                .into_iter()
        })
        .collect()
}

// Rule 2: Twice in a Unit
//
// If there are two vertices with the same color and the same candidate that are in the same unit, then that color
// cannot be the solution and the opposite color must be the solution. All vertices with the opposite color can be set
// as the solution.
pub fn medusa_rule_2(board: &Board<Cell>) -> Vec<BoardModification> {
    create_connected_components(board)
        .flat_map(|graph| {
            let colors = graphs::color_to_map(&graph);
            graph
                .nodes()
                .zip_every_pair()
                .find(|(a @ (cell_a, candidate_a), b @ (cell_b, candidate_b))| {
                    candidate_a == candidate_b
                        && colors[a] == colors[b]
                        && cell_a.is_in_same_unit(cell_b)
                })
                .map(|(a, _)| colors[&a])
                .map(|color| color.opposite())
                .iter()
                .flat_map(|&color_to_set| {
                    graph
                        .nodes()
                        .filter({
                            let colors = &colors;
                            move |vertex| colors[vertex] == color_to_set
                        })
                        .map(|(cell, candidate)| SetValue::from_cell(cell, candidate))
                })
                .collect::<Vec<_>>()
                .into_iter()
        })
        .collect()
}

// Rule 3: Two colors in a cell
//
// If there are two differently colored candidates in a cell, then the solution must be one of the two candidates. All
// other candidates in the cell can be removed.
pub fn medusa_rule_3(board: &Board<Cell>) -> Vec<BoardModification> {
    create_connected_components(board)
        .flat_map(|graph| {
            let colors = graphs::color_to_map(&graph);
            graph
                .nodes()
                .filter(|(cell, _)| cell.candidates().len() > 2)
                .zip_every_pair()
                .find(|(a @ (cell_a, _), b @ (cell_b, _))| {
                    cell_a == cell_b && colors[a] != colors[b]
                })
                .map(|(a, _)| a)
                .map(|(cell, _)| cell)
                .iter()
                .flat_map(|&cell| {
                    cell.candidates()
                        .iter()
                        .map(move |&candidate| (cell, candidate))
                        .filter(|&removal| !graph.contains_node(removal))
                })
                .collect::<Vec<_>>()
                .into_iter()
        })
        .merge_to_remove_candidates()
}

// Rule 4: Two colors 'elsewhere'
//
// Given a candidate, if there is an unsolved cell with that candidate, it is uncolored, and the cell can see two other
// cells which both have that candidate, and they are differently colored, then the candidate must be the solution to
// one of the other cells, and it cannot be the solution to the first cell with the uncolored candidate. The uncolored
// candidate can be removed from the first cell.
pub fn medusa_rule_4(board: &Board<Cell>) -> Vec<BoardModification> {
    create_connected_components(board)
        .flat_map(|graph| {
            let (color_one, color_two) = graphs::color_to_lists(&graph);
            board
                .cells()
                .unsolved_cells()
                .flat_map(|cell| {
                    cell.candidates()
                        .iter()
                        .map(move |&candidate| (cell, candidate))
                })
                .filter(move |&removal| !graph.contains_node(removal))
                .filter(move |&(cell, candidate)| {
                    can_see_color(cell, candidate, &color_one)
                        && can_see_color(cell, candidate, &color_two)
                })
        })
        .merge_to_remove_candidates()
}

fn can_see_color(
    cell: &UnsolvedCell,
    candidate: SudokuNumber,
    color: &Vec<LocatedCandidate>,
) -> bool {
    color.iter().any(|&(colored_cell, colored_candidate)| {
        candidate == colored_candidate && cell.is_in_same_unit(colored_cell)
    })
}

// Rule 5: Two colors Unit + Cell
//
// If there is an unsolved cell with an uncolored candidate, that candidate can see a colored candidate of the same
// number, and the unsolved cell contains a candidate colored with the opposite color, then either the candidate in the
// same unit is the solution for that cell or the candidate in the same cell is the solution. In either case, the
// uncolored candidate cannot be the solution and can be removed from the unsolved cell.
pub fn medusa_rule_5(board: &Board<Cell>) -> Vec<BoardModification> {
    create_connected_components(board)
        .flat_map(|graph| {
            let (color_one, color_two) = graphs::color_to_lists(&graph);
            board
                .cells()
                .unsolved_cells()
                .flat_map(|cell| {
                    cell.candidates()
                        .iter()
                        .map(move |&candidate| (cell, candidate))
                })
                .filter(move |&removal| !graph.contains_node(removal))
                .filter(move |&(cell, candidate)| {
                    fn color_in_cell(cell: &UnsolvedCell, color: &Vec<LocatedCandidate>) -> bool {
                        cell.candidates()
                            .iter()
                            .any(|&candidate| color.contains(&(cell, candidate)))
                    }

                    can_see_color(cell, candidate, &color_one) && color_in_cell(cell, &color_two)
                        || can_see_color(cell, candidate, &color_two)
                            && color_in_cell(cell, &color_one)
                })
        })
        .merge_to_remove_candidates()
}

// Rule 6: Cell Emptied by Color
//
// If there is an unsolved cell in which every candidate is uncolored and every candidate can see the same color, then
// that color cannot be the solution since it would lead to the cell being emptied of candidates and still have no
// solution. All vertices with the opposite color can be set as the solution.
pub fn medusa_rule_6(board: &Board<Cell>) -> Vec<BoardModification> {
    create_connected_components(board)
        .flat_map(|graph| {
            let (color_one, color_two) = graphs::color_to_lists(&graph);
            board
                .cells()
                .unsolved_cells()
                .filter(|cell| {
                    cell.candidates()
                        .iter()
                        .all(|&candidate| !graph.contains_node((cell, candidate)))
                })
                .find_map(|cell| {
                    fn every_candidate_can_see_color(
                        cell: &UnsolvedCell,
                        color: &Vec<LocatedCandidate>,
                    ) -> bool {
                        cell.candidates().iter().all(|candidate| {
                            color.iter().any(|(colored_cell, colored_candidate)| {
                                candidate == colored_candidate && cell.is_in_same_unit(colored_cell)
                            })
                        })
                    }

                    if every_candidate_can_see_color(cell, &color_one) {
                        Some(color_two.clone())
                    } else if every_candidate_can_see_color(cell, &color_two) {
                        Some(color_one.clone())
                    } else {
                        None
                    }
                })
                .into_iter()
                .flatten()
                .map(|(colored_cell, colored_candidate)| {
                    SetValue::from_cell(colored_cell, colored_candidate)
                })
        })
        .collect()
}

#[allow(dead_code)]
fn to_dot(graph: &UnGraphMap<LocatedCandidate, ()>) -> String {
    let dot = Dot::with_attr_getters(
        graph,
        &[Config::EdgeNoLabel, Config::NodeNoLabel],
        &|_, _| String::new(),
        &|_, ((cell, candidate), _)| format!(r#"label = "{} : {candidate}""#, cell.vertex_label()),
    );
    format!("{dot:?}")
}

fn create_connected_components(
    board: &Board<Cell>,
) -> impl Iterator<Item = UnGraphMap<LocatedCandidate<'_>, ()>> {
    let same_cell_edges = board
        .cells()
        .unsolved_cells()
        .filter(|cell| cell.candidates().len() == 2)
        .map(|cell| {
            let a = (cell, *cell.candidates().first().unwrap());
            let b = (cell, *cell.candidates().last().unwrap());
            (a, b)
        });
    let same_candidate_edges = SudokuNumber::iter().flat_map(|candidate| {
        board
            .units()
            .map(move |unit| {
                unit.unsolved_cells()
                    .filter(|cell| cell.candidates().contains(&candidate))
                    .collect::<Vec<_>>()
            })
            .filter(|unit| unit.len() == 2)
            .map(move |unit| {
                let a = (*unit.first().unwrap(), candidate);
                let b = (*unit.last().unwrap(), candidate);
                (a, b)
            })
    });
    let edges = same_cell_edges.chain(same_candidate_edges);
    let graph = GraphMap::from_edges(edges);
    graphs::connected_components(&graph)
        .collect::<Vec<_>>()
        .into_iter()
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
            UnsolvedCell::with_all_candidates(0, 0),
        ];
        let cells: Vec<_> = cells.iter().unsolved_cells().collect();
        let a = (cells[0], SudokuNumber::Two);
        let b = (cells[1], SudokuNumber::Six);
        let graph = GraphMap::from_edges([(a, b)]);
        let actual = to_dot(&graph);
        let expected = indoc! {r#"
            graph {
                0 [ label = "[0,0] : 2"]
                1 [ label = "[0,0] : 6"]
                0 -- 1 [ ]
            }
        "#};
        assert_eq!(expected, actual);
    }

    #[test]
    fn rule_1_test_1() {
        let board = "\
            {17}9382456{17}\
            {147}856{39}{13}{49}{137}2\
            2{14}6{139}75{49}{13}8\
            321769845\
            {469}{46}{49}2583{17}{17}\
            578{13}4{13}296\
            85{49}{49}16723\
            {149}{134}7{349}8265{49}\
            {69}{346}25{39}718{49}\
        ";
        let expected = [
            SetValue::from_indices(1, 0, 4),
            SetValue::from_indices(1, 4, 3),
            SetValue::from_indices(1, 6, 9),
            SetValue::from_indices(2, 1, 1),
            SetValue::from_indices(2, 3, 9),
            SetValue::from_indices(2, 6, 4),
            SetValue::from_indices(7, 0, 1),
            SetValue::from_indices(7, 3, 3),
            SetValue::from_indices(8, 1, 3),
            SetValue::from_indices(8, 4, 9),
        ];
        assertions::assert_logical_solution(&expected, board, medusa_rule_1);
    }

    #[test]
    fn rule_1_test_2() {
        let board = "\
            {567}{267}{26}9{16}843{15}\
            {59}{359}47{13}268{15}\
            {36}81{36}54{79}{79}2\
            {78}{47}5{68}{46}3129\
            {169}{469}{69}52{17}3{47}8\
            {12}{23}{38}{48}9{17}56{47}\
            {256}{56}{36}{24}7981{34}\
            {38}17{23}{48}5{29}{49}6\
            4{29}{289}1{38}6{27}5{37}\
        ";
        let expected = [
            SetValue::from_indices(0, 1, 7),
            SetValue::from_indices(0, 4, 1),
            SetValue::from_indices(0, 8, 5),
            SetValue::from_indices(1, 4, 3),
            SetValue::from_indices(1, 8, 1),
            SetValue::from_indices(2, 0, 3),
            SetValue::from_indices(2, 3, 6),
            SetValue::from_indices(2, 6, 9),
            SetValue::from_indices(2, 7, 7),
            SetValue::from_indices(3, 0, 7),
            SetValue::from_indices(3, 1, 4),
            SetValue::from_indices(3, 3, 8),
            SetValue::from_indices(3, 4, 6),
            SetValue::from_indices(4, 0, 1),
            SetValue::from_indices(4, 5, 7),
            SetValue::from_indices(4, 7, 4),
            SetValue::from_indices(5, 0, 2),
            SetValue::from_indices(5, 1, 3),
            SetValue::from_indices(5, 2, 8),
            SetValue::from_indices(5, 3, 4),
            SetValue::from_indices(5, 5, 1),
            SetValue::from_indices(5, 8, 7),
            SetValue::from_indices(6, 2, 3),
            SetValue::from_indices(6, 3, 2),
            SetValue::from_indices(6, 8, 4),
            SetValue::from_indices(7, 0, 8),
            SetValue::from_indices(7, 3, 3),
            SetValue::from_indices(7, 4, 4),
            SetValue::from_indices(7, 6, 2),
            SetValue::from_indices(7, 7, 9),
            SetValue::from_indices(8, 4, 8),
            SetValue::from_indices(8, 6, 7),
            SetValue::from_indices(8, 8, 3),
        ];
        assertions::assert_logical_solution(&expected, board, medusa_rule_1);
    }

    #[test]
    fn rule_2_test_1() {
        let board = "\
            3{168}{1679}{189}52{46}{479}{789}\
            25{679}3{489}{49}{67}1{789}\
            {19}{18}46{189}7523\
            {16}932{467}{14}8{47}5\
            57{126}{89}{689}{149}{1249}3{19}\
            4{12}8{79}35{179}6{127}\
            {1679}{126}54{179}83{79}{1279}\
            {179}3{129}5{179}6{1279}84\
            84{19}{179}23{179}56\
        ";
        let expected = [
            SetValue::from_indices(0, 6, 4),
            SetValue::from_indices(1, 6, 6),
            SetValue::from_indices(3, 4, 7),
            SetValue::from_indices(3, 7, 4),
            SetValue::from_indices(4, 5, 4),
            SetValue::from_indices(5, 3, 9),
            SetValue::from_indices(8, 3, 7),
        ];
        assertions::assert_logical_solution(&expected, board, medusa_rule_2);
    }

    #[test]
    fn rule_2_test_2() {
        let board = "\
            748156{39}{29}{23}\
            359284{67}1{67}\
            612379458\
            {19}86{49}{149}3275\
            47{13}5{16}2{368}{68}9\
            2{39}57{69}814{36}\
            5{269}7{489}{49}1{689}3{246}\
            {89}{29}46375{289}1\
            {189}{369}{13}{489}25{6789}{689}{467}\
        ";
        let expected = [
            SetValue::from_indices(0, 6, 9),
            SetValue::from_indices(0, 7, 2),
            SetValue::from_indices(0, 8, 3),
            SetValue::from_indices(3, 0, 9),
            SetValue::from_indices(3, 4, 1),
            SetValue::from_indices(4, 2, 1),
            SetValue::from_indices(4, 4, 6),
            SetValue::from_indices(4, 6, 3),
            SetValue::from_indices(5, 1, 3),
            SetValue::from_indices(5, 4, 9),
            SetValue::from_indices(5, 8, 6),
            SetValue::from_indices(6, 8, 2),
            SetValue::from_indices(7, 1, 2),
            SetValue::from_indices(8, 0, 1),
            SetValue::from_indices(8, 2, 3),
        ];
        assertions::assert_logical_solution(&expected, board, medusa_rule_2);
    }

    #[test]
    fn rule_3_test_1() {
        let board = "\
            29{1467}{56}{57}{46}83{156}\
            {145}{18}{1468}{3568}2{3468}97{156}\
            {357}{378}{678}1{578}94{56}2\
            845761293\
            6{123}{12}{2389}{89}{238}547\
            {37}{237}9{23}45{16}{16}8\
            9{128}34{158}7{16}{1256}{56}\
            {14}6{1248}{258}3{28}7{125}9\
            {17}5{127}{269}{19}{26}384\
        ";
        let expected = [remove_candidates!(2, 1, 8)];
        assertions::assert_logical_solution(&expected, board, medusa_rule_3);
    }

    #[test]
    fn rule_3_test_2() {
        let board = "\
            9{35}8{13}2{134}{45}76\
            6{25}{24}{389}{359}71{48}{389}\
            17{34}{3689}{34569}{34689}{59}2{389}\
            {78}{28}54{36}{36}{27}91\
            391782{46}{46}5\
            46{27}{19}{19}583{27}\
            {78}4{37}{123689}{1369}{13689}{269}5{289}\
            5{38}6{239}{349}{349}{279}1{2789}\
            21957{68}3{68}4\
        ";
        let expected = [remove_candidates!(7, 8, 2, 9)];
        assertions::assert_logical_solution(&expected, board, medusa_rule_3);
    }

    #[test]
    fn rule_3_test_3() {
        let board = "\
            {2567}{2567}{26}9{16}843{15}\
            {359}{359}47{13}268{15}\
            {36}81{36}54{79}{79}2\
            {78}{47}5{468}{46}3129\
            {169}{469}{69}52{17}3{47}8\
            {1238}{234}{238}{48}9{17}56{47}\
            {2356}{2356}{236}{24}7981{34}\
            {389}17{234}{348}5{29}{49}6\
            4{239}{2389}1{38}6{279}5{37}\
        ";
        let expected = [remove_candidates!(8, 6, 9)];
        assertions::assert_logical_solution(&expected, board, medusa_rule_3);
    }

    #[test]
    fn rule_4_test_1() {
        let board = "\
            1{79}{29}{278}56{478}{489}3\
            {256}43{1278}9{78}{1578}{568}{68}\
            8{679}{569}{17}43{157}{569}2\
            {47}3{48}56{789}21{49}\
            95{68}421{68}37\
            {467}21{78}3{789}{4568}{4568}{469}\
            31798{24}{46}{246}5\
            {2456}{68}{245}31{245}97{48}\
            {245}{89}{2459}67{245}3{248}1\
        ";
        let expected = [remove_candidates!(1, 0, 6), remove_candidates!(2, 7, 6)];
        assertions::assert_logical_solution(&expected, board, medusa_rule_4);
    }

    #[test]
    fn rule_4_test_2() {
        let board = "\
            1{79}{29}{278}56{478}{489}3\
            {25}43{1278}9{78}{1578}{568}{68}\
            8{679}{569}{17}43{157}{59}2\
            {47}3{48}56{789}21{49}\
            95{68}421{68}37\
            {467}21{78}3{789}{4568}{4568}{469}\
            31798{24}{46}{246}5\
            {2456}{68}{245}31{245}97{48}\
            {245}{89}{2459}67{245}3{248}1\
        ";
        let expected = [
            remove_candidates!(1, 5, 8),
            remove_candidates!(3, 8, 4),
            remove_candidates!(5, 6, 6),
            remove_candidates!(5, 7, 6),
            remove_candidates!(7, 2, 4),
        ];
        assertions::assert_logical_solution(&expected, board, medusa_rule_4);
    }

    #[test]
    fn rule_4_test_3() {
        let board = "\
            9{35}8{13}2{134}{45}76\
            6{235}{234}{389}{359}71{48}{389}\
            17{34}{3689}{34569}{34689}{59}2{389}\
            {78}{28}54{36}{36}{27}91\
            391782{46}{46}5\
            46{27}{19}{19}583{27}\
            {78}4{37}{123689}{1369}{13689}{269}5{289}\
            5{38}6{239}{349}{349}{279}1{2789}\
            21957{68}3{68}4\
        ";
        let expected = [remove_candidates!(1, 1, 3), remove_candidates!(1, 2, 3)];
        assertions::assert_logical_solution(&expected, board, medusa_rule_4);
    }

    #[test]
    fn rule_4_test_4() {
        let board = "\
            9{35}8{13}2{134}{45}76\
            6{25}{24}{389}{359}71{48}{389}\
            17{34}{3689}{34569}{34689}{59}2{389}\
            {78}{28}54{36}{36}{27}91\
            391782{46}{46}5\
            46{27}{19}{19}583{27}\
            {78}4{37}{123689}{1369}{13689}{269}5{289}\
            5{38}6{239}{349}{349}{279}1{78}\
            21957{68}3{68}4\
        ";
        let expected = [remove_candidates!(6, 8, 8)];
        assertions::assert_logical_solution(&expected, board, medusa_rule_4);
    }

    #[test]
    fn rule_5_test_1() {
        let board = "\
            9234{68}7{68}15\
            876{13}5{13}924\
            5{14}{14}2{689}{69}{678}3{78}\
            769{358}2{35}14{38}\
            432{168}{167}{16}{78}59\
            185{39}{79}426{37}\
            {36}98{56}42{35}71\
            2{15}7{159}3{159}486\
            {36}{145}{14}7{16}8{35}92\
        ";
        let expected = [
            remove_candidates!(2, 4, 8),
            remove_candidates!(2, 6, 6),
            remove_candidates!(4, 3, 6),
            remove_candidates!(4, 4, 1),
        ];
        assertions::assert_logical_solution(&expected, board, medusa_rule_5);
    }

    #[test]
    fn rule_5_test_2() {
        let board = "\
            3{168}{1679}{189}52{4679}{479}{789}\
            25{679}3{489}{49}{67}1{789}\
            {19}{18}46{189}7523\
            {16}932{467}{14}8{47}5\
            57{126}{89}{4689}{149}{1249}3{19}\
            4{12}8{79}35{179}6{127}\
            {1679}{126}54{179}83{79}{1279}\
            {179}3{129}5{179}6{1279}84\
            84{19}{179}23{179}56\
        ";
        let expected = [remove_candidates!(0, 6, 7, 9), remove_candidates!(4, 4, 4)];
        assertions::assert_logical_solution(&expected, board, medusa_rule_5);
    }

    #[test]
    fn rule_5_test_3() {
        let board = "\
            9{35}8{13}2{134}{45}76\
            6{235}{234}{389}{3459}71{48}{389}\
            17{34}{3689}{34569}{34689}{59}2{389}\
            {78}{28}54{36}{36}{27}91\
            391782{46}{46}5\
            46{27}{19}{19}583{27}\
            {78}4{37}{123689}{1369}{13689}{269}5{289}\
            5{38}6{239}{349}{349}{279}1{2789}\
            21957{68}3{68}4\
        ";
        let expected = [remove_candidates!(1, 4, 4), remove_candidates!(7, 8, 2)];
        assertions::assert_logical_solution(&expected, board, medusa_rule_5);
    }

    #[test]
    fn rule_5_test_4() {
        let board = "\
            9{35}8{13}2{134}{45}76\
            6{25}{24}{389}{359}71{48}{389}\
            17{34}{3689}{34569}{4689}{59}2{389}\
            {78}{28}54{36}{36}{27}91\
            391782{46}{46}5\
            46{27}{19}{19}583{27}\
            {78}4{37}{12368}{136}{1368}{69}5{29}\
            5{38}6{239}{349}{49}{27}1{78}\
            21957{68}3{68}4\
        ";
        let expected = [remove_candidates!(2, 4, 3), remove_candidates!(6, 3, 8)];
        assertions::assert_logical_solution(&expected, board, medusa_rule_5);
    }

    #[test]
    fn rule_5_test_5() {
        let board = "\
            {28}19{36}4{38}75{26}\
            {78}5{24}{68}{79}{19}{13}{236}{246}\
            {47}36{17}52{14}89\
            {16}8542{19}{69}73\
            {24}97{38}{38}6{24}15\
            3{246}{124}{17}{79}5{69}{24}8\
            {16}{247}{124}5{36}{37}89{247}\
            5{267}8914{23}{236}{267}\
            9{47}32{68}{78}5{46}1\
        ";
        let expected = [
            remove_candidates!(1, 7, 2),
            remove_candidates!(1, 8, 6),
            remove_candidates!(5, 1, 4),
            remove_candidates!(5, 2, 2),
            remove_candidates!(6, 2, 4),
            remove_candidates!(6, 8, 2, 7),
            remove_candidates!(7, 1, 2, 7),
            remove_candidates!(7, 7, 6),
        ];
        assertions::assert_logical_solution(&expected, board, medusa_rule_5);
    }

    #[test]
    fn rule_5_test_6() {
        let board = "\
            748156{39}{29}{23}\
            359284{67}1{67}\
            612379458\
            {19}86{49}{149}3275\
            47{13}5{16}2{368}{68}9\
            {29}{239}57{69}814{36}\
            5{269}7{489}{49}1{689}3{246}\
            {289}{29}46375{289}1\
            {189}{369}{13}{489}25{6789}{689}{467}\
        ";
        let expected = [remove_candidates!(5, 1, 2)];
        assertions::assert_logical_solution(&expected, board, medusa_rule_5);
    }

    #[test]
    fn rule_6_test_1() {
        let board = "\
            986721345\
            3{12}4956{18}{128}7\
            {25}{125}7{48}3{48}96{12}\
            {248}73{248}65{148}{18}9\
            69{28}{248}17{458}{58}3\
            1{45}{58}39{48}276\
            {2458}{245}{258}679{15}3{128}\
            {258}691437{25}{28}\
            731582694\
        ";
        let expected = [
            SetValue::from_indices(1, 1, 1),
            SetValue::from_indices(1, 7, 2),
            SetValue::from_indices(2, 8, 1),
            SetValue::from_indices(4, 6, 5),
            SetValue::from_indices(4, 7, 8),
            SetValue::from_indices(6, 6, 1),
            SetValue::from_indices(7, 7, 5),
        ];
        assertions::assert_logical_solution(&expected, board, medusa_rule_6);
    }

    #[test]
    fn rule_6_test_2() {
        let board = "\
            9{35}8{13}2{134}{45}76\
            6{25}{24}{389}{359}71{48}{389}\
            17{34}{3689}{4569}{469}{59}2{389}\
            {78}{28}54{36}{36}{27}91\
            391782{46}{46}5\
            46{27}{19}{19}583{27}\
            {78}4{37}{1236}{136}{1368}{69}5{29}\
            5{38}6{239}{349}{49}{27}1{78}\
            21957{68}3{68}4\
        ";
        let expected = [
            SetValue::from_indices(0, 1, 5),
            SetValue::from_indices(0, 6, 4),
            SetValue::from_indices(1, 1, 2),
            SetValue::from_indices(1, 2, 4),
            SetValue::from_indices(1, 4, 5),
            SetValue::from_indices(1, 7, 8),
            SetValue::from_indices(2, 2, 3),
            SetValue::from_indices(2, 6, 5),
            SetValue::from_indices(3, 0, 7),
            SetValue::from_indices(3, 1, 8),
            SetValue::from_indices(3, 6, 2),
            SetValue::from_indices(4, 6, 6),
            SetValue::from_indices(4, 7, 4),
            SetValue::from_indices(5, 2, 2),
            SetValue::from_indices(5, 8, 7),
            SetValue::from_indices(6, 0, 8),
            SetValue::from_indices(6, 2, 7),
            SetValue::from_indices(6, 6, 9),
            SetValue::from_indices(6, 8, 2),
            SetValue::from_indices(7, 1, 3),
            SetValue::from_indices(7, 3, 2),
            SetValue::from_indices(7, 6, 7),
            SetValue::from_indices(7, 8, 8),
            SetValue::from_indices(8, 5, 8),
            SetValue::from_indices(8, 7, 6),
        ];
        assertions::assert_logical_solution(&expected, board, medusa_rule_6);
    }

    #[test]
    fn rule_6_test_3() {
        let board = "\
            2{147}{179}35{679}{4679}8{69}\
            5{47}{79}{269}81{24679}{2467}3\
            836{29}4{79}{2579}1{59}\
            4{157}{17}83{69}{5679}{567}2\
            6{578}2{59}143{57}{589}\
            9{58}3{56}72{4568}{456}1\
            325468197\
            768193{25}{25}4\
            194725{68}3{68}\
        ";
        let expected = [
            SetValue::from_indices(1, 3, 6),
            SetValue::from_indices(3, 5, 6),
            SetValue::from_indices(3, 6, 9),
            SetValue::from_indices(4, 3, 9),
            SetValue::from_indices(5, 3, 5),
        ];
        assertions::assert_logical_solution(&expected, board, medusa_rule_6);
    }
}
