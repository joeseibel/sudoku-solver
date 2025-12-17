use crate::{
    board::Board,
    board_modification::{BoardModification, IteratorRemoveCandidatesExt},
    cell::{Cell, IteratorCellExt, LocatedCandidate, Location, UnsolvedCell},
    collections::IteratorZipExt,
    sudoku_number::SudokuNumber,
};
use std::collections::HashSet;
use strum::IntoEnumIterator;

// http://www.sudokuwiki.org/Sword_Fish_Strategy
//
// For a triple of rows, if a candidate appears in two or three cells for each row and the candidate appears in exactly
// three columns across the three rows, forming a three by three grid, then the candidate must be placed in three of the
// nine cells. The candidate can be removed from cells which are in the three columns, but different rows.
//
// For a triple of columns, if a candidate appears in two or three cells for each column and the candidate appears in
// exactly three rows across the three columns, forming a three by three grid, then the candidate must be placed in
// three of the nine cells. The candidate can be removed from cells which are in the three rows, but different columns.
pub fn swordfish(board: &Board<Cell>) -> Vec<BoardModification> {
    SudokuNumber::iter()
        .flat_map(|candidate| {
            fn swordfish<
                'a,
                Z: Iterator<Item = &'a Cell> + IteratorCellExt<'a>,
                U: Iterator<Item = &'a Cell> + Clone,
            >(
                candidate: SudokuNumber,
                units: impl Iterator<Item = impl Iterator<Item = &'a Cell>> + IteratorZipExt<Z>,
                get_other_unit: impl Fn(usize) -> U,
                get_other_unit_index: impl Fn(&UnsolvedCell) -> usize,
            ) -> impl Iterator<Item = LocatedCandidate<'a>> {
                units
                    .zip_every_triple()
                    .flat_map(move |(unit_a, unit_b, unit_c)| {
                        let a_with_candidate: Vec<_> = unit_a
                            .unsolved_cells()
                            .filter(|cell| cell.candidates().contains(&candidate))
                            .collect();
                        let b_with_candidate: Vec<_> = unit_b
                            .unsolved_cells()
                            .filter(|cell| cell.candidates().contains(&candidate))
                            .collect();
                        let c_with_candidate: Vec<_> = unit_c
                            .unsolved_cells()
                            .filter(|cell| cell.candidates().contains(&candidate))
                            .collect();
                        if (2..=3).contains(&a_with_candidate.len())
                            && (2..=3).contains(&b_with_candidate.len())
                            && (2..=3).contains(&c_with_candidate.len())
                        {
                            let mut with_candidate = Vec::new();
                            with_candidate.extend(a_with_candidate);
                            with_candidate.extend(b_with_candidate);
                            with_candidate.extend(c_with_candidate);
                            let other_unit_indices: HashSet<_> = with_candidate
                                .iter()
                                .map(|cell| get_other_unit_index(cell))
                                .collect();
                            if other_unit_indices.len() == 3 {
                                let removals = other_unit_indices
                                    .iter()
                                    .flat_map(|&index| get_other_unit(index))
                                    .unsolved_cells()
                                    .filter(|cell| {
                                        cell.candidates().contains(&candidate)
                                            && !with_candidate.contains(cell)
                                    })
                                    .map(|cell| (cell, candidate))
                                    .collect::<Vec<_>>();
                                Some(removals)
                            } else {
                                None
                            }
                        } else {
                            None
                        }
                    })
                    .flatten()
            }

            let row_removals = swordfish(
                candidate,
                board.rows(),
                |index| board.get_column(index),
                Location::column,
            );
            let column_removals = swordfish(
                candidate,
                board.columns(),
                |index| board.get_row(index),
                Location::row,
            );
            row_removals.chain(column_removals)
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
            52941{68}7{68}3\
            {478}{148}6{59}{789}3{18}{14589}2\
            {478}{148}32{789}{56}{189}{56}{1489}\
            {48}523{89}{148}{189}76\
            637{19}5{148}2{1489}{1489}\
            19{48}62753{48}\
            3{78}{158}{15}6942{178}\
            2{47}{145}83{15}6{19}{179}\
            96{18}7423{18}5\
        ";
        let expected = [
            remove_candidates!(1, 1, 8),
            remove_candidates!(1, 7, 8),
            remove_candidates!(2, 1, 8),
            remove_candidates!(2, 8, 8),
            remove_candidates!(3, 5, 8),
        ];
        assertions::assert_logical_solution(&expected, board, swordfish);
    }

    #[test]
    fn test_2() {
        let board = "\
            926{3458}{48}{3578}1{57}{578}\
            537{689}1{689}42{89}\
            841{259}{59}{2579}6{579}3\
            259734816\
            714{589}6{589}{259}3{259}\
            36812{59}{579}4{579}\
            1{79}2{36}{59}{36}{579}84\
            485{29}7136{29}\
            6{79}3{24589}{48}{2589}{2579}{579}1\
        ";
        let expected = [
            remove_candidates!(2, 3, 9),
            remove_candidates!(2, 5, 9),
            remove_candidates!(6, 6, 9),
            remove_candidates!(8, 3, 9),
            remove_candidates!(8, 5, 9),
            remove_candidates!(8, 6, 9),
        ];
        assertions::assert_logical_solution(&expected, board, swordfish);
    }

    #[test]
    fn test_3() {
        let board = "\
            {157}2{1578}{17}43{1578}69\
            {1457}{145}38962{45}{1457}\
            96{1478}{17}25{1478}3{1478}\
            89{247}56{27}{47}13\
            6{145}{12457}{249}3{279}{45789}{458}{4578}\
            {457}3{457}{49}81{4579}26\
            3{458}{456}{29}1{29}{4568}7{458}\
            {15}{158}96743{58}2\
            27{46}358{146}9{14}\
        ";
        let expected = [
            remove_candidates!(1, 1, 4),
            remove_candidates!(1, 8, 4),
            remove_candidates!(4, 1, 4),
            remove_candidates!(4, 2, 4),
            remove_candidates!(4, 6, 4),
            remove_candidates!(4, 8, 4),
            remove_candidates!(5, 2, 4),
            remove_candidates!(5, 6, 4),
            remove_candidates!(6, 2, 4),
            remove_candidates!(6, 6, 4),
            remove_candidates!(6, 8, 4),
        ];
        assertions::assert_logical_solution(&expected, board, swordfish);
    }

    #[test]
    fn test_4() {
        let board = "\
            1673{259}{259}{289}4{589}\
            8{23}{239}{245}{12459}6{1279}{2579}{159}\
            {29}5487{129}63{19}\
            {236}9{2368}7{2348}{238}51{346}\
            {2356}{234}{123568}{245}{1234589}{123589}{239}{269}7\
            7{234}{1235}{245}6{12359}{239}8{349}\
            {2356}7{2356}9{2358}4{138}{56}{13568}\
            {3569}8{3569}1{35}{357}4{5679}2\
            41{2359}6{2358}{23578}{3789}{579}{3589}\
        ";
        let expected = [
            remove_candidates!(1, 2, 2),
            remove_candidates!(1, 4, 2),
            remove_candidates!(1, 6, 2),
            remove_candidates!(4, 0, 2),
            remove_candidates!(4, 2, 2),
            remove_candidates!(4, 4, 2),
            remove_candidates!(4, 5, 2),
            remove_candidates!(4, 6, 2),
            remove_candidates!(5, 2, 2),
            remove_candidates!(5, 5, 2),
            remove_candidates!(5, 6, 2),
        ];
        assertions::assert_logical_solution(&expected, board, swordfish);
    }

    #[test]
    fn test_5() {
        let board = "\
            3{26789}{12678}{1689}4{1269}{126}5{16}\
            {12}{269}{1256}{1569}37{126}48\
            {128}{2468}{124568}{1568}{168}{126}937\
            {27}1{267}4{679}358{69}\
            4{3678}{3678}{1679}5{169}{16}2{1369}\
            95{36}2{16}847{136}\
            5{478}{1478}3{1789}{149}{78}62\
            {1278}{23478}{123478}{1678}{1678}{146}{78}95\
            6{78}9{78}25314\
        ";
        let expected = [
            remove_candidates!(2, 1, 8),
            remove_candidates!(2, 2, 8),
            remove_candidates!(2, 3, 8),
            remove_candidates!(3, 2, 7),
            remove_candidates!(6, 1, 7, 8),
            remove_candidates!(6, 2, 7, 8),
            remove_candidates!(7, 1, 7, 8),
            remove_candidates!(7, 2, 7, 8),
            remove_candidates!(7, 3, 7, 8),
        ];
        assertions::assert_logical_solution(&expected, board, swordfish);
    }
}
