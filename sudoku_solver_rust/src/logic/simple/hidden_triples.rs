use crate::{
    board::Board,
    board_modification::{BoardModification, IteratorRemoveCandidatesExt},
    cell::{Cell, IteratorCellExt},
    collections::IteratorZipExt,
    sudoku_number::SudokuNumber,
};
use std::collections::BTreeSet;
use strum::VariantArray;

// http://www.sudokuwiki.org/Hidden_Candidates#HT
//
// If three candidates exist across three cells in a unit, then those three candidates must be placed in those three
// cells. All other candidates can be removed from those three cells.
pub fn hidden_triples(board: &Board<Cell>) -> Vec<BoardModification> {
    board
        .units()
        .flat_map(|unit| {
            SudokuNumber::VARIANTS
                .iter()
                .zip_every_triple()
                .flat_map(move |(a, b, c)| {
                    let cells: Vec<_> = unit
                        .clone()
                        .unsolved_cells()
                        .filter(|cell| {
                            cell.candidates().contains(a)
                                || cell.candidates().contains(b)
                                || cell.candidates().contains(c)
                        })
                        .collect();
                    if cells.len() == 3 {
                        let mut union = BTreeSet::<SudokuNumber>::new();
                        for cell in &cells {
                            union.extend(cell.candidates());
                        }
                        if union.contains(a) && union.contains(b) && union.contains(c) {
                            Some(cells.clone().into_iter().flat_map(|cell| {
                                cell.candidates()
                                    .difference(&[*a, *b, *c].into())
                                    .copied()
                                    .collect::<Vec<_>>()
                                    .into_iter()
                                    .map(move |candidate| (cell, candidate))
                            }))
                        } else {
                            None
                        }
                    } else {
                        None
                    }
                })
        })
        .flatten()
        .merge_to_remove_candidates()
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::{logic::assertions, remove_candidates};

    #[test]
    fn test() {
        let board = "\
            {4789}{489}{47}{245678}{478}1{2469}3{245789}\
            231{45678}9{578}{46}{56}{4578}\
            {4789}65{2478}{478}31{289}{24789}\
            6789243{15}{15}\
            1{249}3{78}5{78}{249}{29}6\
            {459}{2459}{24}1367{289}{2489}\
            {48}{1248}936{28}57{12}\
            {57}{25}6{257}19843\
            3{12458}{247}{24578}{478}{2578}{269}{16}{129}\
        ";
        let expected = [
            remove_candidates!(0, 3, 4, 7, 8),
            remove_candidates!(0, 6, 4, 9),
            remove_candidates!(0, 8, 4, 7, 8, 9),
        ];
        assertions::assert_logical_solution(&expected, board, hidden_triples);
    }
}
