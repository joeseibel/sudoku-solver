use crate::{board, sudoku_number::SudokuNumber};
use std::collections::HashSet;
use strum::IntoEnumIterator;

// Rust enums suffer from the same problem as Swift enums. That is, the enum variants are not types themselves. For
// details on why this is a frustration, see the comments on Cell in the Swift implementation.
//
// There is interest in the Rust community in changing this, but it might be a while before we see this change. See the
// following Rust issue: https://github.com/rust-lang/lang-team/issues/122
enum Cell {
    SolvedCell(SolvedCell),
    UnsolvedCell(UnsolvedCell),
}

impl Cell {
    // TODO: Cell, SolvedCell, and UnsolvedCell constructors currently follow the pattern in Swift. Review to see if
    // this pattern makes sense in Rust.
    fn new_solved(row: usize, column: usize, value: SudokuNumber) -> Self {
        Cell::SolvedCell(SolvedCell::new(row, column, value))
    }

    fn new_unsolved(row: usize, column: usize) -> Self {
        Cell::UnsolvedCell(UnsolvedCell::new(
            row,
            column,
            SudokuNumber::iter().collect(),
        ))
    }
}

struct SolvedCell {
    row: usize,
    column: usize,
    value: SudokuNumber,
}

impl SolvedCell {
    fn new(row: usize, column: usize, value: SudokuNumber) -> Self {
        board::validate_row_and_column(row, column);
        SolvedCell { row, column, value }
    }
}

struct UnsolvedCell {
    row: usize,
    column: usize,
    candidates: HashSet<SudokuNumber>,
}

impl UnsolvedCell {
    fn new(row: usize, column: usize, candidates: HashSet<SudokuNumber>) -> Self {
        board::validate_row_and_column(row, column);
        if candidates.is_empty() {
            panic!("candidates must not be empty.");
        }
        UnsolvedCell {
            row,
            column,
            candidates,
        }
    }
}
