use crate::{
    board::Board,
    board_modification::{BoardModification, IteratorRemoveCandidatesExt},
    cell::{Cell, IteratorCellExt},
    collections::IteratorZipExt,
    sudoku_number::SudokuNumber,
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
            unit.clone()
                .unsolved_cells()
                .zip_every_triple()
                .flat_map(move |(a, b, c)| {
                    let mut union_of_candidates: BTreeSet<SudokuNumber> = BTreeSet::new();
                    union_of_candidates.extend(a.candidates());
                    union_of_candidates.extend(b.candidates());
                    union_of_candidates.extend(c.candidates());
                    if union_of_candidates.len() == 3 {
                        let removals = unit
                            .clone()
                            .unsolved_cells()
                            .filter(move |&cell| cell != a && cell != b && cell != c)
                            .flat_map(move |cell| {
                                cell.candidates()
                                    .intersection(&union_of_candidates)
                                    .copied()
                                    .collect::<Vec<_>>()
                                    .into_iter()
                                    .map(move |candidate| (cell, candidate))
                            });
                        Some(removals)
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
    use crate::{cell, logic::brute_force};

    #[test]
    fn test1() {
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
        let expected = vec![
            BoardModification::new_remove_candidates_with_indices(4, 0, &[5, 9]),
            BoardModification::new_remove_candidates_with_indices(4, 2, &[5, 9]),
            BoardModification::new_remove_candidates_with_indices(4, 6, &[5, 8, 9]),
            BoardModification::new_remove_candidates_with_indices(4, 7, &[5, 8, 9]),
        ];

        // TODO: Factor out to assert_logical_solution
        let board = cell::parse_cells_with_candidates(board);
        let optional_board = board.map_cells(|cell| match cell {
            Cell::SolvedCell(solved_cell) => Some(solved_cell.value()),
            Cell::UnsolvedCell(_) => None,
        });
        let brute_force_solution = brute_force::brute_force(&optional_board).unwrap();
        let mut actual = naked_triples(&board);
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
        let expected = vec![
            BoardModification::new_remove_candidates_with_indices(3, 1, &[1, 8]),
            BoardModification::new_remove_candidates_with_indices(3, 2, &[1, 8]),
            BoardModification::new_remove_candidates_with_indices(3, 6, &[8]),
            BoardModification::new_remove_candidates_with_indices(3, 7, &[2, 8]),
            BoardModification::new_remove_candidates_with_indices(4, 2, &[1, 5]),
            BoardModification::new_remove_candidates_with_indices(5, 1, &[1, 5, 8]),
            BoardModification::new_remove_candidates_with_indices(5, 2, &[1, 5, 8]),
            BoardModification::new_remove_candidates_with_indices(5, 6, &[8]),
            BoardModification::new_remove_candidates_with_indices(5, 7, &[2, 8]),
        ];

        // TODO: Factor out to assert_logical_solution
        let board = cell::parse_cells_with_candidates(board);
        let optional_board = board.map_cells(|cell| match cell {
            Cell::SolvedCell(solved_cell) => Some(solved_cell.value()),
            Cell::UnsolvedCell(_) => None,
        });
        let brute_force_solution = brute_force::brute_force(&optional_board).unwrap();
        let mut actual = naked_triples(&board);
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
