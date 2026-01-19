use crate::{
    board::Board,
    board_modification::{BoardModification, IteratorRemoveCandidatesExt},
    cell::{Cell, IteratorCellExt, LocatedCandidate, Location, UnsolvedCell},
    rectangles::{self, Rectangle},
    sudoku_number::SudokuNumber,
};

// https://www.sudokuwiki.org/Hidden_Unique_Rectangles
//
// The basic premise of the Hidden Unique Rectangles solution is exactly the same as Unique Rectangles. The only
// difference is that Hidden Unique Rectangles adds more specific types to the solution. These additional types look for
// strong links between cells of a rectangle. A strong link exists between two cells for a given candidate when those
// two cells are the only cells with the candidate in a given row or column.
pub fn hidden_unique_rectangles(board: &Board<Cell>) -> Vec<BoardModification> {
    rectangles::create_rectangles(board)
        .flat_map(|rectangle| {
            let (floor, roof): (Vec<_>, Vec<_>) = rectangle
                .cells()
                .iter()
                .partition(|cell| cell.candidates().len() == 2);
            if let [floor] = floor[..] {
                type_1(board, &rectangle, floor)
            } else if let [roof_a, roof_b] = roof[..] {
                type_2(board, roof_a, roof_b, rectangle.common_candidates())
            } else {
                None
            }
        })
        .merge_to_remove_candidates()
}

// Type 1
//
// If a rectangle has one floor cell, then consider the roof cell on the opposite corner of the rectangle. If one of the
// common candidates appears twice in that cell's row and twice in that cell's column, which implies that the other
// occurrences in that row and column are in the two other corners of the rectangle, then setting the other common
// candidate as the value to that cell would lead to the Deadly Pattern. Therefore, the other common candidate cannot be
// the solution to that cell. The other common candidate can be removed from the roof cell which is opposite of the one
// floor cell.
fn type_1<'a>(
    board: &Board<Cell>,
    rectangle: &Rectangle<'a>,
    floor: &UnsolvedCell,
) -> Option<LocatedCandidate<'a>> {
    let row: Vec<_> = board.get_row(floor.row()).unsolved_cells().collect();
    let column: Vec<_> = board.get_column(floor.column()).unsolved_cells().collect();
    let mut strong_candidates = rectangle.common_candidates().iter().filter(|candidate| {
        row.iter()
            .filter(|cell| cell.candidates().contains(candidate))
            .count()
            == 2
            && column
                .iter()
                .filter(|cell| cell.candidates().contains(candidate))
                .count()
                == 2
    });
    if let Some(strong_candidate) = strong_candidates.next()
        && strong_candidates.next().is_none()
    {
        let &opposite_cell = rectangle
            .cells()
            .iter()
            .find(|cell| cell.row() != floor.row() && cell.column() != floor.column())
            .unwrap();
        let &other_candidate = rectangle
            .common_candidates()
            .iter()
            .find(|&candidate| candidate != strong_candidate)
            .unwrap();
        Some((opposite_cell, other_candidate))
    } else {
        None
    }
}

// Type 2
//
// If a rectangle has two roof cells, those cells are in the same row, and there exists a strong link for one of the
// common candidates between one of the roof cells and its corresponding floor cell in the same column, then setting the
// other common candidate as the value to the other roof cell would lead to the Deadly Pattern. Therefore, the other
// common candidate cannot be the solution to the other roof cell. The other common candidate can be removed from the
// other roof cell.
//
// If a rectangle has two roof cells, those cells are in the same column, and there exists a strong link for one of the
// common candidates between one of the roof cells and its corresponding floor cell in the same row, then setting the
// other common candidate as the value to the other roof cell would lead to the Deadly Pattern. Therefore, the other
// common candidate cannot be the solution to the other roof cell. The other common candidate can be removed from the
// other roof cell.
fn type_2<'a>(
    board: &'a Board<Cell>,
    roof_a: &'a UnsolvedCell,
    roof_b: &'a UnsolvedCell,
    common_candidates: &[SudokuNumber; 2],
) -> Option<LocatedCandidate<'a>> {
    fn get_removal<'a, U: IteratorCellExt<'a>>(
        roof_a: &'a UnsolvedCell,
        roof_b: &'a UnsolvedCell,
        common_candidates: &[SudokuNumber; 2],
        get_unit_index: impl Fn(&UnsolvedCell) -> usize,
        get_unit: impl Fn(usize) -> U,
    ) -> Option<LocatedCandidate<'a>> {
        let &[candidate_a, candidate_b] = common_candidates;
        let unit_a: Vec<_> = get_unit(get_unit_index(roof_a)).unsolved_cells().collect();
        let unit_b: Vec<_> = get_unit(get_unit_index(roof_b)).unsolved_cells().collect();
        if unit_a
            .iter()
            .filter(|cell| cell.candidates().contains(&candidate_a))
            .count()
            == 2
        {
            Some((roof_b, candidate_b))
        } else if unit_a
            .iter()
            .filter(|cell| cell.candidates().contains(&candidate_b))
            .count()
            == 2
        {
            Some((roof_b, candidate_a))
        } else if unit_b
            .iter()
            .filter(|cell| cell.candidates().contains(&candidate_a))
            .count()
            == 2
        {
            Some((roof_a, candidate_b))
        } else if unit_b
            .iter()
            .filter(|cell| cell.candidates().contains(&candidate_b))
            .count()
            == 2
        {
            Some((roof_a, candidate_a))
        } else {
            None
        }
    }

    if roof_a.row() == roof_b.row() {
        get_removal(
            roof_a,
            roof_b,
            common_candidates,
            UnsolvedCell::column,
            |index| board.get_column(index),
        )
    } else if roof_a.column() == roof_b.column() {
        get_removal(
            roof_a,
            roof_b,
            common_candidates,
            UnsolvedCell::row,
            |index| board.get_row(index),
        )
    } else {
        None
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::{logic::assertions, remove_candidates};

    #[test]
    fn type_1_test_1() {
        let board = "\
            1{24}957{2468}3{26}{2468}\
            {568}7{56}39{24}{68}1{24}\
            {68}{24}3{468}1{2468}597\
            {56}8{156}743{169}{256}{2569}\
            492{16}5{16}783\
            73{156}289{16}4{56}\
            317{89}2{58}4{56}{5689}\
            26{48}{19}3{145}{89}7{589}\
            95{48}{48}67231\
        ";
        let expected = [remove_candidates!(0, 5, 2, 4), remove_candidates!(3, 2, 6)];
        assertions::assert_logical_solution(&expected, board, hidden_unique_rectangles);
    }

    #[test]
    fn type_1_test_2() {
        let board = "\
            518472639\
            3{27}6859{127}{127}4\
            4{27}9316{257}{2578}{278}\
            94562{17}3{178}{78}\
            861{79}34{279}{279}5\
            732{19}85{149}{149}6\
            65{47}{127}9{17}8{247}3\
            293{57}48{57}61\
            18{47}{257}63{24579}{24579}{27}\
        ";
        let expected = [
            remove_candidates!(2, 7, 7),
            remove_candidates!(8, 6, 7),
            remove_candidates!(8, 7, 7),
        ];
        assertions::assert_logical_solution(&expected, board, hidden_unique_rectangles);
    }

    #[test]
    fn type_2_test_1() {
        let board = "\
            5{47}{47}291836\
            {68}3{68}475{29}1{29}\
            {12}{12}9386457\
            {2689}5{678}143{2679}{689}{289}\
            4{26}{368}759{236}{68}1\
            {19}{17}{137}862{379}45\
            3{469}{456}{56}2{48}17{89}\
            {16}8{156}937{56}24\
            7{49}2{56}1{48}{569}{689}3\
        ";
        let expected = [remove_candidates!(3, 2, 6), remove_candidates!(3, 6, 9)];
        assertions::assert_logical_solution(&expected, board, hidden_unique_rectangles);
    }

    #[test]
    fn type_2_test_2() {
        let board = "\
            518472639\
            3{27}6859{127}{127}4\
            4{27}9316{257}{258}{278}\
            94562{17}3{178}{78}\
            861{79}34{279}{279}5\
            732{19}85{149}{149}6\
            65{47}{127}9{17}8{247}3\
            293{57}48{57}61\
            18{47}{257}63{24579}{24579}{27}\
        ";
        let expected = [remove_candidates!(8, 6, 7), remove_candidates!(8, 7, 7)];
        assertions::assert_logical_solution(&expected, board, hidden_unique_rectangles);
    }

    #[test]
    fn type_2_b_test_1() {
        let board = "\
            {147}2{479}58{469}{679}3{179}\
            35{79}{129}{12}{1269}{679}84\
            {14}867{34}{349}{59}2{159}\
            {27}48{23}9{237}156\
            5{17}{127}6{27}8{39}4{39}\
            963{14}5{14}278\
            {247}9{247}{234}6581{37}\
            6{17}{1457}8{1347}{134}{3457}92\
            83{12457}{1249}{1247}{12479}{457}6{57}\
        ";
        let expected = [remove_candidates!(7, 2, 7), remove_candidates!(7, 5, 4)];
        assertions::assert_logical_solution(&expected, board, hidden_unique_rectangles);
    }

    #[test]
    fn type_2_b_test_2() {
        let board = "\
            518472639\
            3{27}6859{127}{127}4\
            4{27}9316{257}{258}{278}\
            94562{17}3{178}{78}\
            861{79}34{279}{279}5\
            732{19}85{149}{149}6\
            65{47}{127}9{17}8{247}3\
            293{57}48{57}61\
            18{47}{257}63{24579}{2459}{27}\
        ";
        let expected = [remove_candidates!(8, 6, 7)];
        assertions::assert_logical_solution(&expected, board, hidden_unique_rectangles);
    }
}
