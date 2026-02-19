use crate::{
    board::Board,
    board_modification::{BoardModification, IteratorRemoveCandidatesExt},
    cell::{Cell, IteratorCellExt, LocatedCandidate, Location, UnsolvedCell},
    sudoku_number::SudokuNumber,
};
use itertools::Itertools;
use std::collections::HashMap;
use strum::IntoEnumIterator;

// https://www.sudokuwiki.org/Finned_X_Wing
//
// Finned X-Wing is an extension of X-Wing in which one of the corners of a rectangle of cells has a fin next to it. As
// a reminder, X-Wing looks for a rectangle of unsolved cells with a particular candidate. If the candidate only appears
// twice in each of the rows of the rectangle, then the candidate can be removed from the columns of the rectangle, but
// in different rows. If the candidate only appears twice in each of the columns of the rectangle, then the candidate
// can be removed from the rows of the rectangle, but in different columns.
//
// In Finned X-Wing, three of the corners of a rectangle will follow the same rules as X-Wing. Only one corner will have
// additional unsolved cells with the candidate next to it. The fin must be in the same block as the corner, but the
// corner itself may or may not have the candidate. If the corner does not have the candidate, the pattern is called a
// Sashimi Finned X-Wing. From an implementation perspective, there is no difference between a regular Finned X-Wing and
// Sashimi.
//
// For a pair of rows in different blocks, one row is the base row if the candidate appears exactly twice in that row,
// but in different blocks. The other row is considered to be a finned row if the candidate appears in two blocks of
// that row, one of those blocks of the row contains a regular corner, and the other block of the row contains a fin. A
// regular corner is a cell with the candidate, it shares the same column as one of the candidates of the base row, and
// there are no other candidates in that block of the row. The candidates of the base row along with the regular corner
// form three corners of a rectangle with the fourth corner being a finned corner. The fourth corner may or may not have
// the candidate. A fin is one or two cells in the finned row that do not share a column with either of the candidates
// of the base row, but are in the same block as the finned corner. With all of these constraints, the candidate must be
// placed in opposite corners of the rectangle, or the fin in the case of the finned corner. The candidate can be
// removed from cells which are in the same column as the finned corner, the same block as the fin, but different rows.
//
// For a pair of columns in different blocks, one column is the base column if the candidate appears exactly twice in
// that column, but in different blocks. The other column is considered to be a finned column if the candidate appears
// in two blocks of that column, one of those blocks of the column contains a regular corner, and the other block of the
// column contains a fin. A regular corner is a cell with the candidate, it shares the same row as one of the candidates
// of the base column, and there are no other candidates in that block of the column. The candidates of the base column
// along with the regular corner form three corners of a rectangle with the fourth corner being a finned corner. The
// fourth corner may or may not have the candidate. A fin is one or two cells in the finned column that do not share a
// row with either of the candidates of the base column, but are in the same block as the finned corner. With all of
// these constraints, the candidate must be placed in opposite corners of the rectangle, or the fin in the case of the
// finned corner. The candidate can be removed from cells which are in the same row as the finned corner, the same block
// as the fin, but different columns.
pub fn finned_x_wing(board: &Board<Cell>) -> Vec<BoardModification> {
    SudokuNumber::iter()
        .flat_map(|candidate| {
            fn finned_x_wing<'a>(
                board: &'a Board<Cell>,
                candidate: SudokuNumber,
                units: impl Iterator<Item = impl Iterator<Item = &'a Cell>>,
                get_other_unit_index: impl Fn(&dyn Location) -> usize,
            ) -> impl Iterator<Item = LocatedCandidate<'a>> {
                let units: Vec<Vec<_>> = units.map(Iterator::collect).collect();
                units
                    .iter()
                    .flat_map(|base_unit| {
                        let mut with_candidate = base_unit
                            .iter()
                            .copied()
                            .unsolved_cells()
                            .filter(|cell| cell.candidates().contains(&candidate));
                        if let Some(base_unit_cell_1) = with_candidate.next()
                            && let Some(base_unit_cell_2) = with_candidate.next()
                            && with_candidate.next().is_none()
                            && base_unit_cell_1.block() != base_unit_cell_2.block()
                        {
                            let removals = units
                                .iter()
                                .filter(|finned_unit| {
                                    finned_unit.first().unwrap().block()
                                        != base_unit.first().unwrap().block()
                                })
                                .flat_map(|finned_unit| {
                                    let finned_unit_by_block = finned_unit
                                        .iter()
                                        .copied()
                                        .unsolved_cells()
                                        .filter(|cell| cell.candidates().contains(&candidate))
                                        .into_group_map_by(|cell| cell.block());
                                    if finned_unit_by_block.len() == 2 {
                                        fn try_fin<'a, F: Fn(&dyn Location) -> usize>(
                                            board: &'a Board<Cell>,
                                            candidate: SudokuNumber,
                                            get_other_unit_index: F,
                                            finned_unit_by_block: &HashMap<
                                                usize,
                                                Vec<&UnsolvedCell>,
                                            >,
                                            finned_corner: &'a Cell,
                                            other_corner: &Cell,
                                        ) -> Option<
                                            impl Iterator<Item = LocatedCandidate<'a>> + use<'a, F>,
                                        > {
                                            if let Some(finned_block) =
                                                finned_unit_by_block.get(&finned_corner.block())
                                                && let Some(other_block) =
                                                    finned_unit_by_block.get(&other_corner.block())
                                                && finned_block
                                                    .iter()
                                                    .any(|&cell| cell != finned_corner)
                                                && let [other_block] = other_block[..]
                                                && other_block == other_corner
                                            {
                                                let removals = board
                                                    .get_block(finned_corner.block())
                                                    .unsolved_cells()
                                                    .filter(move |&cell| {
                                                        get_other_unit_index(cell)
                                                            == get_other_unit_index(finned_corner)
                                                            && cell != finned_corner
                                                            && cell
                                                                .candidates()
                                                                .contains(&candidate)
                                                    })
                                                    .map(move |cell| (cell, candidate));
                                                Some(removals)
                                            } else {
                                                None
                                            }
                                        }

                                        let finned_unit_cell_1 =
                                            finned_unit[get_other_unit_index(base_unit_cell_1)];
                                        let finned_unit_cell_2 =
                                            finned_unit[get_other_unit_index(base_unit_cell_2)];
                                        try_fin(
                                            board,
                                            candidate,
                                            &get_other_unit_index,
                                            &finned_unit_by_block,
                                            finned_unit_cell_1,
                                            finned_unit_cell_2,
                                        )
                                        .or_else(|| {
                                            try_fin(
                                                board,
                                                candidate,
                                                &get_other_unit_index,
                                                &finned_unit_by_block,
                                                finned_unit_cell_2,
                                                finned_unit_cell_1,
                                            )
                                        })
                                    } else {
                                        None
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

            let row_removals =
                finned_x_wing(board, candidate, board.rows(), |location| location.column());
            let column_removals =
                finned_x_wing(board, candidate, board.columns(), |location| location.row());
            row_removals.chain(column_removals)
        })
        .merge_to_remove_candidates()
}

impl PartialEq<Cell> for UnsolvedCell {
    fn eq(&self, other: &Cell) -> bool {
        if let Cell::UnsolvedCell(other) = other {
            self == other
        } else {
            false
        }
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::{logic::assertions, remove_candidates};

    #[test]
    fn test_1() {
        let board = "\
            754{126}{269}8{12}3{1269}\
            836{14}{279}{12479}{57}{279}{12579}\
            19235{67}84{67}\
            2459{68}{16}{37}{78}{137}\
            {36}{68}{138}745{12}{289}{129}\
            9{78}{178}{12}{28}3654\
            {3456}2981{467}{3457}{67}{357}\
            {3456}{678}{378}{246}{2367}{2467}91{2357}\
            {346}1{37}5{23679}{24679}{347}{267}8\
        ";
        let expected = [remove_candidates!(7, 8, 7)];
        assertions::assert_logical_solution(&expected, board, finned_x_wing);
    }

    #[test]
    fn test_2() {
        let board = "\
            9{156}{1256}{25}4{1567}38{267}\
            7{136}4{23}8{136}95{26}\
            {235}8{2356}9{27}{3567}14{267}\
            {135}{135}769{35}824\
            629418735\
            {345}{345}8{235}{27}{357}619\
            {345}7{35}162{45}98\
            89{25}734{25}61\
            {124}{146}{126}859{24}73\
        ";
        let expected = [remove_candidates!(5, 0, 3, 5)];
        assertions::assert_logical_solution(&expected, board, finned_x_wing);
    }

    #[test]
    fn test_3() {
        let board = "\
            9{156}{1256}{25}4{1567}38{267}\
            7{136}4{23}8{136}95{26}\
            {235}8{2356}9{27}{3567}14{267}\
            {135}{135}769{35}824\
            629418735\
            {45}{345}8{235}{27}{357}619\
            {345}7{35}162{45}98\
            89{25}734{25}61\
            {124}{146}{126}859{24}73\
        ";
        let expected = [remove_candidates!(5, 0, 5)];
        assertions::assert_logical_solution(&expected, board, finned_x_wing);
    }

    #[test]
    fn test_sashimi() {
        let board = "\
            3{467}{67}{46}12598\
            {249}{245}1{49}8{459}763\
            {69}8{56}7{356}{369}241\
            7{269}{256}{469}{456}138{246}\
            {246}{24569}387{4569}{469}1{246}\
            1{469}82{346}{3469}{469}75\
            5193{46}8{46}27\
            {26}3{267}19{467}85{46}\
            8{67}452{67}139\
        ";
        let expected = [remove_candidates!(4, 5, 4), remove_candidates!(5, 5, 4)];
        assertions::assert_logical_solution(&expected, board, finned_x_wing);
    }
}
