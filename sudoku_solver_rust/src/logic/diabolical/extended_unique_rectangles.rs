use crate::{
    board::Board,
    board_modification::{BoardModification, IteratorRemoveCandidatesExt},
    cell::{Cell, LocatedCandidate, UnsolvedCell},
    collections::IteratorZipExt,
    sudoku_number::SudokuNumber,
};
use std::collections::{BTreeSet, HashSet};

// https://www.sudokuwiki.org/Extended_Unique_Rectangles
//
// Extended Unique Rectangles are like Unique Rectangles except that they are 2x3 instead of 2x2. The cells in the
// rectangle must be spread over three blocks and the dimension that has three elements must be spread over three units
// (rows or columns). If there are only three candidates found among the six cells, then such a rectangle is the Deadly
// Pattern. If there is one cell with additional candidates, then the removal of such candidates would lead to a Deadly
// Pattern. The common candidates can be removed from the cell leaving only the additional candidates remaining.
pub fn extended_unique_rectangles(board: &Board<Cell>) -> Vec<BoardModification> {
    get_removals(board.rows())
        .chain(get_removals(board.columns()))
        .merge_to_remove_candidates()
}

fn get_removals<'a, U: Iterator<Item = &'a Cell> + Clone>(
    units: impl IteratorZipExt<U>,
) -> impl Iterator<Item = LocatedCandidate<'a>> {
    units
        .zip_every_pair()
        .flat_map(|(unit_a, unit_b)| {
            unit_a
                .zip(unit_b)
                .flat_map(|(cell_a, cell_b)| {
                    if let Cell::UnsolvedCell(cell_a) = cell_a
                        && let Cell::UnsolvedCell(cell_b) = cell_b
                    {
                        Some((cell_a, cell_b))
                    } else {
                        None
                    }
                })
                .zip_every_triple()
        })
        .map(
            |((other_a_a, other_a_b), (other_b_a, other_b_b), (other_c_a, other_c_b))| {
                (
                    [other_a_a, other_b_a, other_c_a],
                    [other_a_b, other_b_b, other_c_b],
                )
            },
        )
        .filter(|(unit_a, unit_b)| {
            unit_a
                .iter()
                .chain(unit_b)
                .map(|cell| cell.block())
                .collect::<HashSet<_>>()
                .len()
                == 3
        })
        .flat_map(|(unit_a, unit_b)| {
            let unit_a_candidates: BTreeSet<_> = unit_a
                .iter()
                .flat_map(|cell| cell.candidates())
                .copied()
                .collect();
            let unit_b_candidates = unit_b
                .iter()
                .flat_map(|cell| cell.candidates())
                .copied()
                .collect();
            if unit_a_candidates.len() == 3 {
                get_unit_removals(&unit_a_candidates, &unit_b, &unit_b_candidates)
            } else if unit_b_candidates.len() == 3 {
                get_unit_removals(&unit_b_candidates, &unit_a, &unit_a_candidates)
            } else {
                Vec::new()
            }
        })
}

fn get_unit_removals<'a>(
    common_candidates: &BTreeSet<SudokuNumber>,
    unit: &[&'a UnsolvedCell],
    unit_candidates: &BTreeSet<SudokuNumber>,
) -> Vec<LocatedCandidate<'a>> {
    if unit_candidates.len() > 3 && unit_candidates.is_superset(common_candidates) {
        let mut with_additional_iter = unit
            .iter()
            .filter(|cell| !common_candidates.is_superset(cell.candidates()));
        if let Some(&with_additional) = with_additional_iter.next()
            && with_additional_iter.next().is_none()
        {
            with_additional
                .candidates()
                .intersection(common_candidates)
                .map(|&candidate| (with_additional, candidate))
                .collect()
        } else {
            Vec::new()
        }
    } else {
        Vec::new()
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::{logic::assertions, remove_candidates};

    #[test]
    fn test_1() {
        let board = "\
            9{16}7{18}243{68}5\
            842365917\
            {156}3{15}9{18}74{268}{28}\
            {125}{1578}4{57}{138}96{258}{238}\
            {135}{178}{13}246{17}{58}9\
            {256}{1567}9{57}{138}{18}{17}4{238}\
            {135}{15}{135}492876\
            796{18}5{18}234\
            428673591\
        ";
        let expected = [remove_candidates!(2, 0, 1, 5)];
        assertions::assert_logical_solution(&expected, board, extended_unique_rectangles);
    }

    #[test]
    fn test_2() {
        let board = "\
            {45}6382{145}{79}{79}{145}\
            7{48}{58}{1345}{135}92{14}6\
            219{456}{56}738{45}\
            {456}32{14567}9{1456}8{1467}{14}\
            9{478}{58}{134567}{13568}{13456}{47}{1467}2\
            {46}{478}1{467}{68}2539\
            124978653\
            8562{13}{13}{49}{49}7\
            397{56}4{56}128\
        ";
        let expected = [remove_candidates!(4, 7, 4, 7)];
        assertions::assert_logical_solution(&expected, board, extended_unique_rectangles);
    }

    #[test]
    fn test_3() {
        let board = "\
            {367}9{347}8{46}152{37}\
            851{37}926{37}4\
            {367}2{347}{37}{46}5918\
            {1237}{138}965{37}{378}4{237}\
            4{38}{25}{129}{12}{37}{378}{59}6\
            {2357}6{2357}{29}841{3579}{2379}\
            {2359}7{235}{12}{123}648{139}\
            {139}{13}64782{39}5\
            {23}485{123}9{37}6{137}\
        ";
        let expected = [remove_candidates!(3, 1, 3, 8)];
        assertions::assert_logical_solution(&expected, board, extended_unique_rectangles);
    }
}
