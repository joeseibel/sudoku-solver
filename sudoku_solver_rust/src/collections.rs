pub trait IteratorZipExt<T> {
    fn zip_every_pair(self) -> impl Iterator<Item = (T, T)>;
    fn zip_every_triple(self) -> impl Iterator<Item = (T, T, T)>;
    fn zip_every_quad(self) -> impl Iterator<Item = (T, T, T, T)>;
}

impl<T: Clone, I: Iterator<Item = T> + Clone> IteratorZipExt<T> for I {
    fn zip_every_pair(self) -> impl Iterator<Item = (T, T)> {
        self.clone()
            .enumerate()
            .flat_map(move |(first_index, first)| {
                self.clone()
                    .skip(first_index + 1)
                    .map(move |second| (first.clone(), second))
            })
    }

    fn zip_every_triple(self) -> impl Iterator<Item = (T, T, T)> {
        self.clone()
            .enumerate()
            .flat_map(move |(first_index, first)| {
                self.clone().enumerate().skip(first_index + 1).flat_map({
                    let iter = self.clone();
                    move |(second_index, second)| {
                        iter.clone().skip(second_index + 1).map({
                            let first = first.clone();
                            move |third| (first.clone(), second.clone(), third)
                        })
                    }
                })
            })
    }

    fn zip_every_quad(self) -> impl Iterator<Item = (T, T, T, T)> {
        self.clone()
            .enumerate()
            .flat_map(move |(first_index, first)| {
                self.clone().enumerate().skip(first_index + 1).flat_map({
                    let iter = self.clone();
                    move |(second_index, second)| {
                        iter.clone().enumerate().skip(second_index + 1).flat_map({
                            let iter = iter.clone();
                            let first = first.clone();
                            move |(third_index, third)| {
                                iter.clone().skip(third_index + 1).map({
                                    let first = first.clone();
                                    let second = second.clone();
                                    move |fourth| {
                                        (first.clone(), second.clone(), third.clone(), fourth)
                                    }
                                })
                            }
                        })
                    }
                })
            })
    }
}
