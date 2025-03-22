use crate::{
    board::Board,
    board_modification::{BoardModification, RemoveCandidates},
    cell::{Cell, IteratorCellExt, SolvedCell},
};
use std::collections::BTreeSet;

// If a cell is solved, then no other cells in the same unit can have that number as a candidate.
pub fn prune_candidates(board: &Board<Cell>) -> Vec<BoardModification> {
    board
        .cells()
        .unsolved_cells()
        .flat_map(|cell| {
            let same_row = board.get_row(cell.row());
            let same_column = board.get_column(cell.column());
            let same_block = board.get_block(cell.block());
            let visible_values = same_row
                .chain(same_column)
                .chain(same_block)
                .solved_cells()
                .map(SolvedCell::value)
                .collect();
            let to_remove: BTreeSet<_> = cell
                .candidates()
                .intersection(&visible_values)
                .copied()
                .collect();
            if to_remove.is_empty() {
                None
            } else {
                Some(RemoveCandidates::from_cell(cell, to_remove))
            }
        })
        .collect()
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::{cell, logic::assertions, remove_candidates};

    #[test]
    fn test() {
        let board =
            "000105000140000670080002400063070010900000003010090520007200080026000035000409000";
        let expected = [
            remove_candidates!(0, 0, 1, 4, 5, 8, 9),
            remove_candidates!(0, 1, 1, 2, 4, 5, 6, 8),
            remove_candidates!(0, 2, 1, 3, 4, 5, 6, 7, 8),
            remove_candidates!(0, 4, 1, 2, 5, 7, 9),
            remove_candidates!(0, 6, 1, 4, 5, 6, 7),
            remove_candidates!(0, 7, 1, 2, 3, 4, 5, 6, 7, 8),
            remove_candidates!(0, 8, 1, 3, 4, 5, 6, 7),
            remove_candidates!(1, 2, 1, 3, 4, 6, 7, 8),
            remove_candidates!(1, 3, 1, 2, 4, 5, 6, 7),
            remove_candidates!(1, 4, 1, 2, 4, 5, 6, 7, 9),
            remove_candidates!(1, 5, 1, 2, 4, 5, 6, 7, 9),
            remove_candidates!(1, 8, 1, 3, 4, 5, 6, 7),
            remove_candidates!(2, 0, 1, 2, 4, 8, 9),
            remove_candidates!(2, 2, 1, 2, 3, 4, 6, 7, 8),
            remove_candidates!(2, 3, 1, 2, 4, 5, 8),
            remove_candidates!(2, 4, 1, 2, 4, 5, 7, 8, 9),
            remove_candidates!(2, 7, 1, 2, 3, 4, 6, 7, 8),
            remove_candidates!(2, 8, 2, 3, 4, 5, 6, 7, 8),
            remove_candidates!(3, 0, 1, 3, 6, 7, 9),
            remove_candidates!(3, 3, 1, 2, 3, 4, 6, 7, 9),
            remove_candidates!(3, 5, 1, 2, 3, 5, 6, 7, 9),
            remove_candidates!(3, 6, 1, 2, 3, 4, 5, 6, 7),
            remove_candidates!(3, 8, 1, 2, 3, 5, 6, 7),
            remove_candidates!(4, 1, 1, 2, 3, 4, 6, 8, 9),
            remove_candidates!(4, 2, 1, 3, 6, 7, 9),
            remove_candidates!(4, 3, 1, 2, 3, 4, 7, 9),
            remove_candidates!(4, 4, 3, 7, 9),
            remove_candidates!(4, 5, 2, 3, 5, 7, 9),
            remove_candidates!(4, 6, 1, 2, 3, 4, 5, 6, 9),
            remove_candidates!(4, 7, 1, 2, 3, 5, 7, 8, 9),
            remove_candidates!(5, 0, 1, 2, 3, 5, 6, 9),
            remove_candidates!(5, 2, 1, 2, 3, 5, 6, 7, 9),
            remove_candidates!(5, 3, 1, 2, 4, 5, 7, 9),
            remove_candidates!(5, 5, 1, 2, 5, 7, 9),
            remove_candidates!(5, 8, 1, 2, 3, 5, 9),
            remove_candidates!(6, 0, 1, 2, 6, 7, 8, 9),
            remove_candidates!(6, 1, 1, 2, 4, 6, 7, 8),
            remove_candidates!(6, 4, 2, 4, 7, 8, 9),
            remove_candidates!(6, 5, 2, 4, 5, 7, 8, 9),
            remove_candidates!(6, 6, 2, 3, 4, 5, 6, 7, 8),
            remove_candidates!(6, 8, 2, 3, 5, 7, 8),
            remove_candidates!(7, 0, 1, 2, 3, 5, 6, 7, 9),
            remove_candidates!(7, 3, 1, 2, 3, 4, 5, 6, 9),
            remove_candidates!(7, 4, 2, 3, 4, 5, 6, 7, 9),
            remove_candidates!(7, 5, 2, 3, 4, 5, 6, 9),
            remove_candidates!(7, 6, 2, 3, 4, 5, 6, 8),
            remove_candidates!(8, 0, 1, 2, 4, 6, 7, 9),
            remove_candidates!(8, 1, 1, 2, 4, 6, 7, 8, 9),
            remove_candidates!(8, 2, 2, 3, 4, 6, 7, 9),
            remove_candidates!(8, 4, 2, 4, 7, 9),
            remove_candidates!(8, 6, 3, 4, 5, 6, 8, 9),
            remove_candidates!(8, 7, 1, 2, 3, 4, 5, 7, 8, 9),
            remove_candidates!(8, 8, 3, 4, 5, 8, 9),
        ];
        assertions::assert_logical_solution_with_parsed(
            &expected,
            &board.parse().unwrap(),
            prune_candidates,
        );
    }
}
