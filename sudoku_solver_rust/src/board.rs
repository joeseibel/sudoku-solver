use std::fmt::Debug;

const UNIT_SIZE_SQUARE_ROOT: usize = 3;
pub const UNIT_SIZE: usize = UNIT_SIZE_SQUARE_ROOT * UNIT_SIZE_SQUARE_ROOT;
pub const UNIT_SIZE_SQUARED: usize = UNIT_SIZE * UNIT_SIZE;

// TODO: Remove Debug trait after removing println! statements from main.
#[derive(Debug)]
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
    pub fn rows(&self) -> impl Iterator<Item = impl Iterator<Item = &T>> {
        self.rows.iter().map(|row| row.iter())
    }

    pub fn columns(&self) -> impl Iterator<Item = impl Iterator<Item = &T>> {
        (0..UNIT_SIZE).map(|index| self.rows.iter().map(move |row| &row[index]))
    }

    pub fn blocks(&self) -> impl Iterator<Item = impl Iterator<Item = &T>> {
        (0..UNIT_SIZE).map(|index| self.get_block(index))
    }

    pub fn cells(&self) -> impl Iterator<Item = &T> {
        self.rows.as_flattened().iter()
    }

    pub fn get_block(&self, block_index: usize) -> impl Iterator<Item = &T> {
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
        const EXPECT_MESSAGE: &str =
            "This should not happen because the original board already has a valid size.";

        let rows = self.rows().map(|row| {
            row.map(&mut f)
                .collect::<Vec<_>>()
                .try_into()
                .expect(EXPECT_MESSAGE)
        });
        Board::new(rows.collect::<Vec<_>>().try_into().expect(EXPECT_MESSAGE))
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
}
