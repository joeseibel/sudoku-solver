use crate::board::Board;

// TODO: Remove Debug trait after removing println! statements from main.
#[derive(Debug)]
pub enum SudokuNumber {
    One,
    Two,
    Three,
    Four,
    Five,
    Six,
    Seven,
    Eight,
    Nine,
}

impl SudokuNumber {
    // I considered using From or TryFrom for this conversion, but neither seemed to be a good fit.
    //
    // From is not appropriate because this conversion can fail. It is only valid for the characters '1' through '9' and
    // panics otherwise.
    //
    // TryFrom almost fits, but not quite. When using TryFrom, the return type is a Result, so returning an error is
    // more appropriate than panicing. In the case of this conversion, panic is a better fit than returning Result since
    // a value outside of '1' through '9' is considered to be a programmer error. The code that calls this conversion
    // should ensure that only valid values are passed in. If a Result were to be returned, there is no reasonable way
    // to recover other than for the callers to panic themselves. Therefore, TryFrom doesn't fit because it really makes
    // sense to panic here and a return value of Result doesn't make much sense.
    //
    // TODO: Reconsider TryFrom.
    pub fn from_digit(ch: char) -> Self {
        match ch {
            '1' => Self::One,
            '2' => Self::Two,
            '3' => Self::Three,
            '4' => Self::Four,
            '5' => Self::Five,
            '6' => Self::Six,
            '7' => Self::Seven,
            '8' => Self::Eight,
            '9' => Self::Nine,
            _ => panic!("ch is '{ch}', must be between '1' and '9'."),
        }
    }
}

// TODO: Refactor after reading about Iterators and Closures.
// TODO: Consider implementing TryFrom. Also look at FromStr.
pub fn parse_optional_board(board: &str) -> Board<Option<SudokuNumber>> {
    use crate::board::{UNIT_SIZE, UNIT_SIZE_SQUARED};

    const EXPECT_MESSAGE: &str = "This should not happen because the size is already checked.";

    let chars: Vec<_> = board.chars().collect();
    if chars.len() != UNIT_SIZE_SQUARED {
        panic!(
            "board.chars().count() is {}, must be {UNIT_SIZE_SQUARED}",
            chars.len()
        );
    }
    let mut board_as_numbers = Vec::new();
    for row in 0..UNIT_SIZE {
        let mut row_as_numbers = Vec::new();
        for column in 0..UNIT_SIZE {
            let number = match chars[row * UNIT_SIZE + column] {
                '0' => None,
                ch => Some(SudokuNumber::from_digit(ch)),
            };
            row_as_numbers.push(number);
        }
        board_as_numbers.push(row_as_numbers.try_into().expect(EXPECT_MESSAGE));
    }
    Board::new(board_as_numbers.try_into().expect(EXPECT_MESSAGE))
}
