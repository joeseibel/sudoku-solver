use crate::{board, cell::UnsolvedCell, sudoku_number::SudokuNumber};
use std::collections::HashSet;
use strum::VariantArray;

// The use of Rust's enum follows the same pattern that was used for Cell.
#[derive(Debug, PartialEq)]
pub enum BoardModification {
    RemoveCandidates(RemoveCandidates),
    SetValue(SetValue),
}

impl BoardModification {
    // TODO: BoardModification, RemoveCandidates, and SetValue constructors currently follow the pattern in Swift.
    // Review to see if this pattern makes sense in Rust.
    pub fn new_remove_candidates_with_cell(
        cell: &UnsolvedCell,
        candidates: HashSet<SudokuNumber>,
    ) -> Self {
        for candidate in &candidates {
            if !cell.candidates().contains(candidate) {
                panic!(
                    "{candidate} is not a candidate for [{}, {}].",
                    cell.row(),
                    cell.column()
                );
            }
        }
        Self::RemoveCandidates(RemoveCandidates::new(cell.row(), cell.column(), candidates))
    }

    // TODO: Look into macros for varargs.
    pub fn new_remove_candidates_with_indices(
        row: usize,
        column: usize,
        candidates: &[usize],
    ) -> Self {
        let candidates: HashSet<_> = candidates
            .iter()
            .map(|candidate| SudokuNumber::VARIANTS[candidate - 1])
            .collect();
        Self::RemoveCandidates(RemoveCandidates::new(row, column, candidates))
    }

    pub fn row(&self) -> usize {
        match self {
            BoardModification::RemoveCandidates(remove_candidates) => remove_candidates.row,
            BoardModification::SetValue(set_value) => set_value.row,
        }
    }

    pub fn column(&self) -> usize {
        match self {
            BoardModification::RemoveCandidates(remove_candidates) => remove_candidates.column,
            BoardModification::SetValue(set_value) => set_value.column,
        }
    }
}

#[derive(Debug, PartialEq)]
pub struct RemoveCandidates {
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

    pub fn candidates(&self) -> &HashSet<SudokuNumber> {
        &self.candidates
    }
}

#[derive(Debug, PartialEq)]
pub struct SetValue {
    row: usize,
    column: usize,
    value: SudokuNumber,
}

impl SetValue {
    pub fn value(&self) -> SudokuNumber {
        self.value
    }
}
