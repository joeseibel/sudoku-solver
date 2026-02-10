use crate::board;
use std::{
    fmt::{self, Debug, Display},
    ops::{Index, IndexMut},
};

pub const UNIT_SIZE_SQUARE_ROOT: usize = 3;
pub const UNIT_SIZE: usize = UNIT_SIZE_SQUARE_ROOT * UNIT_SIZE_SQUARE_ROOT;
pub const UNIT_SIZE_SQUARED: usize = UNIT_SIZE * UNIT_SIZE;

#[derive(Clone, Debug, PartialEq)]
pub struct Board<T> {
    rows: [[T; UNIT_SIZE]; UNIT_SIZE],
}

impl<T> Board<T> {
    // Constructing a Board in Rust is simplier that constructing a Board in other languages, since the size of rows and
    // the size of each row doesn't need to be checked. This is one of the advantages of Rust arrays including its size
    // as a part of an array's type. Rust's type system ensures that a Board can only be constructed with a 9x9 grid.
    pub fn new(rows: [[T; UNIT_SIZE]; UNIT_SIZE]) -> Self {
        Board { rows }
    }

    // What should the return type of rows, columns, etc. be? Should these methods return Iterators, arrays, Vecs, or
    // slices? I decided to return Iterators since this seems to be the most idomatic way in Rust. By comparison,
    // str::bytes, str::chars, and str::lines all return Iterators.
    //
    // This approach is different from my other implementations which return actual collections. This difference is most
    // interesting when comparing Rust and Java since Java's Stream and Rust's Iterator serve similar roles. One
    // difference between a Java Stream and a Rust Iterator is that the Rust compiler can ensure that an Iterator is
    // only consumed once. Methods like collect take ownership of the Iterator so that it cannot be used after
    // collecting. This is different in Java in which the compiler can't prevent multiple consumptions of a Stream. Java
    // instead throws an IllegalStateException if a Stream is operated on after closing.
    pub fn rows(&self) -> impl Iterator<Item = impl Iterator<Item = &T> + Clone> + Clone {
        self.rows.iter().map(|row| row.iter())
    }

    pub fn columns(&self) -> impl Iterator<Item = impl Iterator<Item = &T> + Clone> + Clone {
        (0..UNIT_SIZE).map(|index| self.rows.iter().map(move |row| &row[index]))
    }

    pub fn blocks(&self) -> impl Iterator<Item = impl Iterator<Item = &T> + Clone> {
        (0..UNIT_SIZE).map(|index| self.get_block(index))
    }

    pub fn units(&self) -> impl Iterator<Item = impl Iterator<Item = &T> + Clone> {
        // The implementation of this method was a bit of a surprise. I naively thought that I could implement units in
        // a manner similar to the other languages. Originally, I tried doing this:
        //
        //   self.rows().chain(self.columns()).chain(self.blocks())
        //
        // To my surprise, the previous line would not compile. The problem is that when chaining together two
        // Iterators, the Item types of the two Iterators must be the same. In this case, they were not the same, even
        // though they appeared to be. The Item type for rows(), columns(), and blocks() are all
        // impl Iterator<Item = &T>>, so on the surface these look like the same types. However, when compiling, the
        // actual types for each impl Iterator<...> are different. The complier error message explains this by saying,
        // "distinct uses of `impl Trait` result in different opaque types."
        //
        // I solved this by collecting each inner Iterator into a Vec, chaining the outer Iterators, then converting
        // each inner Vec back into an Iterator. At the moment, I can't think of a way of implementing units without
        // converting inner Iterators to Vecs, then back to Iterators.
        let rows = self.rows().map(Iterator::collect);
        let columns = self.columns().map(Iterator::collect);
        let blocks = self.blocks().map(Iterator::collect);
        rows.chain(columns).chain(blocks).map(Vec::into_iter)
    }

    pub fn cells(&self) -> impl Iterator<Item = &T> + Clone {
        self.rows.as_flattened().iter()
    }

    pub fn get_row(&self, row_index: usize) -> impl Iterator<Item = &T> + Clone {
        self.rows[row_index].iter()
    }

    pub fn get_column(&self, column_index: usize) -> impl Iterator<Item = &T> + Clone {
        self.rows()
            .map(move |mut row| row.nth(column_index).unwrap())
    }

    pub fn get_block(&self, block_index: usize) -> impl Iterator<Item = &T> + Clone {
        assert!(
            block_index < UNIT_SIZE,
            "block_index is {block_index}, must be between 0 and {}.",
            UNIT_SIZE - 1
        );
        let row_index = block_index / UNIT_SIZE_SQUARE_ROOT * UNIT_SIZE_SQUARE_ROOT;
        let column_index = block_index % UNIT_SIZE_SQUARE_ROOT * UNIT_SIZE_SQUARE_ROOT;
        self.rows
            .iter()
            .skip(row_index)
            .take(UNIT_SIZE_SQUARE_ROOT)
            .flat_map(move |row| &row[column_index..column_index + UNIT_SIZE_SQUARE_ROOT])
    }

    pub fn map_cells<B: Debug>(&self, mut f: impl FnMut(&T) -> B) -> Board<B> {
        let rows = self
            .rows()
            .map(|row| row.map(&mut f).collect::<Vec<_>>().try_into().unwrap());
        Board::new(rows.collect::<Vec<_>>().try_into().unwrap())
    }

    pub fn map_cells_indexed<B: Debug>(
        &self,
        mut f: impl FnMut(usize, usize, &T) -> B,
    ) -> Board<B> {
        let rows = self.rows().enumerate().map(|(row_index, row)| {
            row.enumerate()
                .map(|(column_index, cell)| f(row_index, column_index, cell))
                .collect::<Vec<_>>()
                .try_into()
                .unwrap()
        });
        Board::new(rows.collect::<Vec<_>>().try_into().unwrap())
    }
}

impl<T> Index<(usize, usize)> for Board<T> {
    type Output = T;

    fn index(&self, index: (usize, usize)) -> &Self::Output {
        let (row_index, column_index) = index;
        &self.rows[row_index][column_index]
    }
}

impl<T> IndexMut<(usize, usize)> for Board<T> {
    fn index_mut(&mut self, index: (usize, usize)) -> &mut Self::Output {
        let (row_index, column_index) = index;
        &mut self.rows[row_index][column_index]
    }
}

impl<T: Display> Display for Board<T> {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        for (row_index, row) in self.rows().enumerate() {
            for (column_index, cell) in row.enumerate() {
                write!(f, "{cell}")?;
                if column_index < board::UNIT_SIZE - 1 {
                    write!(f, " ")?;
                    if (column_index + 1) % board::UNIT_SIZE_SQUARE_ROOT == 0 {
                        write!(f, "| ")?;
                    }
                }
            }
            if row_index < board::UNIT_SIZE - 1 {
                writeln!(f)?;
                if (row_index + 1) % board::UNIT_SIZE_SQUARE_ROOT == 0 {
                    writeln!(f, "------+-------+------")?;
                }
            }
        }
        Ok(())
    }
}

pub fn get_block_index(row_index: usize, column_index: usize) -> usize {
    row_index / UNIT_SIZE_SQUARE_ROOT * UNIT_SIZE_SQUARE_ROOT + column_index / UNIT_SIZE_SQUARE_ROOT
}

pub fn validate_row_and_column(row: usize, column: usize) {
    assert!(
        row < UNIT_SIZE,
        "row is {row}, must be between 0 and {}.",
        UNIT_SIZE - 1
    );
    assert!(
        column < UNIT_SIZE,
        "column is {column}, must be between 0 and {}.",
        UNIT_SIZE - 1
    );
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::{
        board_modification::{RemoveCandidates, SetValue},
        cell::{SolvedCell, UnsolvedCell},
        sudoku_number::SudokuNumber,
    };
    use std::collections::BTreeSet;

    #[test]
    #[should_panic(expected = "block_index is 9, must be between 0 and 8.")]
    fn test_board_get_block_index_too_high() {
        _ = Board::new([[0; UNIT_SIZE]; UNIT_SIZE]).get_block(9);
    }

    #[test]
    #[should_panic(expected = "row is 9, must be between 0 and 8.")]
    fn test_remove_candidates_new_row_too_high() {
        let mut candidates = BTreeSet::new();
        candidates.insert(SudokuNumber::One);
        RemoveCandidates::from_indices(9, 0, candidates);
    }

    #[test]
    #[should_panic(expected = "row is 9, must be between 0 and 8.")]
    fn test_set_value_new_row_too_high() {
        SetValue::from_indices(9, 0, 1);
    }

    #[test]
    #[should_panic(expected = "column is 9, must be between 0 and 8.")]
    fn test_solved_cell_new_column_too_high() {
        SolvedCell::from_indices(0, 9, SudokuNumber::One);
    }

    #[test]
    #[should_panic(expected = "column is 9, must be between 0 and 8.")]
    fn test_unsolved_cell_new_column_too_high() {
        let mut candidates = BTreeSet::new();
        candidates.insert(SudokuNumber::One);
        UnsolvedCell::from_indices(0, 9, candidates);
    }

    #[test]
    #[should_panic(expected = "column is 9, must be between 0 and 8.")]
    fn test_unsolved_cell_with_all_candidates_column_too_high() {
        UnsolvedCell::with_all_candidates(0, 9);
    }
}
