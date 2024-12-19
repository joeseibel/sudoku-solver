const UNIT_SIZE_SQUARE_ROOT: usize = 3;
const UNIT_SIZE: usize = UNIT_SIZE_SQUARE_ROOT * UNIT_SIZE_SQUARE_ROOT;

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
}
