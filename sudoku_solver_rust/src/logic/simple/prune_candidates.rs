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
    use crate::{cell, logic::assertions};

    #[test]
    fn test() {
        let board =
            "000105000140000670080002400063070010900000003010090520007200080026000035000409000";
        let expected = [
            BoardModification::new_remove_candidates_with_indices(0, 0, &[1, 4, 5, 8, 9]),
            BoardModification::new_remove_candidates_with_indices(0, 1, &[1, 2, 4, 5, 6, 8]),
            BoardModification::new_remove_candidates_with_indices(0, 2, &[1, 3, 4, 5, 6, 7, 8]),
            BoardModification::new_remove_candidates_with_indices(0, 4, &[1, 2, 5, 7, 9]),
            BoardModification::new_remove_candidates_with_indices(0, 6, &[1, 4, 5, 6, 7]),
            BoardModification::new_remove_candidates_with_indices(0, 7, &[1, 2, 3, 4, 5, 6, 7, 8]),
            BoardModification::new_remove_candidates_with_indices(0, 8, &[1, 3, 4, 5, 6, 7]),
            BoardModification::new_remove_candidates_with_indices(1, 2, &[1, 3, 4, 6, 7, 8]),
            BoardModification::new_remove_candidates_with_indices(1, 3, &[1, 2, 4, 5, 6, 7]),
            BoardModification::new_remove_candidates_with_indices(1, 4, &[1, 2, 4, 5, 6, 7, 9]),
            BoardModification::new_remove_candidates_with_indices(1, 5, &[1, 2, 4, 5, 6, 7, 9]),
            BoardModification::new_remove_candidates_with_indices(1, 8, &[1, 3, 4, 5, 6, 7]),
            BoardModification::new_remove_candidates_with_indices(2, 0, &[1, 2, 4, 8, 9]),
            BoardModification::new_remove_candidates_with_indices(2, 2, &[1, 2, 3, 4, 6, 7, 8]),
            BoardModification::new_remove_candidates_with_indices(2, 3, &[1, 2, 4, 5, 8]),
            BoardModification::new_remove_candidates_with_indices(2, 4, &[1, 2, 4, 5, 7, 8, 9]),
            BoardModification::new_remove_candidates_with_indices(2, 7, &[1, 2, 3, 4, 6, 7, 8]),
            BoardModification::new_remove_candidates_with_indices(2, 8, &[2, 3, 4, 5, 6, 7, 8]),
            BoardModification::new_remove_candidates_with_indices(3, 0, &[1, 3, 6, 7, 9]),
            BoardModification::new_remove_candidates_with_indices(3, 3, &[1, 2, 3, 4, 6, 7, 9]),
            BoardModification::new_remove_candidates_with_indices(3, 5, &[1, 2, 3, 5, 6, 7, 9]),
            BoardModification::new_remove_candidates_with_indices(3, 6, &[1, 2, 3, 4, 5, 6, 7]),
            BoardModification::new_remove_candidates_with_indices(3, 8, &[1, 2, 3, 5, 6, 7]),
            BoardModification::new_remove_candidates_with_indices(4, 1, &[1, 2, 3, 4, 6, 8, 9]),
            BoardModification::new_remove_candidates_with_indices(4, 2, &[1, 3, 6, 7, 9]),
            BoardModification::new_remove_candidates_with_indices(4, 3, &[1, 2, 3, 4, 7, 9]),
            BoardModification::new_remove_candidates_with_indices(4, 4, &[3, 7, 9]),
            BoardModification::new_remove_candidates_with_indices(4, 5, &[2, 3, 5, 7, 9]),
            BoardModification::new_remove_candidates_with_indices(4, 6, &[1, 2, 3, 4, 5, 6, 9]),
            BoardModification::new_remove_candidates_with_indices(4, 7, &[1, 2, 3, 5, 7, 8, 9]),
            BoardModification::new_remove_candidates_with_indices(5, 0, &[1, 2, 3, 5, 6, 9]),
            BoardModification::new_remove_candidates_with_indices(5, 2, &[1, 2, 3, 5, 6, 7, 9]),
            BoardModification::new_remove_candidates_with_indices(5, 3, &[1, 2, 4, 5, 7, 9]),
            BoardModification::new_remove_candidates_with_indices(5, 5, &[1, 2, 5, 7, 9]),
            BoardModification::new_remove_candidates_with_indices(5, 8, &[1, 2, 3, 5, 9]),
            BoardModification::new_remove_candidates_with_indices(6, 0, &[1, 2, 6, 7, 8, 9]),
            BoardModification::new_remove_candidates_with_indices(6, 1, &[1, 2, 4, 6, 7, 8]),
            BoardModification::new_remove_candidates_with_indices(6, 4, &[2, 4, 7, 8, 9]),
            BoardModification::new_remove_candidates_with_indices(6, 5, &[2, 4, 5, 7, 8, 9]),
            BoardModification::new_remove_candidates_with_indices(6, 6, &[2, 3, 4, 5, 6, 7, 8]),
            BoardModification::new_remove_candidates_with_indices(6, 8, &[2, 3, 5, 7, 8]),
            BoardModification::new_remove_candidates_with_indices(7, 0, &[1, 2, 3, 5, 6, 7, 9]),
            BoardModification::new_remove_candidates_with_indices(7, 3, &[1, 2, 3, 4, 5, 6, 9]),
            BoardModification::new_remove_candidates_with_indices(7, 4, &[2, 3, 4, 5, 6, 7, 9]),
            BoardModification::new_remove_candidates_with_indices(7, 5, &[2, 3, 4, 5, 6, 9]),
            BoardModification::new_remove_candidates_with_indices(7, 6, &[2, 3, 4, 5, 6, 8]),
            BoardModification::new_remove_candidates_with_indices(8, 0, &[1, 2, 4, 6, 7, 9]),
            BoardModification::new_remove_candidates_with_indices(8, 1, &[1, 2, 4, 6, 7, 8, 9]),
            BoardModification::new_remove_candidates_with_indices(8, 2, &[2, 3, 4, 6, 7, 9]),
            BoardModification::new_remove_candidates_with_indices(8, 4, &[2, 4, 7, 9]),
            BoardModification::new_remove_candidates_with_indices(8, 6, &[3, 4, 5, 6, 8, 9]),
            BoardModification::new_remove_candidates_with_indices(8, 7, &[1, 2, 3, 4, 5, 7, 8, 9]),
            BoardModification::new_remove_candidates_with_indices(8, 8, &[3, 4, 5, 8, 9]),
        ];
        assertions::assert_logical_solution_with_parsed(
            &expected,
            &cell::parse_simple_cells(board),
            prune_candidates,
        );
    }
}
