use crate::{
    board::Board,
    board_modification::{BoardModification, SetValue},
    cell::{Cell, IteratorCellExt},
};

// http://www.sudokuwiki.org/Getting_Started
//
// If an unsolved cell has exactly one candidate, then the candidate must be placed in that cell.
pub fn naked_singles(board: &Board<Cell>) -> Vec<BoardModification> {
    board
        .cells()
        .unsolved_cells()
        .filter(|cell| cell.candidates().len() == 1)
        .map(|cell| SetValue::from_cell(cell, *cell.candidates().iter().next().unwrap()))
        .collect()
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::logic::assertions;

    #[test]
    fn test() {
        let board = "\
            {2367}{379}{29}1{3468}5{2389}{9}{289}\
            14{259}{389}{38}{38}67{289}\
            {3567}8{59}{3679}{36}24{59}{19}\
            {2458}63{58}7{48}{89}1{489}\
            9{57}{2458}{568}{124568}{1468}{78}{46}3\
            {478}1{48}{368}9{3468}52{4678}\
            {345}{359}72{1356}{136}{19}8{1469}\
            {48}26{78}{18}{178}{179}35\
            {358}{35}{158}4{13568}9{127}{6}{1267}\
        ";
        let expected = [SetValue::new(0, 7, 9), SetValue::new(8, 7, 6)];
        assertions::assert_logical_solution(&expected, board, naked_singles);
    }
}
