use crate::{
    board::Board,
    board_modification::{BoardModification, IteratorRemoveCandidatesExt},
    cell::{Cell, IteratorCellExt},
    collections::IteratorZipExt,
};
use std::collections::BTreeSet;

// http://www.sudokuwiki.org/Naked_Candidates#NQ
//
// If a unit has four unsolved cells with a total of four candidates among them, then those four candidates must be
// placed in those four cells. The four candidates can be removed from every other cell in the unit.
pub fn naked_quads(board: &Board<Cell>) -> Vec<BoardModification> {
    board
        .units()
        .flat_map(|unit| {
            let unit: Vec<_> = unit.collect();
            unit.iter()
                .copied()
                .unsolved_cells()
                .zip_every_quad()
                .flat_map(|(a, b, c, d)| {
                    let mut union_of_candidates = BTreeSet::new();
                    union_of_candidates.extend(a.candidates());
                    union_of_candidates.extend(b.candidates());
                    union_of_candidates.extend(c.candidates());
                    union_of_candidates.extend(d.candidates());
                    if union_of_candidates.len() == 4 {
                        unit.iter()
                            .copied()
                            .unsolved_cells()
                            .filter(|&cell| cell != a && cell != b && cell != c && cell != d)
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
    fn test() {
        let board = "\
            {15}{1245}{2457}{45}3{19}{79}86\
            {1568}{1568}{35678}{56}2{19}{79}4{13}\
            {16}9{346}{46}7852{13}\
            371856294\
            9{68}{68}142375\
            4{25}{25}397618\
            2{146}{46}7{16}3859\
            {18}392{18}5467\
            7{568}{568}9{68}4132\
        ";
        let expected = [
            remove_candidates!(0, 1, 1, 5),
            remove_candidates!(0, 2, 5),
            remove_candidates!(1, 2, 5, 6, 8),
            remove_candidates!(2, 2, 6),
        ];
        assertions::assert_logical_solution(&expected, board, naked_quads);
    }
}
