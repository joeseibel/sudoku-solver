pub mod brute_force;
pub mod simple;

#[cfg(test)]
mod assertions {
    use crate::{
        board::Board,
        board_modification::BoardModification,
        cell::{self, Cell},
        logic::brute_force,
    };

    pub fn assert_logical_solution(
        expected: &[BoardModification],
        with_candidates: &str,
        logic_function: impl FnOnce(&Board<Cell>) -> Vec<BoardModification>,
    ) {
        assert_logical_solution_with_parsed(
            expected,
            &cell::parse_cells_with_candidates(with_candidates),
            logic_function,
        );
    }

    pub fn assert_logical_solution_with_parsed(
        expected: &[BoardModification],
        board: &Board<Cell>,
        logic_function: impl FnOnce(&Board<Cell>) -> Vec<BoardModification>,
    ) {
        let optional_board = board.map_cells(|cell| match cell {
            Cell::SolvedCell(solved_cell) => Some(solved_cell.value()),
            Cell::UnsolvedCell(_) => None,
        });
        let brute_force_solution = brute_force::brute_force(&optional_board).unwrap();
        let mut actual = logic_function(board);
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
