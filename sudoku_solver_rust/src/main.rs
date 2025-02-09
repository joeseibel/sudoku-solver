mod board;
mod board_modification;
mod cell;
mod collections;
mod logic;
mod sudoku_number;

use board::Board;
use logic::brute_force;
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
    println!();

    let cells: Vec<_> = board.cells().collect();
    println!("cells:");
    println!("{cells:?}");
    println!();

    let board = "817942563234615789569837142451329678623781495978564321796158234182473956345296817";
    let board = sudoku_number::parse_optional_board(board);
    let mut mapped = board.map_cells(|cell| cell.unwrap());
    println!("mapped:");
    println!("{mapped:?}");
    println!();

    let row: Vec<_> = mapped.get_row(5).collect();
    println!("row:");
    println!("{row:?}");
    println!();

    let column: Vec<_> = mapped.get_column(3).collect();
    println!("column:");
    println!("{column:?}");
    println!();

    println!("Block index: {}", board::get_block_index(8, 3));
    println!();

    println!("Individual cell: {:?}", mapped[(3, 7)]);
    mapped[(3, 7)] = SudokuNumber::One;
    println!("After setting: {:?}", mapped[(3, 7)]);
    println!();

    let brute_force_solution = brute_force::brute_force(&board);
    println!("brute force: ");
    println!("{brute_force_solution:?}");
    println!();

    let simple =
        "000105000140000670080002400063070010900000003010090520007200080026000035000409000";
    let simple = cell::parse_simple_cells(simple);
    println!("simple: ");
    println!("{simple:?}");
}
