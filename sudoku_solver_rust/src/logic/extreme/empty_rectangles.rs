use crate::{
    board::{self, Board},
    board_modification::{BoardModification, IteratorRemoveCandidatesExt},
    cell::{Cell, LocatedCandidate, Location},
    sudoku_number::SudokuNumber,
};
use strum::IntoEnumIterator;

// https://www.sudokuwiki.org/Empty_Rectangles
//
// This solution starts with looking for empty rectangles in blocks. An empty rectangle is a collection of four cells,
// all contained within a single block, arranged in a rectangle, and none of them contain a particular candidate. The
// cells can either be solved cells or unsolved cells without the candidate. For the other cells which are in the block,
// but are outside the rectangle, at least two of them must contain the candidate and those cells must be in at least
// two different rows and two different columns.
//
// This creates a situation in which two lines can be drawn through the block; one line along a row and the other along
// a column. The two lines must not pass through any of the empty rectangle cells and all the cells with the candidate
// must have a line pass through it. A valid block is one in which there is only one option for the placement of these
// lines. This is why the cells with the candidate must be in at least two different rows and two different columns. The
// cell in which these lines intersect is then used to find removals outside the block. The empty rectangle itself is
// used to find a valid intersection point, but then the rectangle is disregarded for the remainder of the solution.
//
// Removals are looked for in cells which are outside the block, but which can see the intersection. If the intersection
// can see one end of a strong link which is outside the intersection's block and there is another cell with the
// candidate outside the intersection's block, but it can see the intersection and the other end of the strong link,
// then there is a contradiction. If the candidate were to be set as the solution to the other cell, then the strong
// link and this newly set solution would remove the candidate from every cell within the intersection's block, thus
// invalidating that block. This means that the candidate cannot be the solution to that cell and can be removed.
pub fn empty_rectangles(board: &Board<Cell>) -> Vec<BoardModification> {
    SudokuNumber::iter()
        .flat_map(|candidate| {
            get_intersections(board, candidate).flat_map(move |(row, column)| {
                let block = board[(row, column)].block();

                #[allow(clippy::too_many_arguments)]
                fn get_removals<'a, U: Iterator<Item = &'a Cell>>(
                    board: &'a Board<Cell>,
                    candidate: SudokuNumber,
                    block: usize,
                    unit: impl Iterator<Item = &'a Cell>,
                    get_other_unit_index: impl Fn(&Cell) -> usize,
                    get_other_unit: impl Fn(usize) -> U,
                    get_removal_row: impl Fn(&Cell) -> usize,
                    get_removal_column: impl Fn(&Cell) -> usize,
                ) -> impl Iterator<Item = LocatedCandidate<'a>> {
                    unit.filter(move |strong_link_1| {
                        strong_link_1.block() != block && strong_link_1.has_candidate(candidate)
                    })
                    .flat_map(move |strong_link_1| {
                        let mut other_unit = get_other_unit(get_other_unit_index(strong_link_1))
                            .filter(|&cell| cell.has_candidate(candidate) && cell != strong_link_1);
                        if let Some(strong_link_2) = other_unit.next()
                            && other_unit.next().is_none()
                            && strong_link_1.block() != strong_link_2.block()
                            && let Cell::UnsolvedCell(removal_cell) = &board[(
                                get_removal_row(strong_link_2),
                                get_removal_column(strong_link_2),
                            )]
                            && removal_cell.candidates().contains(&candidate)
                        {
                            Some((removal_cell, candidate))
                        } else {
                            None
                        }
                    })
                }

                let row_removals = get_removals(
                    board,
                    candidate,
                    block,
                    board.get_row(row),
                    Location::column,
                    |index| board.get_column(index),
                    Location::row,
                    move |_| column,
                );
                let column_removals = get_removals(
                    board,
                    candidate,
                    block,
                    board.get_column(column),
                    Location::row,
                    |index| board.get_row(index),
                    move |_| row,
                    Location::column,
                );
                row_removals.chain(column_removals)
            })
        })
        .merge_to_remove_candidates()
}

fn get_intersections(
    board: &Board<Cell>,
    candidate: SudokuNumber,
) -> impl Iterator<Item = (usize, usize)> {
    (0..board::UNIT_SIZE).flat_map(move |row| {
        let row_in_block = row % board::UNIT_SIZE_SQUARE_ROOT;
        let rectangle_row_1 = if row_in_block == 0 {
            row + 1
        } else {
            row - row_in_block
        };
        let rectangle_row_2 = if row_in_block == 2 {
            row - 1
        } else {
            row - row_in_block + 2
        };
        (0..board::UNIT_SIZE)
            .filter(move |&column| {
                let column_in_block = column % board::UNIT_SIZE_SQUARE_ROOT;
                let rectangle_column_1 = if column_in_block == 0 {
                    column + 1
                } else {
                    column - column_in_block
                };
                let rectangle_column_2 = if column_in_block == 2 {
                    column - 1
                } else {
                    column - column_in_block + 2
                };
                // Check that the rectangle is empty.
                !board[(rectangle_row_1, rectangle_column_1)].has_candidate(candidate)
                    && !board[(rectangle_row_1, rectangle_column_2)].has_candidate(candidate)
                    && !board[(rectangle_row_2, rectangle_column_1)].has_candidate(candidate)
                    && !board[(rectangle_row_2, rectangle_column_2)].has_candidate(candidate)
                    // Check that at least one cell in the same block and row as the intersection has the candidate.
                    && (board[(row, rectangle_column_1)].has_candidate(candidate)
                        || board[(row, rectangle_column_2)].has_candidate(candidate))
                    // Check that at least one cell in the same block and column as the intersection has the candidate.
                    && (board[(rectangle_row_1, column)].has_candidate(candidate)
                        || board[(rectangle_row_2, column)].has_candidate(candidate))
            })
            .map(move |column| (row, column))
    })
}

impl Cell {
    fn has_candidate(&self, candidate: SudokuNumber) -> bool {
        if let Cell::UnsolvedCell(unsolved_cell) = self {
            unsolved_cell.candidates().contains(&candidate)
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
            4{256}{567}1{269}3{67}{678}{789}\
            {36}8{13}5{69}742{1369}\
            9{126}{37}{68}4{268}{136}{367}5\
            139{468}{26}{248}5{678}{678}\
            27{56}91{568}{36}4{368}\
            8{56}473{56}912\
            592{46}8{146}{1367}{367}{137}\
            74835{16}29{16}\
            {36}{16}{136}279854\
        ";
        let expected = [remove_candidates!(3, 8, 6)];
        assertions::assert_logical_solution(&expected, board, empty_rectangles);
    }

    #[test]
    fn test_2() {
        let board = "\
            75{148}96{148}32{18}\
            {3468}{36}{13469}7{48}2{689}5{1689}\
            {68}{26}{12689}{158}3{158}{689}47\
            97{246}{126}5{14}{246}83\
            {346}{236}5{2368}7{389}1{69}{2469}\
            18{236}{236}{24}{349}{2469}75\
            24{68}{35}9{35}71{68}\
            {3568}1{368}4{28}7{2569}{69}{2689}\
            {58}97{28}16{2458}3{248}\
        ";
        let expected = [remove_candidates!(4, 8, 6), remove_candidates!(7, 2, 6)];
        assertions::assert_logical_solution(&expected, board, empty_rectangles);
    }

    #[test]
    fn test_3() {
        let board = "\
            9{37}15{28}{28}{37}46\
            425{367}9{367}{37}81\
            86{37}{347}1{347}{59}2{59}\
            5{3478}2{1469}{378}{469}{19}{37}{89}\
            {37}19{238}{2357}{2358}46{58}\
            6{3478}{3478}{14}{3578}{49}{159}{37}2\
            196{78}4{78}253\
            2{345}{34}{39}6{359}817\
            {37}{3578}{378}{23}{235}1694\
        ";
        let expected = [remove_candidates!(8, 4, 3)];
        assertions::assert_logical_solution(&expected, board, empty_rectangles);
    }

    #[test]
    fn test_4() {
        let board = "\
            695{237}1{278}{2347}{23478}{2378}\
            {137}8{137}4{237}96{237}5\
            {37}245{378}69{378}1\
            {13579}{3456}{1379}{69}{24578}{24578}{2347}{23478}{23789}\
            8{46}2{69}{47}351{79}\
            {3579}{345}{379}1{24578}{24578}{2347}6{23789}\
            {2345}78{23}9{245}1{235}6\
            {2359}{35}{39}{237}618{2357}4\
            {2345}168{23457}{2457}{237}9{237}\
        ";
        let expected = [
            remove_candidates!(0, 3, 7),
            remove_candidates!(0, 8, 7),
            remove_candidates!(7, 7, 7),
            remove_candidates!(8, 4, 2),
        ];
        assertions::assert_logical_solution(&expected, board, empty_rectangles);
    }

    #[test]
    fn test_5() {
        let board = "\
            695{23}1{278}{2347}{23478}{238}\
            {137}8{137}4{237}96{237}5\
            {37}245{378}69{378}1\
            {13579}{3456}{1379}{69}{24578}{24578}{2347}{23478}{23789}\
            8{46}2{69}{47}351{79}\
            {3579}{345}{379}1{24578}{24578}{2347}6{23789}\
            {34}78{23}9{245}1{35}6\
            {2359}{35}{39}7618{235}4\
            {2345}168{345}{45}{237}9{237}\
        ";
        let expected = [remove_candidates!(6, 7, 3)];
        assertions::assert_logical_solution(&expected, board, empty_rectangles);
    }
}
