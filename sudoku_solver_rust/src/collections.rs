pub trait IteratorZipExt<'a, T: 'a> {
    fn zip_every_pair(self) -> impl Iterator<Item = (&'a T, &'a T)>;
    fn zip_every_triple(self) -> impl Iterator<Item = (&'a T, &'a T, &'a T)>;
    fn zip_every_quad(self) -> impl Iterator<Item = (&'a T, &'a T, &'a T, &'a T)>;
}

impl<'a, T: 'a, I: Iterator<Item = &'a T> + Clone> IteratorZipExt<'a, T> for I {
    fn zip_every_pair(self) -> impl Iterator<Item = (&'a T, &'a T)> {
        self.clone()
            .enumerate()
            .flat_map(move |(first_index, first)| {
                self.clone()
                    .skip(first_index + 1)
                    .map(move |second| (first, second))
            })
    }

    fn zip_every_triple(self) -> impl Iterator<Item = (&'a T, &'a T, &'a T)> {
        self.clone()
            .enumerate()
            .flat_map(move |(first_index, first)| {
                self.clone().enumerate().skip(first_index + 1).flat_map({
                    let iter = self.clone();
                    move |(second_index, second)| {
                        iter.clone()
                            .skip(second_index + 1)
                            .map(move |third| (first, second, third))
                    }
                })
            })
    }

    fn zip_every_quad(self) -> impl Iterator<Item = (&'a T, &'a T, &'a T, &'a T)> {
        self.clone()
            .enumerate()
            .flat_map(move |(first_index, first)| {
                self.clone().enumerate().skip(first_index + 1).flat_map({
                    let second_iter = self.clone();
                    move |(second_index, second)| {
                        second_iter
                            .clone()
                            .enumerate()
                            .skip(second_index + 1)
                            .flat_map({
                                let third_iter = second_iter.clone();
                                move |(third_index, third)| {
                                    third_iter
                                        .clone()
                                        .skip(third_index + 1)
                                        .map(move |fourth| (first, second, third, fourth))
                                }
                            })
                    }
                })
            })
    }
}
