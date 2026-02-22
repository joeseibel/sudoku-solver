use crate::{
    board::Board,
    board_modification::{BoardModification, IteratorRemoveCandidatesExt},
    cell::{Cell, IteratorCellExt},
    collections::IteratorZipExt,
};
use std::collections::BTreeSet;

// http://www.sudokuwiki.org/Naked_Candidates#NT
//
// If a unit has three unsolved cells with a total of three candidates among them, then those three candidates must be
// placed in those three cells. The three candidates can be removed from every other cell in the unit.
pub fn naked_triples(board: &Board<Cell>) -> Vec<BoardModification> {
    board
        .units()
        .flat_map(|unit| {
            let unit: Vec<_> = unit.collect();
            unit.iter()
                .copied()
                .unsolved_cells()
                .zip_every_triple()
                .flat_map(|(a, b, c)| {
                    let mut union_of_candidates = BTreeSet::new();
                    union_of_candidates.extend(a.candidates());
                    union_of_candidates.extend(b.candidates());
                    union_of_candidates.extend(c.candidates());
                    if union_of_candidates.len() == 3 {
                        unit.iter()
                            .copied()
                            .unsolved_cells()
                            .filter(|&cell| cell != a && cell != b && cell != c)
                            .flat_map(|cell| {
                                cell.candidates()
                                    .intersection(&union_of_candidates)
                                    .map(move |&candidate| (cell, candidate))
                            })
                            .collect::<Vec<_>>()
                    } else {
                        Vec::new()
                    }
                })
                .collect::<Vec<_>>()
        })
        .merge_to_remove_candidates()
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::{logic::assertions, remove_candidates};

    #[test]
    fn test_1() {
        let board = "\
            {36}7{16}4{135}8{135}29\
            {369}{169}2{1579}{135}{5679}{1358}{3568}4\
            854{19}2{69}{13}{36}7\
            {569}{169}83742{59}{16}\
            {45679}2{15679}{589}{58}{59}{3589}{34589}{16}\
            {459}{49}32617{4589}{58}\
            {457}{48}{57}{578}93612\
            2{689}{5679}{1578}{158}{57}4{589}3\
            13{59}642{589}7{58}\
        ";
        let expected = [
            remove_candidates!(4, 0, 5, 9),
            remove_candidates!(4, 2, 5, 9),
            remove_candidates!(4, 6, 5, 8, 9),
            remove_candidates!(4, 7, 5, 8, 9),
        ];
        assertions::assert_logical_solution(&expected, board, naked_triples);
    }

    #[test]
    fn test_2() {
        let board = "\
            294513{78}{78}6\
            6{57}{57}842319\
            3{18}{18}697254\
            {18}{1278}{123789}{23}56{14789}{24789}{238}\
            {15}4{1579}{23}8{19}{1579}6{23}\
            {158}{12568}{1235689}47{19}{1589}{289}{238}\
            73{28}164{89}{289}5\
            9{268}{268}735{48}{248}1\
            4{15}{15}928637\
        ";
        let expected = [
            remove_candidates!(3, 1, 1, 8),
            remove_candidates!(3, 2, 1, 8),
            remove_candidates!(3, 6, 8),
            remove_candidates!(3, 7, 2, 8),
            remove_candidates!(4, 2, 1, 5),
            remove_candidates!(5, 1, 1, 5, 8),
            remove_candidates!(5, 2, 1, 5, 8),
            remove_candidates!(5, 6, 8),
            remove_candidates!(5, 7, 2, 8),
        ];
        assertions::assert_logical_solution(&expected, board, naked_triples);
    }
}
