mod board;
mod board_modification;
mod cell;
mod collections;
mod logic;
mod sudoku_number;

use board::Board;
use board_modification::BoardModification;
use cell::{Cell, IteratorCellExt, SolvedCell};
use indoc::formatdoc;
use logic::{
    brute_force::{self, BruteForceError},
    simple::{hidden_singles, naked_pairs, naked_singles, naked_triples, prune_candidates},
};
use std::env;
use sudoku_number::SudokuNumber;

// TODO: Consider using clap.
// TODO: Does it make sense for main to return Result?
fn main() {
    let args: Vec<_> = env::args().collect();
    if args.len() != 2 {
        println!("usage: sudoku_solver_rust board");
    } else {
        let board = &args[1];
        if board.len() != board::UNIT_SIZE_SQUARED
            || board.chars().any(|cell| !('0'..='9').contains(&cell))
        {
            println!(
                "board must be {} numbers with blanks expressed as 0",
                board::UNIT_SIZE_SQUARED
            );
        } else {
            match solve(sudoku_number::parse_optional_board(board)) {
                Ok(solution) => println!("{solution}"),
                Err(SolverError::NoSolutions) => println!("No Solutions"),
                Err(SolverError::MultipleSolutions) => println!("Multiple Solutions"),
                Err(SolverError::UnableToSolve(message)) => println!("{message}"),
            }
        }
    }
}

#[derive(Debug, PartialEq)]
enum SolverError {
    NoSolutions,
    MultipleSolutions,
    UnableToSolve(String),
}

impl SolverError {
    fn new_unable_to_solve(board: &Board<Cell>) -> SolverError {
        SolverError::UnableToSolve(formatdoc! {"
            Unable to solve:
            {board}

            Simple String: {}

            With Candidates:
            {}", board.to_simple_string(), board.to_string_with_candidates()})
    }
}

fn solve(input: Board<Option<SudokuNumber>>) -> Result<Board<SudokuNumber>, SolverError> {
    // TODO: Can we use '?' for NoSolutions and MultipleSolutions?
    match brute_force::brute_force(&input) {
        Ok(brute_force_solution) => {
            let mut board = cell::create_cell_board(&input);
            loop {
                if board.cells().unsolved_cells().next().is_none() {
                    return Ok(brute_force_solution);
                }
                let modifications = perform_next_solution(&board);
                if modifications.is_empty() {
                    return Err(SolverError::new_unable_to_solve(&board));
                }
                for modification in modifications {
                    let row = modification.row();
                    let column = modification.column();
                    let Cell::UnsolvedCell(ref mut cell) = board[(row, column)] else {
                        panic!("[{row}, {column}] is already solved.");
                    };
                    let known_solution = brute_force_solution[(row, column)];
                    match modification {
                        BoardModification::RemoveCandidates(modification) => {
                            for &candidate in modification.candidates() {
                                if candidate == known_solution {
                                    panic!("Cannot remove candidate {candidate} from [{row}, {column}]");
                                }
                                if !cell.candidates().contains(&candidate) {
                                    panic!("{candidate} is not a candidate of [{row}, {column}]");
                                }
                                cell.remove_candidate(&candidate);
                            }
                        }
                        BoardModification::SetValue(modification) => {
                            let value = modification.value();
                            if value != known_solution {
                                panic!("Cannot set value {value} to [{row}, {column}]. Solution is {known_solution}");
                            }
                            board[(row, column)] = SolvedCell::new(row, column, value);
                        }
                    }
                }
            }
        }
        Err(BruteForceError::NoSolutions) => return Err(SolverError::NoSolutions),
        Err(BruteForceError::MultipleSolutions) => return Err(SolverError::MultipleSolutions),
    }
}

fn perform_next_solution(board: &Board<Cell>) -> Vec<BoardModification> {
    let solutions = vec![
        prune_candidates::prune_candidates,
        naked_singles::naked_singles,
        hidden_singles::hidden_singles,
        naked_pairs::naked_pairs,
        naked_triples::naked_triples,
    ];
    solutions
        .iter()
        .map(|solution| solution(board))
        .find(|modifications| !modifications.is_empty())
        .unwrap_or_default()
}

#[cfg(test)]
mod tests {
    use super::*;
    use indoc::indoc;

    #[test]
    fn test_solution() {
        let board =
            "010040560230615080000800100050020008600781005900060020006008000080473056045090010";
        let expected = indoc! {"
            8 1 7 | 9 4 2 | 5 6 3
            2 3 4 | 6 1 5 | 7 8 9
            5 6 9 | 8 3 7 | 1 4 2
            ------+-------+------
            4 5 1 | 3 2 9 | 6 7 8
            6 2 3 | 7 8 1 | 4 9 5
            9 7 8 | 5 6 4 | 3 2 1
            ------+-------+------
            7 9 6 | 1 5 8 | 2 3 4
            1 8 2 | 4 7 3 | 9 5 6
            3 4 5 | 2 9 6 | 8 1 7"};
        assert_eq!(
            expected,
            solve(sudoku_number::parse_optional_board(board))
                .unwrap()
                .to_string()
        );
    }

    // TODO: Uncomment after implementing Alternating Inference Chains.
    // #[test]
    // fn test_unable_to_solve() {
    //     let board =
    //         "004007830000050470720030695080700300649513728007008010470080060016040007005276100";
    //     let expected = indoc! {"
    //         Unable to solve:
    //         0 0 4 | 0 0 7 | 8 3 0
    //         0 0 0 | 0 5 0 | 4 7 0
    //         7 2 0 | 0 3 0 | 6 9 5
    //         ------+-------+------
    //         0 8 0 | 7 0 0 | 3 0 0
    //         6 4 9 | 5 1 3 | 7 2 8
    //         0 0 7 | 0 0 8 | 0 1 0
    //         ------+-------+------
    //         4 7 0 | 0 8 0 | 0 6 0
    //         0 1 6 | 0 4 0 | 0 0 7
    //         0 0 5 | 2 7 6 | 1 0 0

    //         Simple String: 004007830000050470720030695080700300649513728007008010470080060016040007005276100

    //         With Candidates:
    //         {159}{569}4{169}{269}783{12}
    //         {139}{36}{38}{689}5{129}47{12}
    //         72{18}{148}3{14}695
    //         {125}8{12}7{269}{249}3{45}{469}
    //         649513728
    //         {235}{35}7{46}{269}8{59}1{469}
    //         47{23}{139}8{159}{259}6{39}
    //         {2389}16{39}4{59}{25}{58}7
    //         {38}{39}52761{48}{349}"};
    //     assert_eq!(
    //         SolverError::UnableToSolve(String::from(expected)),
    //         solve(sudoku_number::parse_optional_board(board)).unwrap_err()
    //     );
    // }

    #[test]
    fn test_no_solutions() {
        let board =
            "710040560230615080000800100050020008600781005900060020006008000080473056045090010";
        assert_eq!(
            SolverError::NoSolutions,
            solve(sudoku_number::parse_optional_board(board)).unwrap_err()
        );
    }

    #[test]
    fn test_multiple_solutions() {
        let board =
            "000000560230615080000800100050020008600781005900060020006008000080473056045090010";
        assert_eq!(
            SolverError::MultipleSolutions,
            solve(sudoku_number::parse_optional_board(board)).unwrap_err()
        );
    }
}
