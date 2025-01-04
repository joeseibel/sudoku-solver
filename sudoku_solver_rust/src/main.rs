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
    println!();

    let rows: Vec<Vec<_>> = board.rows().map(Iterator::collect).collect();
    println!("rows:");
    println!("{rows:?}");
    println!();

    let columns: Vec<Vec<_>> = board.columns().map(Iterator::collect).collect();
    println!("columns:");
    println!("{columns:?}");
    println!();

    let first_block: Vec<_> = board.get_block(0).collect();
    println!("first block:");
    println!("{first_block:?}");
    println!();

    let blocks: Vec<Vec<_>> = board.blocks().map(Iterator::collect).collect();
    println!("blocks:");
    println!("{blocks:?}");
}
