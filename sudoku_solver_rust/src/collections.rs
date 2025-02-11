pub trait IteratorZipExt<'a, T: 'a> {
    fn zip_every_pair(self) -> impl Iterator<Item = (&'a T, &'a T)>;
}

impl<'a, T: 'a, I: Iterator<Item = &'a T>> IteratorZipExt<'a, T> for I {
    fn zip_every_pair(self) -> impl Iterator<Item = (&'a T, &'a T)> {
        let iter = self.collect::<Vec<_>>().into_iter();
        iter.clone()
            .enumerate()
            .map(move |(first_index, first)| {
                iter.clone()
                    .skip(first_index + 1)
                    .map(move |second| (first, second))
            })
            .flatten()
    }
}
