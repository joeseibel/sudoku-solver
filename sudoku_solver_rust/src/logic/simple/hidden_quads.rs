use crate::{
    board::Board,
    board_modification::{BoardModification, IteratorRemoveCandidatesExt},
    cell::{Cell, IteratorCellExt},
    collections::IteratorZipExt,
    sudoku_number::SudokuNumber,
};
use std::collections::HashSet;
use strum::VariantArray;

// http://www.sudokuwiki.org/Hidden_Candidates#HQ
//
// If four candidates exist across four cells in a unit, then those four candidates must be placed in those four cells.
// All other candidates can be removed from those four cells.
pub fn hidden_quads(board: &Board<Cell>) -> Vec<BoardModification> {
    board
        .units()
        .flat_map(|unit| {
            SudokuNumber::VARIANTS
                .iter()
                .zip_every_quad()
                .flat_map(move |(a, b, c, d)| {
                    let cells: Vec<_> = unit
                        .clone()
                        .unsolved_cells()
                        .filter(|cell| {
                            cell.candidates().contains(a)
                                || cell.candidates().contains(b)
                                || cell.candidates().contains(c)
                                || cell.candidates().contains(d)
                        })
                        .collect();
                    if cells.len() == 4 {
                        let mut union: HashSet<SudokuNumber> = HashSet::new();
                        for cell in &cells {
                            union.extend(cell.candidates());
                        }
                        if union.contains(a)
                            && union.contains(b)
                            && union.contains(c)
                            && union.contains(d)
                        {
                            Some(cells.into_iter().flat_map(|cell| {
                                cell.candidates()
                                    .difference(&[*a, *b, *c, *d].into())
                                    .copied()
                                    .collect::<Vec<_>>()
                                    .into_iter()
                                    .map(move |candidate| (cell, candidate))
                            }))
                        } else {
                            None
                        }
                    } else {
                        None
                    }
                })
        })
        .flatten()
        .merge_to_remove_candidates()
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::{logic::assertions, remove_candidates};

    #[test]
    fn test_1() {
        let board = "\
            65{139}{13}87{19}24\
            {278}{28}{1378}649{18}5{37}\
            {89}4{378}{13}25{168}{37}{69}\
            57{29}438{29}61\
            {2489}{2689}{468}5{67}1{347}{347}{29}\
            31{46}9{67}2{47}85\
            {247}{26}{457}89{46}{357}1{237}\
            {4789}{689}{578}213{4576}{47}{67}\
            13{246}75{46}{26}98\
        ";
        let expected = [remove_candidates!(7, 6, 6)];
        assertions::assert_logical_solution(&expected, board, hidden_quads);
    }

    #[test]
    fn test_2() {
        let board = "\
            9{37}15{28}{28}{37}46\
            425{367}9{367}{37}81\
            86{37}{347}1{347}{59}2{59}\
            5{3478}2{1346789}{378}{346789}{19}{37}{89}\
            {37}19{2378}{23578}{23578}46{58}\
            6{3478}{3478}{134789}{3578}{345789}{159}{37}2\
            196{78}4{78}253\
            2{345}{34}{39}6{359}817\
            {37}{3578}{378}{23}{235}1694\
        ";
        let expected = [
            remove_candidates!(3, 3, 3, 7, 8),
            remove_candidates!(3, 5, 3, 7, 8),
            remove_candidates!(5, 3, 3, 7, 8),
            remove_candidates!(5, 5, 3, 5, 7, 8),
        ];
        assertions::assert_logical_solution(&expected, board, hidden_quads);
    }
}
