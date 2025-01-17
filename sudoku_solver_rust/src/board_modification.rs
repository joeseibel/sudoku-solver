use crate::{board, cell::UnsolvedCell, sudoku_number::SudokuNumber};
use std::collections::HashSet;
use strum::IntoEnumIterator;

// The use of Rust's enum follows the same pattern that was used for Cell.
enum BoardModification {
    RemoveCandidates(RemoveCandidates),
    SetValue(SetValue),
}

impl BoardModification {
    // TODO: BoardModification, RemoveCandidates, and SetValue constructors currently follow the pattern in Swift.
    // Review to see if this pattern makes sense in Rust.
    fn new_remove_candidates_with_cell(
        cell: UnsolvedCell,
        candidates: HashSet<SudokuNumber>,
    ) -> Self {
        for candidate in &candidates {
            if cell.candidates().contains(candidate) {
                // TODO: Implement Display for SudokuNumber and remove ':?' from this panic call.
                panic!(
                    "{candidate:?} is not a candidate for [{}, {}].",
                    cell.row(),
                    cell.column()
                );
            }
        }
        Self::RemoveCandidates(RemoveCandidates::new(cell.row(), cell.column(), candidates))
    }

    // TODO: Look into macros for varargs.
    fn new_remove_candidates_with_indices(row: usize, column: usize, candidates: &[usize]) -> Self {
        let all_candidates: Vec<_> = SudokuNumber::iter().collect();
        let candidates: HashSet<_> = candidates
            .iter()
            .map(|candidate| all_candidates[candidate - 1])
            .collect();
        Self::RemoveCandidates(RemoveCandidates::new(row, column, candidates))
    }
}

struct RemoveCandidates {
    row: usize,
    column: usize,
    candidates: HashSet<SudokuNumber>,
}

impl RemoveCandidates {
    fn new(row: usize, column: usize, candidates: HashSet<SudokuNumber>) -> Self {
        board::validate_row_and_column(row, column);
        if candidates.is_empty() {
            panic!("candidates must not be empty.");
        }
        Self {
            row,
            column,
            candidates,
        }
    }
}

struct SetValue {}
