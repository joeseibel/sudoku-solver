mod board;
mod sudoku_number;

use board::Board;
use sudoku_number::SudokuNumber;

fn main() {
    println!("Hello, Sudoku Solver!");
    println!("{:?}", SudokuNumber::One);
    println!("{:?}", SudokuNumber::Two);
    println!("{:?}", SudokuNumber::Three);
    println!("{:?}", SudokuNumber::Four);
    println!("{:?}", SudokuNumber::Five);
    println!("{:?}", SudokuNumber::Six);
    println!("{:?}", SudokuNumber::Seven);
    println!("{:?}", SudokuNumber::Eight);
    println!("{:?}", SudokuNumber::Nine);
    println!("{:?}", SudokuNumber::from_digit('1'));
    println!("{:?}", SudokuNumber::from_digit('2'));
    println!("{:?}", SudokuNumber::from_digit('3'));
    println!("{:?}", SudokuNumber::from_digit('4'));
    println!("{:?}", SudokuNumber::from_digit('5'));
    println!("{:?}", SudokuNumber::from_digit('6'));
    println!("{:?}", SudokuNumber::from_digit('7'));
    println!("{:?}", SudokuNumber::from_digit('8'));
    println!("{:?}", SudokuNumber::from_digit('9'));

    let row = [0; 9];
    let rows = [row; 9];
    let board = Board::new(rows);
    println!("{:?}", board);

    let board = "004007830000050470720030695080700300649513728007008010470080060016040007005276100";
    let board = sudoku_number::parse_optional_board(board);
    println!("{:?}", board);
}
