use crate::{
    board::Board,
    board_modification::{BoardModification, IteratorRemoveCandidatesExt},
    cell::{Cell, IteratorCellExt, LocatedCandidate, Location, UnsolvedCell},
    collections::IteratorZipExt,
    sudoku_number::SudokuNumber,
};
use std::collections::HashSet;
use strum::IntoEnumIterator;

// https://www.sudokuwiki.org/Finned_Swordfish
//
// Finned Swordfish is an extension of Swordfish in a similar manner to the way that Finned X-Wing is an extension of
// X-Wing. As a reminder, Swordfish looks for a 3x3 grid of cells in which a particular candidate appears in most or all
// of those cells. If the candidate appears two or three times in each row of the grid and for those rows, the candidate
// appears in exactly three columns, then the candidate can be removed from the columns of the grid, but in different
// rows. If the candidate appears two or three times in each column of the grid and for those columns, the candidate
// appears in exactly three rows, then the candidate can be removed from the rows of the grid, but in different columns.
//
// In Finned Swordfish, eight of the cells of a 3x3 grid will follow the same rules as Swordfish. Only one cell will
// have additional unsolved cells with the candidate next to it. The fin must be in the same block as the cell, but the
// cell itself may or may not have the candidate.
//
// For a triple of rows, two rows are the base rows if the candidate appears two or three times in each row and the
// candidate appears in exactly three columns of the two rows. The remaining row is a finned row if the candidate
// appears once or twice outside the three columns, but in the same block as one of the cells of the grid. That cell is
// the finned cell. The candidate can be removed from cells that are in the same column as the finned cell, but are
// outside the grid.
//
// For a triple of columns, two columns are the base columns if the candidate appears two or three times in each column
// and the candidate appears in exactly three rows of the two columns. The remaining column is a finned column if the
// candidate appears once or twice outside the three rows, but in the same block as one of the cells of the grid. That
// cell is the finned cell. The candidate can be removed from cells that are in the same row as the finned cell, but are
// outside the grid.
pub fn finned_swordfish(board: &Board<Cell>) -> Vec<BoardModification> {
    SudokuNumber::iter()
        .flat_map(|candidate| {
            fn finned_swordfish<'a, U: Iterator<Item = &'a Cell>>(
                candidate: SudokuNumber,
                units: impl Iterator<Item = impl Iterator<Item = &'a Cell>>,
                get_unit_index: impl Fn(&UnsolvedCell) -> usize,
                get_other_unit_index: impl Fn(&(dyn Location + 'a)) -> usize,
                get_other_unit: impl Fn(usize) -> U,
                get_finned_cell: impl Fn(usize, usize) -> &'a Cell,
            ) -> impl Iterator<Item = LocatedCandidate<'a>> {
                let units_with_candidate: Vec<_> = units
                    .map(|unit| {
                        unit.unsolved_cells()
                            .filter(|cell| cell.candidates().contains(&candidate))
                            .collect()
                    })
                    .filter(|unit: &Vec<_>| !unit.is_empty())
                    .collect();
                units_with_candidate
                    .iter()
                    .filter(|unit| (2..=3).contains(&unit.len()))
                    .zip_every_pair()
                    .flat_map(|(base_unit_a, base_unit_b)| {
                        let other_unit_indices: HashSet<_> = base_unit_a
                            .iter()
                            .chain(base_unit_b)
                            .map(|&cell| get_other_unit_index(cell))
                            .collect();
                        if other_unit_indices.len() == 3 {
                            let removals = units_with_candidate
                                .iter()
                                .flat_map({
                                    let get_unit_index = &get_unit_index;
                                    let get_other_unit_index = &get_other_unit_index;
                                    let get_finned_cell = &get_finned_cell;
                                    let get_other_unit = &get_other_unit;
                                    move |finned_unit| {
                                        let finned_unit_index =
                                            get_unit_index(finned_unit.first().unwrap());
                                        let mut unit_indices = HashSet::new();
                                        unit_indices.insert(finned_unit_index);
                                        unit_indices
                                            .insert(get_unit_index(base_unit_a.first().unwrap()));
                                        unit_indices
                                            .insert(get_unit_index(base_unit_b.first().unwrap()));
                                        if unit_indices.len() == 3 {
                                            let outside_other_unit_indices: Vec<_> = finned_unit
                                                .iter()
                                                .filter(|&&cell| {
                                                    !other_unit_indices
                                                        .contains(&get_other_unit_index(cell))
                                                })
                                                .collect();
                                            if (1..=2).contains(&outside_other_unit_indices.len()) {
                                                let block_indices: HashSet<_> =
                                                    outside_other_unit_indices
                                                        .iter()
                                                        .map(|cell| cell.block())
                                                        .collect();
                                                if block_indices.len() == 1 {
                                                    let &block_index =
                                                        block_indices.iter().next().unwrap();
                                                    let mut finned_cells = other_unit_indices
                                                        .iter()
                                                        .map(|&other_unit_index| {
                                                            get_finned_cell(
                                                                finned_unit_index,
                                                                other_unit_index,
                                                            )
                                                        })
                                                        .filter(|finned_cell| {
                                                            finned_cell.block() == block_index
                                                        });
                                                    if let Some(finned_cell) = finned_cells.next()
                                                        && finned_cells.next().is_none()
                                                    {
                                                        let removals = get_other_unit(
                                                            get_other_unit_index(finned_cell),
                                                        )
                                                        .unsolved_cells()
                                                        .filter(move |cell| {
                                                            cell.candidates().contains(&candidate)
                                                                && cell.block() == block_index
                                                                && !unit_indices
                                                                    .contains(&get_unit_index(cell))
                                                        })
                                                        .map(move |cell| (cell, candidate));
                                                        Some(removals)
                                                    } else {
                                                        None
                                                    }
                                                } else {
                                                    None
                                                }
                                            } else {
                                                None
                                            }
                                        } else {
                                            None
                                        }
                                    }
                                })
                                .flatten();
                            Some(removals)
                        } else {
                            None
                        }
                    })
                    .flatten()
                    .collect::<Vec<_>>()
                    .into_iter()
            }

            let row_removals = finned_swordfish(
                candidate,
                board.rows(),
                Location::row,
                Location::column,
                |index| board.get_column(index),
                |finned_unit_index, other_unit_index| &board[(finned_unit_index, other_unit_index)],
            );
            let column_removals = finned_swordfish(
                candidate,
                board.columns(),
                Location::column,
                Location::row,
                |index| board.get_row(index),
                |finned_unit_index, other_unit_index| &board[(other_unit_index, finned_unit_index)],
            );
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
            6{379}5418{379}{37}2\
            8{349}{349}{3569}72{3459}{3456}1\
            21{3479}{3569}{359}{69}{457}8{367}\
            9{37}8265{137}{137}4\
            {34}5{3467}18{49}2{3679}{367}\
            1{246}{246}7{49}38{69}5\
            {34}8{169}{3569}{359}{4679}{13457}2{37}\
            5{69}{169}{369}2{4679}{1347}{1347}8\
            7{234}{234}8{345}16{345}9\
        ";
        let expected = [remove_candidates!(6, 3, 3)];
        assertions::assert_logical_solution(&expected, board, finned_swordfish);
    }

    #[test]
    fn test_2() {
        let board = "\
            2{349}{3489}{1359}{1358}{1389}6{19}7\
            {189}7{89}6{128}4{1259}3{1259}\
            {139}65{1239}7{1239}84{129}\
            582{139}6{139}{139}74\
            4{39}78{123}5{1239}{129}6\
            {39}16{2379}4{2379}{239}58\
            {378}{35}1{2357}9{2378}46{25}\
            {379}2{39}4{135}6{1579}8{159}\
            6{459}{489}{1257}{1258}{1278}{12579}{129}3\
        ";
        let expected = [remove_candidates!(7, 0, 3)];
        assertions::assert_logical_solution(&expected, board, finned_swordfish);
    }

    #[test]
    fn test_3() {
        let board = "\
            42{36}{17}{678}{18}{1367}95\
            {3589}{357}{3569}{1257}4{157}{12367}{267}{2378}\
            {58}{57}19{2567}34{267}{278}\
            {357}6{35}892{57}14\
            {57}4231{57}986\
            1984{57}6{257}3{27}\
            {2359}176{235}48{25}{239}\
            {2359}{35}4{1257}{23578}{15789}{267}{2567}{2379}\
            68{359}{257}{2357}{579}{237}41\
        ";
        let expected = [remove_candidates!(1, 5, 7), remove_candidates!(7, 5, 7)];
        assertions::assert_logical_solution(&expected, board, finned_swordfish);
    }

    #[test]
    fn test_4() {
        let board = "\
            42{36}{17}{678}{18}{1367}95\
            {3589}{357}{3569}{1257}4{15}{12367}{267}{2378}\
            {58}{57}19{2567}34{267}{278}\
            {357}6{35}892{57}14\
            {57}4231{57}986\
            1984{57}6{257}3{27}\
            {2359}176{235}48{25}{239}\
            {2359}{35}4{1257}{23578}{15789}{267}{2567}{2379}\
            68{359}{257}{2357}{579}{237}41\
        ";
        let expected = [remove_candidates!(7, 5, 7)];
        assertions::assert_logical_solution(&expected, board, finned_swordfish);
    }

    #[test]
    fn test_5() {
        let board = "\
            {2456}{2567}3{245}8{259}1{456}{79}\
            {124568}9{1468}{2345}{1345}7{348}{4568}{3568}\
            {1458}{458}{1478}6{39}{15}{79}{3458}2\
            {568}1{79}{258}{567}3{289}{268}4\
            {4689}{4678}2{478}{1467}{168}5{1368}{39}\
            3{4568}{468}9{1456}{12568}{28}7{168}\
            7{2368}{19}{358}{3569}4{238}{1258}{1358}\
            {2468}{23468}{468}1{3567}{568}{23478}9{3578}\
            {19}{348}5{378}2{89}6{148}{1378}\
        ";
        let expected = [remove_candidates!(5, 4, 6)];
        assertions::assert_logical_solution(&expected, board, finned_swordfish);
    }

    #[test]
    fn test_6() {
        let board = "\
            {256}7348{25}1{56}9\
            {26}9{16}{235}{135}74{568}{568}\
            {458}{458}{148}69{15}732\
            {568}17{258}{56}39{268}4\
            9{468}2{78}{1467}{168}5{16}3\
            3{4568}{468}9{145}{12568}{28}7{168}\
            7{2368}9{358}{356}4{238}{1258}{158}\
            {468}{23468}{468}1{3567}{568}{23}9{578}\
            1{38}5{378}2964{78}\
        ";
        let expected = [remove_candidates!(7, 0, 6)];
        assertions::assert_logical_solution(&expected, board, finned_swordfish);
    }
}
