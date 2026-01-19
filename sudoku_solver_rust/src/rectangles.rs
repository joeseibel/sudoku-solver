use crate::{
    board::Board,
    cell::{Cell, UnsolvedCell},
    collections::IteratorZipExt,
    sudoku_number::SudokuNumber,
};
use std::collections::HashSet;
use strum::IntoEnumIterator;

pub struct Rectangle<'a> {
    cells: [&'a UnsolvedCell; 4],
    common_candidates: [SudokuNumber; 2],
}

impl<'a> Rectangle<'a> {
    pub fn cells(&self) -> &[&'a UnsolvedCell; 4] {
        &self.cells
    }

    pub fn common_candidates(&self) -> &[SudokuNumber; 2] {
        &self.common_candidates
    }

    pub fn floor(&self) -> Vec<&'a UnsolvedCell> {
        self.cells
            .into_iter()
            .filter(|cell| cell.candidates().len() == 2)
            .collect()
    }

    pub fn roof(&self) -> Vec<&'a UnsolvedCell> {
        self.cells
            .into_iter()
            .filter(|cell| cell.candidates().len() > 2)
            .collect()
    }
}

pub fn create_rectangles(board: &Board<Cell>) -> impl Iterator<Item = Rectangle<'_>> {
    board.rows().zip_every_pair().flat_map(|(row_a, row_b)| {
        row_a
            .zip(row_b)
            .flat_map(|(cell_a, cell_b)| match (cell_a, cell_b) {
                (Cell::UnsolvedCell(cell_a), Cell::UnsolvedCell(cell_b)) => Some((cell_a, cell_b)),
                _ => None,
            })
            .zip_every_pair()
            .flat_map(|((cell_a, cell_b), (cell_c, cell_d))| {
                let cells = [cell_a, cell_b, cell_c, cell_d];
                let mut common_candidates: HashSet<_> = SudokuNumber::iter().collect();
                for cell in cells {
                    common_candidates.retain(|candidate| cell.candidates().contains(candidate));
                }
                common_candidates
                    .into_iter()
                    .collect::<Vec<_>>()
                    .try_into()
                    .ok()
                    .map(|common_candidates| Rectangle {
                        cells,
                        common_candidates,
                    })
            })
    })
}
