use crate::{
    board::Board,
    board_modification::{BoardModification, IteratorRemoveCandidatesExt},
    cell::{Cell, IteratorCellExt, LocatedCandidate, Location, UnsolvedCell},
    sudoku_number::SudokuNumber,
};
use std::collections::HashSet;
use strum::IntoEnumIterator;

// http://www.sudokuwiki.org/Intersection_Removal#LBR
//
// For a given row, if a candidate appears in only one block, then the candidate for that block must be placed in that
// row. The candidate can be removed from the cells which are in the same block, but different rows.
//
// For a given column, if a candidate appears in only one block, then the candidate for that block must be placed in
// that column. The candidate can be removed from cells which are in the same block, but different columns.
pub fn box_line_reduction(board: &Board<Cell>) -> Vec<BoardModification> {
    SudokuNumber::iter()
        .flat_map(|candidate| {
            fn box_line_reduction<'a>(
                board: &Board<Cell>,
                candidate: SudokuNumber,
                units: impl Iterator<Item = impl Iterator<Item = &'a Cell> + Clone>,
                get_unit_index: impl Fn(&dyn Location) -> usize,
            ) -> impl Iterator<Item = LocatedCandidate<'_>> {
                units
                    .flat_map(|mut unit| {
                        let block_indices: HashSet<_> = unit
                            .clone()
                            .unsolved_cells()
                            .filter(|cell| cell.candidates().contains(&candidate))
                            .map(UnsolvedCell::block)
                            .collect();
                        if let Some(&block_index) = block_indices.iter().next()
                            && block_indices.len() == 1
                        {
                            let unit_index = get_unit_index(unit.next().unwrap());
                            let removals = board
                                .get_block(block_index)
                                .unsolved_cells()
                                .filter(|&cell| {
                                    get_unit_index(cell) != unit_index
                                        && cell.candidates().contains(&candidate)
                                })
                                .map(|cell| (cell, candidate))
                                .collect::<Vec<_>>()
                                .into_iter();
                            Some(removals)
                        } else {
                            None
                        }
                    })
                    .flatten()
                    .collect::<Vec<_>>()
                    .into_iter()
            }

            let row_removals =
                box_line_reduction(board, candidate, board.rows(), |location| location.row());
            let column_removals =
                box_line_reduction(board, candidate, board.columns(), |location| {
                    location.column()
                });
            row_removals.chain(column_removals)
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
            {45}16{245}{2459}78{49}3\
            {345}928{3456}{3456}{147}{47}{1457}\
            87{35}{345}{3459}126{459}\
            {127}48{1257}{12567}{56}3{79}{179}\
            65{17}{1347}{1347}9{147}82\
            {127}39{1247}{12478}{48}65{147}\
            {1357}6{1357}9{1578}{58}{47}2{478}\
            {157}8{157}{1457}{1457}2936\
            9246{378}{38}51{78}\
        ";
        let expected = [
            remove_candidates!(1, 6, 4),
            remove_candidates!(1, 8, 4),
            remove_candidates!(2, 8, 4),
        ];
        assertions::assert_logical_solution(&expected, board, box_line_reduction)
    }

    #[test]
    fn test_2() {
        let board = "\
            {68}2{68}943715\
            9{13}4{1578}{127}{157}6{23}{28}\
            75{13}{168}{126}{16}{389}4{289}\
            5{1367}{13679}48{1679}{19}{279}{2679}\
            2{1678}{16789}{167}{1679}{1679}453\
            4{167}{1679}352{189}{79}{6789}\
            {36}42{567}{3679}{5679}{39}81\
            {138}{1378}5{17}{1379}426{79}\
            {136}9{1367}2{1367}85{37}4\
        ";
        let expected = [
            remove_candidates!(3, 2, 6),
            remove_candidates!(3, 6, 9),
            remove_candidates!(3, 8, 9),
            remove_candidates!(4, 2, 6),
            remove_candidates!(5, 2, 6),
            remove_candidates!(5, 6, 9),
            remove_candidates!(5, 8, 9),
            remove_candidates!(7, 1, 1, 3),
            remove_candidates!(7, 3, 7),
            remove_candidates!(7, 4, 7),
            remove_candidates!(8, 2, 1, 3),
            remove_candidates!(8, 4, 7),
        ];
        assertions::assert_logical_solution(&expected, board, box_line_reduction)
    }
}
