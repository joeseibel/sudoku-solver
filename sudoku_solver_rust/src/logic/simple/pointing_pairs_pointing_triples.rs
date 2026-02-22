use crate::{
    board::Board,
    board_modification::{BoardModification, IteratorRemoveCandidatesExt},
    cell::{Cell, IteratorCellExt, LocatedCandidate, Location, UnsolvedCell},
    sudoku_number::SudokuNumber,
};
use std::collections::HashSet;
use strum::IntoEnumIterator;

// http://www.sudokuwiki.org/Intersection_Removal#IR
//
// For a given block, if a candidate appears in only one row, then the candidate for that row must be placed in that
// block. The candidate can be removed from cells which are in the same row, but different blocks.
//
// For a given block, if a candidate appears in only one column, then the candidate for that column must be placed in
// that block. The candidate can be removed from cells which are in the same column, but different blocks.
pub fn pointing_pairs_pointing_triples(board: &Board<Cell>) -> Vec<BoardModification> {
    board
        .blocks()
        .flat_map(|block| {
            let mut block = block.peekable();
            let block_index = block.peek().unwrap().block();
            let unsolved: Vec<_> = block.unsolved_cells().collect();
            SudokuNumber::iter().flat_map(move |candidate| {
                let with_candidate: Vec<_> = unsolved
                    .iter()
                    .copied()
                    .filter(|cell| cell.candidates().contains(&candidate))
                    .collect();

                fn pointing_pairs_pointing_triples<'a, T: IteratorCellExt<'a>>(
                    block_index: usize,
                    candidate: SudokuNumber,
                    with_candidate: &[&UnsolvedCell],
                    get_unit: impl FnOnce(usize) -> T,
                    get_unit_index: impl Fn(&UnsolvedCell) -> usize,
                ) -> impl Iterator<Item = LocatedCandidate<'a>> {
                    let unit_indices: HashSet<_> = with_candidate
                        .iter()
                        .map(|cell| get_unit_index(cell))
                        .collect();
                    let removals = if let Some(&unit_index) = unit_indices.iter().next()
                        && unit_indices.len() == 1
                    {
                        let removals = get_unit(unit_index)
                            .unsolved_cells()
                            .filter(move |cell| {
                                cell.block() != block_index
                                    && cell.candidates().contains(&candidate)
                            })
                            .map(move |cell| (cell, candidate));
                        Some(removals)
                    } else {
                        None
                    };
                    removals.into_iter().flatten()
                }

                let row_modifications = pointing_pairs_pointing_triples(
                    block_index,
                    candidate,
                    &with_candidate,
                    |index| board.get_row(index),
                    Location::row,
                );
                let column_modifications = pointing_pairs_pointing_triples(
                    block_index,
                    candidate,
                    &with_candidate,
                    |index| board.get_column(index),
                    Location::column,
                );
                row_modifications
                    .chain(column_modifications)
                    .collect::<Vec<_>>()
            })
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
            {2458}179{245}36{48}{248}\
            {23456}{2345}{36}{1257}8{57}{139}{149}{12349}\
            9{2348}{368}{12}{246}{46}5{148}7\
            {58}72{58}1{69}43{69}\
            {1358}{3589}{389}4{569}2{189}7{1689}\
            {18}6437{89}25{189}\
            7{23489}1{28}{249}{489}{389}65\
            {2468}{2489}{689}{57}3{57}{189}{1489}{1489}\
            {348}{3489}56{49}172{3489}\
        ";
        let expected = [
            remove_candidates!(1, 0, 3),
            remove_candidates!(1, 1, 3),
            remove_candidates!(1, 2, 3),
            remove_candidates!(2, 2, 6),
            remove_candidates!(4, 4, 9),
            remove_candidates!(4, 6, 9),
            remove_candidates!(4, 8, 9),
            remove_candidates!(6, 1, 2, 8),
            remove_candidates!(6, 6, 8),
        ];
        assertions::assert_logical_solution(&expected, board, pointing_pairs_pointing_triples);
    }

    #[test]
    fn test_2() {
        let board = "\
            {789}32{478}{4578}61{4589}{78}\
            41{5689}{2378}{3578}{2357}{23679}{23589}{23678}\
            {678}{78}{568}9{34578}1{23467}{23458}{23678}\
            5{278}{18}{16}9{37}{236}{238}4\
            {289}6{489}{348}{3458}{345}{239}71\
            3{4789}{1489}{16}2{47}{69}{89}5\
            {1269}{249}{13469}5{13467}8{2347}{234}{237}\
            {268}{248}{3468}{2347}{3467}{2347}519\
            {12}57{234}{134}986{23}\
        ";
        let expected = [
            remove_candidates!(0, 7, 8),
            remove_candidates!(1, 5, 7),
            remove_candidates!(1, 6, 2, 6),
            remove_candidates!(1, 7, 2, 8),
            remove_candidates!(1, 8, 2),
            remove_candidates!(2, 1, 7),
            remove_candidates!(2, 6, 6),
            remove_candidates!(2, 7, 8),
            remove_candidates!(4, 0, 8),
            remove_candidates!(4, 2, 8),
            remove_candidates!(6, 1, 4),
            remove_candidates!(6, 2, 1, 4),
            remove_candidates!(6, 4, 4, 7),
            remove_candidates!(7, 5, 7),
        ];
        assertions::assert_logical_solution(&expected, board, pointing_pairs_pointing_triples);
    }

    #[test]
    fn test_3() {
        let board = "\
            93{147}{47}5{18}{24678}{1246}{1267}\
            2{147}{147}63{18}{478}95\
            856{479}{479}2{347}{134}{137}\
            {46}{29}318{469}57{26}\
            {1467}{1467}5{347}2{3467}98{136}\
            {1467}8{29}{3479}{479}5{2346}{12346}{1236}\
            {3467}{2467}{247}8{47}{347}159\
            5{679}821{379}{367}{36}4\
            {1347}{12479}{12479}56{3479}{237}{23}8\
        ";
        let expected = [
            remove_candidates!(0, 6, 7),
            remove_candidates!(1, 6, 7),
            remove_candidates!(2, 6, 7),
            remove_candidates!(3, 5, 9),
            remove_candidates!(4, 5, 3),
            remove_candidates!(5, 0, 4),
            remove_candidates!(5, 3, 4),
            remove_candidates!(5, 4, 4),
            remove_candidates!(7, 1, 6),
            remove_candidates!(8, 1, 2),
            remove_candidates!(8, 2, 2),
        ];
        assertions::assert_logical_solution(&expected, board, pointing_pairs_pointing_triples);
    }
}
