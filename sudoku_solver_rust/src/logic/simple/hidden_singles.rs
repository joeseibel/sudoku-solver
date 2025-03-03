use crate::{
    board::Board,
    board_modification::BoardModification,
    cell::{Cell, IteratorCellExt},
    sudoku_number::SudokuNumber,
};
use std::collections::HashSet;
use strum::IntoEnumIterator;

// http://www.sudokuwiki.org/Getting_Started
//
// If a candidate exists in only one cell in a unit, then the candidate must be placed in that cell.
pub fn hidden_singles(board: &Board<Cell>) -> Vec<BoardModification> {
    board
        .units()
        .flat_map(|unit| {
            let unsolved: Vec<_> = unit.unsolved_cells().collect();
            SudokuNumber::iter().flat_map(move |candidate| {
                let mut with_candidate = unsolved
                    .iter()
                    .filter(|cell| cell.candidates().contains(&candidate));
                match with_candidate.next() {
                    Some(cell) if with_candidate.next().is_none() => {
                        Some(BoardModification::new_set_value_with_cell(cell, candidate))
                    }
                    _ => None,
                }
            })
        })
        .collect::<HashSet<_>>()
        .into_iter()
        .collect()
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::{cell, logic::brute_force};

    #[test]
    fn test() {
        let board = "\
            2{459}{1569}{159}7{159}{159}38\
            {458}{4589}{159}{123589}{159}6{1259}7{145}\
            3{5789}{159}{12589}4{12589}6{1259}{15}\
            {456}{3459}8{1569}2{1459}7{159}{135}\
            1{23459}{2359}{5789}{59}{45789}{23589}{2589}6\
            {56}{259}7{15689}3{1589}4{12589}{15}\
            {57}{2357}4{12357}8{12357}{135}{156}9\
            {578}6{235}4{159}{123579}{1358}{158}{1357}\
            91{35}{357}6{357}{358}{458}2\
        ";
        let expected = vec![
            BoardModification::new_set_value_with_indices(0, 1, 4),
            BoardModification::new_set_value_with_indices(0, 2, 6),
            BoardModification::new_set_value_with_indices(1, 3, 3),
            BoardModification::new_set_value_with_indices(1, 8, 4),
            BoardModification::new_set_value_with_indices(2, 1, 7),
            BoardModification::new_set_value_with_indices(6, 7, 6),
            BoardModification::new_set_value_with_indices(7, 0, 8),
            BoardModification::new_set_value_with_indices(7, 8, 7),
            BoardModification::new_set_value_with_indices(8, 7, 4),
        ];

        // TODO: Factor out to assert_logical_solution
        let board = cell::parse_cells_with_candidates(board);
        let optional_board = board.map_cells(|cell| match cell {
            Cell::SolvedCell(solved_cell) => Some(solved_cell.value()),
            Cell::UnsolvedCell(_) => None,
        });
        let brute_force_solution = brute_force::brute_force(&optional_board).unwrap();
        let mut actual = hidden_singles(&board);
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
