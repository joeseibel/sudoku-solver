use crate::{
    board::Board,
    board_modification::{BoardModification, IteratorRemoveCandidatesExt},
    cell::{Cell, IteratorCellExt},
    collections::IteratorZipExt,
};

// http://www.sudokuwiki.org/Naked_Candidates#NP
//
// If a pair of unsolved cells in a unit has the same two candidates, then those two candidates must be placed in those
// two cells. The two candidates can be removed from every other cell in the unit.
//
// TODO: Should this return a Vec or an Iterator?
pub fn naked_pairs(board: &Board<Cell>) -> Vec<BoardModification> {
    board
        .units()
        .flat_map(|unit| {
            unit.clone()
                .unsolved_cells()
                .filter(|cell| cell.candidates().len() == 2)
                .zip_every_pair()
                .filter(|(a, b)| a.candidates() == b.candidates())
                .flat_map(move |(a, b)| {
                    unit.clone()
                        .unsolved_cells()
                        .filter(move |&cell| cell != a && cell != b)
                        .flat_map(|cell| {
                            cell.candidates()
                                .intersection(a.candidates())
                                .map(move |&candidate| (cell, candidate))
                        })
                })
        })
        .merge_to_remove_candidates()
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::{cell, logic::brute_force};

    #[test]
    fn test1() {
        let board = "\
            4{16}{16}{125}{12567}{2567}938\
            {78}32{58}941{56}{567}\
            {178}953{1678}{67}24{67}\
            37{18}6{258}9{58}{1258}4\
            529{48}{48}1673\
            6{18}47{258}3{58}9{125}\
            957{124}{1246}83{126}{126}\
            {18}{168}39{12567}{2567}4{12568}{1256}\
            24{168}{15}3{56}7{1568}9\
        ";
        let expected = vec![
            BoardModification::new_remove_candidates_with_indices(0, 3, &[1]),
            BoardModification::new_remove_candidates_with_indices(0, 4, &[1, 6]),
            BoardModification::new_remove_candidates_with_indices(0, 5, &[6]),
            BoardModification::new_remove_candidates_with_indices(2, 0, &[1, 7]),
            BoardModification::new_remove_candidates_with_indices(2, 4, &[6, 7]),
            BoardModification::new_remove_candidates_with_indices(3, 4, &[8]),
            BoardModification::new_remove_candidates_with_indices(3, 7, &[5, 8]),
            BoardModification::new_remove_candidates_with_indices(5, 4, &[8]),
            BoardModification::new_remove_candidates_with_indices(5, 8, &[5]),
        ];

        // TODO: Factor out to assert_logical_solution
        let board = cell::parse_cells_with_candidates(board);
        let optional_board = board.map_cells(|cell| match cell {
            Cell::SolvedCell(solved_cell) => Some(solved_cell.value()),
            Cell::UnsolvedCell(_) => None,
        });
        let brute_force_solution = brute_force::brute_force(&optional_board).unwrap();
        let mut actual = naked_pairs(&board);
        // Why am I using sort_unstable_by_key instead of sort_unstable and implementing Ord for BoardModification?
        // In short, implementing Ord for BoardModification would lead to PartialOrd and PartialEq disagreeing with each
        // other. I want to sort BoardModifications by the row and column indices only while ignoring other fields.
        // However, I want equality to check all fields, as that is useful in unit tests. Having a different standard of
        // equality between PartialOrd and PartialEq breaks the contract of PartialOrd.
        actual.sort_unstable_by_key(|modification| (modification.row(), modification.column()));
        for modification in &actual {
            let row = modification.row();
            let column = modification.column();
            let solution = brute_force_solution[(row, column)];
            match modification {
                BoardModification::RemoveCandidates(remove_candidates) => assert!(
                    !remove_candidates.candidates().contains(&solution),
                    "Cannot remove candidate {solution} from [{row}, {column}]"
                ),
                BoardModification::SetValue(set_value) => assert_eq!(
                    solution,
                    set_value.value(),
                    "Cannot set value {} to [{row}, {column}]. Solution is {solution}",
                    set_value.value()
                ),
            }
        }
        assert_eq!(expected, actual);
    }

    #[test]
    fn test2() {
        let board = "\
            {1467}8{567}{12457}9{12}{247}3{24}\
            {147}3{57}{12457}{1278}{128}{247}69\
            9{47}2{47}63158\
            {67}2{67}8{13}459{13}\
            8519{23}7{23}46\
            3946{12}587{12}\
            563{12}4{12}987\
            2{47}{789}{37}{378}{689}{346}15\
            {47}1{789}{37}5{689}{346}2{34}\
        ";
        let expected = vec![
            BoardModification::new_remove_candidates_with_indices(0, 3, &[7]),
            BoardModification::new_remove_candidates_with_indices(1, 3, &[7]),
            BoardModification::new_remove_candidates_with_indices(1, 5, &[1, 2]),
            BoardModification::new_remove_candidates_with_indices(2, 3, &[7]),
            BoardModification::new_remove_candidates_with_indices(7, 2, &[7]),
            BoardModification::new_remove_candidates_with_indices(7, 4, &[3, 7]),
            BoardModification::new_remove_candidates_with_indices(8, 2, &[7]),
        ];

        // TODO: Factor out to assert_logical_solution
        let board = cell::parse_cells_with_candidates(board);
        let optional_board = board.map_cells(|cell| match cell {
            Cell::SolvedCell(solved_cell) => Some(solved_cell.value()),
            Cell::UnsolvedCell(_) => None,
        });
        let brute_force_solution = brute_force::brute_force(&optional_board).unwrap();
        let mut actual = naked_pairs(&board);
        // Why am I using sort_unstable_by_key instead of sort_unstable and implementing Ord for BoardModification?
        // In short, implementing Ord for BoardModification would lead to PartialOrd and PartialEq disagreeing with each
        // other. I want to sort BoardModifications by the row and column indices only while ignoring other fields.
        // However, I want equality to check all fields, as that is useful in unit tests. Having a different standard of
        // equality between PartialOrd and PartialEq breaks the contract of PartialOrd.
        actual.sort_unstable_by_key(|modification| (modification.row(), modification.column()));
        for modification in &actual {
            let row = modification.row();
            let column = modification.column();
            let solution = brute_force_solution[(row, column)];
            match modification {
                BoardModification::RemoveCandidates(remove_candidates) => assert!(
                    !remove_candidates.candidates().contains(&solution),
                    "Cannot remove candidate {solution} from [{row}, {column}]"
                ),
                BoardModification::SetValue(set_value) => assert_eq!(
                    solution,
                    set_value.value(),
                    "Cannot set value {} to [{row}, {column}]. Solution is {solution}",
                    set_value.value()
                ),
            }
        }
        assert_eq!(expected, actual);
    }
}
