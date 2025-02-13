use std::{
    fmt::Debug,
    ops::{Index, IndexMut},
};

const UNIT_SIZE_SQUARE_ROOT: usize = 3;
pub const UNIT_SIZE: usize = UNIT_SIZE_SQUARE_ROOT * UNIT_SIZE_SQUARE_ROOT;
pub const UNIT_SIZE_SQUARED: usize = UNIT_SIZE * UNIT_SIZE;

// TODO: Remove Debug trait after removing println! statements from main.
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
    pub fn rows(&self) -> impl Iterator<Item = impl Iterator<Item = &T>> + Clone {
        self.rows.iter().map(|row| row.iter())
    }

    pub fn columns(&self) -> impl Iterator<Item = impl Iterator<Item = &T>> {
        (0..UNIT_SIZE).map(|index| self.rows.iter().map(move |row| &row[index]))
    }

    pub fn blocks(&self) -> impl Iterator<Item = impl Iterator<Item = &T>> {
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
        if block_index >= UNIT_SIZE {
            panic!(
                "block_index is {block_index}, must be between 0 and {}.",
                UNIT_SIZE - 1
            );
        }
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

pub fn get_block_index(row_index: usize, column_index: usize) -> usize {
    row_index / UNIT_SIZE_SQUARE_ROOT * UNIT_SIZE_SQUARE_ROOT + column_index / UNIT_SIZE_SQUARE_ROOT
}

pub fn validate_row_and_column(row: usize, column: usize) {
    if row >= UNIT_SIZE {
        panic!("row is {row}, must be between 0 and {}.", UNIT_SIZE - 1);
    }
    if column >= UNIT_SIZE {
        panic!(
            "column is {column}, must be between 0 and {}.",
            UNIT_SIZE - 1
        );
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    #[should_panic(expected = "block_index is 9, must be between 0 and 8.")]
    fn test_board_get_block_index_too_high() {
        _ = Board::new([[0; UNIT_SIZE]; UNIT_SIZE]).get_block(9);
    }

    //TODO: Test validate_row_and_column after creating Cell and BoardModification.
}
