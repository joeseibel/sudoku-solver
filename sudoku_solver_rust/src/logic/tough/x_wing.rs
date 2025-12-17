use crate::{
    board::Board,
    board_modification::{BoardModification, IteratorRemoveCandidatesExt},
    cell::{Cell, IteratorCellExt, LocatedCandidate, Location, UnsolvedCell},
    collections::IteratorZipExtOwned,
    sudoku_number::SudokuNumber,
};
use strum::IntoEnumIterator;

// http://www.sudokuwiki.org/X_Wing_Strategy
//
// For a pair of rows, if a candidate appears in only two columns of both rows and the columns are the same, forming a
// rectangle, then the candidate must be placed in opposite corners of the rectangle. The candidate can be removed from
// cells which are in the two columns, but different rows.
//
// For a pair of columns, if a candidate appears in only two rows of both columns and the rows are the same, forming a
// rectangle, then the candidate must be placed in opposite corners of the rectangle. The candidate can be removed from
// cells which are in the two rows, but different columns.
pub fn x_wing(board: &Board<Cell>) -> Vec<BoardModification> {
    SudokuNumber::iter().flat_map(|candidate| {

        fn x_wing<
            'a,
            Z: Iterator<Item = &'a Cell> + IteratorCellExt<'a>,
            U: Iterator<Item = &'a Cell> + Clone
        >(
            candidate: SudokuNumber,
            units: impl Iterator<Item = impl Iterator<Item = &'a Cell>> + IteratorZipExtOwned<Z>,
            get_other_unit: impl Fn(usize) -> U,
            get_other_unit_index: impl Fn(&UnsolvedCell) -> usize
        ) -> impl Iterator<Item = LocatedCandidate<'a>> {
            units.zip_every_pair().flat_map(move |(unit_a, unit_b)| {
                let unit_a: Vec<_> = unit_a.unsolved_cells().collect();
                let unit_b: Vec<_> = unit_b.unsolved_cells().collect();
                let a_with_candidate: Result<[_; 2], _> = unit_a.iter()
                    .filter(|cell| cell.candidates().contains(&candidate))
                    .collect::<Vec<_>>()
                    .try_into();
                let b_with_candidate: Result<[_; 2], _> = unit_b.iter()
                    .filter(|cell| cell.candidates().contains(&candidate))
                    .collect::<Vec<_>>()
                    .try_into();
                if let Ok([first_a, last_a]) = a_with_candidate &&
                    let Ok([first_b, last_b]) = b_with_candidate &&
                    get_other_unit_index(first_a) == get_other_unit_index(first_b) &&
                    get_other_unit_index(last_a) == get_other_unit_index(last_b)
                {
                    let other_unit_a = get_other_unit(get_other_unit_index(first_a));
                    let other_unit_b = get_other_unit(get_other_unit_index(last_a));
                    let removals = other_unit_a.chain(other_unit_b)
                        .unsolved_cells()
                        .filter(|cell| {
                            cell.candidates().contains(&candidate) && !unit_a.contains(cell) && !unit_b.contains(cell)
                        })
                        .map(|cell| (cell, candidate))
                        .collect::<Vec<_>>();
                    Some(removals)
                } else {
                    None
                }
            }).flatten()
        }

        let row_removals = x_wing(
            candidate,
            board.rows(),
            |index| board.get_column(index),
            Location::column
        );
        let column_removals = x_wing(
            candidate,
            board.columns(),
            |index| board.get_row(index),
            Location::row
        );
        row_removals.chain(column_removals)
    }).merge_to_remove_candidates()
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::{logic::assertions, remove_candidates};

    #[test]
    fn test_1() {
        let board = "\
            1{378}{37}{23478}{278}{23478}569\
            492{37}561{37}8\
            {378}561{78}924{37}\
            {357}{37}964{27}8{25}1\
            {57}64{2789}1{278}{379}{25}{37}\
            218{79}356{79}4\
            {378}4{37}5{2789}{2378}{379}16\
            9{378}5{378}614{378}2\
            621{3478}{789}{3478}{379}{3789}5\
        ";
        let expected = [
            remove_candidates!(0, 3, 7),
            remove_candidates!(4, 3, 7),
            remove_candidates!(7, 3, 7),
            remove_candidates!(7, 7, 7),
            remove_candidates!(8, 3, 7),
            remove_candidates!(8, 7, 7),
        ];
        assertions::assert_logical_solution(&expected, board, x_wing);
    }

    #[test]
    fn test_2() {
        let board = "\
            {1358}{235}{12358}{3568}{678}{35678}{67}94\
            76{48}91{48}{23}5{23}\
            {345}9{345}{3456}{467}2{67}81\
            {346}7{23469}{2468}5{468}{23489}1{2389}\
            {13456}{235}{123456}7{2468}9{23458}{23}{238}\
            {45}8{2459}{24}31{2459}67\
            24{3568}1{68}{3568}{389}7{3689}\
            {368}1{3678}{2368}9{3678}{238}45\
            9{35}{35678}{234568}{24678}{345678}1{23}{2368}\
        ";
        let expected = [
            remove_candidates!(4, 1, 2),
            remove_candidates!(4, 2, 2),
            remove_candidates!(4, 6, 2),
            remove_candidates!(4, 8, 2),
            remove_candidates!(8, 3, 2),
            remove_candidates!(8, 8, 2),
        ];
        assertions::assert_logical_solution(&expected, board, x_wing);
    }

    #[test]
    fn test_3() {
        let board = "\
            {24568}{2458}391{568}7{58}{258}\
            {568}{578}{67}{568}23491\
            1{258}9{58}47{238}{358}6\
            {4589}617{35}{48}{238}{3458}{24589}\
            {458}{34578}21{35}96{34578}{4578}\
            {4589}{34578}{47}{48}62{38}1{45789}\
            79{46}{456}8{456}123\
            31829{46}5{467}{47}\
            {246}{24}53719{468}{48}\
        ";
        let expected = [
            remove_candidates!(1, 0, 6),
            remove_candidates!(5, 0, 4),
            remove_candidates!(5, 1, 4),
            remove_candidates!(5, 8, 4),
            remove_candidates!(6, 5, 4, 6),
        ];
        assertions::assert_logical_solution(&expected, board, x_wing);
    }

    #[test]
    fn test_4() {
        let board = "\
            {2589}1{258}{69}37{58}4{56}\
            {4789}{38}{478}{69}25{378}1{67}\
            6{35}{57}418{357}29\
            {25}7314968{25}\
            1{256}{245}87{26}{245}93\
            {248}{268}935{26}{124}7{14}\
            39{17}264{17}58\
            {27}4658193{27}\
            {258}{258}{1258}793{124}6{14}\
        ";
        let expected = [
            remove_candidates!(0, 0, 2),
            remove_candidates!(1, 2, 7),
            remove_candidates!(1, 6, 7),
            remove_candidates!(5, 0, 2),
            remove_candidates!(8, 0, 2),
        ];
        assertions::assert_logical_solution(&expected, board, x_wing);
    }
}
