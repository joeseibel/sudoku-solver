use crate::board_modification::IteratorRemoveCandidatesExt;
use crate::cell::IteratorCellExt;
use crate::collections::IteratorZipExtRef;
use crate::{
    board::Board, board_modification::BoardModification, cell::Cell, sudoku_number::SudokuNumber,
};
use strum::VariantArray;

// http://www.sudokuwiki.org/Hidden_Candidates#HP
//
// If a pair of candidates exists in exactly two cells in a unit, then those two candidates must be placed in those two
// cells. All other candidates can be removed from those two cells.
pub fn hidden_pairs(board: &Board<Cell>) -> Vec<BoardModification> {
    board
        .units()
        .flat_map(|unit| {
            SudokuNumber::VARIANTS
                .iter()
                .zip_every_pair()
                .flat_map(move |(a, b)| {
                    let cells_with_a: Vec<_> = unit
                        .clone()
                        .unsolved_cells()
                        .filter(|cell| cell.candidates().contains(a))
                        .collect();
                    let cells_with_b: Vec<_> = unit
                        .clone()
                        .unsolved_cells()
                        .filter(|cell| cell.candidates().contains(b))
                        .collect();
                    if cells_with_a.len() == 2 && cells_with_a == cells_with_b {
                        Some(cells_with_a.into_iter().flat_map(|cell| {
                            cell.candidates()
                                .difference(&[*a, *b].into())
                                .copied()
                                .collect::<Vec<_>>()
                                .into_iter()
                                .map(move |candidate| (cell, candidate))
                        }))
                    } else {
                        None
                    }
                })
                .flatten()
        })
        .merge_to_remove_candidates()
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::{logic::assertions, remove_candidates};

    #[test]
    fn test_1() {
        let board = "\
            {1258}{1238}{23}{129}{12359}{59}{4589}{2345679}{345679}\
            9{1238}46{1235}7{58}{235}{35}\
            {25}768{2359}41{2359}{359}\
            3{246}97{2456}1{45}8{456}\
            7{246}8{29}{24569}{569}3{4569}1\
            {46}513{469}87{469}2\
            {48}{3489}75{89}261{349}\
            {16}{169}54{1679}32{79}8\
            {12468}{1234689}{23}{19}{16789}{69}{459}{34579}{34579}\
        ";
        let expected = [
            remove_candidates!(0, 7, 2, 3, 4, 5, 9),
            remove_candidates!(0, 8, 3, 4, 5, 9),
        ];
        assertions::assert_logical_solution(&expected, board, hidden_pairs);
    }

    #[test]
    fn test_2() {
        let board = "\
            72{56}4{19}8{1569}3{169}\
            {569}8{356}{135}{129}{25}{1569}47\
            4{359}1{35}768{59}2\
            81{2456}739{56}{256}{46}\
            {69}{379}{23467}851{3679}{269}{469}\
            {59}{3579}{357}264{13579}8{19}\
            2{57}968{57}413\
            34{57}{15}{12}{257}{69}{69}8\
            168943275\
        ";
        let expected = [
            remove_candidates!(3, 2, 5, 6),
            remove_candidates!(4, 2, 3, 6, 7),
            remove_candidates!(4, 6, 6, 9),
            remove_candidates!(5, 6, 1, 5, 9),
        ];
        assertions::assert_logical_solution(&expected, board, hidden_pairs);
    }
}
