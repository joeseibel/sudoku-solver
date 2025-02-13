pub trait IteratorZipExt<'a, T: 'a> {
    fn zip_every_pair(self) -> impl Iterator<Item = (&'a T, &'a T)>;
}

impl<'a, T: 'a, I: Iterator<Item = &'a T> + Clone> IteratorZipExt<'a, T> for I {
    fn zip_every_pair(self) -> impl Iterator<Item = (&'a T, &'a T)> {
        self.clone()
            .enumerate()
            .map(move |(first_index, first)| {
                self.clone()
                    .skip(first_index + 1)
                    .map(move |second| (first, second))
            })
            .flatten()
    }
}
