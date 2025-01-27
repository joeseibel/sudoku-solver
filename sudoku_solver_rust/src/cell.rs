use crate::{
    board::{self, Board},
    sudoku_number::SudokuNumber,
};
use std::collections::HashSet;
use strum::IntoEnumIterator;

// Rust enums suffer from the same problem as Swift enums. That is, the enum variants are not types themselves. For
// details on why this is a frustration, see the comments on Cell in the Swift implementation.
//
// There is interest in the Rust community in changing this, but it might be a while before we see this change. See the
// following Rust issue: https://github.com/rust-lang/lang-team/issues/122
#[derive(Debug)]
pub enum Cell {
    SolvedCell(SolvedCell),
    UnsolvedCell(UnsolvedCell),
}

impl Cell {
    // TODO: Cell, SolvedCell, and UnsolvedCell constructors currently follow the pattern in Swift. Review to see if
    // this pattern makes sense in Rust.
    fn new_solved(row: usize, column: usize, value: SudokuNumber) -> Self {
        Self::SolvedCell(SolvedCell::new(row, column, value))
    }

    fn new_unsolved(row: usize, column: usize) -> Self {
        Self::UnsolvedCell(UnsolvedCell::new(
            row,
            column,
            SudokuNumber::iter().collect(),
        ))
    }
}

#[derive(Debug)]
pub struct SolvedCell {
    row: usize,
    column: usize,
    value: SudokuNumber,
}

impl SolvedCell {
    fn new(row: usize, column: usize, value: SudokuNumber) -> Self {
        board::validate_row_and_column(row, column);
        Self { row, column, value }
    }

    pub fn value(&self) -> SudokuNumber {
        self.value
    }
}

#[derive(Debug)]
pub struct UnsolvedCell {
    row: usize,
    column: usize,
    block: usize,
    candidates: HashSet<SudokuNumber>,
}

impl UnsolvedCell {
    fn new(row: usize, column: usize, candidates: HashSet<SudokuNumber>) -> Self {
        board::validate_row_and_column(row, column);
        if candidates.is_empty() {
            panic!("candidates must not be empty.");
        }
        Self {
            row,
            column,
            block: board::get_block_index(row, column),
            candidates,
        }
    }

    pub fn row(&self) -> usize {
        self.row
    }

    pub fn column(&self) -> usize {
        self.column
    }

    pub fn block(&self) -> usize {
        self.block
    }

    // After some consideration, I decided to return a reference to the candidates HashSet. In total, I considered three
    // options for exposing the set of candidates:
    //
    // 1. Make the candidates field public and allow direct access to the candidates HashSet. While this was the
    //    simpliest option, I didn't want to allow callers to mutate candidates. If a caller were to empty the
    //    candidates HashSet, then the UnsolvedCell would be in an invalid state.
    // 2. Return an Iterator. This would be consistent with the methods on Board that return Iterators, but it would
    //    make checking if a candidate is in the set less efficient. It is better to call HashSet::contains than
    //    Iterator::any.
    // 3. Return a reference to the candidates HashSet. This prevents mutation while allowing callers to call
    //    HashSet::contains.
    //
    // This process of consideration is probably obvious for experienced Rust users, but it was useful to document my
    // thought process here as I learn Rust. This level of consideration for which type I return here isn't necessary in
    // other languages, so it takes a little getting used to in Rust.
    pub fn candidates(&self) -> &HashSet<SudokuNumber> {
        &self.candidates
    }
}

// TODO: Consider implementing TryFrom. Also look at FromStr.
pub fn parse_simple_cells(simple_board: &str) -> Board<Cell> {
    let chars: Vec<_> = simple_board.chars().collect();
    if chars.len() != board::UNIT_SIZE_SQUARED {
        panic!(
            "simple_board.chars().count() is {}, must be {}.",
            chars.len(),
            board::UNIT_SIZE_SQUARED
        );
    }
    let chunks = chars.chunks_exact(board::UNIT_SIZE);
    assert!(chunks.remainder().is_empty());
    let rows: [[Cell; board::UNIT_SIZE]; board::UNIT_SIZE] = chunks
        .enumerate()
        .map(|(row_index, row)| {
            row.iter()
                .enumerate()
                .map(|(column_index, &cell)| match cell {
                    '0' => Cell::new_unsolved(row_index, column_index),
                    _ => Cell::new_solved(row_index, column_index, SudokuNumber::from_digit(cell)),
                })
                .collect::<Vec<_>>()
                .try_into()
                .unwrap()
        })
        .collect::<Vec<_>>()
        .try_into()
        .unwrap();
    Board::new(rows)
}

// Here I follow Rust's Extension Trait pattern: https://rust-lang.github.io/rfcs/0445-extension-trait-conventions.html
//
// On the surface, it looks as if Rust would support extension methods like Swift. However, this is not the case. Swift
// was designed with extensions in mind and it is possible to extend many kinds of types in Swift including Protocols.
// However, Rust does not allow me to implement new methods for the trait Iterator. The solution is a little bit of a
// hack that has become a Rust pattern.
//
// Even though I can't implement a new method directly on the trait Iterator, I can create a new trait, IteratorCellExt,
// with the new methods. The magic happens in the impl block where I implement IteratorCellExt for any type that
// implements Iterator in which the Item is a reference to a Cell.
//
// It took some time to figure out this pattern. It also feels a little clunky in Rust. The extension methods in Kotlin,
// Scala, and Swift have cleaner syntax.
pub trait IteratorCellExt<'a> {
    fn solved_cells(self) -> impl Iterator<Item = &'a SolvedCell>;
    fn unsolved_cells(self) -> impl Iterator<Item = &'a UnsolvedCell>;
}

impl<'a, I: Iterator<Item = &'a Cell>> IteratorCellExt<'a> for I {
    fn solved_cells(self) -> impl Iterator<Item = &'a SolvedCell> {
        self.flat_map(|cell| match cell {
            Cell::SolvedCell(solved_cell) => Some(solved_cell),
            Cell::UnsolvedCell(_) => None,
        })
    }

    fn unsolved_cells(self) -> impl Iterator<Item = &'a UnsolvedCell> {
        self.flat_map(|cell| match cell {
            Cell::SolvedCell(_) => None,
            Cell::UnsolvedCell(unsolved_cell) => Some(unsolved_cell),
        })
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    #[should_panic(expected = "simple_board.chars().count() is 0, must be 81.")]
    fn test_parse_simple_cells_wrong_length() {
        parse_simple_cells("");
    }
}
