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
}

// TODO: Remove Debug trait after removing println! statements from main.
#[derive(Debug)]
enum SudokuNumber {
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
