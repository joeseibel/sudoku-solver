use crate::board::{self, Board};
use std::{fmt, str::FromStr};
use strum_macros::{EnumIter, VariantArray};

#[derive(Clone, Copy, Debug, EnumIter, Eq, Hash, Ord, PartialEq, PartialOrd, VariantArray)]
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

impl TryFrom<char> for SudokuNumber {
    type Error = String;

    fn try_from(value: char) -> Result<Self, Self::Error> {
        match value {
            '1' => Ok(Self::One),
            '2' => Ok(Self::Two),
            '3' => Ok(Self::Three),
            '4' => Ok(Self::Four),
            '5' => Ok(Self::Five),
            '6' => Ok(Self::Six),
            '7' => Ok(Self::Seven),
            '8' => Ok(Self::Eight),
            '9' => Ok(Self::Nine),
            _ => Err(format!("char is '{value}', must be between '1' and '9'.")),
        }
    }
}

impl fmt::Display for SudokuNumber {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(f, "{}", *self as usize + 1)
    }
}

impl FromStr for Board<Option<SudokuNumber>> {
    type Err = String;

    fn from_str(s: &str) -> Result<Self, Self::Err> {
        let chars: Vec<_> = s.chars().collect();
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
            .map(|row| {
                row.iter()
                    .copied()
                    .map(|cell| match cell {
                        '0' => None,
                        _ => Some(cell.try_into()),
                    })
                    .map(Option::transpose)
                    .collect::<Result<Vec<_>, _>>()
                    .map(|row| row.try_into().unwrap())
            })
            .collect::<Result<Vec<_>, _>>()?
            .try_into()
            .unwrap();
        Ok(Self::new(rows))
    }
}

// TODO: Consider implementing TryFrom. Also look at FromStr.
pub fn parse_board(board: &str) -> Board<SudokuNumber> {
    let chars: Vec<_> = board.chars().collect();
    if chars.len() != board::UNIT_SIZE_SQUARED {
        panic!(
            "board.chars().count() is {}, must be {}.",
            chars.len(),
            board::UNIT_SIZE_SQUARED
        );
    }
    let chunks = chars.chunks_exact(board::UNIT_SIZE);
    assert!(chunks.remainder().is_empty());
    let rows = chunks
        .map(|row| {
            row.iter()
                .copied()
                .map(|cell| cell.try_into().unwrap())
                .collect::<Vec<_>>()
                .try_into()
                .unwrap()
        })
        .collect::<Vec<_>>()
        .try_into()
        .unwrap();
    Board::new(rows)
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_try_from_unexpected_char() {
        assert_eq!(
            "char is 'a', must be between '1' and '9'.",
            SudokuNumber::try_from('a').unwrap_err()
        );
    }

    #[test]
    fn test_parse_optional_board_wrong_length() {
        assert_eq!(
            "str.chars().count() is 0, must be 81.",
            "".parse::<Board<Option<SudokuNumber>>>().unwrap_err()
        );
    }

    #[test]
    #[should_panic(expected = "board.chars().count() is 0, must be 81.")]
    fn test_parse_board_wrong_length() {
        parse_board("");
    }
}
