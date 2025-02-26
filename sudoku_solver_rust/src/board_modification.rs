use crate::{
    board,
    cell::{LocatedCandidate, UnsolvedCell},
    sudoku_number::SudokuNumber,
};
use itertools::Itertools;
use std::collections::BTreeSet;
use strum::VariantArray;

// The use of Rust's enum follows the same pattern that was used for Cell.
#[derive(Debug, Eq, Hash, PartialEq)]
pub enum BoardModification {
    RemoveCandidates(RemoveCandidates),
    SetValue(SetValue),
}

impl BoardModification {
    // TODO: BoardModification, RemoveCandidates, and SetValue constructors currently follow the pattern in Swift.
    // Review to see if this pattern makes sense in Rust.
    pub fn new_remove_candidates_with_cell(
        cell: &UnsolvedCell,
        candidates: BTreeSet<SudokuNumber>,
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
        let candidates: BTreeSet<_> = candidates
            .iter()
            .map(|candidate| SudokuNumber::VARIANTS[candidate - 1])
            .collect();
        Self::RemoveCandidates(RemoveCandidates::new(row, column, candidates))
    }

    pub fn new_set_value_with_cell(cell: &UnsolvedCell, value: SudokuNumber) -> Self {
        if !cell.candidates().contains(&value) {
            panic!(
                "{value} is not a candidate for [{}, {}].",
                cell.row(),
                cell.column()
            );
        }
        Self::SetValue(SetValue::new(cell.row(), cell.column(), value))
    }

    pub fn new_set_value_with_indices(row: usize, column: usize, value: usize) -> Self {
        Self::SetValue(SetValue::new(
            row,
            column,
            SudokuNumber::VARIANTS[value - 1],
        ))
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

#[derive(Debug, Eq, Hash, PartialEq)]
pub struct RemoveCandidates {
    row: usize,
    column: usize,
    // Why am I using a BTreeSet instead of a HashSet? This was required so that RemoveCandidates could derive Hash.
    //
    // This requirement started with the need to store BoardModifications in a HashSet (The Hidden Singles logical
    // solution puts BoardModifications into a HashSet to remove duplicates). In order to put BoardModifications in a
    // HashSet, BoardModifications must implement Hash which also means that RemoveCandidates must implement Hash.
    //
    // However, I was surprised to discover that HashSet does not implement Hash itself! If it did, then candidates
    // could be a HashSet of SudokuNumber instead of a BTreeSet. Rust is the first langauge I've encountered that
    // doesn't allow HashSets to be hashed. This seems to have caused a bit of confusion online:
    // https://users.rust-lang.org/t/hashmap-hashset-not-implemented-hash/63173
    // https://users.rust-lang.org/t/hash-not-implemented-why-cant-it-be-derived/92416
    // https://internals.rust-lang.org/t/implementing-hash-for-hashset-hashmap/3817
    // and many more... It seems like there is some uncertainty of how to hash a HashSet since the order of elements are
    // semantically insignificant for a HashSet, but are significant when hashing.
    //
    // In JVM languages, this is not a problem because all objects are hashable by virtue of java.lang.Object having the
    // method hashCode(). java.util.AbstractSet overrides hashCode() and simply sums up the hash codes of each of the
    // elements, thus order doesn't matter. In Swift, the struct Set conforms to the protocol Hashable, so this is also
    // not a problem in Swift. Rust is unique here and I haven't yet dug into the full details to really know why.
    //
    // While HashSet doesn't implement Hash, BTreeSet does. This problem is solved by simply using a BTreeSet instead of
    // a HashSet, but oh boy has this caused a lot of confusion.
    candidates: BTreeSet<SudokuNumber>,
}

impl RemoveCandidates {
    fn new(row: usize, column: usize, candidates: BTreeSet<SudokuNumber>) -> Self {
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

    pub fn candidates(&self) -> &BTreeSet<SudokuNumber> {
        &self.candidates
    }
}

#[derive(Debug, Eq, Hash, PartialEq)]
pub struct SetValue {
    row: usize,
    column: usize,
    value: SudokuNumber,
}

impl SetValue {
    fn new(row: usize, column: usize, value: SudokuNumber) -> Self {
        board::validate_row_and_column(row, column);
        Self { row, column, value }
    }

    pub fn value(&self) -> SudokuNumber {
        self.value
    }
}

pub trait IteratorRemoveCandidatesExt {
    fn merge_to_remove_candidates(self) -> Vec<BoardModification>;
}

impl<'a, I: Iterator<Item = LocatedCandidate<'a>>> IteratorRemoveCandidatesExt for I {
    // The receiver represents a list of numbers that should be removed from specific cells. This helper function allows
    // the logic functions to focus on simply marking the numbers to be removed, then at the end use this function to
    // produce at most one RemoveCandidates per cell.
    //
    // TODO: Look at implementing FromIterator so that collect can be called instead of this method.
    fn merge_to_remove_candidates(self) -> Vec<BoardModification> {
        self.into_group_map()
            .into_iter()
            .map(|(cell, candidates)| {
                BoardModification::new_remove_candidates_with_cell(
                    cell,
                    candidates.into_iter().collect(),
                )
            })
            .collect()
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::cell::IteratorCellExt;
    use crate::{cell::Cell, sudoku_number::SudokuNumber};
    use std::{collections::BTreeSet, iter::once};

    #[test]
    #[should_panic(expected = "candidates must not be empty.")]
    fn test_remove_candidates_candidates_are_empty() {
        BoardModification::new_remove_candidates_with_indices(0, 0, &[]);
    }

    #[test]
    #[should_panic(expected = "1 is not a candidate for [0, 0].")]
    fn test_remove_candidates_not_a_candidate_for_cell() {
        BoardModification::new_remove_candidates_with_cell(
            once(&Cell::new_unsolved_with_candidates(
                0,
                0,
                BTreeSet::from([SudokuNumber::Two]),
            ))
            .unsolved_cells()
            .next()
            .unwrap(),
            BTreeSet::from([SudokuNumber::One]),
        );
    }

    #[test]
    #[should_panic(expected = "1 is not a candidate for [0, 0].")]
    fn test_set_value_not_a_candidate_for_cell() {
        BoardModification::new_set_value_with_cell(
            once(&Cell::new_unsolved_with_candidates(
                0,
                0,
                BTreeSet::from([SudokuNumber::Two]),
            ))
            .unsolved_cells()
            .next()
            .unwrap(),
            SudokuNumber::One,
        );
    }
}
