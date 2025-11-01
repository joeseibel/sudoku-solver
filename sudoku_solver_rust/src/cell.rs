use crate::{
    board::{self, Board},
    sudoku_number::SudokuNumber,
};
use itertools::Itertools;
use std::{
    collections::BTreeSet,
    fmt::{self, Display},
    str::FromStr,
};
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
    pub fn block(&self) -> usize {
        match self {
            Self::SolvedCell(cell) => cell.block,
            Self::UnsolvedCell(cell) => cell.block(),
        }
    }
}

impl Location for Cell {
    fn row(&self) -> usize {
        match self {
            Self::SolvedCell(cell) => cell.row,
            Self::UnsolvedCell(cell) => cell.row,
        }
    }

    fn column(&self) -> usize {
        match self {
            Self::SolvedCell(cell) => cell.column,
            Self::UnsolvedCell(cell) => cell.column,
        }
    }
}

impl Display for Cell {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        match self {
            Self::SolvedCell(cell) => write!(f, "{cell}"),
            Self::UnsolvedCell(cell) => write!(f, "{cell}"),
        }
    }
}

// Some of the logical solutions require the ability to get the row or column from a Cell or UnsolvedCell. Since Cell,
// SolvedCell, and UnsolvedCell are all independent types, I decided to create this Location trait so that the row and
// column values can be retrieved regardless of whether the cell is a Cell or an UnsolvedCel. Currently, SolvedCell does
// not implement Location because that functionality has not been needed.
pub trait Location {
    fn row(&self) -> usize;
    fn column(&self) -> usize;
}

#[derive(Debug)]
pub struct SolvedCell {
    row: usize,
    column: usize,
    block: usize,
    value: SudokuNumber,
}

impl SolvedCell {
    pub fn from_indices(row: usize, column: usize, value: SudokuNumber) -> Cell {
        board::validate_row_and_column(row, column);
        Cell::SolvedCell(Self {
            row,
            column,
            block: board::get_block_index(row, column),
            value,
        })
    }

    pub fn value(&self) -> SudokuNumber {
        self.value
    }
}

impl Display for SolvedCell {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{}", self.value)
    }
}

#[derive(Debug, Eq, Hash, PartialEq)]
pub struct UnsolvedCell {
    row: usize,
    column: usize,
    block: usize,
    candidates: BTreeSet<SudokuNumber>,
}

impl UnsolvedCell {
    pub fn from_indices(row: usize, column: usize, candidates: BTreeSet<SudokuNumber>) -> Cell {
        board::validate_row_and_column(row, column);
        if candidates.is_empty() {
            panic!("candidates must not be empty.");
        }
        Cell::UnsolvedCell(Self {
            row,
            column,
            block: board::get_block_index(row, column),
            candidates,
        })
    }

    pub fn with_all_candidates(row: usize, column: usize) -> Cell {
        Self::from_indices(row, column, SudokuNumber::iter().collect())
    }

    pub fn block(&self) -> usize {
        self.block
    }

    // After some consideration, I decided to return a reference to the candidates BTreeSet. In total, I considered
    // three options for exposing the set of candidates:
    //
    // 1. Make the candidates field public and allow direct access to the candidates BTreeSet. While this was the
    //    simpliest option, I didn't want to allow callers to mutate candidates. If a caller were to empty the
    //    candidates BTreeSet, then the UnsolvedCell would be in an invalid state.
    // 2. Return an Iterator. This would be consistent with the methods on Board that return Iterators, but it would
    //    make checking if a candidate is in the set less efficient. It is better to call BTreeSet::contains than
    //    Iterator::any.
    // 3. Return a reference to the candidates BTreeSet. This prevents mutation while allowing callers to call
    //    BTreeSet::contains.
    //
    // This process of consideration is probably obvious for experienced Rust users, but it was useful to document my
    // thought process here as I learn Rust. This level of consideration for which type I return here isn't necessary in
    // other languages, so it takes a little getting used to in Rust.
    pub fn candidates(&self) -> &BTreeSet<SudokuNumber> {
        &self.candidates
    }

    pub fn remove_candidate(&mut self, candidate: &SudokuNumber) {
        self.candidates.remove(candidate);
    }

    pub fn is_in_same_unit(&self, other: &UnsolvedCell) -> bool {
        self.row == other.row || self.column == other.column || self.block == other.block
    }

    pub fn vertex_label(&self) -> String {
        format!("[{},{}]", self.row, self.column)
    }
}

impl Location for UnsolvedCell {
    fn row(&self) -> usize {
        self.row
    }

    fn column(&self) -> usize {
        self.column
    }
}

impl Display for UnsolvedCell {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "0")
    }
}

pub type LocatedCandidate<'a> = (&'a UnsolvedCell, SudokuNumber);

impl Board<Cell> {
    pub fn to_simple_string(&self) -> String {
        self.cells().join("")
    }

    pub fn to_string_with_candidates(&self) -> String {
        self.rows()
            .map(|row| {
                row.map(|cell| match cell {
                    Cell::SolvedCell(cell) => cell.value().to_string(),
                    Cell::UnsolvedCell(cell) => {
                        format!("{{{}}}", cell.candidates().iter().join(""))
                    }
                })
                .join("")
            })
            .join("\n")
    }
}

impl From<&Board<Option<SudokuNumber>>> for Board<Cell> {
    fn from(value: &Board<Option<SudokuNumber>>) -> Self {
        value.map_cells_indexed(|row, column, &cell| match cell {
            Some(cell) => SolvedCell::from_indices(row, column, cell),
            None => UnsolvedCell::with_all_candidates(row, column),
        })
    }
}

impl FromStr for Board<Cell> {
    type Err = String;

    fn from_str(s: &str) -> Result<Self, Self::Err> {
        let chars: Vec<_> = s.chars().collect();
        if chars.contains(&'{') || chars.contains(&'}') {
            parse_cells_with_candidates(&chars)
        } else {
            parse_simple_cells(&chars)
        }
    }
}

fn parse_simple_cells(chars: &[char]) -> Result<Board<Cell>, String> {
    if chars.len() != board::UNIT_SIZE_SQUARED {
        return Err(format!(
            "str.chars().count() is {}, must be {}.",
            chars.len(),
            board::UNIT_SIZE_SQUARED
        ));
    }
    let chunks = chars.chunks_exact(board::UNIT_SIZE);
    assert!(chunks.remainder().is_empty());
    let rows = chunks
        .enumerate()
        .map(|(row_index, row)| {
            row.iter()
                .enumerate()
                .map(|(column_index, cell)| match cell {
                    '0' => Ok(UnsolvedCell::with_all_candidates(row_index, column_index)),
                    _ => cell
                        .try_into()
                        .map(|cell| SolvedCell::from_indices(row_index, column_index, cell)),
                })
                .collect::<Result<Vec<_>, _>>()
                .map(|row| row.try_into().unwrap())
        })
        .collect::<Result<Vec<_>, _>>()?
        .try_into()
        .unwrap();
    Ok(Board::new(rows))
}

fn parse_cells_with_candidates(chars: &[char]) -> Result<Board<Cell>, String> {
    let mut cell_builders: Vec<Box<dyn Fn(usize, usize) -> Cell>> = vec![];
    let mut index = 0;
    while index < chars.len() {
        match chars[index] {
            '{' => {
                index += 1;
                let closing_brace = chars[index..]
                    .iter()
                    .position(|&ch| ch == '}')
                    .map(|position| position + index)
                    .ok_or("Unmatched '{'.")?;
                if closing_brace == index {
                    return Err("Empty \"{}\".".into());
                }
                let chars_in_braces = &chars[index..closing_brace];
                if chars_in_braces.contains(&'{') {
                    return Err("Nested '{'.".into());
                }
                let candidates = chars_in_braces
                    .iter()
                    .map(|ch| ch.try_into())
                    .collect::<Result<BTreeSet<_>, _>>()?;
                cell_builders.push(Box::new(move |row, column| {
                    UnsolvedCell::from_indices(row, column, candidates.clone())
                }));
                index = closing_brace + 1;
            }
            '}' => return Err("Unmatched '}'.".into()),
            ch => {
                let value = ch.try_into()?;
                cell_builders.push(Box::new(move |row, column| {
                    SolvedCell::from_indices(row, column, value)
                }));
                index += 1;
            }
        }
    }
    if cell_builders.len() != board::UNIT_SIZE_SQUARED {
        return Err(format!(
            "Found {} cells, required {}.",
            cell_builders.len(),
            board::UNIT_SIZE_SQUARED
        ));
    }
    let chunks = cell_builders.chunks_exact(board::UNIT_SIZE);
    assert!(chunks.remainder().is_empty());
    let rows = chunks
        .enumerate()
        .map(|(row_index, row)| {
            row.iter()
                .enumerate()
                .map(|(column_index, cell)| cell(row_index, column_index))
                .collect::<Vec<_>>()
                .try_into()
                .unwrap()
        })
        .collect::<Vec<_>>()
        .try_into()
        .unwrap();
    Ok(Board::new(rows))
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
    fn unsolved_cells(self) -> impl Iterator<Item = &'a UnsolvedCell> + Clone;
}

impl<'a, I: Iterator<Item = &'a Cell> + Clone> IteratorCellExt<'a> for I {
    fn solved_cells(self) -> impl Iterator<Item = &'a SolvedCell> {
        self.flat_map(|cell| match cell {
            Cell::SolvedCell(solved_cell) => Some(solved_cell),
            Cell::UnsolvedCell(_) => None,
        })
    }

    fn unsolved_cells(self) -> impl Iterator<Item = &'a UnsolvedCell> + Clone {
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
    #[should_panic(expected = "candidates must not be empty.")]
    fn test_unsolved_cell_candidates_are_empty() {
        UnsolvedCell::from_indices(0, 0, BTreeSet::new());
    }

    #[test]
    fn test_parse_simple_cells_wrong_length() {
        assert_eq!(
            "str.chars().count() is 0, must be 81.",
            "".parse::<Board<Cell>>().unwrap_err()
        );
    }

    #[test]
    fn test_parse_cells_with_candidates_unmatched_opening_brace() {
        assert_eq!("Unmatched '{'.", "{".parse::<Board<Cell>>().unwrap_err());
    }

    #[test]
    fn test_parse_cells_with_candidates_empty_braces() {
        assert_eq!("Empty \"{}\".", "{}".parse::<Board<Cell>>().unwrap_err());
    }

    #[test]
    fn test_parse_cells_with_candidates_nested_brace() {
        assert_eq!("Nested '{'.", "{{}".parse::<Board<Cell>>().unwrap_err());
    }

    #[test]
    fn test_parse_cells_with_candidates_invalid_character_in_braces() {
        assert_eq!(
            "char is 'a', must be between '1' and '9'.",
            "{a}".parse::<Board<Cell>>().unwrap_err()
        );
    }

    #[test]
    fn test_parse_cells_with_candidates_unmatched_closing_brace() {
        assert_eq!("Unmatched '}'.", "}".parse::<Board<Cell>>().unwrap_err());
    }

    #[test]
    fn test_parse_cells_with_candidates_invalid_character() {
        assert_eq!(
            "char is 'a', must be between '1' and '9'.",
            "a{".parse::<Board<Cell>>().unwrap_err()
        );
    }

    #[test]
    fn test_parse_cells_with_candidates_wrong_length() {
        assert_eq!(
            "Found 1 cells, required 81.",
            "{1}".parse::<Board<Cell>>().unwrap_err()
        );
    }
}
