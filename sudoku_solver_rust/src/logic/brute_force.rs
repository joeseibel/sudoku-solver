use crate::{
    board::{self, Board},
    sudoku_number::SudokuNumber,
};
use std::collections::HashSet;
use strum::IntoEnumIterator;

#[derive(Debug, PartialEq)]
pub enum BruteForceError {
    NoSolutions,
    MultipleSolutions,
}

// Recursively tries every number for each unsolved cell looking for a solution.
//
// Motivation for implementing a brute force solution:
//
// The purpose of this solver is to go through the exercise of implementing various logical solutions. Why implement
// brute force if I only care about logical solutions? The first reason is to check the correctness of the logical
// solutions. When solving a board, the first thing that is done is to get the brute force solution. After that, any
// logical modifications will be checked against the brute force solution. If a logical solution tries to set an
// incorrect value to a cell or remove a candidate from a cell which is the known solution, then the solver will panic.
//
// The second reason for implementing brute force is to check for the number of solutions for a board before trying the
// logical solutions. If a board cannot be solved or if it has multiple solutions, then I don't bother with the logical
// solutions. The logical solutions are written assuming that they are operating on a board with only one solution.
pub fn brute_force(
    board: &Board<Option<SudokuNumber>>,
) -> Result<Board<SudokuNumber>, BruteForceError> {
    if board.cells().all(Option::is_some) {
        let filled_board = board.map_cells(|cell| cell.unwrap());
        return if is_solved(&filled_board) {
            Ok(filled_board)
        } else {
            Err(BruteForceError::NoSolutions)
        };
    }

    let mut trial_and_error = board.clone();

    fn brute_force(
        trial_and_error: &mut Board<Option<SudokuNumber>>,
        row_index: usize,
        column_index: usize,
    ) -> Result<Board<SudokuNumber>, BruteForceError> {
        fn move_to_next_cell(
            trial_and_error: &mut Board<Option<SudokuNumber>>,
            row_index: usize,
            column_index: usize,
        ) -> Result<Board<SudokuNumber>, BruteForceError> {
            if column_index + 1 >= board::UNIT_SIZE {
                brute_force(trial_and_error, row_index + 1, 0)
            } else {
                brute_force(trial_and_error, row_index, column_index + 1)
            }
        }

        if row_index >= board::UNIT_SIZE {
            Ok(trial_and_error.map_cells(|cell| cell.unwrap()))
        } else if trial_and_error[(row_index, column_index)].is_some() {
            move_to_next_cell(trial_and_error, row_index, column_index)
        } else {
            let row_invalid = trial_and_error.get_row(row_index);
            let column_invalid = trial_and_error.get_column(column_index);
            let block_invalid =
                trial_and_error.get_block(board::get_block_index(row_index, column_index));
            let invalid: HashSet<_> = row_invalid
                .chain(column_invalid)
                .chain(block_invalid)
                .flatten()
                .copied()
                .collect();
            let valid = SudokuNumber::iter().filter(|number| !invalid.contains(number));
            let mut single_solution = None;
            for guess in valid {
                trial_and_error[(row_index, column_index)] = Some(guess);
                match move_to_next_cell(trial_and_error, row_index, column_index) {
                    Ok(intermediate_solution) if single_solution.is_none() => {
                        single_solution = Some(intermediate_solution)
                    }
                    Ok(_) | Err(BruteForceError::MultipleSolutions) => {
                        return Err(BruteForceError::MultipleSolutions)
                    }
                    Err(BruteForceError::NoSolutions) => (),
                }
            }
            trial_and_error[(row_index, column_index)] = None;
            match single_solution {
                Some(single_solution) => Ok(single_solution),
                None => Err(BruteForceError::NoSolutions),
            }
        }
    }

    brute_force(&mut trial_and_error, 0, 0)
}

fn is_solved(board: &Board<SudokuNumber>) -> bool {
    let rows_solved = board
        .rows()
        .all(|row| row.collect::<HashSet<_>>().len() == board::UNIT_SIZE);
    let columns_solved = board
        .columns()
        .all(|column| column.collect::<HashSet<_>>().len() == board::UNIT_SIZE);
    let blocks_solved = board
        .blocks()
        .all(|block| block.collect::<HashSet<_>>().len() == board::UNIT_SIZE);

    rows_solved && columns_solved && blocks_solved
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::sudoku_number::{parse_board, parse_optional_board};

    #[test]
    fn test_brute_force_single_solution() {
        let board =
            "010040560230615080000800100050020008600781005900060020006008000080473056045090010";
        let expected =
            "817942563234615789569837142451329678623781495978564321796158234182473956345296817";
        assert_eq!(
            Ok(parse_board(expected)),
            brute_force(&parse_optional_board(board))
        );
    }

    #[test]
    fn test_brute_force_no_solutions() {
        let board =
            "710040560230615080000800100050020008600781005900060020006008000080473056045090010";
        assert_eq!(
            Err(BruteForceError::NoSolutions),
            brute_force(&parse_optional_board(board))
        );
    }

    #[test]
    fn test_brute_force_multiple_solutions() {
        let board =
            "000000560230615080000800100050020008600781005900060020006008000080473056045090010";
        assert_eq!(
            Err(BruteForceError::MultipleSolutions),
            brute_force(&parse_optional_board(board))
        );
    }

    #[test]
    fn test_brute_force_already_solved() {
        let board =
            "817942563234615789569837142451329678623781495978564321796158234182473956345296817";
        assert_eq!(
            Ok(parse_board(board)),
            brute_force(&parse_optional_board(board))
        );
    }

    #[test]
    fn test_brute_force_invalid_solution() {
        let board =
            "817942563234615789569837142451329678623781495978564321796158234182473956345296818";
        assert_eq!(
            Err(BruteForceError::NoSolutions),
            brute_force(&parse_optional_board(board))
        );
    }
}
